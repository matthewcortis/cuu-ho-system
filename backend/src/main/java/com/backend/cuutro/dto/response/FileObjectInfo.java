package com.backend.cuutro.dto.response;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileObjectInfo {
    private String objectName;
    private String bucketName;
    private Long size;
    private String contentType;
    private String etag;
    private ZonedDateTime lastModified;
    private Map<String, String> userMetadata;
    private String region;
    private String versionId;
}