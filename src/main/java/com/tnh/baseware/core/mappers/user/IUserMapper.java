package com.tnh.baseware.core.mappers.user;

import com.tnh.baseware.core.dtos.user.UserDTO;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.forms.user.UserEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;

import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IUserMapper extends IGenericMapper<User, UserEditorForm, UserDTO> {

    @Mapping(target = "password", source = "password", qualifiedByName = "passwordToPassword")
    User formToEntity(UserEditorForm form,
            @Context PasswordEncoder passwordEncoder);

    @Mapping(target = "password", ignore = true)
    void updateUserFromForm(UserEditorForm form,
            @MappingTarget User user);

    @Named("passwordToPassword")
    default String passwordToPassword(String password, @Context PasswordEncoder passwordEncoder) {
        return passwordEncoder.encode(password);
    }

}
