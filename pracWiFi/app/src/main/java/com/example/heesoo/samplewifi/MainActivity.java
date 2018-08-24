package com.example.heesoo.samplewifi;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //다른 액티비티(WiFiDemo) 실행
        final Context ctx=this;
        Intent intent=new Intent(ctx,WiFiDemo.class);
        startActivity(intent);

        //백그라운드 실행
        Intent intent2=new Intent(getApplicationContext(), BackgroundService.class);
        startService(intent2);
    }
}
