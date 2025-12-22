package com.tnh.baseware.core.mappers.user;

import com.tnh.baseware.core.dtos.user.UserDTO;
import com.tnh.baseware.core.entities.user.UserOrganization;
import com.tnh.baseware.core.forms.user.UserEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IUserOrganizationMapper extends IGenericMapper<UserOrganization, UserEditorForm, UserDTO> {
}
