package io.jessytsiriniaina.businessoperationmanagement.service;

import io.jessytsiriniaina.businessoperationmanagement.dto.requestcomment.CreateRequestCommentDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.requestcomment.RequestCommentResponseDto;
import io.jessytsiriniaina.businessoperationmanagement.entity.Request;
import io.jessytsiriniaina.businessoperationmanagement.entity.RequestComment;
import io.jessytsiriniaina.businessoperationmanagement.entity.User;
import io.jessytsiriniaina.businessoperationmanagement.exception.BusinessException;
import io.jessytsiriniaina.businessoperationmanagement.exception.ResourceNotFoundException;
import io.jessytsiriniaina.businessoperationmanagement.mapper.RequestCommentMapper;
import io.jessytsiriniaina.businessoperationmanagement.repository.RequestCommentRepository;
import io.jessytsiriniaina.businessoperationmanagement.repository.RequestRepository;
import io.jessytsiriniaina.businessoperationmanagement.repository.UserRepository;
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

    public RequestCommentResponseDto create(Long requestId, CreateRequestCommentDto dto) {
        Request request = getActiveRequestOrThrow(requestId);

        User author =
                userRepository
                        .findById(dto.authorId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found (author): " + dto.authorId()));

        // TODO Phase 4: replace authorId with authenticated user
        RequestComment comment = new RequestComment(dto.content(), request, author);
        request.getComments().add(comment);
        RequestComment saved = requestCommentRepository.save(comment);
        return RequestCommentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<RequestCommentResponseDto> findAll(Long requestId, Boolean includeDeleted, Pageable pageable) {
        // Verify request exists (404 if not)
        getRequestOrThrow(requestId);

        boolean withDeleted = Boolean.TRUE.equals(includeDeleted);
        Page<RequestComment> page =
                withDeleted
                        ? requestCommentRepository.findByRequestId(requestId, pageable)
                        : requestCommentRepository.findByRequestIdAndDeletedFalse(requestId, pageable);
        return page.map(RequestCommentMapper::toResponse);
    }

    public void delete(Long requestId, Long commentId) {
        // Verify request exists and not soft-deleted
        getActiveRequestOrThrow(requestId);

        RequestComment comment =
                requestCommentRepository
                        .findByIdAndRequestIdAndDeletedFalse(commentId, requestId)
                        .orElseThrow(() -> new ResourceNotFoundException("RequestComment not found: " + commentId));

        // TODO Phase 4: check author == caller || MANAGER/ADMIN else throw BusinessException(FORBIDDEN)
        comment.softDelete();
        requestCommentRepository.save(comment);
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
