package com.citanova.modelo;

import java.time.LocalDateTime;

/**
 * Modelo de dominio alineado con la tabla cita de la base de datos CitaNOVA.
 */
public class Cita {

    private Long idCita;
    private Long idCliente;
    private Long idProfesional;
    private Long idServicio;
    private Integer idEstadoCita;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCancelacion;
    private String motivoCancelacion;

    public Cita() {
    }

    public Cita(Long idCliente,
                Long idProfesional,
                Long idServicio,
                Integer idEstadoCita,
                LocalDateTime fechaHoraInicio,
                LocalDateTime fechaHoraFin) {
        this.idCliente = idCliente;
        this.idProfesional = idProfesional;
        this.idServicio = idServicio;
        this.idEstadoCita = idEstadoCita;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
    }

    public Long getIdCita() {
        return idCita;
    }

    public void setIdCita(Long idCita) {
        this.idCita = idCita;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdProfesional() {
        return idProfesional;
    }

    public void setIdProfesional(Long idProfesional) {
        this.idProfesional = idProfesional;
    }

    public Long getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Long idServicio) {
        this.idServicio = idServicio;
    }

    public Integer getIdEstadoCita() {
        return idEstadoCita;
    }

    public void setIdEstadoCita(Integer idEstadoCita) {
        this.idEstadoCita = idEstadoCita;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    @Override
    public String toString() {
        return "Cita{" +
                "idCita=" + idCita +
                ", idCliente=" + idCliente +
                ", idProfesional=" + idProfesional +
                ", idServicio=" + idServicio +
                ", idEstadoCita=" + idEstadoCita +
                ", fechaHoraInicio=" + fechaHoraInicio +
                ", fechaHoraFin=" + fechaHoraFin +
                '}';
    }
}
