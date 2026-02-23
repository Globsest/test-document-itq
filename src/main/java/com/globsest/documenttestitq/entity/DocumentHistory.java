package com.globsest.documenttestitq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_history", indexes = {
    @Index(name = "idx_history_document_id", columnList = "document_id"),
    @Index(name = "idx_history_performed_at", columnList = "performed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_seq")
    @SequenceGenerator(name = "history_seq", sequenceName = "history_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private DocumentAction action;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "comment", length = 1000)
    private String comment;
}
