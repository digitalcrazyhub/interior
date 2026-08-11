package com.example.littlebighome.gallery.contact;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactLeadService {

    private final ContactLeadRepository repository;

    private final EmailService emailService;

    private final GoogleSheetsService googleSheetsService;


    public ContactLeadService(
            ContactLeadRepository repository,
            EmailService emailService,
            GoogleSheetsService googleSheetsService
    ) {

        this.repository = repository;

        this.emailService = emailService;

        this.googleSheetsService =
                googleSheetsService;
    }


    // =====================================================
    // SUBMIT LEAD
    // =====================================================

    @Transactional
    public ContactLead submitLead(
            ContactLeadRequest request
    ) {

        validate(request);


        ContactLead lead =
                new ContactLead();


        lead.setName(
                request.getName().trim()
        );


        lead.setEmail(
                request.getEmail().trim()
        );


        lead.setPhone(
                clean(request.getPhone())
        );


        lead.setProjectType(
                clean(request.getType())
        );


        lead.setMessage(
                clean(request.getMessage())
        );


        // =================================================
        // 1. SAVE TO MYSQL
        // =================================================

        ContactLead savedLead =
                repository.save(lead);


        // =================================================
        // 2. SEND VISITOR EMAIL
        // =================================================

        try {

            emailService.sendThankYouEmail(
                    savedLead
            );

        } catch (Exception e) {

            System.err.println(
                    "Visitor email failed: " +
                            e.getMessage()
            );
        }


        // =================================================
        // 3. SEND OWNER EMAIL
        // =================================================

        try {

            emailService.sendOwnerLeadEmail(
                    savedLead
            );

        } catch (Exception e) {

            System.err.println(
                    "Owner email failed: " +
                            e.getMessage()
            );
        }


        // =================================================
        // 4. GOOGLE SHEET
        // =================================================

        try {

            googleSheetsService.addLead(
                    savedLead
            );

        } catch (Exception e) {

            System.err.println(
                    "Google Sheet update failed: " +
                            e.getMessage()
            );
        }


        return savedLead;
    }


    // =====================================================
    // VALIDATION
    // =====================================================

    private void validate(
            ContactLeadRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Invalid request."
            );
        }


        if (
                isBlank(
                        request.getName()
                )
        ) {

            throw new IllegalArgumentException(
                    "Please enter your name."
            );
        }


        if (
                isBlank(
                        request.getEmail()
                )
        ) {

            throw new IllegalArgumentException(
                    "Please enter your email address."
            );
        }


        if (
                !request
                        .getEmail()
                        .trim()
                        .matches(
                                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                        )
        ) {

            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }


        if (
                request.getName()
                        .trim()
                        .length() > 100
        ) {

            throw new IllegalArgumentException(
                    "Name is too long."
            );
        }


        if (
                request.getEmail()
                        .trim()
                        .length() > 255
        ) {

            throw new IllegalArgumentException(
                    "Email address is too long."
            );
        }


        if (
                request.getMessage() != null &&
                        request.getMessage().length() > 5000
        ) {

            throw new IllegalArgumentException(
                    "Message is too long."
            );
        }
    }


    private boolean isBlank(String value) {

        return value == null ||
                value.trim().isEmpty();
    }


    private String clean(String value) {

        if (
                value == null ||
                        value.trim().isEmpty()
        ) {

            return null;
        }

        return value.trim();
    }
}