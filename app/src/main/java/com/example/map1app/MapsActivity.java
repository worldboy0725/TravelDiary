package com.example.map1app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.map1app.FirstPage.LOC_DIST;
import static com.example.map1app.FirstPage.LOC_TIME;
import static com.example.map1app.FirstPage.LOC_ZOOM;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager mLocationMgr;
//    private OnLocationChangedListener mLocationChangedListener;
    private SupportMapFragment supportMapFragment, mapFragment;
    private UiSettings mUiSettings;
    private TextView mTextView;

//    private Location location;   // 系統使用儲存定位座標的變數 location (不可設定在這裡，要用呼叫方式)
    private Location mLocationChanged;
    private Marker mMarker;
    private Marker mMarkerChanged;
    private Polyline mPolylineRoute;
    private ShowAddress mShowAddress;

    private ArrayList<Location> mLocations;
    private ArrayList<Marker> mMarkers;
    private ArrayList<String> mIDs;
    private ArrayList<LatLng> mListLatLng;

    private boolean mbIsZoomFirst = true;

    // 給選到的地標 marker 一個編號
    private String mSelectedStringId;
    // 最新一個的地標 marker 的編號
    private String mId;
    private String title = "地標";
    private String snippet = "地標說明";

    private int mLocUpdatesTime,
            mLocUpdatesDistance;
    private float zoom;

    private String bestProv;

    public static final String KEYID = "mId";
    public static final String KEY1 = "Time";
    public static final String KEY2 = "Title";
    public static final String KEY3 = "Snippet";

    // 經緯度要用","隔開，而且不能有空白
    private static String[] mSelectLocations = {
            "25.048103638757308,121.51696082983375",
            "25.04936915558993,121.57827851329795",
            "25.13279481538982,121.73786679638567",
            "25.0932741762734,121.71401286835268",
            "25.01510167222574,121.46353194184188",
            "25.108871593938005,121.80592628184397",
            "24.989704788612578,121.31416824977194",
            "24.802171801537916,120.97171699573788",
            "24.570786406996884,120.8224823456589",
            "24.136529626011253,120.68287536989357",
            "24.082147924041077,120.53868512618212",
            "23.47938437919122,120.44103090879507",
            "23.712072915760064,120.54100110879932",
            "22.9976585721211,120.21265283485963",
            "22.639571923446763,120.30247602412554",
            "22.669075685118536,120.48608362598068",
            "22.793978795295306,121.12305021063733",
            "23.992607906636998,121.60132928367726",
            "24.754799271292313,121.75801541067344",
    };
    private Spinner spnLocation, spnMapType;
    private Button btnPosition, btnAddMarker, btnShowRoute, btnEditMkr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // 利用 getSupportFragmentManager() 方法取得管理器
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        // 以非同步方式取得 GoogleMap 物件
        mapFragment.getMapAsync(this);

        spnLocation = findViewById(R.id.spnLocation);
        spnMapType = findViewById(R.id.spnMapType);

        // 設定 spnGeoPoint 元件 ItemSelected 事件的 listener
        spnLocation.setOnItemSelectedListener(spnLocationOnItemSelected);
        spnMapType.setOnItemSelectedListener(spnMapTypeOnItemSelected);

        btnPosition = findViewById(R.id.btnPosition);
        btnPosition.setOnClickListener(btnPositionOnClick);

        btnAddMarker = findViewById(R.id.btnAddMarker);
        btnAddMarker.setOnClickListener(btnAddMarkerOnClick);

        btnShowRoute = findViewById(R.id.btnShowRoute);
        btnShowRoute.setOnClickListener(btnShowRouteOnClick);

        btnEditMkr = findViewById(R.id.btnEditMkr);
        btnEditMkr.setOnClickListener(btnEditMkrOnClick);

        // 儲存 mLocations 資料用
        mLocations = new ArrayList<>();

        // 取得 FirstPage 的 intent資料
        mLocUpdatesTime = getIntent().getIntExtra(LOC_TIME,5000);
        mLocUpdatesDistance = getIntent().getIntExtra(LOC_DIST,5);
        zoom = getIntent().getFloatExtra(LOC_ZOOM, 16);

        if (mLocUpdatesTime <= 999 || mLocUpdatesTime > 1000000) mLocUpdatesTime = 5000;
        if (mLocUpdatesDistance < 1 || mLocUpdatesDistance > 10000) mLocUpdatesDistance = 5;
        if (zoom < 1 || zoom > 21) zoom = 16;    // 設定放大倍率1(地球)-21(街景)

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;         // 取得 GoogleMap 物件
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setAllGesturesEnabled(true);
        mUiSettings.setMapToolbarEnabled(true);
        mMap.setIndoorEnabled(true);

        // 檢查授權
        requestPermission();

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {

                View v;
                TextView txtTitle, txtSnippet;

                v = getLayoutInflater().inflate(R.layout.map_info_window, null);
                txtTitle = v.findViewById(R.id.txtTitle);
                txtTitle.setText(marker.getTitle());
                txtSnippet = v.findViewById(R.id.txtSnippet);
                txtSnippet.setText(marker.getSnippet());
                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {


            @Override
            public void onInfoWindowClick(Marker marker) {

                mSelectedStringId = marker.getId();
                marker.hideInfoWindow();
            }
        });

    }

    // 檢查授權，要先定義 "精確定位(GPS)" 的請求代碼
    private void requestPermission() {

        // 判斷是否已取得授權
        int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);   // 判斷目前定位狀態是否與 "精確定位(GPS)"的請求碼相同
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {  // 未取得授權
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);   // 這裡定義了 "精確定位(GPS)" 的請求碼 = 1
            return;
        }

        setMyLocation(); //  顯示定位圖層(要在模擬器上執行必須加入)

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // "精確定位(GPS)" 的請求碼 = 1
        // 這裡會顯示是否允許裝置使用定位功能，"僅在使用該應用程式時允許","只有這一次允許","拒絕"
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // 按"允許" -> (包括2種情況 "僅在使用該應用程式時允許","只有這一次允許")
                setMyLocation(); //  顯示定位圖層(要在模擬器上執行必須加入)

            } else {  //按 拒絕 鈕
                Toast.makeText(this, "未取得授權！", Toast.LENGTH_SHORT).show();
                finish();  // 結束 this.Activity (結束目前的 Activity)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //  顯示定位圖層(要在模擬器上執行必須加入)
    private void setMyLocation() throws SecurityException {
        mMap.setMyLocationEnabled(true);  // 顯示定位圖層(要在模擬器上執行必須加入)

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        // 取得地圖座標值:緯度,經度,地址
        String x = "緯度:" + location.getLatitude();
        String y = "經度:" + location.getLongitude();
        // 移動地圖到新位置。
        LatLng Point = new LatLng(location.getLatitude(), location.getLongitude());
//        zoom = 16; //設定放大倍率1(地球)-21(街景)
        // 移動地圖到新位置。
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Point, zoom));
        ShowAddress showAddress = new ShowAddress();
        String addr = showAddress.getAddrList(MapsActivity.this, location.getLatitude(), location.getLongitude()).getPostalCode() + showAddress.getAddrList(MapsActivity.this, location.getLatitude(), location.getLongitude()).getAddressLine(0).replaceAll("\\d+台灣", " ");
