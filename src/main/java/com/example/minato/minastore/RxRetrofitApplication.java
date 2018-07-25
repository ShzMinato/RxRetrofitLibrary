package com.example.minato.minastore;

import android.app.Application;

/**
 * Created by minato on 2018/7/20.
 */

public class RxRetrofitApplication {
    private static Application application;
    private static boolean debug=true;


    public static void init(Application app){
        setApplication(app);
        setDebug(true);
    }

    public static void init(Application app,boolean debug){
        setApplication(app);
        setDebug(debug);
    }

    public static Application getApplication() {
        return application;
    }

    private static void setApplication(Application application) {
        RxRetrofitApplication.application = application;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        RxRetrofitApplication.debug = debug;
    }
}
