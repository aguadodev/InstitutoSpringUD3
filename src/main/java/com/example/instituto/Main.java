package com.example.instituto;

import java.util.List;
import java.util.Scanner;

import com.example.instituto.dao.AlumnoDao;
import com.example.instituto.model.Alumno;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Aplicación de consola con las operaciones CRUD básicas sobre Alumno
 * usando JPA "puro" (sin Spring), configurado en META-INF/persistence.xml.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        // La EntityManagerFactory es costosa de crear: una sola para toda la aplicación.
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("institutoPU")) {
            AlumnoDao dao = new AlumnoDao(emf);

            int opcion;
            do {
                System.out.println("""

                        ===== GESTIÓN DE ALUMNOS =====
                        1. Listar alumnos
                        2. Listar alumnos de un curso
                        3. Buscar alumno por id
                        4. Nuevo alumno
                        5. Modificar alumno
                        6. Borrar alumno
                        0. Salir""");
                opcion = leerEntero("Opción: ");

                switch (opcion) {
                    case 1 -> mostrar(dao.listarTodos());
                    case 2 -> mostrar(dao.listarPorCurso(leerTexto("Curso: ")));
                    case 3 -> {
                        Alumno a = dao.buscarPorId(leerEntero("Id: "));
                        System.out.println(a != null ? a : "No existe ningún alumno con ese id.");
                    }
                    case 4 -> {
                        Alumno nuevo = new Alumno(leerTexto("Nombre: "), leerTexto("Apellidos: "), leerTexto("Curso: "));
                        System.out.println("Alumno guardado con id " + dao.insertar(nuevo));
                    }
                    case 5 -> {
                        int id = leerEntero("Id del alumno a modificar: ");
                        boolean ok = dao.actualizar(id, leerTexto("Nuevo nombre: "),
                                leerTexto("Nuevos apellidos: "), leerTexto("Nuevo curso: "));
                        System.out.println(ok ? "Alumno modificado." : "No existe ningún alumno con ese id.");
                    }
                    case 6 -> {
                        boolean ok = dao.borrar(leerEntero("Id del alumno a borrar: "));
                        System.out.println(ok ? "Alumno borrado." : "No existe ningún alumno con ese id.");
                    }
                    case 0 -> System.out.println("¡Hasta luego!");
                    default -> System.out.println("Opción no válida.");
                }
            } while (opcion != 0);
        }
    }

    private static void mostrar(List<Alumno> alumnos) {
        if (alumnos.isEmpty()) {
            System.out.println("No hay alumnos.");
        } else {
            alumnos.forEach(System.out::println);
        }
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número entero.");
            }
        }
    }
}
