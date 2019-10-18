package com.john.johndownloadframe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

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

}
