package com.piedrazul.reports.model;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ReportRequest {

    @NotNull(message = "'from' no puede ser nulo")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @NotNull(message = "'to' no puede ser nulo")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }
}
