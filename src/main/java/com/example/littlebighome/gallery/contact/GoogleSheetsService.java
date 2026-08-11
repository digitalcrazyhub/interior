package com.example.littlebighome.gallery.contact;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
public class GoogleSheetsService {

    private static final GsonFactory JSON_FACTORY =
            GsonFactory.getDefaultInstance();


    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;


    @Value("${google.sheets.sheet-name}")
    private String sheetName;


    // =====================================================
    // GOOGLE SHEETS SERVICE
    // =====================================================

    private Sheets getSheetsService() throws Exception {

        ClassPathResource resource =
                new ClassPathResource(
                        "google-service-account.json"
                );


        if (!resource.exists()) {

            throw new IllegalStateException(
                    "google-service-account.json not found. " +
                            "Place it inside src/main/resources/"
            );
        }


        if (resource.contentLength() == 0) {

            throw new IllegalStateException(
                    "google-service-account.json is empty."
            );
        }


        System.out.println(
                "Google service account file found."
        );


        System.out.println(
                "Google credential file size: " +
                        resource.contentLength() +
                        " bytes"
        );


        try (
                InputStream inputStream =
                        resource.getInputStream()
        ) {

            GoogleCredentials credentials =
                    GoogleCredentials
                            .fromStream(inputStream)
                            .createScoped(
                                    List.of(
                                            "https://www.googleapis.com/auth/spreadsheets"
                                    )
                            );


            HttpCredentialsAdapter adapter =
                    new HttpCredentialsAdapter(
                            credentials
                    );


            return new Sheets.Builder(

                    GoogleNetHttpTransport
                            .newTrustedTransport(),

                    JSON_FACTORY,

                    adapter

            )
                    .setApplicationName(
                            "Little Big Home Interiors"
                    )
                    .build();
        }
    }


    // =====================================================
    // ADD LEAD
    // =====================================================

    public void addLead(
            ContactLead lead
    ) throws Exception {

        if (
                spreadsheetId == null ||
                        spreadsheetId.trim().isEmpty()
        ) {

            throw new IllegalStateException(
                    "Google Spreadsheet ID is missing."
            );
        }


        if (
                sheetName == null ||
                        sheetName.trim().isEmpty()
        ) {

            throw new IllegalStateException(
                    "Google Sheet name is missing."
            );
        }


        Sheets sheets =
                getSheetsService();


        List<Object> row =
                Arrays.asList(

                        lead.getId(),

                        safe(
                                lead.getName()
                        ),

                        safe(
                                lead.getEmail()
                        ),

                        safe(
                                lead.getPhone()
                        ),

                        safe(
                                lead.getProjectType()
                        ),

                        safe(
                                lead.getMessage()
                        ),

                        lead.getCreatedAt()
                                .toString()
                );


        ValueRange body =
                new ValueRange()
                        .setValues(
                                List.of(row)
                        );


        sheets
                .spreadsheets()
                .values()
                .append(

                        spreadsheetId,

                        sheetName + "!A:G",

                        body

                )
                .setValueInputOption(
                        "USER_ENTERED"
                )
                .setInsertDataOption(
                        "INSERT_ROWS"
                )
                .execute();


        System.out.println(
                "Lead successfully added to Google Sheet."
        );
    }


    private String safe(String value) {

        if (
                value == null ||
                        value.trim().isEmpty()
        ) {

            return "";
        }

        return value.trim();
    }
}