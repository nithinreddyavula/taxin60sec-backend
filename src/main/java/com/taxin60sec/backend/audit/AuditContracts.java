package com.taxin60sec.backend.audit;
import java.time.Instant; import java.util.Map;
public final class AuditContracts { private AuditContracts(){} public record AuditEvent(String action,String entityType,String entityId,String actorId,Instant occurredAt,Map<String,String> attributes){} public interface AuditService { void record(AuditEvent event); } public interface AuditPublisher { void publish(AuditEvent event); } public interface AuditListener { void onEvent(AuditEvent event); } }
