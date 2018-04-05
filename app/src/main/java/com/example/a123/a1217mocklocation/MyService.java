package com.example.a123.a1217mocklocation;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static com.example.a123.a1217mocklocation.RockerView.DirectionMode.DIRECTION_8;

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
    private int statusBarHeight;// 状态栏高度
    private View view;// 透明窗体
    private boolean viewAdded = false;// 透明窗体是否已经显示
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private static RockerView mRockerView;

    private mTimertask mtask;
    private Timer mtimer;
    private mhandler mh;
    static int x = 0, y = 0;
    static int point = 0;
    private static double mlat, mlot;
    private String provider;
    private boolean status = false;
    private Location mylocation;


    public MyService() {
        broadcastIntent = new Intent("check");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        location_mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = LocationManager.GPS_PROVIDER;
        mtimer = new Timer();
        mh = new mhandler();
        mtask = new mTimertask();
        mWindow();
        myrock();
        mtimer.schedule(mtask, 0, 1000);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mlat = intent.getDoubleExtra(getString(R.string.mocklat), -1);
        mlot = intent.getDoubleExtra(getString(R.string.mocklot), -1);
        startMockLocation();
        licationlistener();
        refresh();
        return START_STICKY;
    }

    /**
     * mhandler 區
     */
    private class mhandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    startMockLocation();
                    Log.i("AAA", "緯度:" + mlat + "  經度:" + mlot);
                    break;

            }
        }
    }

    private boolean licationlistener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        location_mgr.requestLocationUpdates(provider2, 1000, 5, locationListener);
        return false;
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //mlocation = location;
            Log.i("AAA","假緯度:"+location.getLatitude());
            Log.i("AAA","假經度:"+location.getLongitude());
