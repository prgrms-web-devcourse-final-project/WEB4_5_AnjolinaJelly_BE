package com.jelly.zzirit.domain.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreSignedUrlAbortRequest {
    private String uploadId;
    private String objectKey;
}