package com.tnh.baseware.core.forms.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tnh.baseware.core.enums.task.MemberStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateTaskReportForm {
    @NotBlank(message = "{report.content.required}")
    String content;

    Integer progress; // 0-100, optional

    @jakarta.validation.constraints.NotNull(message = "{report.status.required}")
    MemberStatus status;

    List<UUID> fileIds; // Using FileDocument IDs
}
