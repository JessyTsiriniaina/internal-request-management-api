package io.jessytsiriniaina.internalrequestmanagement.repository;

import io.jessytsiriniaina.internalrequestmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
