/*
 * Copyright (c) 2023 Wilfried-Tech.  All rights reserved.
 */

package com.wilfried.tech.net;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class HttpURLRequest {
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_NOT_AUTHORITATIVE = 203;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_RESET = 205;
    public static final int HTTP_PARTIAL = 206;
    public static final int HTTP_MULT_CHOICE = 300;
    public static final int HTTP_MOVED_PERM = 301;
    public static final int HTTP_MOVED_TEMP = 302;
    public static final int HTTP_SEE_OTHER = 303;
    public static final int HTTP_NOT_MODIFIED = 304;
    public static final int HTTP_USE_PROXY = 305;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_PAYMENT_REQUIRED = 402;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_BAD_METHOD = 405;
    public static final int HTTP_NOT_ACCEPTABLE = 406;
    public static final int HTTP_PROXY_AUTH = 407;
    public static final int HTTP_CLIENT_TIMEOUT = 408;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_GONE = 410;
    public static final int HTTP_LENGTH_REQUIRED = 411;
    public static final int HTTP_PRECON_FAILED = 412;
    public static final int HTTP_ENTITY_TOO_LARGE = 413;
    public static final int HTTP_REQ_TOO_LONG = 414;
    public static final int HTTP_UNSUPPORTED_TYPE = 415;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int HTTP_SERVER_ERROR = 500;
    public static final int HTTP_INTERNAL_ERROR = 500;
    public static final int HTTP_NOT_IMPLEMENTED = 501;
    public static final int HTTP_BAD_GATEWAY = 502;
    public static final int HTTP_UNAVAILABLE = 503;
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION = 505;

    private final Map<String, String> headers = new HashMap<>();
    private HttpURLConnection httpURLConnection = null;
    private String method = "GET";
    private URL url = null;
    private boolean opened = false;
    private boolean sent = false;
    private int timeout = 0;

    public HttpURLRequest() {
        headers.put("User-Agent", "Wilfried-Tech@XMLHttpRequest");
    }

    public void open(final @NotNull String method, final @NotNull String url) throws IllegalArgumentException, MalformedURLException, ProtocolException {
        open(method, url, null, null);
    }

    public void open(final @NotNull String method, final @NotNull String url, final String username, final String password) throws IllegalArgumentException, MalformedURLException, ProtocolException {
        String methods = "^(GET|POST|HEAD|OPTIONS|PUT|DELETE|TRACE)$";
        if (!method.trim().toUpperCase().matches(methods)) {
            throw new ProtocolException("Invalid HTTP method: " + method);
        }

        if (!Objects.equals(username, null) || !Objects.equals(password, null)) {
            if (username.isEmpty()) throw new IllegalArgumentException("username is empty");
            if (password.isEmpty()) throw new IllegalArgumentException("password is empty");
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            });
            headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + " : " + password).getBytes()));
        }
        this.opened = true;
        this.url = new URL(url);
        this.method = method.trim().toUpperCase();
    }

    public void checkState() {
        checkState(true);
    }

    private void checkState(boolean checkSendStatus) {
        if (!this.opened) {
            throw new HttpURLRequestException("the connection is not opened");
        }
        if (checkSendStatus) {
            if (this.sent) {
                throw new HttpURLRequestException("request already sent");
            }
        }
    }

    public void setRequestHeader(String name, String value) {
        checkState();
        headers.put(name, value);
    }

    public void setTimeOut(int timeout) {
        checkState();
        this.timeout = timeout;
    }

    public void abort() {
        checkState(false);
        if (httpURLConnection != null) httpURLConnection.disconnect();
    }

    private void initSendData() throws IOException {
        this.sent = true;
        httpURLConnection = (HttpURLConnection) this.url.openConnection();
        httpURLConnection.setRequestMethod(method.toUpperCase().trim());
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        if (timeout > 0) {
            httpURLConnection.setReadTimeout(this.timeout);
        }
        for (String key : headers.keySet()) {
            httpURLConnection.setRequestProperty(key, headers.get(key));
        }
    }

    public HttpURLResponse send() throws IOException {
        initSendData();
        return new HttpURLResponse(httpURLConnection);
    }

    public HttpURLResponse send(String body) throws IOException {
        setRequestHeader("Content-Type", "text/plain");
        initSendData();

        BufferedOutputStream writer = new BufferedOutputStream(httpURLConnection.getOutputStream());
        writer.write(body.getBytes(StandardCharsets.UTF_8));
        writer.flush();
        writer.close();
        return new HttpURLResponse(httpURLConnection);
    }

    public HttpURLResponse send(InputStream blob) throws IOException {
        setRequestHeader("Content-Type", URLConnection.guessContentTypeFromStream(blob));
        setRequestHeader("Content-Transfer-Encoding", "binary");
        initSendData();
        OutputStream outputStream = httpURLConnection.getOutputStream();
        new BufferedInputStream(blob).transferTo(outputStream);
        outputStream.flush();
        outputStream.close();
        return new HttpURLResponse(httpURLConnection);
    }

    public HttpURLResponse send(HttpURLRequestData httpURLRequestData) throws IOException {
        if (method.equals("GET")) {
            initURLParams(httpURLRequestData.getFields());
            initSendData();
            if (!httpURLRequestData.getBlobs().isEmpty()) {
                throw new HttpURLRequestException("Could'nt send files via GET Request !");
            }
        } else {
            setRequestHeader("Content-Type", "multipart/form-data; boundary=" + httpURLRequestData.getBoundary());
            initSendData();
            BufferedOutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            outputStream.write(httpURLRequestData.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        }

        return new HttpURLResponse(httpURLConnection);
    }

    private void initURLParams(HashMap<String, String> formDataFields) {
        StringBuilder urlParam = new StringBuilder();
        String url = this.url.toString();
        if (url.contains("?")) {
            urlParam.append(url.substring(url.indexOf("?") + 1));
            url = url.substring(0, url.indexOf("?"));
        }
        try {
            for (String key : formDataFields.keySet()) {
                urlParam.append("&").append(key).append("=").append(URLEncoder.encode(formDataFields.get(key), StandardCharsets.UTF_8.displayName()));
            }
        } catch (Exception ignored) {
        }
        if (urlParam.toString().startsWith("&")) {
            urlParam = new StringBuilder(urlParam.substring(1));
        }
        try {
            this.url = new URL(url + "?" + urlParam);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
