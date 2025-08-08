// package com.mgaye.banking_application.entity;

// import jakarta.persistence.*;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import org.hibernate.annotations.CreationTimestamp;

// import java.time.LocalDateTime;

// @Entity
// @Table(name = "audit_logs")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class AuditLog {

// @Id
// @GeneratedValue(strategy = GenerationType.IDENTITY)
// private Long id;

// @Column(name = "user_id")
// private Long userId;

// @Column(name = "username")
// private String username;

// @Column(name = "action", nullable = false)
// private String action;

// @Column(name = "entity_type")
// private String entityType;

// @Column(name = "entity_id")
// private Long entityId;

// @Column(name = "ip_address")
// private String ipAddress;

// @Column(name = "user_agent")
// private String userAgent;

// @Column(name = "details", columnDefinition = "TEXT")
// private String details;

// @CreationTimestamp
// @Column(name = "created_at", updatable = false)
// private LocalDateTime createdAt;
// }

package com.mgaye.banking_application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "username")
    private String username;

    @Column(name = "old_values", length = 2000)
    private String oldValues; // JSON

    @Column(name = "new_values", length = 2000)
    private String newValues; // JSON

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
