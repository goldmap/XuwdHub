package com.xuwd.jmap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    public LatLng latLng;

    public MyLocationListener listener=new MyLocationListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        Button btnPin=findViewById(R.id.btnMapPin);
        btnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              fixMap();
            }
        });

        mapView=findViewById(R.id.mapView);
        positionText=findViewById(R.id.positionTextView);

        initMap();
    }

    @Override
    protected void onStart() {
        locationClient.start();
        super.onStart();
    }

    @Override
    protected void onPause() {
        locationClient.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        locationClient.unRegisterLocationListener(listener);
        //取消位置提醒
        //locationClient.removeNotifyEvent(notifyListener);
        locationClient.stop();
    }

    private void initMap() {
        baiduMap = mapView.getMap();
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
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

        MapStatus.Builder mapStatusBuilder = new MapStatus.Builder();
        mapStatusBuilder.target(latLng).zoom(20.0f);
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatusBuilder.build()));

        initLocation();
    }

    private void initLocation() {
        //自定义图标
        Bitmap bmp=BitmapFactory.decodeResource(getResources(),R.drawable.pin02);
        bmp=fixBitmap(bmp,32,32);
        mCurrentMarker = BitmapDescriptorFactory.fromBitmap(bmp);

        MyLocationConfiguration configuration =new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
        baiduMap.setMyLocationConfiguration(configuration);
        baiduMap.setMyLocationEnabled(true);

        //定位管理、配置
        locationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        locationClient.registerLocationListener(listener);    //注册监听函数

        LocationClientOption option=new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(5000);//表示每5秒更新一下当前位置
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);

        locationClient.start();
    }


    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            float radius=location.getRadius();
            if(location==null){
                Toast.makeText(getBaseContext(),"XXX",Toast.LENGTH_SHORT).show();
                return;
            }
            if(location.getLocType()==BDLocation.TypeNetWorkLocation){
                positionText.setText(location.getAddrStr());
            }
            fixMap();
        }
    }
    private void fixMap(){
        //为标志设置坐标
        MyLocationData locationData = new MyLocationData.Builder().latitude(latLng.latitude)
                .longitude(latLng.longitude).build();
        baiduMap.setMyLocationData(locationData);

        Toast.makeText(getBaseContext(),"YYY",Toast.LENGTH_SHORT).show();
        positionText.setText(latLng.toString()+", Zoom:"+baiduMap.getMapStatus().zoom);

        //if (isFirstLocate) {
        isFirstLocate = false;
        //设置地图中心坐标，并依照其显示地图
        MapStatus.Builder mapStatusBuilder = new MapStatus.Builder();
        mapStatusBuilder.target(latLng);
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatusBuilder.build()));
        //}
    }

    private Bitmap fixBitmap(Bitmap bmpOrigin,int nWidth,int nHeight){
        if(bmpOrigin==null){
            return null;
        }
        int bmpWidth=bmpOrigin.getWidth();
        int bmpHeight=bmpOrigin.getHeight();
        float scaleWidth=(float) nWidth/bmpWidth;
        float scaleHeight=(float) nHeight/bmpHeight;

        Matrix matrix=new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);

        Bitmap bmp=Bitmap.createBitmap(bmpOrigin,0,0,bmpWidth,bmpHeight,matrix,false);
        if(!bmpOrigin.isRecycled()){
            bmpOrigin.recycle();
        }

        return bmp;
    }

}
