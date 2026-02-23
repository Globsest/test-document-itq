package com.globsest.documenttestitq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_registry", indexes = {
    @Index(name = "idx_registry_document_id", columnList = "document_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registry_seq")
    @SequenceGenerator(name = "registry_seq", sequenceName = "registry_seq", allocationSize = 1)
    private Long id;

    @Column(name = "document_id", nullable = false, unique = true)
    private Long documentId;

    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;

    @Column(name = "approved_by", nullable = false, length = 100)
    private String approvedBy;
}
