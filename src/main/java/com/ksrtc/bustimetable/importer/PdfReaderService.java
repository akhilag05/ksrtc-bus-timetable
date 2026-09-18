package com.ksrtc.bustimetable.importer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class PdfReaderService {

    private static final String PDF_FILE =
            "/data/MandyaBusStand.pdf";

    public String readPdfText() throws IOException {

        try (InputStream inputStream =
                     getClass().getResourceAsStream(PDF_FILE)) {

            if (inputStream == null) {
                throw new IOException(
                        "PDF file not found: " + PDF_FILE);
            }

            byte[] pdfBytes = inputStream.readAllBytes();

            try (PDDocument document =
                         Loader.loadPDF(pdfBytes)) {

                PDFTextStripper stripper =
                        new PDFTextStripper();

                return stripper.getText(document);
            }
        }
    }
}