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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;

public class MapActivity extends AppCompatActivity {
    public LocationClient locationClient;
    private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    BitmapDescriptor mCurrentMarker;
    private float radius;

    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    private boolean isFirstLocate=true;
    private  MyLocationData locationData;
    public LatLng latLng=new LatLng(23,114);

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
        //locationClient.start();
        super.onStart();
    }

    @Override
    protected void onPause() {
        //locationClient.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // mapView.onDestroy();
        //locationClient.unRegisterLocationListener(listener);
        //取消位置提醒
        //locationClient.removeNotifyEvent(notifyListener);
        //locationClient.stop();
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
        /*
        Bitmap bmp=BitmapFactory.decodeResource(getResources(),R.drawable.pin02);
        bmp=fixBitmap(bmp,32,32);
        mCurrentMarker = BitmapDescriptorFactory.fromBitmap(bmp);
*/
        //MyLocationConfiguration configuration =new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
        //MyLocationConfiguration configuration =new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
        //baiduMap.setMyLocationConfiguration(configuration);
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
            radius=location.getRadius();
            if(location==null){
                Toast.makeText(getBaseContext(),"XXX",Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getBaseContext(),"YYY",Toast.LENGTH_SHORT).show();
            if(location.getLocType()==BDLocation.TypeNetWorkLocation){
                positionText.setText(location.getAddrStr());
            }
            //fixMap();
        }
    }
    private void fixMap(){
        //为标志设置坐标
        MyLocationData locationData = new MyLocationData.Builder().latitude(latLng.latitude)
                .longitude(latLng.longitude)
                .accuracy(radius).build();
        baiduMap.setMyLocationData(locationData);


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
    class MyOverLay extends Overlay {

        private Location location;
        public void setLocation(Location location){
            this.location=location;
        }

        @Override
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
                            long when) {
            // TODO Auto-generated method stub
            super.draw(canvas, mapView, shadow);
            Paint paint=new Paint();
            Point myScreen=new Point();
            //将经纬度换成实际屏幕的坐标。
            GeoPoint geoPoint=new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
            mapView.getProjection().toPixels(geoPoint, myScreen);
            paint.setStrokeWidth(1);
            paint.setARGB(255, 255, 0, 0);
            paint.setStyle(Paint.Style.STROKE);
            Bitmap bmp=BitmapFactory.decodeResource(getResources(), R.drawable.mypicture);
            //把这张图片画到相应的位置。
            canvas.drawBitmap(bmp, myScreen.x, myScreen.y,paint);
            canvas.drawText("天堂没有路", myScreen.x, myScreen.y, paint);
            return true;

        }
    }
    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("你确定退出吗？")
                    .setCancelable(false)
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    MapActivity.this.finish();
                                    android.os.Process
                                            .killProcess(android.os.Process
                                                    .myPid());
                                    android.os.Process.killProcess(android.os.Process.myTid());
                                    android.os.Process.killProcess(android.os.Process.myUid());
                                }
                            })
                    .setNegativeButton("返回",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
