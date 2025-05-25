package com.dcode.jobconnect.components;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


public class FileTypeValidator {

  private FileTypeValidator() {}
    
    public static void validateMimeType(MultipartFile file,
                              List<String> allowed,
                              String fieldName) {
    if (file != null && ! allowed.contains(file.getContentType())) {
        throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE,
          String.format("%s must be one of: %s", fieldName, "pdf, docx, doc, txt")
        );
    }
}
}