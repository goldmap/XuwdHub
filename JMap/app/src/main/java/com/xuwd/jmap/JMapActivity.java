package com.xuwd.jmap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.opengl.GLES20;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.animation.Animation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;

import javax.microedition.khronos.opengles.GL10;

public class JMapActivity extends Activity {
    private Marker oMarker;
    public LocationClient locationClient;
    //private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    BitmapDescriptor mCurrentMarker;
    private float radius;

    private boolean isFirstLocate=true;
    private  MyLocationData locationData;
    public LatLng latLng;

    public MyLocationListener listener=new MyLocationListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        initView();
        initMap();
    }
    private void initView(){
        mapView=findViewById(R.id.mapView);
        baiduMap = mapView.getMap();

        RadioGroup rdMapType=findViewById(R.id.radioMapType);
        rdMapType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                if(baiduMap==null)
                    initMap();

                if(checkId==R.id.radioNomalMap){
                    baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }else{
                    baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }
            }
        });

        CheckBox chkBoxTraffic=findViewById(R.id.chkBoxTraffic);
        chkBoxTraffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                baiduMap.setTrafficEnabled(checked);
            }
        });

        Button btnPin=findViewById(R.id.btnMapPin);
        btnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fixMap();
            }
        });

    }

    private void initMap() {
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setMyLocationEnabled(true);

        MapStatus.Builder mapStatusBuilder = new MapStatus.Builder();
        mapStatusBuilder.zoom(19.0f);
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatusBuilder.build()));
        baiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                oMarker.setPosition(mapStatus.target);
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                oMarker.setPosition(mapStatus.target);
            }
        });

        initLocation();

    }

    private void initLocation() {
        baiduMap.setMyLocationEnabled(true);

        //定位管理、配置
        locationClient = new LocationClient(getApplicationContext());     //声明LocationClient类

        LocationClientOption option=new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.setScanSpan(5000);//表示每5秒更新一下当前位置
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(listener);    //注册监听函数

        //自定义图标
        //MyLocationConfiguration configuration =new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
        Bitmap bmp=BitmapFactory.decodeResource(getResources(),R.drawable.pin04);
        bmp=fixBitmap(bmp,48,48);
        mCurrentMarker = BitmapDescriptorFactory.fromBitmap(bmp);
        MyLocationConfiguration locationConfiguration=new MyLocationConfiguration( MyLocationConfiguration.LocationMode.FOLLOWING,true,mCurrentMarker,0xAAFFFF88,0xAA00FF00);
        baiduMap.setMyLocationConfiguration(locationConfiguration);

        locationClient.start();
    }


    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            radius=location.getRadius();
            if(location==null || mapView==null){
                Toast.makeText(getBaseContext(),"XXX",Toast.LENGTH_SHORT).show();
                return;
            }
            //Toast.makeText(getBaseContext(),"YYY",Toast.LENGTH_SHORT).show();

            if(isFirstLocate){
                Bitmap bmp=BitmapFactory.decodeResource(getResources(),R.drawable.pin07);
                bmp=fixBitmap(bmp,64,64);
                OverlayOptions ooA=new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                        .animateType(MarkerOptions.MarkerAnimateType.jump).draggable(true);
                oMarker=(Marker) baiduMap.addOverlay(ooA);

                isFirstLocate=false;
                fixMap();
                /*
                OverlayOptions textOption= new TextOptions()
                        .text("gggggggggggggggHHHH").bgColor(0xAAFFFF00).fontSize(24) //字号
                        .fontColor(0xFFFF00FF).position(latLng);
                baiduMap.addOverlay(textOption);

                Button button=new Button(getApplicationContext());
                button.setBackgroundResource(R.drawable.pin07);
                baiduMap.showInfoWindow(new InfoWindow(button,latLng,-100));
                */
            }
        }
    }
    private void fixMap(){
        //为标志设置坐标
        MyLocationData locationData = new MyLocationData.Builder().latitude(latLng.latitude)
                .longitude(latLng.longitude)
                .accuracy(radius)
                .build();
        baiduMap.setMyLocationData(locationData);

        //设置地图中心坐标，并依照其显示地图
//        MapStatus.Builder mapStatusBuilder = new MapStatus.Builder();
//        mapStatusBuilder.target(latLng);
//        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatusBuilder.build()));
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
    class MyOverLay extends Overlay {


    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onStart() {
        locationClient.start();
        super.onStart();
    }

    @Override
    protected void onPause() {
        locationClient.stop();
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        locationClient.stop();
        //locationClient.unRegisterLocationListener(listener);
        baiduMap.setMyLocationEnabled(false);
         mapView.onDestroy();
         mapView=null;
        //取消位置提醒
        //locationClient.removeNotifyEvent(notifyListener);
        super.onDestroy();
    }

}