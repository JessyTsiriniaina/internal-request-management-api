package io.jessytsiriniaina.businessoperationmanagement.service;

import io.jessytsiriniaina.businessoperationmanagement.dto.department.CreateDepartmentDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.department.DepartmentResponseDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.department.UpdateDepartmentDto;
import io.jessytsiriniaina.businessoperationmanagement.entity.Department;
import io.jessytsiriniaina.businessoperationmanagement.exception.BusinessException;
import io.jessytsiriniaina.businessoperationmanagement.exception.ResourceNotFoundException;
import io.jessytsiriniaina.businessoperationmanagement.mapper.DepartmentMapper;
import io.jessytsiriniaina.businessoperationmanagement.repository.DepartmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentResponseDto create(CreateDepartmentDto dto) {
        String normalizedName = normalizeName(dto.name());
        String normalizedDescription = normalizeDescription(dto.description());

        if (departmentRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Department already exists: " + normalizedName);
        }

        Department department = new Department(normalizedName, normalizedDescription);
        try {
            Department saved = departmentRepository.save(department);
            return DepartmentMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(HttpStatus.CONFLICT, "Department already exists: " + normalizedName);
        }
    }

    @Transactional(readOnly = true)
    public DepartmentResponseDto findById(Long id) {
        Department department = getByIdOrThrow(id);
        return DepartmentMapper.toResponse(department);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponseDto> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(DepartmentMapper::toResponse);
    }

    public DepartmentResponseDto update(Long id, UpdateDepartmentDto dto) {
        Department department = getByIdOrThrow(id);

        String normalizedName = normalizeName(dto.name());
        String normalizedDescription = normalizeDescription(dto.description());

        if (!department.getName().equalsIgnoreCase(normalizedName)
                && departmentRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Department already exists: " + normalizedName);
        }

        department.setName(normalizedName);
        department.setDescription(normalizedDescription);

        try {
            Department saved = departmentRepository.save(department);
            return DepartmentMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(HttpStatus.CONFLICT, "Department already exists: " + normalizedName);
        }
    }

    public void delete(Long id) {
        Department department = getByIdOrThrow(id);

        // Block delete if users are assigned (robust vs ON DELETE SET NULL)
        if (department.getUsers() != null && !department.getUsers().isEmpty()) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Cannot delete department with assigned users (" + department.getUsers().size() + ")");
        }

        departmentRepository.delete(department);
    }

    private Department getByIdOrThrow(Long id) {
        return departmentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    private String normalizeName(String raw) {
        if (raw == null) {
            return null;
        }
        // trim + collapse internal whitespace (e.g. "Finance   Office" -> "Finance Office")
        return raw.trim().replaceAll("\\s+", " ");
    }

    private String normalizeDescription(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim().replaceAll("\\s+", " ");
        return trimmed.isEmpty() ? null : trimmed;
    }
}
