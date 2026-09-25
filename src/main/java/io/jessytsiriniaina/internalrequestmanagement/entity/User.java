package io.jessytsiriniaina.internalrequestmanagement.entity;

import io.jessytsiriniaina.internalrequestmanagement.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Column(nullable = false)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "createdBy")
    private List<Request> createdRequests = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTo")
    private List<Request> assignedRequests = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<RequestComment> comments = new ArrayList<>();

    protected User() {
    }

    public User(String firstName, String lastName, String email, String password, UserRole role, Department department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.department = department;
    }

    /** Legacy constructor kept for tests/migrations — delegates to firstName/lastName split */
    public User(String name, String email, String password, UserRole role, Department department) {
        String trimmed = name != null ? name.trim() : "";
        int idx = trimmed.indexOf(' ');
        if (idx > 0) {
            this.firstName = trimmed.substring(0, idx);
            this.lastName = trimmed.substring(idx + 1).trim();
        } else {
            this.firstName = trimmed;
            this.lastName = "";
        }
        this.email = email;
        this.password = password;
        this.role = role;
        this.department = department;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        if (lastName == null || lastName.isBlank()) return firstName;
        return firstName + " " + lastName;
    }

    public void setName(String name) {
        String trimmed = name != null ? name.trim() : "";
        int idx = trimmed.indexOf(' ');
        if (idx > 0) {
            this.firstName = trimmed.substring(0, idx);
            this.lastName = trimmed.substring(idx + 1).trim();
        } else {
            this.firstName = trimmed;
            this.lastName = "";
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<Request> getCreatedRequests() {
        return createdRequests;
    }

    public List<Request> getAssignedRequests() {
        return assignedRequests;
    }

    public List<RequestComment> getComments() {
        return comments;
    }
}
