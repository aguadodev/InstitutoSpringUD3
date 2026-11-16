# Instituto · Ejemplo de JPA (UD3 · Acceso a Datos · DAM2)

Pequeña aplicación de consola que gestiona los **alumnos** de un instituto con las
operaciones CRUD básicas (*Create, Read, Update, Delete*) usando el **estándar JPA**
(Jakarta Persistence). Hibernate actúa como *proveedor* de JPA y SQLite como base de datos.

> Objetivo didáctico: trabajar con objetos Java y dejar que JPA se encargue de
> traducirlos a SQL, sin escribir ni una sola sentencia `INSERT`, `UPDATE` o `DELETE`.

---

## 1. Estructura del proyecto

```
src/main/java/com/example/instituto/
├── Main.java                 → Menú de consola (interfaz con el usuario)
├── InstitutoApplication.java → Arranque de Spring Boot (no se usa en este ejemplo)
├── dao/
│   └── AlumnoDao.java        → Operaciones CRUD con JPA
└── model/
    └── Alumno.java           → Entidad JPA (se mapea a la tabla Alumno)

src/main/resources/
├── META-INF/persistence.xml  → Configuración de JPA (unidad de persistencia)
└── application.properties    → Configuración de Spring Boot

instituto.db                  → Base de datos SQLite
```

Cada clase tiene una única responsabilidad:

| Capa        | Clase         | Responsabilidad                                       |
|-------------|---------------|-------------------------------------------------------|
| Modelo      | `Alumno`      | Representa los datos (una fila de la tabla)           |
| Acceso a datos | `AlumnoDao` | Sabe *cómo* guardar, buscar, modificar y borrar      |
| Presentación | `Main`       | Pide datos al usuario y muestra resultados            |

`Main` no sabe nada de SQL ni de transacciones; `AlumnoDao` no sabe nada de la consola.
Este es el patrón **DAO** (*Data Access Object*).

---

## 2. Conceptos clave de JPA

| Concepto | Qué es | Dónde aparece |
|----------|--------|---------------|
| **Entidad** | Clase Java cuyos objetos se guardan en la BD | `Alumno` (`@Entity`) |
| **Unidad de persistencia** | Configuración con nombre: BD, entidades, opciones | `persistence.xml` → `institutoPU` |
| **`EntityManagerFactory`** | Fábrica de `EntityManager`. Costosa de crear: **una por aplicación** | `Main` |
| **`EntityManager`** | Objeto con el que trabajamos: guarda, busca, borra… **Uno por operación** | `AlumnoDao` |
| **`EntityTransaction`** | Agrupa cambios: o se aplican todos (`commit`) o ninguno (`rollback`) | `AlumnoDao` |
| **JPQL** | Lenguaje de consultas parecido a SQL, pero sobre **clases y atributos**, no tablas y columnas | `listarTodos`, `listarPorCurso` |

---

## 3. La entidad `Alumno`

```java
@Entity
public class Alumno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String apellidos;
    private String curso;

    public Alumno() { }   // obligatorio para JPA
    ...
}
```

- `@Entity` → la clase se corresponde con la tabla `Alumno`.
- `@Id` → clave primaria.
- `@GeneratedValue(IDENTITY)` → el id lo genera la base de datos (autoincremento).
  Por eso al crear un alumno **no** se indica el id: JPA lo rellena tras el `persist`.
- Cada atributo se convierte en una columna con el mismo nombre.
- JPA necesita un **constructor sin parámetros** para crear los objetos al leer de la BD.

La tabla **no** se crea a mano: JPA la genera (o la actualiza) al arrancar gracias a
la propiedad `schema-generation` de `persistence.xml`.

---

## 4. Configuración: `persistence.xml`

```xml
<persistence-unit name="institutoPU" transaction-type="RESOURCE_LOCAL">
    <class>com.example.instituto.model.Alumno</class>
    <properties>
        <property name="jakarta.persistence.jdbc.driver" value="org.sqlite.JDBC"/>
        <property name="jakarta.persistence.jdbc.url"    value="jdbc:sqlite:instituto.db"/>
        <property name="jakarta.persistence.schema-generation.database.action" value="update"/>
        <property name="hibernate.show_sql" value="true"/>
    </properties>
</persistence-unit>
```

- Las propiedades `jakarta.persistence.*` son **estándar**: funcionarían igual con otro
  proveedor (EclipseLink, OpenJPA…).
- `schema-generation.database.action`:
  - `none` → no toca la BD.
  - `create` → crea las tablas.
  - `drop-and-create` → borra y vuelve a crear (¡se pierden los datos!).
  - `update` → crea lo que falte sin borrar datos *(valor admitido por Hibernate)*.
- `hibernate.show_sql` es **propia de Hibernate**: muestra en consola el SQL que se
  ejecuta. Muy útil para ver qué hace JPA "por debajo".

El dialecto SQL de SQLite no se indica: Hibernate lo detecta automáticamente a partir
de la conexión (necesita la dependencia `hibernate-community-dialects` del `pom.xml`).

---

## 5. Las operaciones CRUD (`AlumnoDao`)

Todas siguen el mismo esquema:

```java
try (EntityManager em = emf.createEntityManager()) {   // 1. abrir (se cierra solo)
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();                                     // 2. empezar transacción
        // ... operación ...
        tx.commit();                                    // 3. confirmar
    } catch (RuntimeException e) {
        if (tx.isActive()) tx.rollback();               // 4. deshacer si falla
        throw e;
    }
}
```

> **Regla:** las operaciones que **modifican** datos necesitan transacción.
> Las consultas de **solo lectura** no.

### C · Create → `persist`

```java
tx.begin();
em.persist(alumno);   // INSERT INTO Alumno ...
tx.commit();
return alumno.getId(); // el id ya está asignado
```

