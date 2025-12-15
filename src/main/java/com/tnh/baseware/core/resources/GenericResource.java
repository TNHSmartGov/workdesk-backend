package com.tnh.baseware.core.resources;

import com.tnh.baseware.core.dtos.audit.EnumDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.specs.SearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class GenericResource<E, F, D extends RepresentationModel<D> & Identifiable<I>, I>
                implements IGenericResource<E, F, D, I> {

        IGenericService<E, F, D, I> service;
        MessageService messageService;
        String basePath;

        @Operation(summary = "Create a new entity")
        @ApiResponse(responseCode = "200", description = "Entity created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @PostMapping
        public ResponseEntity<ApiMessageDTO<D>> create(@Valid @RequestBody F f) {
                var created = service.create(f);
                return ResponseEntity.ok(ApiMessageDTO.<D>builder()
                                .data(created)
                                .result(true)
                                .message(messageService.getMessage("entity.created"))
                                .code(HttpStatus.CREATED.value())
                                .build());
        }

        @Operation(summary = "Update an existing entity by ID")
        @ApiResponse(responseCode = "200", description = "Entity updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @PutMapping("/{id}")
        public ResponseEntity<ApiMessageDTO<D>> update(@PathVariable I id,
                        @Valid @RequestBody F f) {
                var updated = service.update(id, f);
                return ResponseEntity.ok(ApiMessageDTO.<D>builder()
                                .data(updated)
                                .result(true)
                                .message(messageService.getMessage("entity.updated"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Delete an entity by ID")
        @ApiResponse(responseCode = "200", description = "Entity deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiMessageDTO<Integer>> deleteById(@PathVariable I id) {
                service.delete(id);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("entity.deleted"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Delete multiple entities by IDs")
        @ApiResponse(responseCode = "200", description = "Entities deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @DeleteMapping
        public ResponseEntity<ApiMessageDTO<Integer>> deleteAllByIds(@RequestBody List<I> ids) {
                service.deleteAllByIds(ids);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(ids.size())
                                .result(true)
                                .message(messageService.getMessage("entities.deleted"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Soft delete an entity by ID")
        @ApiResponse(responseCode = "200", description = "Entity soft deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @DeleteMapping("/soft-delete/{id}")
        public ResponseEntity<ApiMessageDTO<Integer>> softDeleteById(@PathVariable I id) {
                service.softDeleteById(id);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("entity.soft.deleted"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Soft delete multiple entities by IDs")
        @ApiResponse(responseCode = "200", description = "Entities soft deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @DeleteMapping("/soft-delete")
        public ResponseEntity<ApiMessageDTO<Integer>> softDeleteAllByIds(@RequestBody List<I> ids) {
                service.softDeleteAllByIds(ids);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(ids.size())
                                .result(true)
                                .message(messageService.getMessage("entities.soft.deleted"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find an entity by ID")
        @ApiResponse(responseCode = "200", description = "Entity found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/{id}")
        public ResponseEntity<ApiMessageDTO<D>> findById(@PathVariable I id) {
                var d = service.findById(id);
                return ResponseEntity.ok(ApiMessageDTO.<D>builder()
                                .data(d)
                                .result(true)
                                .message(messageService.getMessage("entity.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Safely find an entity by ID")
        @ApiResponse(responseCode = "200", description = "Entity found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/safe/{id}")
        public ResponseEntity<ApiMessageDTO<D>> safeFindById(@PathVariable I id) {
                var d = service.safeFindById(id);
                return ResponseEntity.ok(ApiMessageDTO.<D>builder()
                                .data(toModel(d))
                                .result(true)
                                .message(messageService.getMessage("entity.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get reference by ID")
        @ApiResponse(responseCode = "200", description = "Entity reference found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/reference/{id}")
        public ResponseEntity<ApiMessageDTO<E>> getReferenceById(@PathVariable I id) {
                var e = service.getReferenceById(id);
                return ResponseEntity.ok(ApiMessageDTO.<E>builder()
                                .data(e)
                                .result(true)
                                .message(messageService.getMessage("entity.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all entities")
        @ApiResponse(responseCode = "200", description = "All entities found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping
        public ResponseEntity<ApiMessageDTO<List<D>>> findAll() {
                var ds = service.findAll();
                return ResponseEntity.ok(ApiMessageDTO.<List<D>>builder()
                                .data(ds)
                                .result(true)
                                .message(messageService.getMessage("entities.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all entities with pagination")
        @ApiResponse(responseCode = "200", description = "All entities found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/pagination")
        public ResponseEntity<ApiMessageDTO<PagedModel<D>>> findAllWithPagination(Pageable pageable,
                        PagedResourcesAssembler<D> assembler) {
                var ds = service.findAll(pageable);
                var pagedModel = assembler.toModel(ds, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<D>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("entities.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all active entities")
        @ApiResponse(responseCode = "200", description = "All entities found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/active")
        public ResponseEntity<ApiMessageDTO<List<D>>> findAllActive() {
                var ds = service.findAllActive();
                return ResponseEntity.ok(ApiMessageDTO.<List<D>>builder()
                                .data(ds)
                                .result(true)
                                .message(messageService.getMessage("entities.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all active entities with pagination")
        @ApiResponse(responseCode = "200", description = "All entities found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/active/pagination")
        public ResponseEntity<ApiMessageDTO<PagedModel<D>>> findAllActiveWithPagination(Pageable pageable,
                        PagedResourcesAssembler<D> assembler) {
                var ds = service.findAllActive(pageable);
                var pagedModel = assembler.toModel(ds, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<D>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("entities.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all entities by list of IDs")
        @ApiResponse(responseCode = "200", description = "All entities found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/ids")
        public ResponseEntity<ApiMessageDTO<List<D>>> findAllByIds(@RequestBody List<I> ids) {
                var ds = service.findAllByIds(ids);
                return ResponseEntity.ok(ApiMessageDTO.<List<D>>builder()
                                .data(ds)
                                .result(true)
                                .message(messageService.getMessage("entities.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all not-deleted entities by list of IDs")
        @ApiResponse(responseCode = "200", description = "Entities found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/ids/active")
        public ResponseEntity<ApiMessageDTO<List<D>>> findAllByIdsAndNotDeleted(@RequestBody List<I> ids) {
                var ds = service.findAllByIdsAndNotDeleted(ids);
                return ResponseEntity.ok(ApiMessageDTO.<List<D>>builder()
                                .data(ds)
                                .result(true)
                                .message(messageService.getMessage("entities.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all not-deleted entities by list of IDs with pagination")
        @ApiResponse(responseCode = "200", description = "Entities found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/ids/active/pagination")
        public ResponseEntity<ApiMessageDTO<PagedModel<D>>> findAllByIdsAndNotDeletedWithPagination(
                        @RequestBody List<I> ids,
                        Pageable pageable,
                        PagedResourcesAssembler<D> assembler) {
                var ds = service.findAllByIdsAndNotDeleted(ids, pageable);
                var pagedModel = assembler.toModel(ds, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<D>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("entities.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all entities by field name and value")
        @ApiResponse(responseCode = "200", description = "All entities found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/by-field")
        public ResponseEntity<ApiMessageDTO<List<D>>> findAllByField(@RequestParam String fieldName,
                        @RequestParam String value) {
                var ds = service.findAllByField(fieldName, value)
                                .stream()
                                .map(this::toModel)
                                .toList();
                return ResponseEntity.ok(ApiMessageDTO.<List<D>>builder()
                                .data(ds)
                                .result(true)
                                .message(messageService.getMessage("entities.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Find all entities by field name and value with pagination")
        @ApiResponse(responseCode = "200", description = "All entities found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/by-field/pagination")
        public ResponseEntity<ApiMessageDTO<PagedModel<D>>> findAllByFieldWithPagination(@RequestParam String fieldName,
                        @RequestParam String value,
                        Pageable pageable,
                        PagedResourcesAssembler<D> assembler) {
                var ds = service.findAllByField(fieldName, value, pageable);
                var pagedModel = assembler.toModel(ds, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<D>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("entities.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Check if entity exists by field name and value")
        @ApiResponse(responseCode = "200", description = "Entity exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/by-field/exists")
        public ResponseEntity<ApiMessageDTO<Boolean>> existsByField(@RequestParam String fieldName,
                        @RequestParam String value) {
                var exists = service.existsByField(fieldName, value);
                return ResponseEntity.ok(ApiMessageDTO.<Boolean>builder()
                                .data(exists)
                                .result(true)
                                .message(messageService.getMessage("entity.exists"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Search entities using filter criteria")
        @ApiResponse(responseCode = "200", description = "Entities found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @PostMapping("/search")
        public ResponseEntity<ApiMessageDTO<PagedModel<D>>> search(
                        @RequestBody(required = false) SearchRequest searchRequest,
                        PagedResourcesAssembler<D> assembler) {
                var ds = service.search(searchRequest);
                var pagedModel = assembler.toModel(ds, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<D>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("entities.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get enum values by enum name")
        @ApiResponse(responseCode = "200", description = "Enums found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
        @Override
        @GetMapping("/enums")
        public ResponseEntity<ApiMessageDTO<List<? extends EnumDTO<?>>>> getEnumValues(
                        @RequestParam("name") String name) {
                var enums = service.getEnumValues(name);
                return ResponseEntity.ok(ApiMessageDTO.<List<? extends EnumDTO<?>>>builder()
                                .data(enums)
                                .result(true)
                                .message(messageService.getMessage("enums.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        public D toModel(@NonNull D dto) {
                var id = dto.getId();

                var selfHref = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path(basePath + "/" + id)
                                .toUriString();

                var allHref = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path(basePath)
                                .toUriString();

                dto.add(Link.of(selfHref).withSelfRel());
                dto.add(Link.of(allHref).withRel("all"));
                return dto;
        }

        public PagedModel<D> toPagedModel(Page<D> page) {
                var dtoList = page.getContent().stream()
                                .map(this::toModel)
                                .toList();

                return PagedModel.of(
                                dtoList,
                                new PagedModel.PageMetadata(
                                                page.getSize(),
                                                page.getNumber(),
                                                page.getTotalElements(),
                                                page.getTotalPages()));
        }
}
