package com.ksrtc.bustimetable.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ksrtc.bustimetable.dto.BusSearchRequest;
import com.ksrtc.bustimetable.dto.BusSearchResponse;
import com.ksrtc.bustimetable.dto.RouteStopResponse;
import com.ksrtc.bustimetable.entity.BusService;
import com.ksrtc.bustimetable.entity.Route;
import com.ksrtc.bustimetable.entity.RouteStop;
import com.ksrtc.bustimetable.entity.Stop;
import com.ksrtc.bustimetable.repository.BusServiceRepository;
import com.ksrtc.bustimetable.repository.RouteRepository;
import com.ksrtc.bustimetable.repository.RouteStopRepository;
import com.ksrtc.bustimetable.service.GeoapifyRoutingService;
import com.ksrtc.bustimetable.service.RouteSearchService;

@Service
public class RouteSearchServiceImpl implements RouteSearchService {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final BusServiceRepository busServiceRepository;
    private final GeoapifyRoutingService geoapifyRoutingService;

    public RouteSearchServiceImpl(
            RouteRepository routeRepository,
            RouteStopRepository routeStopRepository,
            BusServiceRepository busServiceRepository,
            GeoapifyRoutingService geoapifyRoutingService) {

        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
        this.busServiceRepository = busServiceRepository;
        this.geoapifyRoutingService = geoapifyRoutingService;
    }

    @Override
    public List<BusSearchResponse> searchBuses(BusSearchRequest request) {

        String normalizedFrom = normalize(request.getFrom());
        String normalizedTo = normalize(request.getTo());

        String serviceClass = request.getServiceClass();

        List<BusSearchResponse> responses = new ArrayList<>();

        if (normalizedFrom.isBlank() || normalizedTo.isBlank()) {
            return responses;
        }

        List<Route> routes = routeRepository.findAll();

        for (Route route : routes) {

            if (route == null) {
                continue;
            }

            if (route.getOriginStop() == null
                    || route.getDestinationStop() == null) {
                continue;
            }

            Stop officialOriginStop = route.getOriginStop();
            Stop officialDestinationStop = route.getDestinationStop();

            List<RouteStop> routeStops =
                    routeStopRepository.findByRouteOrderByStopOrderAsc(route);

            if (routeStops == null || routeStops.isEmpty()) {
                continue;
            }

            int fromIndex = -1;
            int toIndex = -1;

            /*
             * Find the boarding point.
             */
            for (int i = 0; i < routeStops.size(); i++) {

                RouteStop routeStop = routeStops.get(i);

                if (routeStop == null
                        || routeStop.getStop() == null) {
                    continue;
                }

                Stop stop = routeStop.getStop();

                if (stop.getName() == null) {
                    continue;
                }

                if (isSameStop(
                        normalizedFrom,
                        normalize(stop.getName()))) {

                    fromIndex = i;
                    break;
                }
            }

            /*
             * Find the destination AFTER the boarding point.
             *
             * This is important for routes such as:
             *
             * MYSURU
             * MANDYA
             * MADDUR
             * CHANNAPATNA
             * BENGALURU(MRBS)
             */
            if (fromIndex != -1) {

                for (int i = fromIndex + 1;
                        i < routeStops.size();
                        i++) {

                    RouteStop routeStop = routeStops.get(i);

                    if (routeStop == null
                            || routeStop.getStop() == null) {
                        continue;
                    }

                    Stop stop = routeStop.getStop();

                    if (stop.getName() == null) {
                        continue;
                    }

                    if (isSameStop(
                            normalizedTo,
                            normalize(stop.getName()))) {

                        toIndex = i;
                        break;
                    }
                }
            }

            /*
             * From and To must exist and To must come
             * after From in the route.
             */
            if (fromIndex == -1 || toIndex == -1) {
                continue;
            }

            if (fromIndex >= toIndex) {
                continue;
            }

            List<BusService> busServices =
                    busServiceRepository.findByRoute(route);

            if (busServices == null || busServices.isEmpty()) {
                continue;
            }

            for (BusService busService : busServices) {

                if (busService == null) {
                    continue;
                }

                /*
                 * Service-class filtering.
                 */
                if (serviceClass != null
                        && !serviceClass.isBlank()) {

                    if (busService.getServiceClass() == null) {
                        continue;
                    }

                    if (!serviceClass.equalsIgnoreCase(
                            busService.getServiceClass())) {
                        continue;
                    }
                }

                Stop boardingStop =
                        routeStops.get(fromIndex).getStop();

                Stop journeyDestinationStop =
                        routeStops.get(toIndex).getStop();

                if (boardingStop == null
                        || journeyDestinationStop == null) {
                    continue;
                }

                BusSearchResponse response =
                        new BusSearchResponse();

                response.setBusServiceId(
                        busService.getId());

                response.setOfficialOrigin(
                        officialOriginStop.getName());

                response.setOfficialDestination(
                        officialDestinationStop.getName());

                response.setBoardingPoint(
                        boardingStop.getName());

                response.setDestination(
                        journeyDestinationStop.getName());

                response.setServiceClass(
                        busService.getServiceClass());

                if (busService.getDepartureTime() != null) {

                    response.setDepartureTime(
                            busService
                                    .getDepartureTime()
                                    .toString());
                }

                /*
                 * Add only the stops from the user's
                 * boarding point to the requested destination.
                 */
                List<RouteStopResponse> stopResponses =
                        new ArrayList<>();

                for (int i = fromIndex;
                        i <= toIndex;
                        i++) {

                    RouteStop routeStop =
                            routeStops.get(i);

                    if (routeStop == null
                            || routeStop.getStop() == null) {
                        continue;
                    }

                    Stop stop = routeStop.getStop();

                    Integer estimatedMinutes =
                            calculateGeoapifyTime(
                                    boardingStop,
                                    stop);

                    RouteStopResponse stopResponse =
                            new RouteStopResponse(
                                    stop.getName(),
                                    routeStop.getStopOrder(),
                                    estimatedMinutes);

                    stopResponses.add(stopResponse);
                }

                response.setStops(stopResponses);

                /*
                 * Timings are currently kept empty because
                 * estimated stop arrival times are handled
                 * separately.
                 */
                response.setTimings(
                        new ArrayList<>());

                responses.add(response);
            }
        }

        return responses;
    }

