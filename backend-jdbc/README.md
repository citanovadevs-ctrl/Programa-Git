# CitaNOVA - Base JDBC (EV01)

Base tecnica creada para la evidencia **GA7-220501096-AA2-EV01**.

## Alcance de esta rama

Esta rama prepara solamente:

- proyecto Maven;
- dependencia MySQL Connector/J;
- conexion JDBC centralizada;
- modelo `Cita` alineado con la tabla `cita` de CitaNOVA;
- prueba de conectividad con la base `citanova`.

**No incluye el CRUD.** El CRUD se implementara posteriormente en la rama de Fredy.

## Configuracion local

1. Copiar:

```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

2. Editar `config.properties` con el usuario y la clave local de MySQL.

`config.properties` esta ignorado por Git y no debe subirse al repositorio.

## Compilar

```bash
mvn clean compile
```

## Probar conexion

```bash
mvn -q exec:java -Dexec.mainClass=com.citanova.app.PruebaConexion
```

La prueba debe confirmar la conexion, la base `citanova` y la existencia de la tabla `cita`.
