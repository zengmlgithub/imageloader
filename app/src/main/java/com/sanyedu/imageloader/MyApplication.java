package com.sanyedu.imageloader;

import android.app.Application;

/**
 * Created by Administrator on 2016/3/23.
 */
public class MyApplication extends Application {
    private static MyApplication app;


    public static MyApplication getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.app = this;
    }

}
