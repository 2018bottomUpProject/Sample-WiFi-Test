package com.example.heesoo.samplewifi;

import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


public class WiFiDemo extends Activity implements OnClickListener {

    private static final String TAG = "WIFIScanner";
    WifiManager wifimanager;
    int minute = -1;

    //UI
    TextView textStatus;
    TextView gpsStatus;
    TextView gpsDistance;
    TextView wifiCount;
    Button btnScanStart;
    Button btnScanStop;

    private int scanCount = 0;//스캔 횟수 저장 변수
    String text = "";

    private List<ScanResult> mScanResult; //스캔 결과 저장할 리스트
    private List<ScanResult> prevResult;
    private List<ScanResult> currentResult;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {//스캔 결과 받는다
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                getWIFIScanResult();
                wifimanager.startScan();
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
            }
        }
    };

    public void changedWiFiCount(List<ScanResult> prev, List<ScanResult> current){
        Log.i(TAG,"changedWiFiCount()");
        int count=0;
        if(prev==null){
            return;
        }
        for(int i=0;i<prev.size();i++){// 과거 값이 현재에 존재하는 지 확인
            for(int j=0;j<current.size();j++){
                if(prev.get(i).SSID.equals(current.get(j).SSID)){//같은 게 있으면 break
                    break;
                }
                if(j==current.size()-1){//마지막까지 돌았는 데도 같은게 없으면 count++
                    count++;
                }
            }
        }
        wifiCount.setText(minute+"분 WiFi "+count+"개 변경됨(prev:"+prev.size()+"개,current:"+current.size()+"개)");
    }
    public void setPrevCurrent(){
        Log.i(TAG,"setPrevCurrent()");
        Log.i(TAG,minute+"분");
        if(minute==0){//맨 처음에 리스트 받아올 때
            currentResult=wifimanager.getScanResults();
            if(currentResult==null){
                Log.i(TAG,"current가 널이요ㅠㅠㅠ");
            }
        }
        else if(minute>0){//1분마다 prev와 current 리스트를 비교하기 위해 설정
            prevResult=currentResult;
            currentResult=mScanResult;
        }
    }
    public void getWIFIScanResult() {
        mScanResult = wifimanager.getScanResults(); // ScanResult

        // Scan count
        textStatus.setText("Scan count is \t" + ++scanCount + " times \n");

        //화면에 각 스캔 정보 출력
        textStatus.append("=======================================\n");
        for (int i = 0; i < mScanResult.size(); i++) {
            ScanResult result = mScanResult.get(i);
            textStatus.append((i + 1) + ". SSID : " + result.SSID.toString()
                    + "\t\t RSSI : " + result.level + " dBm\n");
        }
        textStatus.append("=======================================\n");
    }

    public void initWIFIScan() {
        scanCount = 0;
        text = "";
        final IntentFilter filter = new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
        wifimanager.startScan();
        Log.d(TAG, "initWIFIScan()");
    }

    /**
     * Called when the activity is first created.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_demo);

        //UI 설정
        textStatus = (TextView) findViewById(R.id.textStatus);
        gpsStatus=(TextView)findViewById(R.id.GPS);
        gpsDistance=(TextView)findViewById(R.id.GPSdistance);
        wifiCount=(TextView)findViewById(R.id.WiFiCount);
        btnScanStart = (Button) findViewById(R.id.btnScanStart);
        btnScanStop = (Button) findViewById(R.id.btnScanStop);

        //OnClickListener 설정
        btnScanStart.setOnClickListener(this);
        btnScanStop.setOnClickListener(this);

        //WIFI 설정
        wifimanager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        Log.d(TAG, "Setup WIfiManager getSystemService");

        //WiFi가 꺼져있으면 켠다
        if (wifimanager.isWifiEnabled() == false) {
            wifimanager.setWifiEnabled(true);
            printToast("WiFi ON");
        }

        //위치 권한 요청
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        getMyLocation();
    }

    public void checkGPS() {
        //GPS가 꺼져있는지 확인
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //꺼져있으면 켜달라는 Text 출력
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            textStatus.append("GPS가 꺼져있습니다, 켜주세요!");
        }
    }

    public void printToast(String messageToast) {//toast 생성 및 출력
        Toast.makeText(this, messageToast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {//특정 버튼 클릭시 실행 함수

        if (v.getId() == R.id.btnScanStart) {//시작 버튼
            Log.d(TAG, "OnClick() btnScanStart()");
            printToast("WIFI SCAN");
            initWIFIScan(); //WiFi 스캔 시작
        }
        if (v.getId() == R.id.btnScanStop) {//정지 버튼
            Log.d(TAG, "OnClick() btnScanStop()");
            printToast("WIFI STOP");
            unregisterReceiver(mReceiver); //스캔 정지
        }
    }

    public void getMyLocation() {// 내 위치 받아오는 메소드
        Log.i(TAG, "getMyLocation()");
        checkGPS();
        GPSListener gpsListener = new GPSListener();
        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록
        // 5분마다 GPS 정보 가져오기<<<<<지금은 테스트를 위해 1분(60000)으로 설정, GPS로 받는 것은 삭제해둠, minTime은 빠르거나 느릴 수 있음
        locationmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, gpsListener);
    }

    private class GPSListener implements LocationListener {
        double latitude, longitude, altitude;
        float accuracy;
        String provider;
        Location prev = null;

        public void onLocationChanged(Location location) {
            Log.i(TAG,"onLocationChanged()");
            minute++;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            accuracy = location.getAccuracy();
            provider = location.getProvider();
            gpsStatus.setText("현재 위치 " + minute + "분 lat: " + latitude + " lng: " + longitude + " alt: " + altitude + " acc: " + accuracy + " pro: " + provider);
            setPrevCurrent();//1분마다 와이파이 prev와 current 갱신
            changedWiFiCount(prevResult,currentResult);//몇 개 달라졌는 지 비교
            if (prev == null) {
                prev = location;
            } else {
                double distance = prev.distanceTo(location);
                gpsDistance.setText("distance: " + distance+"m");
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

    }
}