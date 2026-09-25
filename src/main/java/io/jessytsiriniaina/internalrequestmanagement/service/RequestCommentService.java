package io.jessytsiriniaina.internalrequestmanagement.service;

import io.jessytsiriniaina.internalrequestmanagement.dto.requestcomment.CreateRequestCommentDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.requestcomment.RequestCommentResponseDto;
import io.jessytsiriniaina.internalrequestmanagement.entity.Request;
import io.jessytsiriniaina.internalrequestmanagement.entity.RequestComment;
import io.jessytsiriniaina.internalrequestmanagement.entity.User;
import io.jessytsiriniaina.internalrequestmanagement.enums.UserRole;
import io.jessytsiriniaina.internalrequestmanagement.exception.BusinessException;
import io.jessytsiriniaina.internalrequestmanagement.exception.ResourceNotFoundException;
import io.jessytsiriniaina.internalrequestmanagement.mapper.RequestCommentMapper;
import io.jessytsiriniaina.internalrequestmanagement.repository.RequestCommentRepository;
import io.jessytsiriniaina.internalrequestmanagement.repository.RequestRepository;
import io.jessytsiriniaina.internalrequestmanagement.repository.UserRepository;
import io.jessytsiriniaina.internalrequestmanagement.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequestCommentService {

    private final RequestCommentRepository requestCommentRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public RequestCommentService(
            RequestCommentRepository requestCommentRepository,
            RequestRepository requestRepository,
            UserRepository userRepository) {
        this.requestCommentRepository = requestCommentRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    public RequestCommentResponseDto create(Long requestId, CreateRequestCommentDto dto, UserPrincipal principal) {
        Request request = getActiveRequestOrThrow(requestId);

        Long authorIdParam = principal != null ? principal.getId() : dto.authorId();
        if (authorIdParam == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "author is required (authentication required)");
        }
        final Long effectiveAuthorId = authorIdParam;

        User author =
                userRepository
                        .findById(effectiveAuthorId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found (author): " + effectiveAuthorId));

        RequestComment comment = new RequestComment(dto.content(), request, author);
        request.getComments().add(comment);
        RequestComment saved = requestCommentRepository.save(comment);
        return RequestCommentMapper.toResponse(saved);
    }

    public RequestCommentResponseDto create(Long requestId, CreateRequestCommentDto dto) {
        return create(requestId, dto, null);
    }

    @Transactional(readOnly = true)
    public Page<RequestCommentResponseDto> findAll(Long requestId, Boolean includeDeleted, Pageable pageable) {
        getRequestOrThrow(requestId);

        boolean withDeleted = Boolean.TRUE.equals(includeDeleted);
        Page<RequestComment> page =
                withDeleted
                        ? requestCommentRepository.findByRequestId(requestId, pageable)
                        : requestCommentRepository.findByRequestIdAndDeletedFalse(requestId, pageable);
        return page.map(RequestCommentMapper::toResponse);
    }

    public void delete(Long requestId, Long commentId, UserPrincipal principal) {
        getActiveRequestOrThrow(requestId);

        RequestComment comment =
                requestCommentRepository
                        .findByIdAndRequestIdAndDeletedFalse(commentId, requestId)
                        .orElseThrow(() -> new ResourceNotFoundException("RequestComment not found: " + commentId));

        if (principal != null) {
            boolean isAuthor = comment.getAuthor().getId().equals(principal.getId());
            boolean isManagerOrAdmin = principal.getRole() == UserRole.MANAGER || principal.getRole() == UserRole.ADMIN;
            if (!isAuthor && !isManagerOrAdmin) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "You can only delete your own comments");
            }
        }

        comment.softDelete();
        requestCommentRepository.save(comment);
    }

    public void delete(Long requestId, Long commentId) {
        delete(requestId, commentId, null);
    }

    private Request getActiveRequestOrThrow(Long requestId) {
        Request request =
                requestRepository
                        .findByIdAndDeletedFalse(requestId)
                        .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
        if (request.isDeleted()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Cannot comment on a deleted request");
        }
        return request;
    }

    private Request getRequestOrThrow(Long requestId) {
        return requestRepository
                .findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
    }
}