    /**
     * Checks whether two stop names represent the same
     * searchable location.
     *
     * Example:
     *
     * BENGALURU
     * BENGALURU(MRBS)
     *
     * are treated as the same location for searching.
     */
    private boolean isSameStop(
            String requestedStop,
            String databaseStop) {

        if (requestedStop == null
                || databaseStop == null) {
            return false;
        }

        if (requestedStop.equals(databaseStop)) {
            return true;
        }

        /*
         * Bengaluru aliases.
         */
        if (requestedStop.equals("BENGALURU")
                && databaseStop.equals("BENGALURU(MRBS)")) {
            return true;
        }

        if (requestedStop.equals("BENGALURU(MRBS)")
                && databaseStop.equals("BENGALURU")) {
            return true;
        }

        return false;
    }

    /**
     * Calls Geoapify and extracts the route duration.
     */
    private Integer calculateGeoapifyTime(
            Stop fromStop,
            Stop toStop) {

        if (fromStop == null || toStop == null) {
            return null;
        }

        if (fromStop.getLatitude() == null
                || fromStop.getLongitude() == null
                || toStop.getLatitude() == null
                || toStop.getLongitude() == null) {
            return null;
        }

        if (fromStop.getId() != null
                && fromStop.getId().equals(toStop.getId())) {
            return 0;
        }

        try {

            String geoapifyResponse =
                    geoapifyRoutingService.getRoute(
                            fromStop.getLatitude(),
                            fromStop.getLongitude(),
                            toStop.getLatitude(),
                            toStop.getLongitude());

            return extractDurationMinutes(
                    geoapifyResponse);

        } catch (Exception e) {

            System.err.println(
                    "Geoapify routing failed: "
                            + e.getMessage());

            return null;
        }
    }

    /**
     * Extracts "time" from Geoapify's JSON response.
     *
     * Geoapify returns duration in seconds.
     * We convert it to minutes.
     */
    private Integer extractDurationMinutes(
            String response) {

        if (response == null
                || response.isBlank()) {
            return null;
        }

        try {

            int timeIndex =
                    response.indexOf("\"time\"");

            if (timeIndex == -1) {
                return null;
            }

            int colonIndex =
                    response.indexOf(
                            ":",
                            timeIndex);

            if (colonIndex == -1) {
                return null;
            }

            int commaIndex =
                    response.indexOf(
                            ",",
                            colonIndex);

            if (commaIndex == -1) {

                commaIndex =
                        response.indexOf(
                                "}",
                                colonIndex);
            }

            if (commaIndex == -1) {
                return null;
            }

            String timeValue =
                    response.substring(
                            colonIndex + 1,
                            commaIndex)
                            .trim();

            double seconds =
                    Double.parseDouble(timeValue);

            return (int) Math.ceil(
                    seconds / 60.0);

        } catch (Exception e) {

            System.err.println(
                    "Unable to extract Geoapify duration: "
                            + e.getMessage());

            return null;
        }
    }

    /**
     * Normalizes user input and database stop names.
     */
    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase();
    }
}