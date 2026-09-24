package io.jessytsiriniaina.internalrequestmanagement.repository;

import io.jessytsiriniaina.internalrequestmanagement.entity.Department;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Department> findByNameIgnoreCase(String name);
}
