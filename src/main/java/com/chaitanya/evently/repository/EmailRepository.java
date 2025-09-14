package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {

    @Query("SELECT e FROM Email e WHERE e.user.id = :userId AND e.emailType = :emailType ORDER BY e.createdAt DESC")
    List<Email> findByUserIdAndEmailType(Long userId, Email.EmailType emailType);
}