### R · Read → `find` y JPQL

Buscar por clave primaria (devuelve `null` si no existe):

```java
Alumno a = em.find(Alumno.class, id);   // SELECT ... WHERE id = ?
```

Listar con una consulta JPQL:

```java
em.createQuery("SELECT a FROM Alumno a ORDER BY a.apellidos, a.nombre", Alumno.class)
  .getResultList();
```

Fíjate en que `Alumno` y `a.apellidos` son la **clase** y el **atributo** Java, no la
tabla y la columna. Pasar `Alumno.class` devuelve una `TypedQuery<Alumno>`, así que
`getResultList()` devuelve directamente una `List<Alumno>` sin necesidad de *casts*.

Consulta con **parámetro con nombre** (evita concatenar cadenas e inyección SQL):

```java
em.createQuery("SELECT a FROM Alumno a WHERE a.curso = :curso", Alumno.class)
  .setParameter("curso", curso)
  .getResultList();
```

Otros métodos útiles de `TypedQuery`:
- `getSingleResult()` → un único resultado (lanza excepción si hay 0 o más de 1).
- `setFirstResult(n)` / `setMaxResults(n)` → paginación.

### U · Update → modificar un objeto *gestionado*

```java
tx.begin();
Alumno alumno = em.find(Alumno.class, id);  // objeto GESTIONADO
alumno.setNombre(nombre);                   // solo se cambia el objeto Java...
tx.commit();                                // ...y JPA lanza el UPDATE automáticamente
```

**No existe un método `update()`**. Los objetos obtenidos con `find` (o con una consulta)
dentro de un `EntityManager` abierto están **gestionados**: JPA vigila sus cambios y los
sincroniza con la BD al hacer `commit` (*dirty checking*).

> Si tuviéramos un objeto **separado** (*detached*), por ejemplo recibido de fuera,
> usaríamos `em.merge(alumno)`, que copia su estado en un objeto gestionado.

### D · Delete → `remove`

```java
tx.begin();
Alumno alumno = em.find(Alumno.class, id);  // remove solo admite objetos gestionados
em.remove(alumno);                          // DELETE FROM Alumno WHERE id = ?
tx.commit();
```

---

## 6. Ciclo de vida de una entidad

```
   new Alumno(...)                       em.find / consulta
        │                                       │
        ▼                                       ▼
   ┌─────────┐      persist()           ┌────────────┐     remove()     ┌──────────┐
   │  NUEVA  │ ───────────────────────▶ │ GESTIONADA │ ───────────────▶ │ BORRADA  │
   │  (new)  │                          │ (managed)  │                  │(removed) │
   └─────────┘                          └────────────┘                  └──────────┘
                                         │        ▲
                           em.close()    │        │  merge()
                                         ▼        │
                                        ┌────────────┐
                                        │  SEPARADA  │
                                        │ (detached) │
                                        └────────────┘
```

- **Nueva:** objeto Java normal, JPA no lo conoce.
- **Gestionada:** asociada a un `EntityManager`; sus cambios se guardan al hacer `commit`.
- **Separada:** estuvo gestionada pero el `EntityManager` se cerró. Los cambios ya **no**
  se guardan solos. Es el caso de los alumnos que devuelve `listarTodos()`.
- **Borrada:** marcada para eliminarse al hacer `commit`.

---

## 7. Cómo ejecutarlo

Requisitos: **JDK 25**. Maven se descarga solo mediante el *wrapper* (`mvnw`).

Desde el IDE: ejecutar la clase `com.example.instituto.Main`.

Desde terminal:

```bash
chmod +x mvnw            # solo la primera vez (Linux/macOS)
./mvnw compile
./mvnw dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp "target/classes:$(cat cp.txt)" com.example.instituto.Main
```

La base de datos `instituto.db` se crea/usa en el directorio desde el que se lanza el
programa. Al ejecutar verás el SQL generado por Hibernate:

```
Opción: 4
Nombre: Ana
Apellidos: Pérez López
Curso: 2DAM
Hibernate: insert into Alumno (apellidos,curso,nombre) values (?,?,?)
Alumno guardado con id 5
```

---

## 8. ¿Y Spring?

El proyecto se ha generado con Spring Boot (`spring-boot-starter-data-jpa`) porque esa
dependencia incluye todo lo necesario: la API de JPA y Hibernate. Sin embargo, **este
ejemplo no usa Spring**: `Main` crea la `EntityManagerFactory` a mano con
`Persistence.createEntityManagerFactory("institutoPU")`.

Así se entiende lo que Spring automatiza más adelante: con **Spring Data JPA** la
configuración pasa a `application.properties`, el `EntityManager` y las transacciones
se gestionan solos (`@Transactional`) y el DAO se sustituye por una interfaz:

```java
public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {
    List<Alumno> findByCurso(String curso);   // la consulta se deduce del nombre
}
```

---

## 9. Ejercicios propuestos

1. Añade a `Alumno` un atributo `email` y comprueba en la BD que JPA crea la columna.
2. Añade una consulta que busque alumnos cuyo nombre **contenga** un texto
   (pista: `LIKE :texto` y `setParameter("texto", "%" + texto + "%")`).
3. Añade una opción que muestre **cuántos alumnos** hay en cada curso
   (pista: `SELECT a.curso, COUNT(a) FROM Alumno a GROUP BY a.curso`).
4. Muestra los alumnos de 3 en 3 usando `setFirstResult` y `setMaxResults`.
5. Crea la entidad `Curso` y relaciónala con `Alumno` mediante `@ManyToOne`.
6. Implementa `actualizar` usando `merge` en lugar de `find` + *setters*. ¿Qué diferencias
   observas en el SQL generado?
