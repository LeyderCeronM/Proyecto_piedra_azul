package com.piedrazul.reports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.piedrazul.reports.model.ReportEntity;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    // Define queries necessary for aggregations here
}
