package com.taxin60sec.backend.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.entity.AuditLog;
import com.taxin60sec.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class JpaAuditService implements AuditContracts.AuditService {
    private final AuditLogRepository logs;
    private final ObjectMapper json;

    @Override
    public void record(AuditContracts.AuditEvent event) {
        AuditLog log = new AuditLog();
        log.setAction(event.action());
        log.setEntityType(event.entityType());
        log.setEntityId(event.entityId());
        log.setActorId(event.actorId());
        try {
            log.setAttributes(json.writeValueAsString(event.attributes()));
        } catch (Exception ignored) {
            log.setAttributes("{}");
        }
        logs.save(log);
    }
}
