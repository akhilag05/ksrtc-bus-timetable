package com.ksrtc.bustimetable.importer;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import")
public class TimetableImportController {

    private final TimetableImportService timetableImportService;

    public TimetableImportController(
            TimetableImportService timetableImportService) {

        this.timetableImportService =
                timetableImportService;
    }

    @PostMapping("/timetable")
    public ImportSummary importTimetable() {

        return timetableImportService.importTimetable();
    }
}