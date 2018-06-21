package com.reactlibrary;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.react.uimanager.ThemedReactContext;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;



/**
 * Created by seatell on 2017/11/8.
 */

public class StorageContext extends ReactContextBaseJavaModule {

    final BroadcastReceiver receiver;

    static ThemedReactContext mContext;

    static StorageContext instance = null;

    static ViewGroup vp;

    static View view;
    public StorageContext(ReactApplicationContext reactContext) {
        super(reactContext);

        final ReactApplicationContext ctx = reactContext;

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Configuration newConfig = intent.getParcelableExtra("newConfig");
                Log.d("receiver", String.valueOf(newConfig.orientation));

                String orientationValue = newConfig.orientation == 1 ? "PORTRAIT" : "LANDSCAPE";

                WritableMap params = Arguments.createMap();
                params.putString("orientation", orientationValue);
                if (ctx.hasActiveCatalystInstance()) {
                    ctx
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("orientationDidChange", params);
                }
            }
        };
        Log.d("mActivity",getCurrentActivity() + "哈哈哈");
    }

    @Override
    public String getName() {
        return "Orientation";
    }

    public static void setInstance (StorageContext demo) {
        if(instance == null) {
            instance = demo;
        }else {
            return;
        }
    }

    public static StorageContext getInstance() {
        return instance;
    }

    public Activity getActivity () {
        final Activity activity = getCurrentActivity();
        Log.d("mActivity",activity + "哈哈哈");
        return activity;
    }


    public static void setContext (ThemedReactContext mContexts) {
        mContext = mContexts;
    }

    public static ThemedReactContext getContext() {
        return mContext;
    }


    public static void setContainer (ViewGroup mContexts) {
        vp = mContexts;
    }

    public static ViewGroup getContainer() {
        return vp;
    }


    public static void setView (View views) {
        view = views;
    }

    public static View getView() {
        return view;
    }
}
