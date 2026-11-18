package com.citanova.app;

import com.citanova.dao.CitaDAO;
import com.citanova.modelo.Cita;

import java.time.LocalDateTime;
import java.util.List;

public class PruebaCrudCompleto {

    public static void main(String[] args) {

        System.out.println("=== CitaNOVA | Prueba CRUD COMPLETO JDBC ===");

        CitaDAO citaDAO = new CitaDAO();

        try {
            // CREATE
            LocalDateTime fechaInicio = LocalDateTime.now()
                    .plusDays(2)
                    .withHour(9)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            LocalDateTime fechaFin = fechaInicio.plusHours(1);

            Cita nuevaCita = new Cita(
                    1L, // Cliente
                    2L, // Profesional
                    1L, // Servicio
                    6,  // Estado PENDIENTE
                    fechaInicio,
                    fechaFin
            );

            long idGenerado = citaDAO.insertarCita(nuevaCita);

            System.out.println();
            System.out.println("CREATE - Cita registrada correctamente.");
            System.out.println("ID generado: " + idGenerado);

            // READ
            Cita citaCreada =
                    buscarCita(citaDAO.consultarCitas(), idGenerado);

            System.out.println();
            System.out.println("READ - Cita recuperada:");
            System.out.println(citaCreada);

            // UPDATE
            LocalDateTime nuevoInicio =
                    fechaInicio.withHour(14);

            LocalDateTime nuevoFin =
                    nuevoInicio.plusHours(1);

            citaCreada.setFechaHoraInicio(nuevoInicio);
            citaCreada.setFechaHoraFin(nuevoFin);

            boolean actualizada =
                    citaDAO.actualizarCita(citaCreada);

            System.out.println();
            System.out.println(
                    "UPDATE - Cita actualizada correctamente: "
                            + actualizada);

            Cita citaActualizada =
                    buscarCita(citaDAO.consultarCitas(), idGenerado);

            System.out.println("Cita despues del UPDATE:");
            System.out.println(citaActualizada);

            // DELETE
            boolean eliminada =
                    citaDAO.eliminarCita(idGenerado);

            System.out.println();
            System.out.println(
                    "DELETE - Cita eliminada correctamente: "
                            + eliminada);

            Cita citaEliminada =
                    buscarCita(citaDAO.consultarCitas(), idGenerado);

            System.out.println(
                    "Verificacion posterior al DELETE: "
                            + (citaEliminada == null
                            ? "La cita ya no existe."
                            : "La cita aun existe."));

            System.out.println();
            System.out.println(
                    "Resultado: CRUD COMPLETO EJECUTADO CORRECTAMENTE.");

        } catch (Exception e) {
            System.err.println();
            System.err.println("ERROR DURANTE LA PRUEBA CRUD.");
            System.err.println("Detalle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Cita buscarCita(
            List<Cita> citas,
            long idCita) {

        return citas.stream()
                .filter(cita ->
                        cita.getIdCita() != null
                                && cita.getIdCita() == idCita)
                .findFirst()
                .orElse(null);
    }
}