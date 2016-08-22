package com.cognizant.collab.collabtrak;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.ArrayAdapter;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;


/**
 * Created by Joel Fraga on 8/11/16.
 */
public class AppBeaconManager {

    private static AppBeaconManager _instance = new AppBeaconManager();

    public static AppBeaconManager getInstance() {
        return _instance;
    }

    private BeaconManager beaconManager;

    private boolean isConnected = false;

    public AppBeaconManager() {
    }

    public void createBeaconManager(Context context, final String userId) {

        beaconManager = new BeaconManager(context);

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                RequestParams params = new RequestParams();
                params.put("uuid", region.getProximityUUID());
                params.put("major", region.getMajor());
                params.put("minor", region.getMinor());
                params.put("state", "enter");

                OrchestrateClient.post("user/" + userId + "/event", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        System.out.println(statusCode);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        System.out.println(statusCode);
                    }
                });
            }

            @Override
            public void onExitedRegion(Region region) {
                RequestParams params = new RequestParams();
                params.put("uuid", region.getProximityUUID());
                params.put("major", region.getMajor());
                params.put("minor", region.getMinor());
                params.put("state", "exit");

                OrchestrateClient.post("user/" + userId + "/event", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        System.out.println(statusCode);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        System.out.println(statusCode);
                    }
                });
            }
        });

    }

    public void startMonitoring(final MonitoringCallback callback) {

        beaconManager.connect(new com.estimote.sdk.BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

                OrchestrateClient.get("beacons", null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        ArrayList<String> results = new ArrayList<>();
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
                                results.add(i, obj.getString("UUID"));
                                isConnected = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                isConnected =false;
                            }
                        }
                        callback.onMonitoringStarted(results);
                        System.out.println(statusCode);

                    }
                });
            }
        });
    }

    public void stopMonitoring() {
        beaconManager.disconnect();
        isConnected = false;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
