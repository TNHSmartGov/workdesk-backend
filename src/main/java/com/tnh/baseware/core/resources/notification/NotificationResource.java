package com.tnh.baseware.core.resources.notification;

import com.tnh.baseware.core.dtos.notification.NotificationDTO;
import com.tnh.baseware.core.dtos.notification.NotificationTicketDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.notification.INotificationService;
import com.tnh.baseware.core.services.notification.ISseTicketService;
import com.tnh.baseware.core.services.notification.imp.LocalSseEmitterManager;
import com.tnh.baseware.core.utils.LogStyleHelper;
import com.tnh.baseware.core.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Tag(name = "Notification", description = "Notification SSE APIs")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${baseware.core.system.api-prefix}/notifications")
public class NotificationResource {

    ISseTicketService ticketService;
    LocalSseEmitterManager sseEmitterManager;
    INotificationService notificationService;
    SecurityUtils securityUtils;
    MessageService messageService;

    @Operation(summary = "Create SSE ticket")
    @PostMapping("/ticket")
    public ResponseEntity<ApiMessageDTO<NotificationTicketDTO>> createTicket() {
        var userId = securityUtils.currentUser().getId();
        var ticket = ticketService.generateTicket(userId);
        var dto = new NotificationTicketDTO(ticket);

        var response = ApiMessageDTO.<NotificationTicketDTO>builder()
                .data(dto)
                .result(true)
                .message(messageService.getMessage("processing.success"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Open SSE stream")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam("ticket") String ticket,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {

        log.info(LogStyleHelper.info("📡 SSE stream request - Ticket: {}, Last-Event-ID: {}"),
                ticket.substring(0, 8) + "...", lastEventId != null ? lastEventId : "none");

        var userId = ticketService.validateAndRemoveTicket(ticket);
        if (userId == null) {
            log.warn(LogStyleHelper.warn("❌ SSE stream rejected - Invalid ticket"));
            throw new BWCValidationException(messageService.getMessage("token.invalid"));
        }

        log.info(LogStyleHelper.success("🔌 SSE stream connecting - User: {}"), userId);
        var emitter = sseEmitterManager.addEmitter(userId);
        sseEmitterManager.replayMissed(userId, lastEventId, emitter);
        return emitter;
    }

    @Operation(summary = "List notifications")
    @GetMapping
    public ResponseEntity<ApiMessageDTO<PagedModel<NotificationDTO>>> list(
            @RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            @SortDefault(sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            PagedResourcesAssembler<NotificationDTO> assembler) {

        UUID userId = securityUtils.currentUser().getId();
        var page = notificationService.list(userId, unreadOnly, pageable);
        var pagedModel = assembler.toModel(page, item -> item);

        var response = ApiMessageDTO.<PagedModel<NotificationDTO>>builder()
                .data(pagedModel)
                .result(true)
                .message(messageService.getMessage("entities.retrieved"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Count unread notifications")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiMessageDTO<Long>> countUnread() {
        UUID userId = securityUtils.currentUser().getId();
        long count = notificationService.countUnread(userId);

        var response = ApiMessageDTO.<Long>builder()
                .data(count)
                .result(true)
                .message(messageService.getMessage("entities.retrieved"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark a notification as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiMessageDTO<Integer>> markRead(
            @org.springframework.web.bind.annotation.PathVariable UUID id) {
        UUID userId = securityUtils.currentUser().getId();
        int updated = notificationService.markRead(userId, id, Instant.now());

        var response = ApiMessageDTO.<Integer>builder()
                .data(updated)
                .result(true)
                .message(messageService.getMessage("entity.updated"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<ApiMessageDTO<Integer>> markAllRead() {
        UUID userId = securityUtils.currentUser().getId();
        int updated = notificationService.markAllRead(userId, Instant.now());

        var response = ApiMessageDTO.<Integer>builder()
                .data(updated)
                .result(true)
                .message(messageService.getMessage("entity.updated"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }
}
