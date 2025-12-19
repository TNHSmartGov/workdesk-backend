package com.tnh.baseware.core.forms.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeUserTitleEditorForm {
    @NotBlank
    String title;
}
