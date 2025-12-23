package com.tnh.baseware.core.resources.user;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.dtos.user.UserDTO;
import com.tnh.baseware.core.dtos.user.UserTokenDTO;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.ApiResponseType;
import com.tnh.baseware.core.forms.user.ChangePasswordForm;
import com.tnh.baseware.core.forms.user.ResetPasswordForm;
import com.tnh.baseware.core.forms.user.UserEditorForm;
import com.tnh.baseware.core.forms.user.UserProfileForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "API for managing users")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/users")
public class UserResource extends GenericResource<User, UserEditorForm, UserDTO, UUID> {

        IUserService userService;

        public UserResource(IGenericService<User, UserEditorForm, UserDTO, UUID> service,
                        MessageService messageService, IUserService userService,
                        SystemProperties systemProperties) {
                super(service, messageService, systemProperties.getApiPrefix() + "/users");
                this.userService = userService;
        }

        @Operation(summary = "Edit user profile")
        @ApiOkResponse(value = UserDTO.class, type = ApiResponseType.OBJECT)
        @PutMapping("/{id}/profile")
        public ResponseEntity<ApiMessageDTO<UserDTO>> editProfile(@PathVariable UUID id,
                        @Valid @RequestBody UserProfileForm form) {
                var userDTO = userService.editProfile(id, form);
                return ResponseEntity.ok(ApiMessageDTO.<UserDTO>builder()
                                .data(userDTO)
                                .result(true)
                                .message(messageService.getMessage("user.profile.updated"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Enable a user")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @PutMapping("/{id}/enable")
        public ResponseEntity<ApiMessageDTO<Integer>> enableUser(@PathVariable UUID id) {
                userService.enableUser(id);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("user.enabled"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Disable a user")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @PutMapping("/{id}/disable")
        public ResponseEntity<ApiMessageDTO<Integer>> disableUser(@PathVariable UUID id) {
                userService.disableUser(id);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("user.disabled"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Lock a user")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @PutMapping("/{id}/lock")
        public ResponseEntity<ApiMessageDTO<Integer>> lockUser(@PathVariable UUID id) {
                userService.lockUser(id);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("user.locked"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Unlock a user")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @PutMapping("/{id}/unlock")
        public ResponseEntity<ApiMessageDTO<Integer>> unlockUser(@PathVariable UUID id) {
                userService.unlockUser(id);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("user.unlocked"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Reset failed login attempts")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @PutMapping("/{id}/reset-failed-login-attempts")
        public ResponseEntity<ApiMessageDTO<Integer>> resetFailedLoginAttempts(@PathVariable UUID id) {
                userService.resetFailedLoginAttempts(id);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("user.failed.login.attempts.reset"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Change password")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @PutMapping("/{id}/change-password")
        public ResponseEntity<ApiMessageDTO<Integer>> changePassword(@PathVariable UUID id,
                        @Valid @RequestBody ChangePasswordForm form) {
                userService.changePassword(id, form);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("user.password.changed"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Reset password")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        @PutMapping("/reset-password")
        public ResponseEntity<ApiMessageDTO<Integer>> resetPassword(@RequestBody ResetPasswordForm form) {
                userService.resetPassword(form);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("user.password.reset"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find an user by token")
        @ApiOkResponse(value = UserTokenDTO.class, type = ApiResponseType.OBJECT)
        @GetMapping("/by-token")
        public ResponseEntity<ApiMessageDTO<UserTokenDTO>> findByToken(HttpServletRequest request) {
                var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiMessageDTO.<UserTokenDTO>builder()
                                                        .result(false)
                                                        .message(messageService.getMessage("token.not.found"))
                                                        .code(HttpStatus.UNAUTHORIZED.value())
                                                        .build());
                }

                var userTokenDTO = userService.findByToken(authHeader.substring(7));
                return ResponseEntity.ok(ApiMessageDTO.<UserTokenDTO>builder()
                                .data(userTokenDTO)
                                .result(true)
                                .message(messageService.getMessage("user.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all users by organization")
        @ApiOkResponse(UserDTO.class)
        @GetMapping("/by-organization/{organizationId}")
        public ResponseEntity<ApiMessageDTO<List<UserDTO>>> findAllByOrganization(@PathVariable UUID organizationId) {
                var users = userService.findAllByOrganization(organizationId);
                return ResponseEntity.ok(ApiMessageDTO.<List<UserDTO>>builder()
                                .data(users)
                                .result(true)
                                .message(messageService.getMessage("users.in.organization"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all users by organization with pagination")
        @ApiOkResponse(value = UserDTO.class, type = ApiResponseType.HATEOAS_PAGE)
        @GetMapping("/by-organization/{organizationId}/pagination")
        public ResponseEntity<ApiMessageDTO<PagedModel<UserDTO>>> findAllByOrganizationWithPagination(
                        @PathVariable UUID organizationId,
                        Pageable pageable,
                        PagedResourcesAssembler<UserDTO> assembler) {
                var users = userService.findAllByOrganization(organizationId, pageable);
                var pagedModel = assembler.toModel(users, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<UserDTO>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("users.in.organization"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all users without organization")
        @ApiOkResponse(value = UserDTO.class)
        @GetMapping("/without-organization/{organizationId}")
        public ResponseEntity<ApiMessageDTO<List<UserDTO>>> findAllWithoutOrganization(
                        @PathVariable UUID organizationId) {
                var users = userService.findAllWithoutOrganization(organizationId);
                return ResponseEntity.ok(ApiMessageDTO.<List<UserDTO>>builder()
                                .data(users)
                                .result(true)
                                .message(messageService.getMessage("users.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all users without organization with pagination")
        @ApiOkResponse(value = UserDTO.class, type = ApiResponseType.HATEOAS_PAGE)
        @GetMapping("/without-organization/{organizationId}/pagination")
        public ResponseEntity<ApiMessageDTO<PagedModel<UserDTO>>> findAllWithoutOrganizationWithPagination(
                        @PathVariable UUID organizationId,
                        Pageable pageable,
                        PagedResourcesAssembler<UserDTO> assembler) {
                var users = userService.findAllWithoutOrganization(organizationId, pageable);
                var pagedModel = assembler.toModel(users, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<UserDTO>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("users.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all users without role")
        @ApiOkResponse(value = UserDTO.class)
        @GetMapping("/without-role/{roleId}")
        public ResponseEntity<ApiMessageDTO<List<UserDTO>>> findAllWithoutRole(@PathVariable UUID roleId) {
                var users = userService.findAllWithoutRole(roleId);
                return ResponseEntity.ok(ApiMessageDTO.<List<UserDTO>>builder()
                                .data(users)
                                .result(true)
                                .message(messageService.getMessage("users.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all users without role with pagination")
        @ApiOkResponse(value = UserDTO.class, type = ApiResponseType.HATEOAS_PAGE)
        @GetMapping("/without-role/{roleId}/pagination")
        public ResponseEntity<ApiMessageDTO<PagedModel<UserDTO>>> findAllWithoutRoleWithPagination(
                        @PathVariable UUID roleId,
                        Pageable pageable,
                        PagedResourcesAssembler<UserDTO> assembler) {
                var users = userService.findAllWithoutRole(roleId, pageable);
                var pagedModel = assembler.toModel(users, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<UserDTO>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("users.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all users by role")
        @ApiOkResponse(value = UserDTO.class)
        @GetMapping("/by-role/{roleId}")
        public ResponseEntity<ApiMessageDTO<List<UserDTO>>> findAllByRole(@PathVariable UUID roleId) {
                var users = userService.findAllByRole(roleId);
                return ResponseEntity.ok(ApiMessageDTO.<List<UserDTO>>builder()
                                .data(users)
                                .result(true)
                                .message(messageService.getMessage("users.in.role"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all users by role with pagination")
        @ApiOkResponse(value = UserDTO.class, type = ApiResponseType.HATEOAS_PAGE)
        @GetMapping("/by-role/{roleId}/pagination")
        public ResponseEntity<ApiMessageDTO<PagedModel<UserDTO>>> findAllByRoleWithPagination(@PathVariable UUID roleId,
                        Pageable pageable,
                        PagedResourcesAssembler<UserDTO> assembler) {
                var users = userService.findAllByRole(roleId, pageable);
                var pagedModel = assembler.toModel(users, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<UserDTO>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("users.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }
}
