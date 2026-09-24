package io.jessytsiriniaina.internalrequestmanagement.repository;

import io.jessytsiriniaina.internalrequestmanagement.entity.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestTypeRepository extends JpaRepository<RequestType, Long> {}
