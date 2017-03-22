package com.innovativeincarnates.yash.customer;

/**
 * Created by yash on 22/3/17.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class MyService extends Service {


    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    PendingIntent resultPendingIntent;
    NotificationCompat.Builder mBuilder;

    String Username;

    public static final String PREFS_NAME = "DataFile";
    public static final String PREFS_USERNAME = "Username";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        Username = settings.getString(PREFS_USERNAME, "default");


        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mainWifi.isWifiEnabled()) {
            receiverWifi = new WifiReceiver();
        }
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(receiverWifi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    class WifiReceiver extends BroadcastReceiver {
        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();
            for(int i = 0; i < wifiList.size(); i++){
                if (wifiList.get(i).SSID.equals("snehil")){
                    checkHotspot(Username,"snehil");
                    // Toast.makeText(MyService.this, notification_data[0], Toast.LENGTH_LONG).show();
                    //createNotification(notification_data[0]);
                }
            }
        }


    }

    void createNotification(String message) {
        String[] mess = message.split("\\$");

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My notification")
                .setContentText(mess[0])
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);
        Intent resultIntent = new Intent(this, Notification_wifi.class).putExtra("Address", message) ;
        resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        int mNotificationId = 1;
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyManager.notify(mNotificationId, mBuilder.build());
    }

    public static final String UPLOAD_URL = "http://192.168.208.182:8081/look_for_seller";

    private void checkHotspot(final String username,final String Seller){

        //final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        //              loading.dismiss();
                        //Showing toast message of the response
                        Log.d(TAG,"Listener");


                        // Toast.makeText(MainActivity.this, s , Toast.LENGTH_LONG).show();

                        Toast.makeText(MyService.this, s , Toast.LENGTH_LONG).show();
                        //String[] notification_data = s.split("\\$");
                        if (s != "not_matched")
                        createNotification(s);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        //loading.dismiss();
                        Log.e(TAG,"ErrorListener"+volleyError.getMessage());
                        //Showing toast
                        Toast.makeText(MyService.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
            /*
            String Username = "Username";
            String Phone = "Phone";
            String shopname = "ArIES";
            String Address = "ECE_Dept";
            */
                //Creating parameters
//                Toast.makeText(MyService.this,  "Sending data", Toast.LENGTH_LONG).show();

                Map<String,String> params = new Hashtable<String, String>();

                String KEY_Username = "Username";
                String KEY_Inventory = "hotspot";

                params.put(KEY_Username, username);
                params.put(KEY_Inventory, Seller );

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);

    }
}
