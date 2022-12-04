package com.wilfried.tech.net.test;

import com.wilfried.tech.net.HttpURLRequest;
import com.wilfried.tech.net.FormData;
import com.wilfried.tech.net.HttpURLResponse;

public class Main {
    public static void main( String[] args) {
        HttpURLRequest xhr = new HttpURLRequest();
        FormData formData = new FormData();
        formData.add("authname", "Wilfried-Tech");
        formData.add("authpass", "jtmlucie63");

        try {
            xhr.open("get", "http://localhost/test/", "Wilfried-Tech", "jtmlucie63");
            xhr.setRequestHeader("test", "okay");
            HttpURLResponse response = xhr.send(formData);
            System.out.println("===========Response==========");
            System.out.println(response.getResponseText().replace("<br[ /]*>", "\n"));
            System.out.println(response.getResponseURL());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}