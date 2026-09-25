package io.jessytsiriniaina.internalrequestmanagement.service;

import io.jessytsiriniaina.internalrequestmanagement.dto.request.AssignRequestDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.request.CancelRequestDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.request.CreateRequestDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.request.RejectRequestDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.request.RequestResponseDto;
import io.jessytsiriniaina.internalrequestmanagement.dto.request.UpdateRequestDto;
import io.jessytsiriniaina.internalrequestmanagement.entity.Request;
import io.jessytsiriniaina.internalrequestmanagement.entity.RequestComment;
import io.jessytsiriniaina.internalrequestmanagement.entity.RequestType;
import io.jessytsiriniaina.internalrequestmanagement.entity.User;
import io.jessytsiriniaina.internalrequestmanagement.enums.RequestPriority;
import io.jessytsiriniaina.internalrequestmanagement.enums.RequestStatus;
import io.jessytsiriniaina.internalrequestmanagement.enums.UserRole;
import io.jessytsiriniaina.internalrequestmanagement.exception.BusinessException;
import io.jessytsiriniaina.internalrequestmanagement.exception.ResourceNotFoundException;
import io.jessytsiriniaina.internalrequestmanagement.mapper.RequestMapper;
import io.jessytsiriniaina.internalrequestmanagement.repository.RequestCommentRepository;
import io.jessytsiriniaina.internalrequestmanagement.repository.RequestRepository;
import io.jessytsiriniaina.internalrequestmanagement.repository.RequestTypeRepository;
import io.jessytsiriniaina.internalrequestmanagement.repository.UserRepository;
import io.jessytsiriniaina.internalrequestmanagement.security.UserPrincipal;
import io.jessytsiriniaina.internalrequestmanagement.specification.RequestSpecification;
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

    public RequestResponseDto create(CreateRequestDto dto, UserPrincipal principal) {
        validateUrgent(dto.priority(), dto.description());

        RequestType type =
                requestTypeRepository
                        .findById(dto.typeId())
                        .orElseThrow(() -> new ResourceNotFoundException("RequestType not found: " + dto.typeId()));

        Long createdByIdParam = principal != null ? principal.getId() : dto.createdById();
        if (createdByIdParam == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "createdBy is required (authentication required)");
        }
        final Long effectiveCreatedById = createdByIdParam;
        User createdBy =
                userRepository
                        .findById(effectiveCreatedById)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found (createdBy): " + effectiveCreatedById));

        User assignedTo = null;
        if (dto.assignedToId() != null) {
            assignedTo =
                    userRepository
                            .findById(dto.assignedToId())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("User not found (assignedTo): " + dto.assignedToId()));
        }

        Request request =
                new Request(dto.title(), dto.description(), dto.priority(), type, createdBy);
        request.setAssignedTo(assignedTo);
        Request saved = requestRepository.save(request);
        return RequestMapper.toResponse(saved);
    }

    // Backward compat overload
    public RequestResponseDto create(CreateRequestDto dto) {
        return create(dto, null);
    }

    @Transactional(readOnly = true)
    public RequestResponseDto findById(Long id, UserPrincipal principal) {
        Request request =
                requestRepository
                        .findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));
        enforceReadAccess(request, principal);
        java.util.List<RequestComment> allComments =
                requestCommentRepository.findByRequestIdAndDeletedFalseOrderByCreatedAtAsc(id);
        long count = requestCommentRepository.countByRequestIdAndDeletedFalse(id);
        java.util.List<RequestComment> preview =
                allComments.size() > 10 ? allComments.subList(Math.max(0, allComments.size() - 10), allComments.size()) : allComments;
        return RequestMapper.toResponse(request, preview, count);
    }

    @Transactional(readOnly = true)
    public RequestResponseDto findById(Long id) {
        return findById(id, null);
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
            Pageable pageable,
            UserPrincipal principal) {

        // R4 scoping: enforce principal-based filter
        Long effectiveCreatedById = createdById;
        String effectiveDepartment = department;
        if (principal != null) {
            if (principal.getRole() == UserRole.EMPLOYEE) {
                effectiveCreatedById = principal.getId();
            } else if (principal.getRole() == UserRole.MANAGER) {
                if (principal.getDepartmentId() != null) {
                    effectiveDepartment = String.valueOf(principal.getDepartmentId());
                }
            }
            // ADMIN: no override
        }

        Specification<Request> spec =
                RequestSpecification.filter(
                        status, priority, typeId, effectiveCreatedById, assignedToId, effectiveDepartment, includeDeleted);
        return requestRepository.findAll(spec, pageable).map(req -> {
            long count = requestCommentRepository.countByRequestIdAndDeletedFalse(req.getId());
            return RequestMapper.toResponse(req, java.util.List.of(), count);
        });
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
        return findAll(status, priority, typeId, createdById, assignedToId, department, includeDeleted, pageable, null);
    }

    public RequestResponseDto update(Long id, UpdateRequestDto dto, UserPrincipal principal) {
        Request request =
                requestRepository
                        .findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "Only PENDING requests can be modified (current: " + request.getStatus() + ")");
        }

        if (request.isDeleted()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Cannot modify a deleted request");
        }

        // Ownership check for EMPLOYEE
        if (principal != null && principal.getRole() == UserRole.EMPLOYEE) {
            if (!request.getCreatedBy().getId().equals(principal.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "You can only modify your own requests");
            }
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

    public RequestResponseDto update(Long id, UpdateRequestDto dto) {
        return update(id, dto, null);
    }

    // --- Workflow: strict state machine PENDING -> IN_PROGRESS -> APPROVED|REJECTED ; PENDING|IN_PROGRESS -> CANCELLED ---

    public RequestResponseDto startProgress(Long id) {
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
        Request request = getActiveOrThrow(id);
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

    public RequestResponseDto cancel(Long id, CancelRequestDto dto, UserPrincipal principal) {
        Request request = getActiveOrThrow(id);
        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Only PENDING or IN_PROGRESS can be cancelled (current: " + request.getStatus() + ")");
        }
        // EMPLOYEE can only cancel own
        if (principal != null && principal.getRole() == UserRole.EMPLOYEE) {
            if (!request.getCreatedBy().getId().equals(principal.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "You can only cancel your own requests");
            }
        }
        request.setStatus(RequestStatus.CANCELLED);
        request.setCancellationReason(dto.reason());
        request.setCancelledAt(Instant.now());
        addSystemComment(request, "Cancelled: " + dto.reason());
        return RequestMapper.toResponse(requestRepository.save(request));
    }

    public RequestResponseDto cancel(Long id, CancelRequestDto dto) {
        return cancel(id, dto, null);
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

    private void enforceReadAccess(Request request, UserPrincipal principal) {
        if (principal == null) return;
        if (principal.getRole() == UserRole.ADMIN) return;
        if (principal.getRole() == UserRole.MANAGER) {
            Long deptId = principal.getDepartmentId();
            if (deptId == null) return;
            User creator = request.getCreatedBy();
            if (creator.getDepartment() != null && deptId.equals(creator.getDepartment().getId())) return;
            // Also allow if manager is assignee or creator
            if (request.getCreatedBy().getId().equals(principal.getId())) return;
            if (request.getAssignedTo() != null && request.getAssignedTo().getId().equals(principal.getId())) return;
            throw new BusinessException(HttpStatus.FORBIDDEN, "Managers can only view requests from their department");
        }
        // EMPLOYEE
        if (!request.getCreatedBy().getId().equals(principal.getId())) {
            // Allow if assignedTo is the employee
            if (request.getAssignedTo() != null && request.getAssignedTo().getId().equals(principal.getId())) return;
            throw new BusinessException(HttpStatus.FORBIDDEN, "You can only view your own requests");
        }
    }

    private void validateUrgent(RequestPriority priority, String description) {
        if (priority == RequestPriority.URGENT && (description == null || description.isBlank())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "URGENT requests must have a justification (description)");
        }
    }

    private void addSystemComment(Request request, String content) {
        User author = request.getCreatedBy();
        RequestComment comment = new RequestComment(content, request, author);
        request.getComments().add(comment);
    }

    public void delete(Long id, UserPrincipal principal) {
        Request request =
                requestRepository
                        .findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));
        if (principal != null && principal.getRole() == UserRole.EMPLOYEE) {
            if (!request.getCreatedBy().getId().equals(principal.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "You can only delete your own requests");
            }
        }
        request.softDelete();
        requestRepository.save(request);
    }

    public void delete(Long id) {
        delete(id, null);
    }
}
