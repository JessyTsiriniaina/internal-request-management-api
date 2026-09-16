package io.jessytsiriniaina.businessoperationmanagement.repository;

import io.jessytsiriniaina.businessoperationmanagement.entity.Request;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {

    Optional<Request> findByIdAndDeletedFalse(Long id);
}
