package com.idea.mainactivityidea;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ActivitySplash extends AppCompatActivity {

    public static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        TextView titleSplashTv = (TextView) findViewById(R.id.titleSplashTv);
        titleSplashTv.setText(titleSplashTv.getText().toString().toUpperCase());
        titleSplashTv.setTypeface(BaseApplication.getTypefaceSplashTitle());

        makeSplash();
    }


    private void makeSplash() {
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(SPLASH_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        };
        timerThread.start();
    }
}