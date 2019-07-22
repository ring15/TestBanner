package com.founq.sdk.testbanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Banner mBanner;
    private List<String> mModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBanner = findViewById(R.id.banner);
        mModels = new ArrayList<>();
        for (int i = 0; i < 4; i++){
            mModels.add("test"+i);
        }
        mBanner.setData(R.layout.item_normal, mModels, mModels );
        mBanner.setDelegate(new Banner.Delegate<View, String>() {
            @Override
            public void onBannerItemClick(Banner banner, View itemView, String model, int position) {
                Toast.makeText(MainActivity.this, model, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
