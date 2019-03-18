package com.example.spriteeditor;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class LoadingScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;
    boolean animation;
    ImageView loadingScreen;
    int[] loadingImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        animation = true;
        loadingScreen = findViewById(R.id.loadingScreen);

        loadingScreen.setImageResource(R.drawable.app_loading);
        loadingScreen.setTag(R.drawable.app_loading);

        loadingImages = new int[]{R.drawable.app_loading, R.drawable.app_loading2,
                R.drawable.app_loading3};

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                int counter = bundle.getInt("counter");
                loadingScreen.setImageResource(loadingImages[counter]);
                loadingScreen.setTag(loadingImages[counter]);
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int counter = 0;
                while (animation) {
                    synchronized (this) {
                        try {
                            wait(200);
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putInt("counter", counter);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            if (counter == 2) {
                                counter = 0;
                            } else {
                                counter++;
                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };


        Thread loading = new Thread(runnable);
        loading.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animation = false;
                Intent intent = new Intent(LoadingScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
