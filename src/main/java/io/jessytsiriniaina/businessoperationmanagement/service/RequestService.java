package io.jessytsiriniaina.businessoperationmanagement.service;

import io.jessytsiriniaina.businessoperationmanagement.dto.request.CreateRequestDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.request.RequestResponseDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.request.UpdateRequestDto;
import io.jessytsiriniaina.businessoperationmanagement.entity.Request;
import io.jessytsiriniaina.businessoperationmanagement.entity.RequestType;
import io.jessytsiriniaina.businessoperationmanagement.entity.User;
import io.jessytsiriniaina.businessoperationmanagement.enums.RequestPriority;
import io.jessytsiriniaina.businessoperationmanagement.enums.RequestStatus;
import io.jessytsiriniaina.businessoperationmanagement.exception.BusinessException;
import io.jessytsiriniaina.businessoperationmanagement.exception.ResourceNotFoundException;
import io.jessytsiriniaina.businessoperationmanagement.mapper.RequestMapper;
import io.jessytsiriniaina.businessoperationmanagement.repository.RequestRepository;
import io.jessytsiriniaina.businessoperationmanagement.repository.RequestTypeRepository;
import io.jessytsiriniaina.businessoperationmanagement.repository.UserRepository;
import io.jessytsiriniaina.businessoperationmanagement.specification.RequestSpecification;
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

    public RequestService(
            RequestRepository requestRepository,
            RequestTypeRepository requestTypeRepository,
            UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.requestTypeRepository = requestTypeRepository;
        this.userRepository = userRepository;
    }

    public RequestResponseDto create(CreateRequestDto dto) {
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

        Request saved = requestRepository.save(request);
        return RequestMapper.toResponse(saved);
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
