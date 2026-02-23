package com.globsest.documenttestitq.repository;

import com.globsest.documenttestitq.entity.ApprovalRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistry, Long> {

    Optional<ApprovalRegistry> findByDocumentId(Long documentId);

    boolean existsByDocumentId(Long documentId);
}
