/*
 * Copyright (c) 2023 Wilfried-Tech.  All rights reserved.
 */

package com.wilfried.tech.net.test;


import com.wilfried.tech.net.HttpURLRequest;
import com.wilfried.tech.net.HttpURLRequestData;
import com.wilfried.tech.net.HttpURLResponse;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpURLRequest xhr = new HttpURLRequest();
        HttpURLRequestData httpURLRequestData = new HttpURLRequestData();
        httpURLRequestData.add("authname", "Wilfried-Tech");
        httpURLRequestData.add("authpass", "jtmlucie63");
        httpURLRequestData.addFile("readme.md", "bin/artifacts/HttpURLRequestJar/HttpURLRequest.jar");

        try {
            xhr.open("get", "http://localhost/httpurlrequest/?yo=yeah", "Wilfried-Tech", "jtmlucie63");
            xhr.setRequestHeader("test", "okay");
            HttpURLResponse response = xhr.send(httpURLRequestData);
            System.out.println("===========Response==========");
            System.out.println(response.getResponseText().replaceAll("<br[ /]*>", "\n"));
            System.out.println(response.getResponseURL());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
// handle data send on get request