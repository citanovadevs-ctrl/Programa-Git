package com.citanova.dao;

import com.citanova.conexion.ConexionBD;
import com.citanova.modelo.Cita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona las operaciones de persistencia de la entidad Cita.
 *
 * Primera implementación:
 * - INSERT: registrar una cita.
 * - SELECT: consultar todas las citas.
 */
public class CitaDAO {

    private static final String SQL_INSERTAR = """
            INSERT INTO cita (
                id_cliente,
                id_profesional,
                id_servicio,
                id_estado_cita,
                fecha_hora_inicio,
                fecha_hora_fin,
                fecha_creacion,
                fecha_cancelacion,
                motivo_cancelacion
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_CONSULTAR_TODAS = """
            SELECT
                id_cita,
                id_cliente,
                id_profesional,
                id_servicio,
                id_estado_cita,
                fecha_hora_inicio,
                fecha_hora_fin,
                fecha_creacion,
                fecha_cancelacion,
                motivo_cancelacion
            FROM cita
            ORDER BY id_cita
            """;

    /**
     * Inserta una nueva cita y recupera el identificador generado por MySQL.
     *
     * @param cita objeto con los datos de la cita.
     * @return identificador generado.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public long insertarCita(Cita cita) throws SQLException {
        validarCita(cita);

        if (cita.getFechaCreacion() == null) {
            cita.setFechaCreacion(LocalDateTime.now());
        }

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     SQL_INSERTAR,
                     Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setLong(1, cita.getIdCliente());
            sentencia.setLong(2, cita.getIdProfesional());
            sentencia.setLong(3, cita.getIdServicio());
            sentencia.setInt(4, cita.getIdEstadoCita());
            sentencia.setTimestamp(
                    5,
                    Timestamp.valueOf(cita.getFechaHoraInicio()));
            sentencia.setTimestamp(
                    6,
                    Timestamp.valueOf(cita.getFechaHoraFin()));
            sentencia.setTimestamp(
                    7,
                    Timestamp.valueOf(cita.getFechaCreacion()));

            if (cita.getFechaCancelacion() != null) {
                sentencia.setTimestamp(
                        8,
                        Timestamp.valueOf(cita.getFechaCancelacion()));
            } else {
                sentencia.setNull(8, java.sql.Types.TIMESTAMP);
            }

            if (cita.getMotivoCancelacion() != null
                    && !cita.getMotivoCancelacion().isBlank()) {
                sentencia.setString(
                        9,
                        cita.getMotivoCancelacion().trim());
            } else {
                sentencia.setNull(9, java.sql.Types.VARCHAR);
            }

            int filasAfectadas = sentencia.executeUpdate();

            if (filasAfectadas != 1) {
                throw new SQLException(
                        "No fue posible registrar la cita.");
            }

            try (ResultSet clavesGeneradas =
                         sentencia.getGeneratedKeys()) {

                if (clavesGeneradas.next()) {
                    long idGenerado = clavesGeneradas.getLong(1);
                    cita.setIdCita(idGenerado);
                    return idGenerado;
                }
            }

            throw new SQLException(
                    "La cita fue insertada, pero no se obtuvo el ID generado.");
        }
    }

    /**
     * Consulta todas las citas almacenadas en la base de datos.
     *
     * @return lista de citas ordenadas por identificador.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    public List<Cita> consultarCitas() throws SQLException {
        List<Cita> citas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(SQL_CONSULTAR_TODAS);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                citas.add(mapearCita(resultado));
            }
        }

        return citas;
    }

    /**
     * Convierte una fila del ResultSet en un objeto Cita.
     */
    private Cita mapearCita(ResultSet resultado) throws SQLException {
        Cita cita = new Cita();

        cita.setIdCita(resultado.getLong("id_cita"));
        cita.setIdCliente(resultado.getLong("id_cliente"));
        cita.setIdProfesional(resultado.getLong("id_profesional"));
        cita.setIdServicio(resultado.getLong("id_servicio"));
        cita.setIdEstadoCita(resultado.getInt("id_estado_cita"));

        cita.setFechaHoraInicio(
                obtenerLocalDateTime(resultado, "fecha_hora_inicio"));

        cita.setFechaHoraFin(
                obtenerLocalDateTime(resultado, "fecha_hora_fin"));

        cita.setFechaCreacion(
                obtenerLocalDateTime(resultado, "fecha_creacion"));

        cita.setFechaCancelacion(
                obtenerLocalDateTime(resultado, "fecha_cancelacion"));

        cita.setMotivoCancelacion(
                resultado.getString("motivo_cancelacion"));

        return cita;
    }

    /**
     * Convierte un TIMESTAMP SQL a LocalDateTime permitiendo valores NULL.
     */
    private LocalDateTime obtenerLocalDateTime(
            ResultSet resultado,
            String columna) throws SQLException {

        Timestamp timestamp = resultado.getTimestamp(columna);

        return timestamp != null
                ? timestamp.toLocalDateTime()
                : null;
    }

   /**
 * Aplica validaciones básicas antes de insertar o actualizar una cita.
 */
    private void validarCita(Cita cita) {
        if (cita == null) {
            throw new IllegalArgumentException(
                    "La cita no puede ser nula.");
        }

        if (cita.getIdCliente() == null) {
            throw new IllegalArgumentException(
                    "El cliente es obligatorio.");
        }

        if (cita.getIdProfesional() == null) {
            throw new IllegalArgumentException(
                    "El profesional es obligatorio.");
        }

        if (cita.getIdServicio() == null) {
            throw new IllegalArgumentException(
                    "El servicio es obligatorio.");
        }

        if (cita.getIdEstadoCita() == null) {
            throw new IllegalArgumentException(
                    "El estado de la cita es obligatorio.");
        }

        if (cita.getFechaHoraInicio() == null
                || cita.getFechaHoraFin() == null) {
            throw new IllegalArgumentException(
                    "Las fechas de inicio y fin son obligatorias.");
        }

        if (!cita.getFechaHoraFin()
                .isAfter(cita.getFechaHoraInicio())) {
            throw new IllegalArgumentException(
                    "La fecha y hora de fin debe ser posterior al inicio.");
        }
    }
	/**
 * Actualiza los datos principales de una cita existente.
 *
 * @param cita cita con el identificador y los nuevos datos.
 * @return true si la cita fue actualizada.
 * @throws SQLException si ocurre un error de acceso a datos.
 */
public boolean actualizarCita(Cita cita) throws SQLException {
    validarCita(cita);

    if (cita.getIdCita() == null) {
        throw new IllegalArgumentException(
                "El identificador de la cita es obligatorio para actualizar.");
    }

    String sql = """
            UPDATE cita
            SET id_cliente = ?,
                id_profesional = ?,
                id_servicio = ?,
                id_estado_cita = ?,
                fecha_hora_inicio = ?,
                fecha_hora_fin = ?,
                fecha_cancelacion = ?,
                motivo_cancelacion = ?
            WHERE id_cita = ?
            """;

    try (Connection conexion = ConexionBD.obtenerConexion();
         PreparedStatement sentencia = conexion.prepareStatement(sql)) {

        sentencia.setLong(1, cita.getIdCliente());
        sentencia.setLong(2, cita.getIdProfesional());
        sentencia.setLong(3, cita.getIdServicio());
        sentencia.setInt(4, cita.getIdEstadoCita());

        sentencia.setTimestamp(
                5,
                Timestamp.valueOf(cita.getFechaHoraInicio()));

        sentencia.setTimestamp(
                6,
                Timestamp.valueOf(cita.getFechaHoraFin()));

        if (cita.getFechaCancelacion() != null) {
            sentencia.setTimestamp(
                    7,
                    Timestamp.valueOf(cita.getFechaCancelacion()));
        } else {
            sentencia.setNull(7, java.sql.Types.TIMESTAMP);
        }

        if (cita.getMotivoCancelacion() != null
                && !cita.getMotivoCancelacion().isBlank()) {
            sentencia.setString(
                    8,
                    cita.getMotivoCancelacion().trim());
        } else {
            sentencia.setNull(8, java.sql.Types.VARCHAR);
        }

        sentencia.setLong(9, cita.getIdCita());

        return sentencia.executeUpdate() == 1;
    }
}

/**
 * Elimina una cita por su identificador.
 *
 * Este método se utiliza únicamente con registros académicos
 * de prueba para demostrar la operación DELETE del CRUD.
 *
 * @param idCita identificador de la cita.
 * @return true si la cita fue eliminada.
 * @throws SQLException si ocurre un error de acceso a datos.
 */
public boolean eliminarCita(long idCita) throws SQLException {

    if (idCita <= 0) {
        throw new IllegalArgumentException(
                "El identificador de la cita debe ser mayor que cero.");
    }

    String sql = """
            DELETE FROM cita
            WHERE id_cita = ?
            """;

    try (Connection conexion = ConexionBD.obtenerConexion();
         PreparedStatement sentencia = conexion.prepareStatement(sql)) {

        sentencia.setLong(1, idCita);

        return sentencia.executeUpdate() == 1;
    }
}
}