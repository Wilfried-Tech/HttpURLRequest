/*
 * Copyright (c) 2023 Wilfried-Tech.  All rights reserved.
 */

package com.wilfried.tech.net;

import java.io.*;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * <p>
 * Collections of key/value values
 * representing data intended to be transferred
 * by an HTTP request
 * </p>
 *
 * @author Wilfried-Tech
 * @since 1.0
 */
public class HttpURLRequestData {
    /**
     * HTTP request line feed
     *
     * @since 1.0
     */
    static final String LINE_FEED = "\r\n";

    /**
     * unique separator in an HTTP request
     *
     * @since 1.0
     */
    private final String BOUNDARY;

    /**
     * encoding used by the data
     *
     * @since 1.0
     */
    private final String mCharset = StandardCharsets.UTF_8.name();

    /**
     * associative array of key/value data passed to the class
     *
     * @since 1.0
     */
    private final HashMap<String, String> mParams;

    /**
     * associative array of data name/file passed to class
     *
     * @since 1.0
     */
    private final HashMap<String, Blob> mFiles;

    /**
     * Initialize {@code HttpURLRequestData} object
     *
     * @author Wilfried-Tech
     * @since 1.0
     */
    public HttpURLRequestData() {
        mParams = new HashMap<>();
        mFiles = new HashMap<>();
        BOUNDARY = "@-@-@" + System.currentTimeMillis() + "@-@-@";
    }

    /**
     * @param name  name of which the specified value is to be associated
     * @param value value to be associated with the specified name
     * @return the previous value associated with {@code name}, or
     * *         {@code null} if there was no mapping for {@code name}.
     * *         (A {@code null} return can also indicate that the map
     * *         previously associated {@code null} with {@code name}.)
     */

    public String add(String name, String value) {
        return mParams.put(name, value);
    }

    /**
     * return the data where the key is {@code name}
     *
     * @param name data key
     * @return the value associated with the name or {@code null}
     */

    public String get(String name) {
        return mParams.get(name);
    }


    public String replace(String name, String value) {
        return mParams.replace(name, value);
    }

    public String remove(String name) {
        return mParams.remove(name);
    }

    HashMap<String, String> getFields() {
        return mParams;
    }

    HashMap<String, Blob> getBlobs() {
        return mFiles;
    }

    String getBoundary() {
        return BOUNDARY;
    }

    public void addFile(String name, String path) throws IOException {
        addFile(name, new File(path));
    }

    public void addFile(String name, File file) throws FileNotFoundException {
        if (!file.isFile()) {
            throw new FileNotFoundException(file.getName() + " isn't a file");
        }
        addFile(name, file.getName(), new FileInputStream(file));
    }

    public void addFile(String name, String filename, InputStream inputStream) {
        mFiles.put(name, new Blob(filename, inputStream));
    }

    public void removeFile(String name) {
        mFiles.remove(name);
    }

    String getRawFieldsHttpData() {
        ByteArrayOutputStream rawHttpData = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(rawHttpData);
        for (String name : mParams.keySet()) {
            addFormField(name, get(name), writer);
        }
        return rawHttpData.toString();
    }

    String getRawFileHttpData() {
        ByteArrayOutputStream rawHttpData = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(rawHttpData);
        for (String name : mFiles.keySet()) {
            try {
                addFilePart(name, mFiles.get(name), writer, rawHttpData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return rawHttpData.toString();
    }

    @Override
    public String toString() {
        return getRawFieldsHttpData() + getRawFileHttpData();
    }

    private void addFormField(String name, String value, PrintWriter writer) {
        writer.append("--").append(BOUNDARY).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=").append(mCharset).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    private void addFilePart(String name, Blob blob, PrintWriter writer, OutputStream outputStream) throws IOException {
        writer.append("--").append(BOUNDARY).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"; filename=\"").append(blob.getFilename()).append("\"").append(LINE_FEED);
        writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(blob.getFilename())).append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        new BufferedInputStream(blob.getStream()).transferTo(outputStream);
        writer.append(LINE_FEED);
        writer.flush();
    }

    private static class Blob {
        private final String filename;
        private final InputStream stream;

        public Blob(String filename, InputStream inputStream) {
            this.filename = filename;
            this.stream = inputStream;
        }

        public String getFilename() {
            return filename;
        }

        public InputStream getStream() {
            return stream;
        }
    }
}
