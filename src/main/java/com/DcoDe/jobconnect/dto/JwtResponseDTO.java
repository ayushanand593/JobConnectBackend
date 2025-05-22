package com.DcoDe.jobconnect.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseDTO {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserDTO user;

   @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean requiresTermsAcceptance;
}
