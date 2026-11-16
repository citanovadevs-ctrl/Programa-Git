package com.citanova.app;

import com.citanova.conexion.ConexionBD;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Prueba minima de conectividad JDBC para la evidencia EV01.
 * No realiza operaciones CRUD; esas se integraran posteriormente.
 */
public class PruebaConexion {

    private static final String SQL_BASE_ACTIVA = "SELECT DATABASE()";
    private static final String SQL_TABLA_CITA = """
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = ?
            """;

    public static void main(String[] args) {
        System.out.println("=== CitaNOVA | Prueba de conexion JDBC ===");

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            DatabaseMetaData metadata = conexion.getMetaData();

            System.out.println("Conexion JDBC exitosa.");
            System.out.println("Motor: " + metadata.getDatabaseProductName());
            System.out.println("Version: " + metadata.getDatabaseProductVersion());
            System.out.println("Base activa: " + consultarBaseActiva(conexion));
            System.out.println("Tabla cita disponible: " + existeTablaCita(conexion));
            System.out.println("Resultado: BASE JDBC LISTA PARA INTEGRAR EL CRUD.");
        } catch (SQLException | IllegalStateException e) {
            System.err.println("No fue posible validar la conexion JDBC.");
            System.err.println("Detalle: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String consultarBaseActiva(Connection conexion) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_BASE_ACTIVA);
             ResultSet resultado = sentencia.executeQuery()) {
            return resultado.next() ? resultado.getString(1) : "sin base activa";
        }
    }

    private static boolean existeTablaCita(Connection conexion) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_TABLA_CITA)) {
            sentencia.setString(1, "cita");
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }
}
