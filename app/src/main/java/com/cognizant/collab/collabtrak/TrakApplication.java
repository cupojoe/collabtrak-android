package com.cognizant.collab.collabtrak;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;


/**
 * Created by Joel Fraga on 8/11/16.
 */
public class TrakApplication extends Application {

    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                RequestParams params = new RequestParams();
                params.put("uuid", region.getProximityUUID());
                params.put("major", region.getMajor());
                params.put("minor", region.getMinor());
                params.put("state", "enter");

                OrchestrateClient.post("user/joelfraga/event", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        System.out.println(statusCode);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        System.out.println(statusCode);
                    }
                });
                showNotification(
                        "Enter Event",
                        "You entered the " + region.getIdentifier()
                );
            }

            @Override
            public  void onExitedRegion(Region region) {
                RequestParams params = new RequestParams();
                params.put("uuid", region.getProximityUUID());
                params.put("major", region.getMajor());
                params.put("minor", region.getMinor());
                params.put("state", "exit");

                OrchestrateClient.post("user/joelfraga/event", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        System.out.println(statusCode);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        System.out.println(statusCode);
                    }
                });
                showNotification(
                        "Exit Event",
                        "You exited the " + region.getIdentifier()
                );
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

                OrchestrateClient.get("beacons", null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i).getJSONObject("value");
                                beaconManager.startMonitoring(new Region(
                                        "Region " + i,
                                        UUID.fromString(obj.getString("UUID")),
                                        obj.getInt("major"),
                                        obj.getInt("minor")
                                    )
                                );
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println(statusCode);

                    }
                });
            }
        });


    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
