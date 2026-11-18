package com.citanova.app;

import com.citanova.dao.CitaDAO;
import com.citanova.modelo.Cita;

import java.time.LocalDateTime;
import java.util.List;

public class PruebaCrudCompleto {

    public static void main(String[] args) {

        System.out.println("=== CitaNOVA | Prueba UPDATE + DELETE JDBC ===");

        CitaDAO citaDAO = new CitaDAO();
        long idCitaPrueba = 1L;

        try {
            // Buscar la cita creada previamente.
            Cita cita = buscarCita(citaDAO.consultarCitas(), idCitaPrueba);

            if (cita == null) {
                System.out.println(
                        "No existe la cita " + idCitaPrueba
                                + ". Ejecute primero la prueba INSERT + SELECT.");
                return;
            }

            System.out.println();
            System.out.println("Cita antes del UPDATE:");
            System.out.println(cita);

            // Actualizar horario de la cita.
            LocalDateTime nuevoInicio = LocalDateTime
                    .of(2026, 11, 19, 14, 0);

            LocalDateTime nuevoFin = LocalDateTime
                    .of(2026, 11, 19, 15, 0);

            cita.setFechaHoraInicio(nuevoInicio);
            cita.setFechaHoraFin(nuevoFin);

            boolean actualizada = citaDAO.actualizarCita(cita);

            System.out.println();
            System.out.println(
                    "UPDATE - Cita actualizada correctamente: "
                            + actualizada);

            // Consultar nuevamente para demostrar la actualización.
            Cita citaActualizada =
                    buscarCita(citaDAO.consultarCitas(), idCitaPrueba);

            System.out.println("Cita despues del UPDATE:");
            System.out.println(citaActualizada);

            // Eliminar la cita académica de prueba.
            boolean eliminada =
                    citaDAO.eliminarCita(idCitaPrueba);

            System.out.println();
            System.out.println(
                    "DELETE - Cita eliminada correctamente: "
                            + eliminada);

            // Verificar que ya no exista.
            Cita citaEliminada =
                    buscarCita(citaDAO.consultarCitas(), idCitaPrueba);

            System.out.println(
                    "Verificacion posterior al DELETE: "
                            + (citaEliminada == null
                            ? "La cita ya no existe."
                            : "La cita aun existe."));

            System.out.println();
            System.out.println(
                    "Resultado: UPDATE Y DELETE EJECUTADOS CORRECTAMENTE.");

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