package com.piedrazul.audits.repository;

import com.piedrazul.audits.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByUsuarioOperador(String usuarioOperador, Pageable pageable);

    Page<AuditLog> findByTipoEvento(String tipoEvento, Pageable pageable);

    List<AuditLog> findByTipoEvento(String tipoEvento);
}
