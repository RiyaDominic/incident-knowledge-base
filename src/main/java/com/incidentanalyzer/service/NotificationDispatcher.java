package com.incidentanalyzer.service;

import com.incidentanalyzer.model.Notification;
import com.incidentanalyzer.model.NotificationStatus;
import com.incidentanalyzer.model.User;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final com.incidentanalyzer.repository.NotificationRepository notificationRepository;

    @Async("notificationExecutor")
    public void send(Notification notification, User recipient) {
        Optional<JavaMailSender> senderOptional = Optional.ofNullable(mailSenderProvider.getIfAvailable());
        try {
            senderOptional.ifPresent(sender -> {
                try {
                    sendEmail(sender, recipient, notification);
                } catch (MessagingException ex) {
                    throw new IllegalStateException("Unable to send notification email", ex);
                }
            });
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);
        } catch (Exception ex) {
            log.warn("Failed to send notification {}: {}", notification.getId(), ex.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }
    }

    private void sendEmail(JavaMailSender sender, User recipient, Notification notification) throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        message.setRecipient(Message.RecipientType.TO, new jakarta.mail.internet.InternetAddress(recipient.getEmail()));
        message.setSubject("[Incident] " + notification.getType().name() + " - " + notification.getIncidentId());
        message.setText(notification.getMessage() + System.lineSeparator() + "Incident ID: " + notification.getIncidentId());
        sender.send(message);
    }
}
