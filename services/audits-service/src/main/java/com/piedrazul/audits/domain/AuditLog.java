package com.piedrazul.audits.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    private String servicioOrigen;

    private String tipoEvento;

    private String operacion;

    private String usuarioOperador;

    @Column(columnDefinition = "text")
    private String detallePayload;

    @Column(nullable = false)
    private Instant fecha;

    public AuditLog() {
    }

    public AuditLog(String servicioOrigen, String tipoEvento, String operacion, String usuarioOperador, String detallePayload) {
        this.servicioOrigen = servicioOrigen;
        this.tipoEvento = tipoEvento;
        this.operacion = operacion;
        this.usuarioOperador = usuarioOperador;
        this.detallePayload = detallePayload;
    }

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = Instant.now();
        }
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

    public String getDetallePayload() {
        return detallePayload;
    }

    public void setDetallePayload(String detallePayload) {
        this.detallePayload = detallePayload;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }
}
