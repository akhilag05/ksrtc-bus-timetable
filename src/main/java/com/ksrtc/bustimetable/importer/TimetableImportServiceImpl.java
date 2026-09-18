package com.ksrtc.bustimetable.importer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ksrtc.bustimetable.entity.BusService;
import com.ksrtc.bustimetable.entity.Route;
import com.ksrtc.bustimetable.entity.RouteStop;
import com.ksrtc.bustimetable.entity.Stop;
import com.ksrtc.bustimetable.repository.BusServiceRepository;
import com.ksrtc.bustimetable.repository.RouteRepository;
import com.ksrtc.bustimetable.repository.RouteStopRepository;
import com.ksrtc.bustimetable.repository.StopRepository;

@Service
public class TimetableImportServiceImpl implements TimetableImportService {

    private final PdfReaderService pdfReaderService;
    private final PdfBusParserService pdfBusParserService;
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final BusServiceRepository busServiceRepository;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH);

    public TimetableImportServiceImpl(
            PdfReaderService pdfReaderService,
            PdfBusParserService pdfBusParserService,
            StopRepository stopRepository,
            RouteRepository routeRepository,
            RouteStopRepository routeStopRepository,
            BusServiceRepository busServiceRepository) {

        this.pdfReaderService = pdfReaderService;
        this.pdfBusParserService = pdfBusParserService;
        this.stopRepository = stopRepository;
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
        this.busServiceRepository = busServiceRepository;
    }

    @Override
    @Transactional
    public ImportSummary importTimetable() {

        ImportSummary summary = new ImportSummary();

        try {
            String pdfText = pdfReaderService.readPdfText();

            List<PdfBusRow> rows =
                    pdfBusParserService.parse(pdfText);

            summary.setPdfRows(rows.size());

            for (PdfBusRow pdfRow : rows) {

                if (!isValidRow(pdfRow)) {
                    continue;
                }

                List<String> stopNames =
                        buildOrderedStopNames(pdfRow);

                if (stopNames.size() < 2) {
                    continue;
                }

                List<Stop> stops =
                        getOrCreateStops(stopNames);

                Route route =
                        findExistingRoute(stops);

                if (route == null) {

                    route =
                            createRoute(stops);

                    summary.setRoutesCreated(
                            summary.getRoutesCreated() + 1
                    );

                } else {

                    summary.setRoutesReused(
                            summary.getRoutesReused() + 1
                    );
                }

                LocalTime departureTime =
                        parseDepartureTime(
                                pdfRow.getDepartureTime()
                        );

                if (departureTime == null) {
                    continue;
                }

                boolean serviceExists =
                        serviceAlreadyExists(
                                route,
                                pdfRow.getServiceClass(),
                                departureTime
                        );

                if (serviceExists) {

                    summary.setServicesSkipped(
                            summary.getServicesSkipped() + 1
                    );

                    continue;
                }

                BusService busService =
                        new BusService();

                busService.setRoute(route);
                busService.setServiceClass(
                        normalize(
                                pdfRow.getServiceClass()
                        )
                );
                busService.setDepartureTime(
                        departureTime
                );
                busService.setSourceReference(
                        "MandyaBusStand.pdf row "
                                + pdfRow.getRowNumber()
                );

                busServiceRepository.save(busService);

                summary.setServicesCreated(
                        summary.getServicesCreated() + 1
                );
            }

            return summary;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Timetable import failed.",
                    exception
            );
        }
    }

    private boolean isValidRow(PdfBusRow row) {

        if (row == null) {
            return false;
        }

        if (isBlank(row.getFrom())) {
            return false;
        }

        if (isBlank(row.getTo())) {
            return false;
        }

        if (isBlank(row.getServiceClass())) {
            return false;
        }

        if (isBlank(row.getDepartureTime())) {
            return false;
        }

        return true;
    }

    private List<String> buildOrderedStopNames(
            PdfBusRow pdfRow) {

        List<String> stopNames =
                new ArrayList<>();

        stopNames.add(
                cleanStopName(
                        pdfRow.getFrom()
                )
        );

        String viaPlaces =
                pdfRow.getViaPlaces();

        if (!isBlank(viaPlaces)) {

            String[] viaStops =
                    viaPlaces.split(",");

            for (String viaStop : viaStops) {

                String cleaned =
                        cleanStopName(viaStop);

                if (!cleaned.isEmpty()) {
                    stopNames.add(cleaned);
                }
            }
        }

        stopNames.add(
                cleanStopName(
                        pdfRow.getTo()
                )
        );

        return removeConsecutiveDuplicates(
                stopNames
        );
    }

    private List<Stop> getOrCreateStops(
            List<String> stopNames) {

        List<Stop> stops =
                new ArrayList<>();

        for (String stopName : stopNames) {

            String normalized =
                    normalize(stopName);

            Stop stop =
                    stopRepository
                            .findFirstByNormalizedName(
                                    normalized
                            )
                            .orElse(null);

            if (stop == null) {

                stop =
                        new Stop();

                stop.setName(stopName);
                stop.setNormalizedName(normalized);

                stop =
                        stopRepository.save(stop);
            }

            stops.add(stop);
        }

        return stops;
    }

    private Route findExistingRoute(
            List<Stop> stops) {

        if (stops.size() < 2) {
            return null;
        }

        Stop origin =
                stops.get(0);

        Stop destination =
                stops.get(stops.size() - 1);

        List<Route> routes =
                routeRepository.findAll();

        for (Route route : routes) {

            if (!route.getOriginStop()
                    .getId()
                    .equals(origin.getId())) {
                continue;
            }

            if (!route.getDestinationStop()
                    .getId()
                    .equals(destination.getId())) {
                continue;
            }

            List<RouteStop> existingRouteStops =
                    routeStopRepository
                            .findByRouteOrderByStopOrderAsc(
                                    route
                            );

            if (sameStopSequence(
                    existingRouteStops,
                    stops)) {

                return route;
            }
        }

        return null;
    }

    private boolean sameStopSequence(
            List<RouteStop> existingRouteStops,
            List<Stop> expectedStops) {

        if (existingRouteStops.size()
                != expectedStops.size()) {

            return false;
        }

        for (int i = 0;
             i < expectedStops.size();
             i++) {

            Stop existingStop =
                    existingRouteStops
                            .get(i)
                            .getStop();

            Stop expectedStop =
                    expectedStops.get(i);

            if (!existingStop.getId()
                    .equals(expectedStop.getId())) {

                return false;
            }
        }

        return true;
    }

    private Route createRoute(
            List<Stop> stops) {

        Route route =
                new Route();

        route.setOriginStop(
                stops.get(0)
        );

        route.setDestinationStop(
                stops.get(stops.size() - 1)
        );

        route =
                routeRepository.save(route);

        for (int i = 0;
             i < stops.size();
             i++) {

            RouteStop routeStop =
                    new RouteStop();

            routeStop.setRoute(route);
            routeStop.setStop(
                    stops.get(i)
            );
            routeStop.setStopOrder(
                    i + 1
            );

            routeStopRepository.save(
                    routeStop
            );
        }

        return route;
    }

    private boolean serviceAlreadyExists(
            Route route,
            String serviceClass,
            LocalTime departureTime) {

        List<BusService> services =
                busServiceRepository
                        .findByRoute(route);

        String normalizedClass =
                normalize(serviceClass);

        for (BusService service : services) {

            if (service.getDepartureTime()
                    == null) {
                continue;
            }

            if (!service.getDepartureTime()
                    .equals(departureTime)) {
                continue;
            }

            if (!normalize(
                    service.getServiceClass()
            ).equals(normalizedClass)) {
                continue;
            }

            return true;
        }

        return false;
    }

    private LocalTime parseDepartureTime(
            String value) {

        if (isBlank(value)) {
            return null;
        }

        try {

            return LocalTime.parse(
                    value.trim(),
                    TIME_FORMATTER
            );

        } catch (Exception exception) {

            return null;
        }
    }

    private List<String> removeConsecutiveDuplicates(
            List<String> names) {

        List<String> result =
                new ArrayList<>();

        String previous = null;

        for (String name : names) {

            String normalized =
                    normalize(name);

            if (normalized.isEmpty()) {
                continue;
            }

            if (previous != null
                    && previous.equals(normalized)) {
                continue;
            }

            result.add(name);
            previous = normalized;
        }

        return result;
    }

    private String cleanStopName(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ENGLISH);
    }

    private boolean isBlank(
            String value) {

        return value == null
                || value.trim().isEmpty();
    }
}