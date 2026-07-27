package com.clinzo.service;

import com.clinzo.entity.AuditLog;
import com.clinzo.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String action, String entityType, String entityId, String metadata) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        auditLogRepository.save(auditLog);
    }
}
