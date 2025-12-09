package com.rideshare.userservice;

import java.net.http.*;
import java.net.URI;

public class TrafficPOC {

    public static void main(String[] args) throws Exception {

        String token = "pk.eyJ1IjoiZ29wYWxyYW8tbWFwYm94IiwiYSI6ImNtaXJpMTd2dDBjZnE1aXM5cnc0aXpuMDkifQ.S6glSnGOwW5lgg22ldq7RA";

        // Hyderabad: Gachibowli → Hitec City route
        String url = "https://api.mapbox.com/directions/v5/mapbox/driving-traffic/78.3489,17.4401;78.3908,17.4483?alternatives=true&overview=full&annotations=duration&access_token=" + token;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("API Response:");
        System.out.println(response.body());
    }
}
