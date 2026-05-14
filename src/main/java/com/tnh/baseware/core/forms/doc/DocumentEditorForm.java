package com.tnh.baseware.core.forms.doc;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DocumentEditorForm {

    @NotBlank(message = "{document.number.required}")
    @Schema(description = "Document number (số văn bản)", example = "123/2024/QĐ-TTg")
    String documentNumber;

    @Schema(description = "Document summary (trích yếu)")
    String summary;

    @Schema(description = "Issued date (ngày ban hành)", example = "2024-01-14T00:00:00Z")
    Instant issuedDate;

    @Schema(description = "Issued by (cơ quan ban hành)", example = "Thủ tướng Chính phủ")
    String issuedBy;

    @NotBlank(message = "Document type is required")
    @Schema(description = "Document type")
    String documentType;

    @Schema(description = "External source system", example = "EOFFICE")
    String externalSource;

    @Schema(description = "External ID from source system")
    String externalId;

    @Schema(description = "Document form (hình thức văn bản)", example = "Văn bản đi")
    String documentForm;
}
