package com.ksrtc.bustimetable.importer;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import")
public class PdfTestController {

    private final PdfReaderService pdfReaderService;
    private final PdfBusParserService pdfBusParserService;

    public PdfTestController(
            PdfReaderService pdfReaderService,
            PdfBusParserService pdfBusParserService) {
        this.pdfReaderService = pdfReaderService;
        this.pdfBusParserService = pdfBusParserService;
    }

    @GetMapping("/pdf-text")
    public String readPdfText() throws IOException {
        return pdfReaderService.readPdfText();
    }

    @GetMapping("/parsed-rows")
    public List<PdfBusRow> readParsedRows() throws IOException {

        String pdfText =
                pdfReaderService.readPdfText();

        return pdfBusParserService.parse(pdfText);
    }
}