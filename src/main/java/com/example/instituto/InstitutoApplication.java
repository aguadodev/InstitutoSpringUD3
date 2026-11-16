package com.example.instituto;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.instituto.model.Alumno;
import com.example.instituto.repository.AlumnoRepository;

/**
 * Aplicación de consola con las operaciones CRUD básicas sobre Alumno
 * usando Spring Data JPA Repository.
 */
    @SpringBootApplication
    public class InstitutoApplication implements CommandLineRunner {
        private final AlumnoRepository alumnoRepository;

        public InstitutoApplication(AlumnoRepository alumnoRepository) {
            this.alumnoRepository = alumnoRepository;
        }

    public static void main(String[] args) {
        SpringApplication.run(InstitutoApplication.class, args);
    }

    private static final Scanner sc = new Scanner(System.in);

    @Override
    public void run(String... args) throws Exception {
        // Spring Boot ya configura automáticamente el EntityManagerFactory y las transacciones,
        // por lo que puedes usar directamente alumnoRepository.

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
                case 1 -> mostrar(alumnoRepository.findAllOrdenados());
                case 2 -> mostrar(alumnoRepository.findByCurso(leerTexto("Curso: ")));
                case 3 -> {
                    Alumno a = alumnoRepository.findById(leerEntero("Id: ")).orElse(null);
                    System.out.println(a != null ? a : "No existe ningún alumno con ese id.");
                }
                case 4 -> {
                    Alumno nuevo = new Alumno(leerTexto("Nombre: "), leerTexto("Apellidos: "), leerTexto("Curso: "));
                    // Corrección: save devuelve la entidad guardada, imprimir su id
                    Alumno guardado = alumnoRepository.save(nuevo);
                    System.out.println("Alumno guardado con id " + guardado.getId());
                }
                case 5 -> {
                    int id = leerEntero("Id del alumno a modificar: ");
                    // Corrección: primero buscar, luego modificar, finalmente save
                    Optional<Alumno> opt = alumnoRepository.findById(id);
                    if (opt.isPresent()) {
                        Alumno alumno = opt.get();
                        alumno.setNombre(leerTexto("Nuevo nombre: "));
                        alumno.setApellidos(leerTexto("Nuevos apellidos: "));
                        alumno.setCurso(leerTexto("Nuevo curso: "));
                        alumnoRepository.save(alumno);
                        System.out.println("Alumno modificado.");
                    } else {
                        System.out.println("No existe ningún alumno con ese id.");
                    }
                }
                case 6 -> {
                    int id = leerEntero("Id del alumno a borrar: ");
                    // Corrección: deleteById no devuelve boolean, primero verificar si existe
                    if (alumnoRepository.existsById(id)) {
                        alumnoRepository.deleteById(id);
                        System.out.println("Alumno borrado.");
                    } else {
                        System.out.println("No existe ningún alumno con ese id.");
                    }
                }
                case 0 -> System.out.println("¡Hasta luego!");
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
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