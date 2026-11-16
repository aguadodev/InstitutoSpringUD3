package com.example.Instituto;

import java.util.List;
import java.util.Scanner;

import com.example.Instituto.model.Alumno;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class Main {

    public static void main(String[] args) {

        // 1. Crear la fábrica de EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("institutoPU");

        // 2. Crear el EntityManager
        EntityManager em = emf.createEntityManager();

        // 3. Obtener la transacción
        EntityTransaction tx = em.getTransaction();

        try {
            Scanner sc = new Scanner(System.in);
            
            // 4. Iniciar la transacción
            tx.begin();
            /*
            // 5. Crear un objeto Alumno
            
            System.out.println("Nuevo Alumno");
            System.out.print("Nombre: ");
            String nombre = sc.nextLine();
            System.out.print("apellido: ");
            String apellido = sc.nextLine();

            Alumno alumno = new Alumno(nombre, apellido);

            // 6. Persistir el objeto
            em.persist(alumno);



            System.out.println("Alumnos guardados correctamente.");
            // System.out.println("IDs asignados: " + alumno.getId());
            */

            System.out.println("Alumnos actuales:");
            List<Alumno> alumnos = em.createQuery("SELECT a FROM Alumno a", Alumno.class).getResultList();
            alumnos.forEach(System.out::println);

            System.out.print("Id Alumno a Borrar: ");
            int id = sc.nextInt();

            Alumno alumno2 = em.find(Alumno.class, id);
            System.out.println(alumno2);
            em.remove(alumno2);
            em.flush();
            // 7. Confirmar la transacción
            tx.commit();
            sc.close();
        } catch (Exception e) {

            // 8. Deshacer los cambios si hay algún error
            if (tx.isActive()) {
                tx.rollback();
            }

            e.printStackTrace();

        } finally {

            // 9. Cerrar los recursos
            em.close();
            emf.close();
        }
    }
}