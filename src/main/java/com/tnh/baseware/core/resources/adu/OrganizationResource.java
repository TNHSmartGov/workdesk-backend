package com.tnh.baseware.core.resources.adu;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.dtos.adu.OrganizationDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.dtos.user.UserDTO;
import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.enums.ApiResponseType;
import com.tnh.baseware.core.forms.adu.OrganizationEditorForm;
import com.tnh.baseware.core.forms.user.AssignUserEditorForm;
import com.tnh.baseware.core.forms.user.ChangeUserTitleEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.adu.IOrganizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Organizations", description = "API for managing organizations")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/organizations")
public class OrganizationResource extends
                GenericResource<Organization, OrganizationEditorForm, OrganizationDTO, UUID> {

        IOrganizationService organizationService;

        public OrganizationResource(
                        IGenericService<Organization, OrganizationEditorForm, OrganizationDTO, UUID> service,
                        MessageService messageService, IOrganizationService organizationService,
                        SystemProperties systemProperties) {
                super(service, messageService, systemProperties.getApiPrefix() + "/organizations");
                this.organizationService = organizationService;
        }

        @Operation(summary = "Assign all organizations to a parent unit")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @PostMapping("/{id}/assign-organizations")
        public ResponseEntity<ApiMessageDTO<Integer>> assignOrganizations(@PathVariable UUID id,
                        @RequestBody List<UUID> ids) {
                organizationService.assignOrganizations(id, ids);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("organizations.assigned"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Remove all organizations from a parent unit")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @DeleteMapping("/{id}/remove-organizations")
        public ResponseEntity<ApiMessageDTO<Integer>> removeOrganizations(@PathVariable UUID id,
                        @RequestBody List<UUID> ids) {
                organizationService.removeOrganizations(id, ids);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("organizations.removed"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Assign all users to an organization")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @PostMapping("/{id}/assign-users")
        public ResponseEntity<ApiMessageDTO<Integer>> assignUsers(@PathVariable UUID id,
                        @RequestBody List<AssignUserEditorForm> form) {
                organizationService.assignUsers(id, form);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("users.assigned"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Remove all users from an organization")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @DeleteMapping("/{id}/remove-users")
        public ResponseEntity<ApiMessageDTO<Integer>> removeUsers(@PathVariable UUID id,
                        @RequestBody List<UUID> userIds) {
                organizationService.removeUsers(id, userIds);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("users.removed"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Change user's title")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @PutMapping("{orgId}/users/{userId}/title")
        public ResponseEntity<ApiMessageDTO<Integer>> changeTitle(@PathVariable UUID orgId,
                                                                  @PathVariable UUID userId,
                                                                  @RequestBody ChangeUserTitleEditorForm request) {
                organizationService.changeTitle(orgId, userId, request.getTitleId());
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                        .data(1)
                        .result(true)
                        .message(messageService.getMessage("users.title.changed"))
                        .code(HttpStatus.OK.value())
                        .build());
        }
}
