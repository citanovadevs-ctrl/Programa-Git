package com.citanova.app;

import com.citanova.dao.CitaDAO;
import com.citanova.modelo.Cita;

import java.time.LocalDateTime;
import java.util.List;

public class PruebaCrudCita {

    public static void main(String[] args) {

        System.out.println("=== CitaNOVA | Prueba CRUD JDBC ===");

        CitaDAO citaDAO = new CitaDAO();

        try {
            LocalDateTime fechaInicio = LocalDateTime.now()
                    .plusDays(1)
                    .withHour(10)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            LocalDateTime fechaFin = fechaInicio.plusHours(1);

            Cita nuevaCita = new Cita(
                    1L,     // Cliente
                    2L,     // Profesional
                    1L,     // Servicio
                    6,      // Estado PENDIENTE
                    fechaInicio,
                    fechaFin
            );

            long idGenerado = citaDAO.insertarCita(nuevaCita);

            System.out.println();
            System.out.println("CREATE - Cita registrada correctamente.");
            System.out.println("ID generado: " + idGenerado);

            List<Cita> citas = citaDAO.consultarCitas();

            System.out.println();
            System.out.println("READ - Citas registradas:");
            System.out.println("Total encontrado: " + citas.size());

            for (Cita cita : citas) {
                System.out.println(cita);
            }

            System.out.println();
            System.out.println(
                    "Resultado: INSERT Y SELECT EJECUTADOS CORRECTAMENTE."
            );

        } catch (Exception e) {
            System.err.println();
            System.err.println("ERROR DURANTE LA PRUEBA CRUD.");
            System.err.println("Detalle: " + e.getMessage());
            e.printStackTrace();
        }
    }
}