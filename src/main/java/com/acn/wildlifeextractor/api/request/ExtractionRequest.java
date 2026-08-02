package com.acn.wildlifeextractor.api.request;

import java.time.OffsetDateTime;

import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound extraction request. Bean Validation covers presence and coarse bounds; the configured
 * maximum transcript length is enforced in the service against typed configuration.
 */
public record ExtractionRequest(

        @NotBlank
        @Size(max = 200)
        String requestId,

        @NotBlank
        @Size(max = 200)
        String conversationId,

        @NotNull
        WildlifeFormType formType,

        @NotBlank
        String transcript,

        @NotNull
        OffsetDateTime transcriptTimestamp,

        @Size(max = 16)
        String language
) {
}