//        Toast.makeText(this, x + "," + y + "\n" + addr, Toast.LENGTH_LONG).show();
        mTextView = findViewById(R.id.txtView);
        mTextView.setText(addr);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 取得最佳定位
        Criteria criteria = new Criteria();
        bestProv = mLocationMgr.getBestProvider(criteria, true);

        // 如果GPS或網路定位開啟，更新位置
        if (mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //  確認 ACCESS_FINE_LOCATION 權限是否授權
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // 這個是 事件的更新條件設定。
                mLocationMgr.requestLocationUpdates(bestProv, mLocUpdatesTime, mLocUpdatesDistance, this);
            }
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //  確認 ACCESS_FINE_LOCATION 權限是否授權
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationMgr.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Criteria criteria = new Criteria();
        bestProv = mLocationMgr.getBestProvider(criteria, true);
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }


    private AdapterView.OnItemSelectedListener spnLocationOnItemSelected = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            String[] sLocation;
            double dLat, dLon;

            // 將選單 String[] mSelectLocations 的第 i項(每一筆資料)切成 緯度,經度 座標
            sLocation = mSelectLocations[i].split(",");
            dLat = Double.parseDouble(sLocation[0]);    // 緯度
            dLon = Double.parseDouble(sLocation[1]);    // 經度

            // 如果是第一次執行，把地圖放大到設定的等級。
            if (mbIsZoomFirst) {
                mbIsZoomFirst = false;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(dLat, dLon), zoom));
            } else
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(dLat, dLon), zoom));

            // 顯示地址
            ShowAddress showAddress;
            showAddress = new ShowAddress();
            String addr = showAddress.getAddrList(MapsActivity.this, dLat, dLon).getPostalCode() + showAddress.getAddrList(MapsActivity.this, dLat, dLon).getAddressLine(0).replaceAll("\\d+台灣", " ");
//        Toast.makeText(this, dLat + "," + dLon + "\n" + addr, Toast.LENGTH_LONG).show();
            mTextView = findViewById(R.id.txtView);
            mTextView.setText(addr);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private AdapterView.OnItemSelectedListener spnMapTypeOnItemSelected =
            new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView parent,
                                           View v, int position, long id) {
                    // 依照使用者點選的項目位置，改變地圖模式。
                    switch (position) {
                        case 0:
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            break;
                        case 1:
                            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            break;
                        case 2:
                            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                            break;
                        case 3:
                            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView parent) {
                }
            };

    private Date getTime() {
        Date time;
        SimpleDateFormat df;
        String timeStr;

        time = new Date(System.currentTimeMillis());
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        timeStr = df.format(time);
        time = Timestamp.valueOf(timeStr);
        return time;
    }

    // 定位位置 Button
    private View.OnClickListener btnPositionOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);   // 這個參數超重要 !

            LatLng Point = new LatLng(location.getLatitude(), location.getLongitude());
