package io.jessytsiriniaina.businessoperationmanagement.service;

import io.jessytsiriniaina.businessoperationmanagement.dto.request.AssignRequestDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.request.CancelRequestDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.request.CreateRequestDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.request.RejectRequestDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.request.RequestResponseDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.request.UpdateRequestDto;
import io.jessytsiriniaina.businessoperationmanagement.entity.Request;
import io.jessytsiriniaina.businessoperationmanagement.entity.RequestComment;
import io.jessytsiriniaina.businessoperationmanagement.entity.RequestType;
import io.jessytsiriniaina.businessoperationmanagement.entity.User;
import io.jessytsiriniaina.businessoperationmanagement.enums.RequestPriority;
import io.jessytsiriniaina.businessoperationmanagement.enums.RequestStatus;
import io.jessytsiriniaina.businessoperationmanagement.exception.BusinessException;
import io.jessytsiriniaina.businessoperationmanagement.exception.ResourceNotFoundException;
import io.jessytsiriniaina.businessoperationmanagement.mapper.RequestMapper;
import io.jessytsiriniaina.businessoperationmanagement.repository.RequestCommentRepository;
import io.jessytsiriniaina.businessoperationmanagement.repository.RequestRepository;
import io.jessytsiriniaina.businessoperationmanagement.repository.RequestTypeRepository;
import io.jessytsiriniaina.businessoperationmanagement.repository.UserRepository;
import io.jessytsiriniaina.businessoperationmanagement.specification.RequestSpecification;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final UserRepository userRepository;
    private final RequestCommentRepository requestCommentRepository;

    public RequestService(
            RequestRepository requestRepository,
            RequestTypeRepository requestTypeRepository,
            UserRepository userRepository,
            RequestCommentRepository requestCommentRepository) {
        this.requestRepository = requestRepository;
        this.requestTypeRepository = requestTypeRepository;
        this.userRepository = userRepository;
        this.requestCommentRepository = requestCommentRepository;
    }

    public RequestResponseDto create(CreateRequestDto dto) {
        validateUrgent(dto.priority(), dto.description());

        RequestType type =
                requestTypeRepository
                        .findById(dto.typeId())
                        .orElseThrow(() -> new ResourceNotFoundException("RequestType not found: " + dto.typeId()));

        User createdBy =
                userRepository
                        .findById(dto.createdById())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found (createdBy): " + dto.createdById()));

        User assignedTo = null;
        if (dto.assignedToId() != null) {
            assignedTo =
                    userRepository
                            .findById(dto.assignedToId())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("User not found (assignedTo): " + dto.assignedToId()));
        }

        // TODO: replace createdById with authenticated user when JWT is added
        Request request =
                new Request(dto.title(), dto.description(), dto.priority(), type, createdBy);
        request.setAssignedTo(assignedTo);
        // status forced to PENDING (default in entity), ignore any client status
        Request saved = requestRepository.save(request);
        return RequestMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RequestResponseDto findById(Long id) {
        Request request =
                requestRepository
                        .findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));
        return RequestMapper.toResponse(request);
    }

    @Transactional(readOnly = true)
    public Page<RequestResponseDto> findAll(
            RequestStatus status,
            RequestPriority priority,
            Long typeId,
            Long createdById,
            Long assignedToId,
            String department,
            Boolean includeDeleted,
            Pageable pageable) {

        Specification<Request> spec =
                RequestSpecification.filter(
                        status, priority, typeId, createdById, assignedToId, department, includeDeleted);
        return requestRepository.findAll(spec, pageable).map(RequestMapper::toResponse);
    }

    public RequestResponseDto update(Long id, UpdateRequestDto dto) {
        Request request =
                requestRepository
                        .findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));

        // Règle 1: only PENDING can be modified
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "Only PENDING requests can be modified (current: " + request.getStatus() + ")");
        }

        if (request.isDeleted()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Cannot modify a deleted request");
        }

        RequestType type =
                requestTypeRepository
                        .findById(dto.typeId())
                        .orElseThrow(() -> new ResourceNotFoundException("RequestType not found: " + dto.typeId()));

        User assignedTo = null;
        if (dto.assignedToId() != null) {
            assignedTo =
                    userRepository
                            .findById(dto.assignedToId())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("User not found (assignedTo): " + dto.assignedToId()));
        }

        request.setTitle(dto.title());
        request.setDescription(dto.description());
        request.setPriority(dto.priority());
        request.setType(type);
        request.setAssignedTo(assignedTo);

        validateUrgent(request.getPriority(), request.getDescription());

        Request saved = requestRepository.save(request);
        return RequestMapper.toResponse(saved);
    }

    // --- Workflow: strict state machine PENDING -> IN_PROGRESS -> APPROVED|REJECTED ; PENDING|IN_PROGRESS -> CANCELLED ---

    public RequestResponseDto startProgress(Long id) {
        // TODO Phase 4: check MANAGER/ADMIN else throw BusinessException(FORBIDDEN)
        Request request = getActiveOrThrow(id);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Only PENDING can move to IN_PROGRESS (current: " + request.getStatus() + ")");
        }
        request.setStatus(RequestStatus.IN_PROGRESS);
        request.setStartedAt(Instant.now());
        return RequestMapper.toResponse(requestRepository.save(request));
    }

    public RequestResponseDto approve(Long id) {
        // TODO Phase 4: check MANAGER/ADMIN else throw BusinessException(FORBIDDEN)
        Request request = getActiveOrThrow(id);
        // Strict: only IN_PROGRESS -> APPROVED
        if (request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Only IN_PROGRESS can be approved (current: " + request.getStatus() + ")");
        }
        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedAt(Instant.now());
        addSystemComment(request, "Request approved");
        return RequestMapper.toResponse(requestRepository.save(request));
    }

    public RequestResponseDto reject(Long id, RejectRequestDto dto) {
        // TODO Phase 4: check MANAGER/ADMIN else throw BusinessException(FORBIDDEN)
        Request request = getActiveOrThrow(id);
        if (request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Only IN_PROGRESS can be rejected (current: " + request.getStatus() + ")");
        }
        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(dto.reason());
        request.setRejectedAt(Instant.now());
        addSystemComment(request, "Rejected: " + dto.reason());
        return RequestMapper.toResponse(requestRepository.save(request));
    }

    public RequestResponseDto assign(Long id, AssignRequestDto dto) {
        // TODO Phase 4: check MANAGER/ADMIN else throw BusinessException(FORBIDDEN)
        Request request = getActiveOrThrow(id);
        if (request.isDeleted()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Cannot assign a deleted request");
        }
        if (request.getStatus() == RequestStatus.APPROVED
                || request.getStatus() == RequestStatus.REJECTED
                || request.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "Cannot assign a request with status " + request.getStatus());
        }
        User assignee =
                userRepository
                        .findById(dto.assignedToId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found (assignedTo): " + dto.assignedToId()));
        request.setAssignedTo(assignee);
        addSystemComment(request, "Assigned to " + assignee.getName() + " (" + assignee.getEmail() + ")");
        return RequestMapper.toResponse(requestRepository.save(request));
    }

    public RequestResponseDto cancel(Long id, CancelRequestDto dto) {
        // TODO Phase 4: check MANAGER/ADMIN else throw BusinessException(FORBIDDEN) — per confirmation all workflow needs MANAGER/ADMIN
        Request request = getActiveOrThrow(id);
        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Only PENDING or IN_PROGRESS can be cancelled (current: " + request.getStatus() + ")");
        }
        request.setStatus(RequestStatus.CANCELLED);
        request.setCancellationReason(dto.reason());
        request.setCancelledAt(Instant.now());
        addSystemComment(request, "Cancelled: " + dto.reason());
        return RequestMapper.toResponse(requestRepository.save(request));
    }

    // --- helpers ---

    private Request getActiveOrThrow(Long id) {
        Request request =
                requestRepository
                        .findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));
        if (request.isDeleted()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Cannot modify a deleted request");
        }
        return request;
    }

    private void validateUrgent(RequestPriority priority, String description) {
        if (priority == RequestPriority.URGENT && (description == null || description.isBlank())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "URGENT requests must have a justification (description)");
        }
    }

    private void addSystemComment(Request request, String content) {
        // Use createdBy as author for system comment if no actor context yet (Phase 4 will use authenticated user)
        User author = request.getCreatedBy();
        // Ensure author is initialized; fallback to assignedTo or createdBy
        RequestComment comment = new RequestComment(content, request, author);
        request.getComments().add(comment);
        // Persist via cascade ALL on Request.comments; also save explicitly for clarity
        // requestCommentRepository.save(comment); // not needed due to cascade, but keep for audit
    }

    public void delete(Long id) {
        Request request =
                requestRepository
                        .findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));
        // soft delete with 2 cols (A)
        request.softDelete();
        requestRepository.save(request);
    }
}
