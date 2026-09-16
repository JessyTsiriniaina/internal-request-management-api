package io.jessytsiriniaina.businessoperationmanagement.repository;

import io.jessytsiriniaina.businessoperationmanagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
