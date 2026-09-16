package io.jessytsiriniaina.businessoperationmanagement.repository;

import io.jessytsiriniaina.businessoperationmanagement.entity.RequestComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {
}
