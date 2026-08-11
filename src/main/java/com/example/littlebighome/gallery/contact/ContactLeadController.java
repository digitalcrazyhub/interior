package com.example.littlebighome.gallery.contact;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactLeadController {

    private final ContactLeadService contactLeadService;


    public ContactLeadController(
            ContactLeadService contactLeadService
    ) {

        this.contactLeadService =
                contactLeadService;
    }


    // =====================================================
    // CONTACT FORM SUBMIT
    // =====================================================

    @PostMapping
    public ResponseEntity<?> submitContactForm(
            @RequestBody ContactLeadRequest request
    ) {

        try {

            ContactLead lead =
                    contactLeadService.submitLead(
                            request
                    );


            Map<String, Object> response =
                    new LinkedHashMap<>();


            response.put(
                    "success",
                    true
            );


            response.put(
                    "message",
                    "Thank you! We'll be in touch within one business day."
            );


            response.put(
                    "leadId",
                    lead.getId()
            );


            return ResponseEntity.ok(
                    response
            );


        } catch (IllegalArgumentException e) {

            Map<String, Object> response =
                    new LinkedHashMap<>();


            response.put(
                    "success",
                    false
            );


            response.put(
                    "message",
                    e.getMessage()
            );


            return ResponseEntity
                    .badRequest()
                    .body(response);


        } catch (Exception e) {

            e.printStackTrace();


            Map<String, Object> response =
                    new LinkedHashMap<>();


            response.put(
                    "success",
                    false
            );


            response.put(
                    "message",
                    "Unable to submit your enquiry. Please try again."
            );


            return ResponseEntity
                    .internalServerError()
                    .body(response);
        }
    }
}