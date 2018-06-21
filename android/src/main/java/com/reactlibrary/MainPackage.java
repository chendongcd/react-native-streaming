package com.reactlibrary;

import android.util.Log;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.reactlibrary.push.PushViewManager;
import com.reactlibrary.push.StorageContextPush;
import com.reactlibrary.push.StreamingModule;

/**
 * 自定义组件模块注册类
 */
public class MainPackage implements ReactPackage {

    /**
     * 创建原生模块
     * @param reactContext
     * @return
     */
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules=new ArrayList<>();
        //将我们创建的类添加进原生模块列表中
        modules.add(new StreamingModule(reactContext));
        modules.add(new PlayerModule(reactContext));
        return modules;
    }


    /**
     * 创建原生UI组件控制器
     * @param reactContext
     * @return
     */
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        StorageContext.setInstance(new StorageContext(reactContext));
        StorageContextPush.setInstance(new StorageContextPush(reactContext));
        Log.d("mActivity","执行这里luo");
        return Arrays.<ViewManager>asList(
            new PushViewManager(),
            new PiliPlayerViewManager()
        );
    }
}