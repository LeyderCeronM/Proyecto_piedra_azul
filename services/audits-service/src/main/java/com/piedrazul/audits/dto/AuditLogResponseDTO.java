package com.piedrazul.audits.dto;

import com.piedrazul.audits.domain.AuditLog;

import java.time.Instant;
import java.util.UUID;

public class AuditLogResponseDTO {

    private UUID id;
    private String servicioOrigen;
    private String tipoEvento;
    private String operacion;
    private String usuarioOperador;
    private Instant fecha;

    public AuditLogResponseDTO() {
    }

    public AuditLogResponseDTO(UUID id, String servicioOrigen, String tipoEvento, String operacion, String usuarioOperador, Instant fecha) {
        this.id = id;
        this.servicioOrigen = servicioOrigen;
        this.tipoEvento = tipoEvento;
        this.operacion = operacion;
        this.usuarioOperador = usuarioOperador;
        this.fecha = fecha;
    }

    public static AuditLogResponseDTO fromEntity(AuditLog a) {
        return new AuditLogResponseDTO(a.getId(), a.getServicioOrigen(), a.getTipoEvento(), a.getOperacion(), a.getUsuarioOperador(), a.getFecha());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getServicioOrigen() {
        return servicioOrigen;
    }

    public void setServicioOrigen(String servicioOrigen) {
        this.servicioOrigen = servicioOrigen;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public String getUsuarioOperador() {
        return usuarioOperador;
    }

    public void setUsuarioOperador(String usuarioOperador) {
        this.usuarioOperador = usuarioOperador;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }
}
