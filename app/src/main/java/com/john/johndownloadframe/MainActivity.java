package com.john.johndownloadframe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.john.breakpoint.network.TestService;
import com.tencent.bugly.crashreport.CrashReport;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void download(View view) {
        Intent intent=new Intent(this,DownloadActivity.class);
        startActivity(intent);
    }

    public void upload(View view) {
        startActivity(new Intent(this,UploadActivity.class));
    }

    public void testLog(View view){
//        CrashReport.testJavaCrash();
//        textView.setText("ss");
        Intent intent=new Intent(this, TestService.class);
        startService(intent);
    }




}
