package com.example.littlebighome.gallery.contact;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.owner}")
    private String ownerEmail;


    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    // =====================================================
    // VISITOR THANK YOU EMAIL
    // =====================================================

    public void sendThankYouEmail(ContactLead lead) {

        SimpleMailMessage mail =
                new SimpleMailMessage();

        mail.setFrom(fromEmail);

        mail.setTo(lead.getEmail());

        mail.setSubject(
                "Thank You for Contacting Little Big Home Interiors"
        );

        mail.setText(
                "Dear " +
                        safe(lead.getName()) +
                        ",\n\n" +

                        "Thank you for contacting " +
                        "Little Big Home Interiors.\n\n" +

                        "We have received your enquiry successfully. " +
                        "Our team will review your requirements " +
                        "and get back to you shortly.\n\n" +

                        "Project Type: " +
                        safe(lead.getProjectType()) +
                        "\n\n" +

                        "Your Message:\n" +
                        safe(lead.getMessage()) +
                        "\n\n" +

                        "Regards,\n" +
                        "Little Big Home Interiors"
        );

        mailSender.send(mail);
    }


    // =====================================================
    // OWNER LEAD EMAIL
    // =====================================================

    public void sendOwnerLeadEmail(ContactLead lead) {

        SimpleMailMessage mail =
                new SimpleMailMessage();

        mail.setFrom(fromEmail);

        mail.setTo(ownerEmail);

        // Reply button will reply to visitor
        mail.setReplyTo(lead.getEmail());

        mail.setSubject(
                "New Website Lead - " +
                        safe(lead.getName())
        );

        mail.setText(

                "NEW WEBSITE LEAD\n" +
                        "================================\n\n" +

                        "Lead ID: " +
                        lead.getId() +
                        "\n\n" +

                        "Name: " +
                        safe(lead.getName()) +
                        "\n\n" +

                        "Email: " +
                        safe(lead.getEmail()) +
                        "\n\n" +

                        "Phone: " +
                        safe(lead.getPhone()) +
                        "\n\n" +

                        "Project Type: " +
                        safe(lead.getProjectType()) +
                        "\n\n" +

                        "Message:\n" +
                        safe(lead.getMessage()) +
                        "\n\n" +

                        "Submitted At: " +
                        lead.getCreatedAt() +
                        "\n\n" +

                        "================================"
        );

        mailSender.send(mail);
    }


    private String safe(String value) {

        if (
                value == null ||
                        value.trim().isEmpty()
        ) {
            return "Not provided";
        }

        return value.trim();
    }
}