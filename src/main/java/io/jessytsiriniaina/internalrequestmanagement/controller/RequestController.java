package io.jessytsiriniaina.internalrequestmanagement.controller;

import io.jessytsiriniaina.internalrequestmanagement.dto.request.AssignRequestDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.request.CancelRequestDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.request.CreateRequestDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.request.RejectRequestDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.request.RequestResponseDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.request.UpdateRequestDto;
import io.jessytsiriniaina.internalrequestmanagement.enums.RequestPriority;
import io.jessytsiriniaina.internalrequestmanagement.enums.RequestStatus;
import io.jessytsiriniaina.internalrequestmanagement.service.RequestService;
import io.jessytsiriniaina.internalrequestmanagement.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<RequestResponseDto> create(
            @Valid @RequestBody CreateRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        RequestResponseDto created = requestService.create(dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<RequestResponseDto>> findAll(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestPriority priority,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long createdById,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted,
            @PageableDefault(size = 10, sort = "updatedAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        Page<RequestResponseDto> page =
                requestService.findAll(status, priority, typeId, createdById, assignedToId, department, includeDeleted, pageable, principal);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDto> findById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(requestService.findById(id, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestResponseDto> update(
            @PathVariable Long id, @Valid @RequestBody UpdateRequestDto dto, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(requestService.update(id, dto, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        requestService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/start-progress")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<RequestResponseDto> startProgress(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.startProgress(id));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<RequestResponseDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<RequestResponseDto> reject(
            @PathVariable Long id, @Valid @RequestBody RejectRequestDto dto) {
        return ResponseEntity.ok(requestService.reject(id, dto));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<RequestResponseDto> assign(
            @PathVariable Long id, @Valid @RequestBody AssignRequestDto dto) {
        return ResponseEntity.ok(requestService.assign(id, dto));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RequestResponseDto> cancel(
            @PathVariable Long id, @Valid @RequestBody CancelRequestDto dto, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(requestService.cancel(id, dto, principal));
    }
}
