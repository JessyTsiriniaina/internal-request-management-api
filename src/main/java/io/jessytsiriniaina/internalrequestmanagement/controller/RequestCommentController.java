package io.jessytsiriniaina.internalrequestmanagement.controller;

import io.jessytsiriniaina.internalrequestmanagement.dto.requestcomment.CreateRequestCommentDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.requestcomment.RequestCommentResponseDto;
import io.jessytsiriniaina.internalrequestmanagement.service.RequestCommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests/{requestId}/comments")
public class RequestCommentController {

    private final RequestCommentService requestCommentService;

    public RequestCommentController(RequestCommentService requestCommentService) {
        this.requestCommentService = requestCommentService;
    }

    @PostMapping
    public ResponseEntity<RequestCommentResponseDto> create(
            @PathVariable Long requestId, @Valid @RequestBody CreateRequestCommentDto dto) {
        RequestCommentResponseDto created = requestCommentService.create(requestId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<RequestCommentResponseDto>> findAll(
            @PathVariable Long requestId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<RequestCommentResponseDto> page = requestCommentService.findAll(requestId, includeDeleted, pageable);
        return ResponseEntity.ok(page);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long requestId, @PathVariable Long commentId) {
        requestCommentService.delete(requestId, commentId);
        return ResponseEntity.noContent().build();
    }
}
