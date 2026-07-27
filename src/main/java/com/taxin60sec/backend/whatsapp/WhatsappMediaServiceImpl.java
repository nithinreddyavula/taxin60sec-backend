package com.taxin60sec.backend.whatsapp;

import com.taxin60sec.backend.whatsapp.dto.MetaMediaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class WhatsappMediaServiceImpl implements WhatsappMediaService {

    private final RestClient restClient;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.graph-api-version}")
    private String apiVersion;

    @Override
    public byte[] downloadMedia(String mediaId) {

        MetaMediaResponse response = restClient.get()
                .uri("https://graph.facebook.com/"
                        + apiVersion
                        + "/"
                        + mediaId)
                .header("Authorization",
                        "Bearer " + accessToken)
                .retrieve()
                .body(MetaMediaResponse.class);

        if (response == null || response.getUrl() == null) {
            throw new RuntimeException("Unable to fetch media URL.");
        }

        Resource resource = restClient.get()
                .uri(response.getUrl())
                .header("Authorization",
                        "Bearer " + accessToken)
                .retrieve()
                .body(Resource.class);

        try {
            return resource.getInputStream().readAllBytes();
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Unable to download WhatsApp media.",
                    ex
            );
        }
    }
}