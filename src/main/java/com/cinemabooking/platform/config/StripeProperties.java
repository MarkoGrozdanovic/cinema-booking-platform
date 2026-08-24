package com.cinemabooking.platform.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    @NotBlank
    private String secretKey;

    @NotBlank
    private String currency;

    @NotBlank
    private String webhookSecret;
}
