package com.jelly.zzirit.domain.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreSignedUrlCreateRequest {
    private String uploadId;
    private int partNumber;
    private String objectKey; // 반드시 클라이언트가 기억하고 보내야 함
}
