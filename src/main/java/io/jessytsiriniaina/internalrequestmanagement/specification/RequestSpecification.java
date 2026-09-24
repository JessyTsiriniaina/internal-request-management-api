package io.jessytsiriniaina.internalrequestmanagement.specification;

import io.jessytsiriniaina.internalrequestmanagement.entity.Request;
import io.jessytsiriniaina.internalrequestmanagement.enums.RequestPriority;
import io.jessytsiriniaina.internalrequestmanagement.enums.RequestStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class RequestSpecification {

    private RequestSpecification() {}

    public static Specification<Request> filter(
            RequestStatus status,
            RequestPriority priority,
            Long typeId,
            Long createdById,
            Long assignedToId,
            String department,
            Boolean includeDeleted) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            boolean filterDeleted = includeDeleted == null || !includeDeleted;
            if (filterDeleted) {
                predicates.add(cb.isFalse(root.get("deleted")));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (typeId != null) {
                predicates.add(cb.equal(root.get("type").get("id"), typeId));
            }
            if (createdById != null) {
                predicates.add(cb.equal(root.get("createdBy").get("id"), createdById));
            }
            if (assignedToId != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), assignedToId));
            }
            if (department != null && !department.isBlank()) {
                Join<Object, Object> createdByJoin = root.join("createdBy");
                Join<Object, Object> deptJoin = createdByJoin.join("department");
                // support ?department=ID or ?department=name (e.g. IT, Finance)
                try {
                    Long deptId = Long.parseLong(department);
                    predicates.add(cb.equal(deptJoin.get("id"), deptId));
                } catch (NumberFormatException e) {
                    predicates.add(cb.equal(deptJoin.get("name"), department));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
