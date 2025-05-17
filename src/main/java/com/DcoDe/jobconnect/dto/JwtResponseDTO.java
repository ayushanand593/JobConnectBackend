package com.DcoDe.jobconnect.dto;

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
}
