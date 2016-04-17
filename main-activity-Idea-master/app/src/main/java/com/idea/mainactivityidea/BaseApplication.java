package com.idea.mainactivityidea;

import android.app.Application;
import android.graphics.Typeface;

public class BaseApplication extends Application {

    private static Typeface typefaceTitle;
    private static Typeface typefaceSplashTitle;
    private static Typeface typefaceRegular;

    @Override
    public void onCreate() {
        super.onCreate();
        typefaceTitle = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Black.ttf");
        typefaceSplashTitle = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Black.ttf");
        typefaceRegular = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Regular.ttf");
    }

    public static Typeface getTypeFaceTitle() {
        return typefaceTitle;
    }

    public static Typeface getTypefaceSplashTitle() {
        return typefaceSplashTitle;
    }

    public static Typeface getTypeFaceRegular() {
        return typefaceRegular;
    }
}