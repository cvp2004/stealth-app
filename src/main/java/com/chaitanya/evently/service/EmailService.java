package com.chaitanya.evently.service;

import com.chaitanya.evently.model.Email;
import com.chaitanya.evently.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailRepository emailRepository;

    @Transactional
    public Email createEmail(Long userId, Email.EmailType emailType, String subject, String body) {
        // Create a minimal user object for the email record
        com.chaitanya.evently.model.User user = new com.chaitanya.evently.model.User();
        user.setId(userId);

        Email email = Email.builder()
                .user(user)
                .emailType(emailType)
                .emailSubject(subject)
                .emailBody(body)
                .emailSent(false) // Email is not sent, just stored in database
                .build();

        Email savedEmail = emailRepository.save(email);
        log.info("Created email record for user {} with type {}: {}", userId, emailType, subject);
        return savedEmail;
    }
}