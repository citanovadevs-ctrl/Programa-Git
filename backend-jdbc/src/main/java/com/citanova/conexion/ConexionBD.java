package com.citanova.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona de forma centralizada la conexion JDBC con la base de datos CitaNOVA.
 * Las credenciales se leen desde config.properties, archivo local no versionado.
 */
public final class ConexionBD {

    private static final String ARCHIVO_CONFIGURACION = "/config.properties";
    private static final Properties CONFIGURACION = cargarConfiguracion();

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "No se encontro el driver JDBC de MySQL. Verifique la dependencia mysql-connector-j en pom.xml.", e);
        }
    }

    private ConexionBD() {
        // Evita instanciar esta clase utilitaria.
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = obtenerPropiedadObligatoria("db.url");
        String usuario = obtenerPropiedadObligatoria("db.user");
        String clave = obtenerPropiedadObligatoria("db.password");

        return DriverManager.getConnection(url, usuario, clave);
    }

    private static Properties cargarConfiguracion() {
        Properties propiedades = new Properties();

        try (InputStream entrada = ConexionBD.class.getResourceAsStream(ARCHIVO_CONFIGURACION)) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "No existe src/main/resources/config.properties. "
                                + "Copie config.properties.example y complete sus credenciales locales.");
            }
            propiedades.load(entrada);
            return propiedades;
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible leer config.properties.", e);
        }
    }

    private static String obtenerPropiedadObligatoria(String clave) {
        String valor = CONFIGURACION.getProperty(clave);
        if (valor == null || valor.isBlank() || valor.contains("CAMBIAR_")) {
            throw new IllegalStateException("Configure la propiedad obligatoria: " + clave);
        }
        return valor.trim();
    }
}
