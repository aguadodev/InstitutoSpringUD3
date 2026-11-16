package com.example.instituto.repository;

import com.example.instituto.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    // Consulta derivada del nombre del método (equivalente a listarPorCurso)
    List<Alumno> findByCurso(String curso);

    // Si prefieres JPQL explícito para el listado ordenado
    @Query("SELECT a FROM Alumno a ORDER BY a.apellidos, a.nombre")
    List<Alumno> findAllOrdenados();

    // Consulta con LIKE (ejercicio 2 del README)
    @Query("SELECT a FROM Alumno a WHERE a.nombre LIKE %:texto%")
    List<Alumno> findByNombreContiene(@Param("texto") String texto);

    // Agregación: contar alumnos por curso (ejercicio 3)
    @Query("SELECT a.curso, COUNT(a) FROM Alumno a GROUP BY a.curso")
    List<Object[]> contarPorCurso();
}