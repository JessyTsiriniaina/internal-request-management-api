package io.jessytsiriniaina.businessoperationmanagement.repository;

import io.jessytsiriniaina.businessoperationmanagement.entity.RequestComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {

    Page<RequestComment> findByRequestIdAndDeletedFalse(Long requestId, Pageable pageable);

    Page<RequestComment> findByRequestId(Long requestId, Pageable pageable);

    Optional<RequestComment> findByIdAndRequestIdAndDeletedFalse(Long id, Long requestId);

    Optional<RequestComment> findByIdAndRequestId(Long id, Long requestId);

    List<RequestComment> findByRequestIdAndDeletedFalseOrderByCreatedAtAsc(Long requestId);

    long countByRequestIdAndDeletedFalse(Long requestId);

    long countByRequestId(Long requestId);
}
