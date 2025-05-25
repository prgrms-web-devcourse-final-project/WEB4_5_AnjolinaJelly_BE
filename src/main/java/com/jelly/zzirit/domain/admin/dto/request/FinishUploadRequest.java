package com.jelly.zzirit.domain.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinishUploadRequest {
    private String uploadId;
    private String objectKey;
    private List<Part> parts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private int partNumber;
        private String eTag;
    }
}
