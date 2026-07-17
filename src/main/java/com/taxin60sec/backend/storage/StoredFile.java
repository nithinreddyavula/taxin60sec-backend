package com.taxin60sec.backend.storage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoredFile {

    private String originalName;

    private String storedName;

    private String path;

    private String contentType;

    private long size;

    private String sha256;

}