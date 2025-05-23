package com.example.myhikingmaphk.util;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UtilApi {
    public static class Response {
        private CompletableFuture<String> future;
        public Response(Supplier<String> supplier) {
            future = CompletableFuture.supplyAsync(supplier);
        }

        public static Response get(String url) {
            return new Response(() -> {
                try {
                    return getResponse(url);
                } catch (IOException ignored) {
                    return null;
                }
            });
        }

        public static Response post(String url, Map<String, String> headers, Map<String, String> data) {
            return new Response(() -> {
                try {
                    return postResponse(url, headers, data);
                } catch (IOException ignored) {
                    return null;
                }
            });
        }
        public CompletableFuture<Response> thenAsyncResponse(Function<String, Response> fn) {
            return future.thenApplyAsync(fn);
        }
        public void then(Consumer<String> fn) {
            future.thenAccept(fn);
        }
        public void join(Consumer<String> fn) {
            future.thenAccept(fn).join();
        }
    }

    @NonNull
    public static String getResponse(String url) throws IOException {
        URL requestUrl = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) requestUrl.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(8000);
        urlConnection.setReadTimeout(8000);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.connect();
        InputStream in = urlConnection.getInputStream();
        // Read the response
        Reader reader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        // Close all streams
        bufferedReader.close();
        reader.close();
        in.close();
        // Close connection
        urlConnection.disconnect();
        // Return the request result
        return stringBuilder.toString();
    }

    public static String postResponse(String url, Map<String, String> headers, Map<String, String> data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        headers.forEach(connection::setRequestProperty);
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);

        connection.setDoInput(true);
        connection.setDoOutput(true);
        byte[] dataBytes = String.join("&", data.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .toArray(String[]::new))
                .getBytes(StandardCharsets.UTF_8);
        connection.getOutputStream().write(dataBytes);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error: " + responseCode);
        }
        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        return response.toString();
    }
    private static final String dirApiUrlTemplate = "https://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&key=%s";
    public static String dirUrl(String origin, String dest, String apiKey) {
        return String.format(dirApiUrlTemplate, escapedUrl(origin), escapedUrl(dest), apiKey);
    }

    private static final String revGeoCodeApiUrlTemplate = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s";

    public static String getRevGeoCodeApiUrl(String lat, String lon, String apiKey) {
        return String.format(revGeoCodeApiUrlTemplate, escapedUrl(lat), escapedUrl(lon), apiKey);
    }

    private static String escapedUrl(String url) {
        return url.replace(' ', '+');
    }
}
