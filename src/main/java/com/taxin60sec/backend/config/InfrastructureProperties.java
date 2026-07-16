package com.taxin60sec.backend.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="tax60.infrastructure")
public record InfrastructureProperties(Storage storage, Provider email, Provider whatsapp, Provider payment, Provider ai, Jobs jobs) {
 public record Storage(String provider,String bucket,long presignedUrlTtlSeconds,boolean versioning){} public record Provider(String provider,boolean enabled){} public record Jobs(boolean enabled,int retryLimit){}
}
