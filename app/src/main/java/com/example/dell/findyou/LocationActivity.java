package com.example.dell.findyou;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.model.NaviLatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocationActivity extends Activity implements LocationSource, AMapLocationListener {


    @BindView(R.id.map) MapView mMapView;
    AMap aMap;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient                       mlocationClient;
    private AMapLocationClientOption                 mLocationOption;
    @BindView(R.id.location_errInfo_text) TextView   mLocationErrText;
    @OnClick(R.id.button)
    void sendMessage(){
        Toast.makeText(LocationActivity.this,"button",Toast.LENGTH_SHORT).show();
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("+8618641154350",null,"1234",null,null);
    }


    private IntentFilter receiveFilter;
    private MessageReceiver messageReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();


        setUpMap();
        mLocationErrText.setVisibility(View.GONE);

        receiveFilter =new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        messageReceiver= new MessageReceiver();
        registerReceiver(messageReceiver,receiveFilter);


    }
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        unregisterReceiver(messageReceiver);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
        deactivate();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        String nameOfLocation=aMapLocation.getAddress();
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                mLocationErrText.setVisibility(View.GONE);
                mListener.onLocationChanged(aMapLocation);//显示系统小蓝点

                if(nameOfLocation!=aMapLocation.getAddress()) {
                    if(MyData.myLocation!=aMapLocation.getAddress()){
                        Toast.makeText(LocationActivity.this,"您现在的位置:\n"+aMapLocation.getAddress(),Toast.LENGTH_SHORT).show();
                        MyData.myLocation=aMapLocation.getAddress();
                    }
                }
                //Toast通知经纬度
                //Toast.makeText(MainActivity.this,String.valueOf(aMapLocation.getLatitude())+"\n"+String.valueOf(aMapLocation.getLongitude()),Toast.LENGTH_LONG).show();
                MyData.startPoint = new NaviLatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());

            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
                mLocationErrText.setVisibility(View.VISIBLE);
                mLocationErrText.setText(errText);
            }
        }
    }

    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置返回地址信息，默认为true
            mLocationOption.setNeedAddress(true);

            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }



    class MessageReceiver extends BroadcastReceiver {
        public void onReceive(Context content, Intent intent){
            Bundle bundle =intent.getExtras();
            Object[] pdus = (Object[])bundle.get("pdus");
            SmsMessage[] messages =new SmsMessage[pdus.length];
            for(int i=0;i<messages.length;i++){
                messages[i]=SmsMessage.createFromPdu((byte[])pdus[i]);
            }
            //number保存发送手机的手机号
            MyData.number = messages[0].getOriginatingAddress();
            MyData.messageContent = "";
            for(SmsMessage message:messages){
                MyData.messageContent +=message.getMessageBody();
            }
            Toast.makeText(LocationActivity.this,MyData.number,Toast.LENGTH_SHORT).show();
            if(MyData.number.toString().equals("+8618641154350")){
                Toast.makeText(LocationActivity.this,"+8618641154350",Toast.LENGTH_SHORT).show();
                try {
                    MyData.exchangeData();
                    setPoint();
                    Toast.makeText(LocationActivity.this,"已找到你",Toast.LENGTH_SHORT).show();
                    OpenNavi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    void OpenNavi(){
        new AlertDialog.Builder(this)
                .setTitle("打开导航")
                .setMessage("确定打开导航吗？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(LocationActivity.this,MapActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }

    void setPoint() throws Exception {
        //LatLng latLng = new LatLng(39.07,121.77);
        LatLng latLng =new LatLng(MyData.latitude,MyData.longitude);
        final Marker marker = aMap.addMarker(new MarkerOptions().
                position(latLng).
                title("定位").
                snippet("DefaultMarker"));
        marker.setVisible(true);
    }
}
