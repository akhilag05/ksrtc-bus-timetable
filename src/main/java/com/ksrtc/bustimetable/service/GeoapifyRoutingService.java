package com.ksrtc.bustimetable.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoapifyRoutingService {

    @Value("${geoapify.api.url}")
    private String apiUrl;

    @Value("${geoapify.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getRoute(
            double fromLatitude,
            double fromLongitude,
            double toLatitude,
            double toLongitude) {

        String waypoints =
                fromLatitude + "," + fromLongitude
                + "|" +
                toLatitude + "," + toLongitude;

        String url = apiUrl
                + "?waypoints=" + waypoints
                + "&mode=bus"
                + "&format=json"
                + "&apiKey=" + apiKey;

        return restTemplate.getForObject(url, String.class);
    }
}