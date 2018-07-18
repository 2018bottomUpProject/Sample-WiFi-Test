package com.example.heesoo.samplewifi;

import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class WiFiDemo extends Activity implements OnClickListener {

    private static final String TAG = "WIFIScanner";

    WifiManager wifimanager;

    //UI
    TextView textStatus;
    Button btnScanStart;
    Button btnScanStop;

    private int scanCount = 0;//스캔 횟수 저장 변수
    String text = "";

    private List<ScanResult> mScanResult; //스캔 결과 저장할 리스트

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

    /** Called when the activity is first created. */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_demo);

        //UI 설정
        textStatus = (TextView) findViewById(R.id.textStatus);
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

        //GPS가 꺼져있는지 확인
        LocationManager locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

        //꺼져있으면 켜달라는 Text 출력
        if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
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

}