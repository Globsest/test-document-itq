package com.globsest.documenttestitq.repository;

import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Optional<Document> findByNumber(String number);

    List<Document> findByIdIn(List<Long> ids);

    Page<Document> findByStatus(DocumentStatus status, Pageable pageable);

    Page<Document> findByAuthor(String author, Pageable pageable);

    Page<Document> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Page<Document> findByStatusAndAuthorContainingIgnoreCase(DocumentStatus status, String author, Pageable pageable);

    Page<Document> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);


    //todo возможно пзж перебрать
    @Query("SELECT d FROM Document d WHERE d.status = :status AND d.createdAt BETWEEN :start AND :end")
    Page<Document> findByStatusAndCreatedAtBetween(
            @Param("status") DocumentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    //todo возможно пзж перебрать
    @Query("SELECT d FROM Document d WHERE d.status = :status AND d.author = :author AND d.createdAt BETWEEN :start AND :end")
    Page<Document> findByStatusAndAuthorAndCreatedAtBetween(
            @Param("status") DocumentStatus status,
            @Param("author") String author,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    List<Document> findByStatusOrderByIdAsc(DocumentStatus status, Pageable pageable);

    long countByStatus(DocumentStatus status);
}
