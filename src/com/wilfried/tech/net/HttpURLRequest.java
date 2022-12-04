package com.wilfried.tech.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;


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

    private final String BOUNDARY;
    private final String LINE_FEED = "\r\n";
    private final String charset = StandardCharsets.UTF_8.displayName();
    private final Map<String, String> headers = new HashMap<>();
    private HttpURLConnection httpURLConnection = null;
    private String method = "GET";
    private URL url = null;
    private boolean opened = false;
    private boolean sent = false;
    private int timeout;

    public HttpURLRequest() {
        this.BOUNDARY = "===" + System.currentTimeMillis() + "===";
        headers.put("User-Agent", "Wilfried-Tech@XMLHttpRequest");
    }

    public void open(final @NotNull String method, final @NotNull String url) throws IllegalArgumentException, MalformedURLException, ProtocolException {
        open(method, url, null, null);
    }

    public void open(final @NotNull String method, final @NotNull String url, final String username, final String password)
            throws IllegalArgumentException, MalformedURLException, ProtocolException {
        String methods = "^(GET|POST|HEAD|OPTIONS|PUT|DELETE|TRACE)$";
        if (!method.trim().toUpperCase().matches(methods)) {
            throw new ProtocolException("Invalid HTTP method: " + method);
        }

        if (!Objects.equals(username, null) || !Objects.equals(password, null)) {
            if (username.isEmpty())
                throw new IllegalArgumentException("username is empty");
            if (password.isEmpty())
                throw new IllegalArgumentException("password is empty");

            headers.put("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString((username + " : " + password).getBytes()));
        }
        this.opened = true;
        this.url = new URL(url);
        this.method = method.trim().toUpperCase();
    }

    public void checkState() {
        checkState(true);
    }

    private void checkState(boolean justOpened) {
        if (!this.opened) {
            throw new HttpURLRequestException("the connection is not open");
        }
        if (justOpened) {
            if (this.sent) {
                throw new HttpURLRequestException("data already sent");
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
        if (httpURLConnection != null)
            httpURLConnection.disconnect();
    }

    public HttpURLResponse send() throws IOException {
        return send(new FormData());
    }

    public HttpURLResponse send(FormData formData) throws IOException {
        checkState(false);
        this.sent = true;

        HashMap<String, FormData.FileData> formDataBlobs = formData.getBlobs();
        HashMap<String, String> formDataFields = formData.getFields();
        if (method.equals("GET")) {
            initURLParams(formData.getFields());
        }

        httpURLConnection = (HttpURLConnection) this.url.openConnection();
        httpURLConnection.setRequestMethod(method.toUpperCase().trim());
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setReadTimeout(this.timeout);
        if (formDataBlobs.size() != 0 || method.equals("POST")) {
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        }

        for (String key : headers.keySet()) {
            httpURLConnection.setRequestProperty(key, headers.get(key));
        }

        OutputStream outputStream = null;
        PrintWriter writer = null;
        if (Objects.equals(method, "GET")) {
            if (formDataBlobs.size() != 0) {
                outputStream = httpURLConnection.getOutputStream();
                writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
            }
        } else {
            outputStream = httpURLConnection.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
        }

        if (writer != null) {
            for (String fileKey : formDataBlobs.keySet()) {
                addFilePart(fileKey, formDataBlobs.get(fileKey), writer, outputStream);
            }
            for (String fieldKey : formDataFields.keySet()) {
                addFormField(fieldKey, formDataFields.get(fieldKey), writer);
            }
            writer.append(LINE_FEED).flush();
            writer.append("--").append(BOUNDARY).append("--").append(LINE_FEED);
            writer.close();
        }
        return new HttpURLResponse(httpURLConnection);
    }

    private void addFormField(String name, String value, PrintWriter writer) {
        writer.append("--").append(BOUNDARY).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=").append(charset).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    private void addFilePart(String name, FormData.FileData fileData, PrintWriter writer, OutputStream outputStream)
            throws IOException {
        writer.append("--").append(BOUNDARY).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"; filename=\"")
                .append(fileData.getFilename()).append("\"").append(LINE_FEED);
        writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileData.getFilename()))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        InputStream inputStream = fileData.getStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
        writer.append(LINE_FEED);
        writer.flush();
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
                urlParam.append("&").append(key).append("=")
                        .append(URLEncoder.encode(formDataFields.get(key), StandardCharsets.UTF_8.displayName()));
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
