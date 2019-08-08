package com.xuwd.jmap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MapActivity extends AppCompatActivity {
    public LocationClient locationClient;
    private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    private boolean isFirstLocate=true;
    private  MyLocationData locationData;

    public MyLocationListener listener=new MyLocationListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        mapView=findViewById(R.id.mapView);
        positionText=findViewById(R.id.positionTextView);

        initMap();
    }

    private void initMap() {
        //获取地图控件引用
        baiduMap = mapView.getMap();
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //普通地图
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
        baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker,
                accuracyCircleFillColor, accuracyCircleStrokeColor));

        locationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        locationClient.registerLocationListener(listener);    //注册监听函数
        initLocation();
    }

    private void initLocation() {
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(5000);//表示每5秒更新一下当前位置
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        locationClient.setLocOption(option);
        locationClient.start();
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if(location==null){
                Toast.makeText(getBaseContext(),"XXX",Toast.LENGTH_SHORT).show();
                return;
            }

            MyLocationData locationData = new MyLocationData.Builder().latitude(latLng.latitude)
                    .longitude(latLng.longitude)
                    .accuracy(location.getRadius()).build();
            Toast.makeText(getBaseContext(),"YYY",Toast.LENGTH_SHORT).show();

            //if (isFirstLocate) {
                isFirstLocate = false;
                MapStatus.Builder mapStatusBuilder = new MapStatus.Builder();
                mapStatusBuilder.target(latLng).zoom(18.0f);
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatusBuilder.build()));
            //}
        }
    }

}
