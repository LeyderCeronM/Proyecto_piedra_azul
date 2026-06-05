package com.piedrazul.audits.web;

import com.piedrazul.audits.domain.AuditLog;
import com.piedrazul.audits.dto.AuditLogResponseDTO;
import com.piedrazul.audits.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audits")
public class AuditsController {

    private final AuditLogRepository repository;

    public AuditsController(AuditLogRepository repository) {
        this.repository = repository;
    }

    // E6-HU2: Consultar por usuario con paginación
    @GetMapping
    public ResponseEntity<Page<AuditLogResponseDTO>> getAudits(
            @RequestParam(name = "user", required = false) String usuario,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "fecha"));

        Page<AuditLog> pageResult;
        if (usuario != null && !usuario.isBlank()) {
            pageResult = repository.findByUsuarioOperador(usuario, pageable);
        } else {
            pageResult = repository.findAll(pageable);
        }

        Page<AuditLogResponseDTO> dtoPage = pageResult.map(AuditLogResponseDTO::fromEntity);

        HttpHeaders headers = new HttpHeaders();
        if (dtoPage.isEmpty()) {
            headers.add("X-Message", "Sin registros");
        }
        return new ResponseEntity<>(dtoPage, headers, HttpStatus.OK);
    }

    // E6-HU1: obtener sólo eventos de transferencia y soportar export
    @GetMapping("/transfers")
    public ResponseEntity<?> getTransfers(
            @RequestParam(name = "export", required = false, defaultValue = "false") boolean export,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size
    ) {
        // asumimos que los eventos de tipo transferencia usan el valor "TRANSFER" en tipoEvento
        if (export) {
            List<AuditLog> transfers = repository.findByTipoEvento("TRANSFER");
            String csv = toCsv(transfers);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transfers.csv");
            if (transfers.isEmpty()) {
                headers.add("X-Message", "Sin registros");
            }
            return new ResponseEntity<>(csv, headers, HttpStatus.OK);
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "fecha"));
        Page<AuditLog> pageResult = repository.findByTipoEvento("TRANSFER", pageable);
        Page<AuditLogResponseDTO> dtoPage = pageResult.map(AuditLogResponseDTO::fromEntity);

        HttpHeaders headers = new HttpHeaders();
        if (dtoPage.isEmpty()) {
            headers.add("X-Message", "Sin registros");
        }
        return new ResponseEntity<>(dtoPage, headers, HttpStatus.OK);
    }

    private String toCsv(List<AuditLog> list) {
        var sb = new StringBuilder();
        sb.append("id,servicioOrigen,tipoEvento,operacion,usuarioOperador,detallePayload,fecha\n");
        String rows = list.stream().map(a -> String.format("%s,%s,%s,%s,%s,%s,%s",
                a.getId(),
                safe(a.getServicioOrigen()),
                safe(a.getTipoEvento()),
                safe(a.getOperacion()),
                safe(a.getUsuarioOperador()),
                safe(a.getDetallePayload()),
                a.getFecha() != null ? a.getFecha().toString() : "")).collect(Collectors.joining("\n"));
        sb.append(rows);
        return sb.toString();
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replaceAll("\r|\n|,", " ");
    }
}
