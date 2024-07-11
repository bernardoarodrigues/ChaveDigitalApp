package com.control.chavedigital;

// Imports

import android.app.Activity;
import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class HttpRequest extends Thread {

    // Variable Declaration

    // APIs and others
    OnPutDataListener mListener;

    // Values
    private final String url, method;
    String[] data, field;
    Context mContext;
    Activity mActivity;

    // Interface
    public interface OnPutDataListener {
        void OnResult(String result);
    }

    // GET Constructor
    public HttpRequest(Activity activity, String url, OnPutDataListener listener) {
        mContext = activity.getApplicationContext();
        mActivity = activity;
        mListener = listener;
        this.url = url;
        this.method = "GET";
    }

    // POST Constructor
    public HttpRequest(Activity activity, String url, String[] field, String[] data, OnPutDataListener listener) {
        mContext = activity.getApplicationContext();
        mActivity = activity;
        mListener = listener;
        this.url = url;
        this.method = "POST";
        this.data = new String[data.length];
        this.field = new String[field.length];
        System.arraycopy(field, 0, this.field, 0, field.length);
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    // Starts Thread
    @Override
    public void run() {
        try {
            // Begins connection
            String UTF8 = "UTF-8", iso = "iso-8859-1";
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(this.method);
            httpURLConnection.setDoInput(true);

            // If POST request, sends data
            if(this.method.equals("POST")) {
                // Cria Output stream para enviar dados
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, UTF8));
                StringBuilder post_data = new StringBuilder();
                for (int i = 0; i < this.field.length; i++) {
                    // Envia dados
                    post_data.append(URLEncoder.encode(this.field[i], "UTF-8")).append("=").append(URLEncoder.encode(this.data[i], UTF8)).append("&");
                }
                bufferedWriter.write(post_data.toString());

                // Closes output stream
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
            }

            // Gets data
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, iso));
            StringBuilder result = new StringBuilder();
            String result_line;
            while ((result_line = bufferedReader.readLine()) != null) {
                // Adiciona dados ao resultado
                result.append(result_line);
            }

            // Closes input stream
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

            // Sets data
            setData(result.toString());
        }
        // If any error occurred
        catch (IOException e) {
            // Sets error
            setData(e.toString());
        }
    }

    // Sets result/error
    public void setData(String result_data) {
        // Sends result to the UIThread
        mActivity.runOnUiThread(() -> mListener.OnResult(result_data));
    }
}