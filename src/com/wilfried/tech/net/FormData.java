package com.wilfried.tech.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public final class FormData {

    private final HashMap<String, String> params = new HashMap<>();
    private final HashMap<String, FileData> files = new HashMap<>();

    public void add(String name, String value) {
        params.put(name, value);
    }

    public String get(String name) {
        return params.get(name);
    }

    public void replace(String name, String value) {
        params.replace(name, value);
    }

    public String remove(String name) {
        return params.remove(name);
    }

    HashMap<String, String> getFields() {
        return params;
    }

    HashMap<String, FileData> getBlobs() {
        return files;
    }

    public void addFile(String name, String path) throws IOException {
        addFile(name, new File(path));
    }

    public void addFile(String name, File file) throws FileNotFoundException {
        addFile(name, file.getName(), new FileInputStream(file));
    }

    public void addFile(String name, String filename, InputStream inputStream) {
        files.put(name, new FileData(filename, inputStream));
    }

    static class FileData {
        private final String filename;
        private final InputStream stream;

        public FileData(String filename, InputStream inputStream) {
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
