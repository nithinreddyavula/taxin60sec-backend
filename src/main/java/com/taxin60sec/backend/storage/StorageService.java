package com.taxin60sec.backend.storage;
import java.time.Instant; import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
public interface StorageService {
    StorageMetadata upload(
        MultipartFile file,
        boolean versioned
    );
    PresignedUrl createDownloadMetadata(String key, String version);
    StorageMetadata metadata(String key, String version);
    void softDelete(String key, String version);
    record StorageMetadata(String key,String version,String contentType,long contentLength,Instant createdAt,boolean deleted,Map<String,String> attributes){}
    record PresignedUrl(String url,Instant expiresAt,String method,Map<String,String> headers){}
    interface StorageProvider { String name(); StorageMetadata save(StorageMetadata metadata); PresignedUrl presignDownload(String key,String version); void softDelete(String key,String version); Health health(); }
    record Health(boolean available,String detail){}
}
