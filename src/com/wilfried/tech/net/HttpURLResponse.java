/*
 * Copyright (c) 2023 Wilfried-Tech.  All rights reserved.
 */

package com.wilfried.tech.net;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HttpURLResponse {

    private final int responseCode;
    private final String statusText;
    private final String responseType;
    private final URL responseUrl;
    private final byte[] responseStream;
    private final Map<String, String> responseHeaders = new HashMap<>();


    HttpURLResponse(HttpURLConnection httpURLConnection) throws IOException {
        responseCode = httpURLConnection.getResponseCode();
        statusText = httpURLConnection.getResponseMessage();
        responseType = httpURLConnection.getContentType();
        responseUrl = httpURLConnection.getURL();
        Map<String, List<String>> headers = httpURLConnection.getHeaderFields();
        for (String key : headers.keySet()) {
            if (!Objects.equals(key, null)) responseHeaders.put(key, headers.get(key).get(0));
        }
        if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            responseStream = toBytesArray(httpURLConnection.getInputStream());
        } else {
            responseStream = toBytesArray(httpURLConnection.getErrorStream());
        }

        httpURLConnection.disconnect();
    }

    public String getResponseType() {
        return responseType;
    }

    public String getStatusText() {
        return statusText;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public URL getResponseURL() {
        return responseUrl;
    }

    public @Nullable String getResponseHeader(String headerKey) {
        if (responseHeaders.containsKey(headerKey)) return responseHeaders.get(headerKey);
        return null;
    }

    public Map<String, String> getAllResponseHeaders() {
        return new HashMap<>(responseHeaders);
    }

    public @NotNull InputStream getResponseStream() {
        return new ByteArrayInputStream(responseStream);
    }

    public String getResponseText() throws IOException {
        StringBuilder response = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(getResponseStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }
        reader.close();
        return response.toString();
    }

    private byte[] toBytesArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            arrayOutputStream.write(buffer, 0, read);
        }
        inputStream.close();
        return arrayOutputStream.toByteArray();
    }
}