//            zoom = 16; //設定放大倍率1(地球)-21(街景)
            // 移動地圖到新位置。
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Point, zoom));

            TextView txtView;
            String latLngString = "";
            double lat, lng;
            float spd, getAcc;

            mShowAddress = new ShowAddress();
            txtView = findViewById(R.id.txtView);

            // 在 txtView 顯示定位座標
            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                spd = location.getSpeed();
                getAcc = location.getAccuracy();

                latLngString = mShowAddress.getAddrList(MapsActivity.this,lat,lng).getPostalCode() + mShowAddress.getAddrList(MapsActivity.this,lat,lng).getAddressLine(0).replaceAll("\\d+台灣"," ") + ", 緯度:" + lat + ", 經度:" + lng + ", 時間:" + getTime() + ", 速度:" + spd + ", 定位精準度:" + getAcc;
            }
            txtView.setText(latLngString);

        }
    };

    // 顯示地標 Button
    private View.OnClickListener btnAddMarkerOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            String addr, mkLocation, title;
            mShowAddress = new ShowAddress();

            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);   // 這個參數超重要 !

            // 檢查：點了定位按鈕，卻沒定位成功
            if (location == null){
                return;
            }

            // 檢查是否重複加入地標，這裡不允許加入相同經緯度的地標
            if (mLocations.size() > 0) {
                int n = mLocations.size() - 1;
                if (Double.parseDouble(String.valueOf(location.getLatitude())) == Double.parseDouble(String.valueOf(mLocations.get(n).getLatitude())) && (Double.parseDouble(String.valueOf(location.getLongitude())) == Double.parseDouble(String.valueOf((mLocations.get(n).getLongitude()))))) {
                    Toast.makeText(MapsActivity.this, "不可重複加入地標，請先移動後再加入", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            title = mShowAddress.getAddrList(MapsActivity.this, location.getLatitude(), location.getLongitude()).getThoroughfare();
            if (title == null) {
                title = mShowAddress.getAddrList(MapsActivity.this, location.getLatitude(), location.getLongitude()).getLocality();
            }
            addr = mShowAddress.getAddrList(MapsActivity.this, location.getLatitude(), location.getLongitude()).getPostalCode() + mShowAddress.getAddrList(MapsActivity.this, location.getLatitude(), location.getLongitude()).getAddressLine(0).replaceAll("\\d+台灣", " ");
            snippet = getTime() + "\n" + addr;

            // 儲存地標到集合
            mMarkers = new ArrayList<>();
            mMarker = mMap.addMarker(new MarkerOptions()
                    .title(title).snippet(snippet)
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_r))
//                    .draggable(false)
                    .anchor(0.5f,0.95f));
            mMarkers.add(mMarker);
            mLocations.add(location);
//            mId = mMarker.getId();

            // 重新更新所有 marker 的 infoWindow
            for (int i=0; i< mMarkers.size(); i++) {
                mMarker.setTitle(mMarkers.get(i).getTitle());
                mMarker.setSnippet(mMarkers.get(i).getSnippet());
            }
            // 緯度,經度座標
            mkLocation = location.getLatitude() + "," + location.getLongitude();
            Log.d("InfoClick",getTime().toString() + title + snippet);

            Intent intent = new Intent();
            intent.setClass(MapsActivity.this, EditPage.class);  // 這是切畫面
            title = mMarker.getTitle();
            // 以下是帶資料
            intent.putExtra(KEYID, mkLocation);
            intent.putExtra(KEY1, getTime().toString());
            intent.putExtra(KEY2, title);
            intent.putExtra(KEY3, addr);
            MapsActivity.this.startActivity(intent);
        }
    };


    // 顯示路徑 Button
    private View.OnClickListener btnShowRouteOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            String latLngString;
            latLngString = "";

            mTextView = findViewById(R.id.txtView);

            for (int i = 0; i< mLocations.size(); i++) {
                latLngString +=
                        "緯度:" + mLocations.get(i).getLatitude() + ", 經度:" + mLocations.get(i).getLongitude() + "\n";
            }
            mTextView.setText(latLngString);

            // 建立Polyline
            PolylineOptions polylineOpt = new PolylineOptions()
                    .width(15)
                    .color(Color.BLUE);

            mListLatLng = new ArrayList<LatLng>();

            for (int i = 0; i < mLocations.size(); i++) {
                mListLatLng.add(new LatLng(mLocations.get(i).getLatitude(), mLocations.get(i).getLongitude()));
            }

            polylineOpt.addAll(mListLatLng);

            // 儲存路徑到集合
            mPolylineRoute = mMap.addPolyline(polylineOpt);
            // 顯示路徑
            mPolylineRoute.setVisible(true);
        }
    };

    // 編輯地標資料/日記
    private View.OnClickListener btnEditMkrOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent();
            intent.setClass(MapsActivity.this, RecordPage.class);  // 這是切畫面
            MapsActivity.this.startActivity(intent);

        }
    };

}

