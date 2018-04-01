package com.example.a123.a1217mocklocation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by 123 on 2018/2/24.
 */

public class MyService extends Service {

    private Intent broadcastIntent;
    private String provider2;
    private LocationManager location_mgr;
    private Notification.Builder builder;
    private Notification noti;
    private NotificationManager notifiti_mgr;

    public MyService() {
        broadcastIntent = new Intent("check");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startMockLocation(intent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            notifiti_mgr.cancel(200);
        } catch (Exception e) {
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startMockLocation(Intent intent) {
        try {
            location_mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            provider2 = LocationManager.GPS_PROVIDER;//假位置提供者  以下為假位置必要條件  缺一不可
            location_mgr.addTestProvider(provider2, false, true, false, false, false, false, false, Criteria.POWER_LOW, Criteria.ACCURACY_FINE); //增加一個假位置的提供者
            Location location = new Location(provider2);
            location.setLatitude(intent.getDoubleExtra(getString(R.string.mocklat), -1));
            location.setLongitude(intent.getDoubleExtra(getString(R.string.mocklot), -1));
            location.setTime(System.currentTimeMillis());
            location.setAccuracy(0.1f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {  //如果使用者的版本大於我的最低版本
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            location_mgr.setTestProviderEnabled(provider2, true); //假位置啟用
            location_mgr.setTestProviderLocation(provider2, location);
            broadcastIntent.putExtra(getString(R.string.lat), location.getLatitude());
            broadcastIntent.putExtra(getString(R.string.lot), location.getLongitude());
            sendBroadcast(broadcastIntent);
            notifity();
        } catch (Exception e) {
            broadcastIntent.putExtra(getString(R.string.mock), 13);
            sendBroadcast(broadcastIntent);
            Toast.makeText(this, "請在開發者開啟模擬定位權限", Toast.LENGTH_LONG).show();
        }
    }

    private void notifity() {
        notifiti_mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Notification.Builder(MyService.this);
        builder.setSmallIcon(R.drawable.apple);
        builder.setContentTitle("開始模擬定位服務");
        builder.setAutoCancel(true);
        noti = builder.build();
        startForeground(200,noti);
        notifiti_mgr.notify(200, noti);
    }
}
