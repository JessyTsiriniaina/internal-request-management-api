package io.jessytsiriniaina.businessoperationmanagement.repository;

import io.jessytsiriniaina.businessoperationmanagement.entity.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestTypeRepository extends JpaRepository<RequestType, Long> {}
