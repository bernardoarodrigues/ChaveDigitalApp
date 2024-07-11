package com.control.chavedigital;

// Imports

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class GetConnection extends Thread {

    // Variable Declaration

    // APIs and others
    OnConnListener mListener;

    // Values
    public boolean con;
    public String type = "";
    boolean finished = false;
    Context mContext;
    Activity mActivity;

    // Interface
    public interface OnConnListener {
        void OnConnection(boolean con, String type);
    }

    // Constructor
    public GetConnection(Activity activity, OnConnListener listener) {
        mContext = activity.getApplicationContext();
        mActivity = activity;
        mListener = listener;
        con = true;
    }

    // Starts Thread
    public void run()
    {
        // While not finished
        while(!finished) {
            // Gets network status
            getStatus();
        }
    }

    // Stops Thread
    public void Stop()
    {
        finished = true;
    }

    // Gets network status
    public void getStatus() {
        // Sets connection state
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        con = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;

        // Sets connection type
        type = "none";
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) type = "wifi";
        else if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) type = "mobile";

        // Sends status to the UiThread
        mActivity.runOnUiThread(() -> mListener.OnConnection(con, type));

        // Waits 1 second
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }

}

