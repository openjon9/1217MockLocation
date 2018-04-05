package com.example.a123.a1217mocklocation;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView latlotTextView, text_lat, text_lot, text_point;
    private LocationManager location_mgr;
    private String provider, provider2;
    private Location location;
    private EditText et_lat, et_lot, et_point;
    private ListView listview;
    private int mock_permission;
    private MyBroadcaseReceiver receiver;
    private myHendler myhandler;
    private double mock_lat, mock_lot;
    private MyDB dbhelper;
    private static final String DB_NAME = "MyDB";
    private SQLiteDatabase db;
    private String tableName = "mocklocation";
    List<Map<String, Object>> list2;
    Map<String, Object> map;
    PopupMenu popupMenu;
    private Location mlocation;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        permission();
        location_mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = LocationManager.GPS_PROVIDER;
        try {
            licationlistener();
        }catch (Exception e){

        }

        myhandler = new myHendler();
        list2 = new ArrayList<>();
        initlistview();
    }

    private boolean licationlistener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        location_mgr.requestLocationUpdates(provider, 5000, 1, locationListener);
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        location_mgr.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            licationlistener();
        }catch (Exception e){

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMockGPS();
        myhandler.removeCallbacksAndMessages(null);
        location_mgr.removeUpdates(locationListener);
        db.close();
        dbhelper.close();
    }

    private boolean permission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return false;
        }
        return true;
    }

    private void findView() {
        latlotTextView = (TextView) findViewById(R.id.latlotTextView);
        text_point = (TextView) findViewById(R.id.text_point);
        text_lat = (TextView) findViewById(R.id.text_lat);
        text_lot = (TextView) findViewById(R.id.text_lot);
        et_lat = (EditText) findViewById(R.id.et_lat);
        et_lot = (EditText) findViewById(R.id.et_lot);
        et_point = (EditText) findViewById(R.id.et_point);
        listview = (ListView) findViewById(R.id.listview);
    }

    public void start(View view) {
        if (permission()) {
            startMockGPS();
        }
    }

    public void stop(View view) {
        stopMockGPS();
        myhandler.sendEmptyMessage(2);
    }

    public void save(View view) {
        insertSQLite();
        initlistview();
    }

    public void clean(View view) {
        clean_et();
    }

    private void clean_et() {
        et_point.setText("");
        et_lat.setText("");
        et_lot.setText("");
    }

    public void mlociationtext(View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            mlocation = location_mgr.getLastKnownLocation(provider);
            latlotTextView.setText(String.format("%.6f%n%.6f", mlocation.getLatitude(), mlocation.getLongitude()));
        } catch (Exception e) {
            Log.i("AA", e.getMessage().toString());
            latlotTextView.setText("");
            Toast.makeText(this, "無法獲取座標", Toast.LENGTH_SHORT).show();
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mlocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            getLastLocation(provider);
        }

        private void getLastLocation(String provider) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location_mgr.getLastKnownLocation(provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            getLastLocation(null);
        }
    };

    private class MyBroadcaseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mock_permission = intent.getIntExtra(getString(R.string.mock), -1);
            if (mock_permission == 13) {
                myhandler.sendEmptyMessage(13);
            } else {
                mock_lat = intent.getDoubleExtra(getString(R.string.lat), -1);
                mock_lot = intent.getDoubleExtra(getString(R.string.lot), -1);
                myhandler.sendEmptyMessage(1);
            }
        }
    }

    private void startMockGPS() {
        if (et_lat() == null || et_lot() == null) {
            return;
        }
        if (receiver == null) {
            receiver = new MyBroadcaseReceiver();
        }
        registerReceiver(receiver, new IntentFilter("check")); //廣播 接收傳回的質改變UI狀態
        Intent intent = new Intent(this, MyService.class); //開啟背景服務 由背景服務新增加位置
        intent.putExtra(getString(R.string.mocklat), et_lat());
        intent.putExtra(getString(R.string.mocklot), et_lot());
        startService(intent);
    }

    private void stopMockGPS() {
        try {
            Intent intent = new Intent(this, MyService.class);
            stopService(intent); //停止背景服務
            unregisterReceiver(receiver); //停止廣播
            location_mgr.removeTestProvider(LocationManager.GPS_PROVIDER); //解除假GPS
        } catch (Exception ex) {

        }
    }

    private class myHendler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    text_point.setText(point());
                    text_lat.setText(String.valueOf(mock_lat));
                    text_lot.setText(String.valueOf(mock_lot));
                    break;
                case 2:
                    text_point.setText("");
                    text_lat.setText("");
                    text_lot.setText("");
                    break;
                case 13:
                    Development_Settings();
                    break;
            }
        }
    }

    private void Development_Settings() {
        new AlertDialog.Builder(this)
                .setTitle("開發者模擬定位裝置未設定")
                .setMessage("點擊確定將引導去開發者人員頁面")
                .setPositiveButton(R.string.確定, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                        finish();
                    }
                })
                .setNegativeButton(R.string.取消, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void initlistview() {
        list2.clear();
        querySQLite();
        listview.setAdapter(new SimpleAdapter(this, list2, R.layout.mylistview, new String[]{getString(R.string.markpoint)}, new int[]{R.id.list_view}));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                et_point.setText(list2.get(position).get(getString(R.string.markpoint)).toString());
                et_lat.setText(list2.get(position).get(getString(R.string.lat)).toString());
                et_lot.setText(list2.get(position).get(getString(R.string.lot)).toString());
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {

                popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.mymenu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getOrder()) {
                            case 1:
                                update_dialog(position);
                                break;
                            case 2:
                                delete_dialog(position);
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;//放開不會觸發普通點擊
            }
        });
    }

    private void update_dialog(final int position) {
        View myview = getLayoutInflater().inflate(R.layout.update_layout, null);
        final EditText dialog_point = (EditText) myview.findViewById(R.id.dialog_point);
        final EditText dialog_lat = (EditText) myview.findViewById(R.id.dialog_lat);
        final EditText dialog_lot = (EditText) myview.findViewById(R.id.dialog_lot);
        final String date[] = {list2.get(position).get(getString(R.string._id)).toString(), list2.get(position).get(getString(R.string.markpoint)).toString(), list2.get(position).get("lat").toString(), list2.get(position).get("lot").toString()};

        dialog_point.setText(date[1]);
        dialog_lat.setText(date[2]);
        dialog_lot.setText(date[3]);

        new AlertDialog.Builder(MainActivity.this)
                .setView(myview)
                .setPositiveButton(R.string.確定, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updatedb(mdialog_point(dialog_point), mdialog_lat(dialog_lat), mdialog_lot(dialog_lot), date);
                        initet_view(position);
                    }
                })
                .setNegativeButton(R.string.取消, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void updatedb(String dialogPoint, Double dialog_lat, Double dialog_lot, String[] date) {
        if (dialogPoint == null || dialog_lat == null || dialog_lot == null) {
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(getString(R.string.markpoint), dialogPoint);
        contentValues.put(getString(R.string.lat), dialog_lat);
        contentValues.put(getString(R.string.lot), dialog_lot);
        int rows = db.update(tableName, contentValues, "_id=?", new String[]{date[0]});
        Toast.makeText(this, rows + "筆資料修改完成", Toast.LENGTH_SHORT).show();
        initlistview();

    }

    private void initet_view(int position) {
        et_point.setText(list2.get(position).get(getString(R.string.markpoint)).toString());
        et_lat.setText(list2.get(position).get(getString(R.string.lat)).toString());
        et_lot.setText(list2.get(position).get(getString(R.string.lot)).toString());
    }

    private void delete_dialog(final int position) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("確定刪除" + list2.get(position).get(getString(R.string.markpoint)) + "?")
                .setPositiveButton(R.string.確定, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deletedb(list2.get(position).get(getString(R.string._id)).toString());
                        clean_et();
                    }
                })
                .setNegativeButton(R.string.取消, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void deletedb(String id) {
        int row = db.delete(tableName, "_id=?", new String[]{id});
        Toast.makeText(this, row + "筆資料已刪除", Toast.LENGTH_SHORT).show();
        initlistview();
    }

    private void querySQLite() {
        dbhelper = new MyDB(this, DB_NAME, null, 1);
        db = dbhelper.getWritableDatabase();
        Cursor cursor = db.query(tableName, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            map = new HashMap<>();
            map.put(getString(R.string._id), cursor.getInt(0));
            map.put(getString(R.string.markpoint), cursor.getString(1));
            map.put(getString(R.string.lat), cursor.getString(2));
            map.put(getString(R.string.lot), cursor.getString(3));
            list2.add(map);
        }
        cursor.close();
    }

    private void insertSQLite() {
        if (point() == null || et_lat() == null || et_lot() == null) {
            return;
        }
        dbhelper = new MyDB(this, DB_NAME, null, 1);
        db = dbhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(getString(R.string.markpoint), point());
        contentValues.put(getString(R.string.lat), String.valueOf(et_lat()));
        contentValues.put(getString(R.string.lot), String.valueOf(et_lot()));

        db.insert(tableName, null, contentValues);
        Toast.makeText(this, R.string.新增一筆資料, Toast.LENGTH_SHORT).show();
    }

    private Double et_lat() {
        if (et_lat.getText().toString().equals("")) {
            Toast.makeText(this, R.string.格式錯誤不能空白, Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            Double mlat = Double.valueOf(et_lat.getText().toString());
            return mlat;
        } catch (Exception e) {
            Toast.makeText(this, R.string.格式錯誤請輸入數字, Toast.LENGTH_SHORT).show();
        }
        return null;

    }

    private Double et_lot() {
        if (et_lot.getText().toString().equals("")) {
            Toast.makeText(this, R.string.格式錯誤不能空白, Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            Double mlot = Double.valueOf(et_lot.getText().toString());
            return mlot;
        } catch (Exception e) {
            Toast.makeText(this, R.string.格式錯誤請輸入數字, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private String point() {
        if (et_point.getText().toString().equals("")) {
            Toast.makeText(this, R.string.格式錯誤不能空白, Toast.LENGTH_SHORT).show();
            return null;
        }
        String mpoint = et_point.getText().toString();
        return mpoint;
    }

    private String mdialog_point(EditText dialog_point) {
        if (dialog_point.getText().toString().equals("")) {
            Toast.makeText(this, R.string.格式錯誤不能空白, Toast.LENGTH_SHORT).show();
            return null;
        }
        String npoint = dialog_point.getText().toString();
        return npoint;
    }

    private Double mdialog_lat(EditText dialog_lat) {
        if (dialog_lat.getText().toString().equals("")) {
            Toast.makeText(this, R.string.格式錯誤不能空白, Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            double nlat = Double.parseDouble(dialog_lat.getText().toString());
            return nlat;
        } catch (Exception e) {
            Toast.makeText(this, R.string.格式錯誤請輸入數字, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private Double mdialog_lot(EditText dialog_lot) {
        if (dialog_lot.getText().toString().equals("")) {
            Toast.makeText(this, R.string.格式錯誤不能空白, Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            double nlot = Double.parseDouble(dialog_lot.getText().toString());
            return nlot;
        } catch (Exception e) {
            Toast.makeText(this, R.string.格式錯誤請輸入數字, Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}
