package com.dcode.jobconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageUploadResponseDTO {
    private String fileId;
    private String message;
}