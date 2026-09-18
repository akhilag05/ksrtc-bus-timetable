package com.ksrtc.bustimetable.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class PdfBusParserService {

    /*
     * Detects row numbers in normal cases and also when PDFBox
     * joins the previous departure time with the next row number.
     *
     * Example:
     *
     * 17:50 235 MANDYA ...
     *
     * The parser must still recognize 235 as a new row.
     */
    private static final Pattern ROW_NUMBER_PATTERN =
            Pattern.compile(
                    "(?:^|(?<=\\s)|(?<=\\d{2}:\\d{2}))(\\d{1,3})(?=\\s+[A-Z])"
            );

    private static final List<String> SERVICE_CLASSES =
            Arrays.asList(
                    "EXPRESS"
            );

    private static final List<String> KNOWN_ORIGINS =
            Arrays.asList(
                    "M.M.HILLS",
                    "MM HILLS",
                    "B.C.ROAD",
                    "MANDYA",
                    "MYSURU",
                    "MADIKERI",
                    "MANGALURU",
                    "BENGALURU",
                    "NAGAMANGALA",
                    "TIRUPATHI"
            );

    private static final Pattern TIME_PATTERN =
            Pattern.compile("\\b(\\d{1,2}:\\d{2})\\b");

    public List<PdfBusRow> parse(String pdfText) {

        List<PdfBusRow> rows = new ArrayList<>();

        if (pdfText == null || pdfText.isBlank()) {
            return rows;
        }

        String normalizedText =
                normalizeText(pdfText);

        Matcher rowMatcher =
                ROW_NUMBER_PATTERN.matcher(normalizedText);

        List<RowBoundary> boundaries =
                new ArrayList<>();

        while (rowMatcher.find()) {

            int rowNumber =
                    Integer.parseInt(rowMatcher.group(1));

            if (rowNumber >= 1 && rowNumber <= 999) {

                /*
                 * IMPORTANT:
                 *
                 * start(1) gives the actual position of the
                 * row number.
                 *
                 * We do NOT use start(), because the regex may
                 * match the whitespace immediately before the
                 * row number.
                 */
                boundaries.add(
                        new RowBoundary(
                                rowNumber,
                                rowMatcher.start(1)
                        )
                );
            }
        }

        boundaries =
                removeDuplicateBoundaries(boundaries);

        boundaries.sort(
                Comparator.comparingInt(
                        RowBoundary::getPosition
                )
        );

        for (int i = 0; i < boundaries.size(); i++) {

            RowBoundary current =
                    boundaries.get(i);

            int start =
                    current.getPosition();

            int end;

            if (i + 1 < boundaries.size()) {

                end =
                        boundaries
                                .get(i + 1)
                                .getPosition();

            } else {

                end =
                        normalizedText.length();
            }

            if (start >= end) {
                continue;
            }

            String rowText =
                    normalizedText
                            .substring(start, end)
                            .trim();

            PdfBusRow row =
                    parseSingleRow(
                            current.getRowNumber(),
                            rowText
                    );

            if (row != null) {
                rows.add(row);
            }
        }

        rows.sort(
                Comparator.comparing(
                        PdfBusRow::getRowNumber
                )
        );

        return rows;
    }

    private PdfBusRow parseSingleRow(
            Integer rowNumber,
            String rowText) {

        if (rowText == null || rowText.isBlank()) {
            return null;
        }

        String content =
                rowText.replaceFirst(
                        "^\\s*\\d{1,3}\\s+",
                        ""
                ).trim();

        ServiceClassMatch serviceClassMatch =
                findServiceClass(content);

        if (serviceClassMatch == null) {
            return null;
        }

        String beforeClass =
                cleanSpaces(
                        content.substring(
                                0,
                                serviceClassMatch.getPosition()
                        )
                );

        String afterClass =
                content.substring(
                        serviceClassMatch.getPosition()
                                + serviceClassMatch.getServiceClass().length()
                ).trim();

        Matcher timeMatcher =
                TIME_PATTERN.matcher(afterClass);

        if (!timeMatcher.find()) {
            return null;
        }

        String departureTime =
                timeMatcher.group(1);

        String viaPlaces =
                afterClass
                        .substring(
                                0,
                                timeMatcher.start()
                        )
                        .trim();

        String textAfterTime =
                afterClass
                        .substring(
                                timeMatcher.end()
                        )
                        .trim();

        if (!textAfterTime.isEmpty()) {

            if (!viaPlaces.isEmpty()) {

                viaPlaces =
                        viaPlaces
                                + " "
                                + textAfterTime;

            } else {

                viaPlaces =
                        textAfterTime;
            }
        }

        viaPlaces =
                cleanViaPlaces(viaPlaces);

        String[] routeParts =
                splitFromAndTo(beforeClass);

        if (routeParts == null) {
            return null;
        }

        return new PdfBusRow(
                rowNumber,
                routeParts[0],
                routeParts[1],
                serviceClassMatch.getServiceClass(),
                viaPlaces,
                departureTime
        );
    }

    private ServiceClassMatch findServiceClass(
            String text) {

        String upperText =
                text.toUpperCase();

        ServiceClassMatch bestMatch =
                null;

        for (String serviceClass : SERVICE_CLASSES) {

            String marker =
                    " "
                            + serviceClass
                            + " ";

            int position =
                    upperText.indexOf(marker);

            if (position < 0) {
                continue;
            }

            if (bestMatch == null
                    || position < bestMatch.getPosition()) {

                bestMatch =
                        new ServiceClassMatch(
                                serviceClass,
                                position + 1
                        );
            }
        }

        return bestMatch;
    }

    private String[] splitFromAndTo(
            String text) {

        if (text == null || text.isBlank()) {
            return null;
        }

        String normalized =
                cleanSpaces(text);

        String upper =
                normalized.toUpperCase();

        List<String> sortedOrigins =
                new ArrayList<>(
                        KNOWN_ORIGINS
                );

        sortedOrigins.sort(
                Comparator.comparingInt(
                        String::length
                ).reversed()
        );

        for (String origin : sortedOrigins) {

            String originUpper =
                    origin.toUpperCase();

            if (!upper.startsWith(originUpper)) {
                continue;
            }

            if (upper.length() > originUpper.length()) {

                char nextCharacter =
                        upper.charAt(
                                originUpper.length()
                        );

                if (!Character.isWhitespace(
                        nextCharacter)) {
                    continue;
                }
            }

            String destination =
                    normalized
                            .substring(
                                    origin.length()
                            )
                            .trim();

            if (destination.isEmpty()) {
                return null;
            }

            return new String[] {
                    origin,
                    destination
            };
        }

        String[] words =
                normalized.split("\\s+", 2);

        if (words.length < 2) {
            return null;
        }

        return new String[] {
                words[0],
                words[1]
        };
    }

    private String cleanViaPlaces(
            String viaPlaces) {

        if (viaPlaces == null
                || viaPlaces.isBlank()) {
            return "";
        }

        String cleaned =
                viaPlaces
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();

        cleaned =
                cleaned.replaceAll(
                        ",\\s+",
                        ","
                );

        return cleaned;
    }

    private String normalizeText(
            String text) {

        return text
                .replace("\r\n", "\n")
                .replace('\r', '\n');
    }

    private String cleanSpaces(
            String text) {

        return text
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private List<RowBoundary> removeDuplicateBoundaries(
            List<RowBoundary> boundaries) {

        List<RowBoundary> result =
                new ArrayList<>();

        Set<String> seen =
                new HashSet<>();

        for (RowBoundary boundary : boundaries) {

            String key =
                    boundary.getRowNumber()
                            + ":"
                            + boundary.getPosition();

            if (seen.add(key)) {
                result.add(boundary);
            }
        }

        return result;
    }

    private static class RowBoundary {

        private final Integer rowNumber;
        private final int position;

        RowBoundary(
                Integer rowNumber,
                int position) {

            this.rowNumber = rowNumber;
            this.position = position;
        }

        public Integer getRowNumber() {
            return rowNumber;
        }

        public int getPosition() {
            return position;
        }
    }

    private static class ServiceClassMatch {

        private final String serviceClass;
        private final int position;

        ServiceClassMatch(
                String serviceClass,
                int position) {

            this.serviceClass = serviceClass;
            this.position = position;
        }

        public String getServiceClass() {
            return serviceClass;
        }

        public int getPosition() {
            return position;
        }
    }
}