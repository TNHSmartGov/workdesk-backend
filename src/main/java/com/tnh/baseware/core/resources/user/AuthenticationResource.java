package com.tnh.baseware.core.resources.user;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.dtos.user.AuthenticationDTO;
import com.tnh.baseware.core.dtos.user.UserDTO;
import com.tnh.baseware.core.enums.ApiResponseType;
import com.tnh.baseware.core.forms.user.AuthenticationForm;
import com.tnh.baseware.core.forms.user.RegisterForm;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.user.IUserService;
import com.tnh.baseware.core.services.user.imp.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "API for authentication")
@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/auth")
public class AuthenticationResource {

    AuthenticationService authenticationService;
    MessageService messageService;
    IUserService userService;

    @Operation(summary = "Register a new user")
    @ApiOkResponse(value = UserDTO.class, type = ApiResponseType.OBJECT)
    @PostMapping("/register")
    public ResponseEntity<ApiMessageDTO<UserDTO>> registerUser(@Valid @RequestBody RegisterForm form,
                                                               HttpServletRequest request) {
        var userDTO = userService.registerUser(form, request);
        var dto = ApiMessageDTO.<UserDTO>builder()
                .data(userDTO)
                .result(true)
                .message(messageService.getMessage("user.registered"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Authenticate a user")
    @ApiOkResponse(value = UserDTO.class, type = ApiResponseType.OBJECT)
    @PostMapping("/login")
    public ResponseEntity<ApiMessageDTO<AuthenticationDTO>> login(@Valid @RequestBody AuthenticationForm form,
                                                                  HttpServletRequest request) {
        var authenticationDTO = authenticationService.login(form, request);
        var apiMessageDTO = ApiMessageDTO.<AuthenticationDTO>builder()
                .data(authenticationDTO)
                .result(true)
                .message(messageService.getMessage("user.logged.in"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(apiMessageDTO);
    }

    @Operation(summary = "Refresh token")
    @ApiOkResponse(value = AuthenticationDTO.class, type = ApiResponseType.OBJECT)
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiMessageDTO<AuthenticationDTO>> refreshToken(HttpServletRequest request) {
        var authenticationDTO = authenticationService.refreshToken(request);
        var apiMessageDTO = ApiMessageDTO.<AuthenticationDTO>builder()
                .data(authenticationDTO)
                .result(true)
                .message(messageService.getMessage("token.refreshed"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(apiMessageDTO);
    }
}