//            mylocation.setLatitude(mlat);
//            mylocation.setLongitude(mlot);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
             getLastLocation();
        }

        @Override
        public void onProviderDisabled(String provider) {
            getLastLocation();
        }
    };

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(MyService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MyService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location_mgr.getLastKnownLocation(provider2);
    }

    /**
     * TimerTask 區
     */
    private class mTimertask extends TimerTask {

        //经度（正：东经　负：西经）

        //纬度（正：北纬　负：南纬）

        @Override
        public void run() {
            switch (point) {
                case 0: //中心
                    break;
                case 1: //上
                    mlat -= 0.00015;
                    mh.sendEmptyMessage(1);
                    break;
                case 2:     //下
                    mlat += 0.00015;
                    mh.sendEmptyMessage(1);
                    break;
                case 3: //左
                    mlot -= 0.00015;
                    mh.sendEmptyMessage(1);
                    break;
                case 4: //右
                    mlot += 0.00015;
                    mh.sendEmptyMessage(1);
                    break;
                case 5: //右上
                    mlat += 0.0001;
                    mlot -= 0.0001;
                    mh.sendEmptyMessage(1);
                    break;
                case 6: //右下
                    mlat += 0.0001;
                    mlot += 0.0001;
                    mh.sendEmptyMessage(1);
                    break;
                case 7: //左上
                    mlat -= 0.0001;
                    mlot -= 0.0001;
                    mh.sendEmptyMessage(1);
                    break;
                case 8: //左下
                    mlat -= 0.0001;
                    mlot += 0.0001;
                    mh.sendEmptyMessage(1);
                    break;
                default:
                    Log.i("AA", "出錯了");
                    break;
            }

        }
    }

    private void mWindow() {
        view = LayoutInflater.from(this).inflate(R.layout.floating, null);
        mRockerView = (RockerView) view.findViewById(R.id.my_rocker);

        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        /*
         * LayoutParams.TYPE_SYSTEM_ERROR：保证该悬浮窗所有View的最上层
         * LayoutParams.FLAG_NOT_FOCUSABLE:该浮动窗不会获得焦点，但可以获得拖动
         * PixelFormat.TRANSPARENT：悬浮窗透明
         */
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);
        // layoutParams.gravity = Gravity.RIGHT|Gravity.BOTTOM; //悬浮窗开始在右下角显示
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;

        view.setOnTouchListener(new View.OnTouchListener() {
            float[] temp = new float[]{0f, 0f};

            public boolean onTouch(View v, MotionEvent event) {
                layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                int eventaction = event.getAction();
                switch (eventaction) {
                    case MotionEvent.ACTION_DOWN: // 按下事件，记录按下时手指在悬浮窗的XY坐标值
                        temp[0] = event.getX();
                        temp[1] = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        refreshView((int) (event.getRawX() - temp[0]), (int) (event.getRawY() - temp[1]));
                        break;

                }
                return true;
            }
        });
    }


    /**
     * 虛擬搖桿區
     */
    private void myrock() {
        mRockerView.setOnShakeListener(DIRECTION_8, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void direction(RockerView.Direction direction) {
                if (direction == RockerView.Direction.DIRECTION_CENTER) {
                    point = 0;
                } else if (direction == RockerView.Direction.DIRECTION_DOWN) {
                    point = 2;
                } else if (direction == RockerView.Direction.DIRECTION_LEFT) {
                    point = 3;
                } else if (direction == RockerView.Direction.DIRECTION_UP) {
                    point = 1;
                } else if (direction == RockerView.Direction.DIRECTION_RIGHT) {
                    point = 4;
                } else if (direction == RockerView.Direction.DIRECTION_DOWN_LEFT) {
                    point = 8;
                } else if (direction == RockerView.Direction.DIRECTION_DOWN_RIGHT) {
                    point = 6;
                } else if (direction == RockerView.Direction.DIRECTION_UP_LEFT) {
                    point = 7;
                } else if (direction == RockerView.Direction.DIRECTION_UP_RIGHT) {
                    point = 5;
                }
            }

            @Override
            public void onFinish() {

            }
        });


        mRockerView.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void angle(double angle) {
                // Log.i("AA", "当前角度:" + angle);
            }

            @Override
            public void onFinish() {

            }
        });

        mRockerView.setOnDistanceLevelListener(new RockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                // Log.i("AA", "当前距离级别:" + level);
            }
        });
    }

    /**
     * 刷新悬浮窗
     *
     * @param x 拖动后的X轴坐标
     * @param y 拖动后的Y轴坐标
     */
    public void refreshView(int x, int y) {
        //状态栏高度不能立即取，不然得到的值是0
        if (statusBarHeight == 0) {
            View rootView = view.getRootView();
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            statusBarHeight = r.top;
        }

        layoutParams.x = x;
        // y轴减去状态栏的高度，因为状态栏不是用户可以绘制的区域，不然拖动的时候会有跳动
        layoutParams.y = y - statusBarHeight;//STATUS_HEIGHT;
        refresh();
    }

    /**
     * 添加悬浮窗或者更新悬浮窗 如果悬浮窗还没添加则添加 如果已经添加则更新其位置
     */
    private void refresh() {
        if (viewAdded) {
            windowManager.updateViewLayout(view, layoutParams);
        } else {
            windowManager.addView(view, layoutParams);
            viewAdded = true;
        }
    }

    /**
     * 关闭悬浮窗
     */
    public void removeView() {
        if (viewAdded) {
            windowManager.removeView(view);
            viewAdded = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            notifiti_mgr.cancel(200);
            removeView();
            location_mgr.removeUpdates(locationListener);
            if (mtimer != null) {
                mtimer.cancel();
            }
            if (mh != null) {
                mh.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startMockLocation() {
        try {
            location_mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            provider2 = LocationManager.GPS_PROVIDER;//假位置提供者  以下為假位置必要條件  缺一不可
            mylocation = new Location(provider2);
            mylocation.setLatitude(mlat);
            mylocation.setLongitude(mlot);
            mylocation.setTime(System.currentTimeMillis());
            mylocation.setAccuracy(0.1f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {  //如果使用者的版本大於我的最低版本
                mylocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            if (!status) {
                location_mgr.addTestProvider(provider2, false, true, false, false, false, false, false, Criteria.POWER_LOW, Criteria.ACCURACY_FINE); //增加一個假位置的提供者
                location_mgr.setTestProviderEnabled(provider2, true); //假位置啟用
                status = true;
            }
            location_mgr.setTestProviderLocation(provider2, mylocation);
            location_mgr.setTestProviderStatus(provider2, 100, null, System.currentTimeMillis());

            broadcastIntent.putExtra(getString(R.string.lat), mylocation.getLatitude());
            broadcastIntent.putExtra(getString(R.string.lot), mylocation.getLongitude());
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
        startForeground(200, noti);
        notifiti_mgr.notify(200, noti);
    }
}
