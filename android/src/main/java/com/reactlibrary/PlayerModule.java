package com.reactlibrary;

/**
 * Created by seatell on 2017/12/18.
 */

import android.util.Log;
import android.widget.Toast;
import android.content.Context;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class PlayerModule extends ReactContextBaseJavaModule{
    private Context mContext;

    PlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
    }
    @Override
    public String getName() {
        return "PlayerManager";
    }
    /*
    *
    * 移除视图（但是播放器仍在播放）
    *
    * */
    @ReactMethod
    public void removePlayer() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("playersss", Us.getInstance().getPiliPlayView() + "");
                Us.getInstance().getPiliPlayView().removePlayerView();
            }
        });
    }
    /*
    * 停止播放
    *
    * */
    @ReactMethod
    public void stopPlayer() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("playersss", Us.getInstance().getPiliPlayView() + "");
                Us.getInstance().getPiliPlayView().stopPlayerback();
            }
        });
    }

    /*
    *
    * 暂停播放
    *
    * */
    @ReactMethod
    public void pausePlayer () {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("playersss", Us.getInstance().getPiliPlayView() + "");
                Us.getInstance().getPiliPlayView().playerPause();
            }
        });
    }
    /*
    *
    * 开始播放 or 继续播放
    *
    * */
    @ReactMethod
    public void startPlayer () {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("playersss", Us.getInstance().getPiliPlayView() + "");
                Us.getInstance().getPiliPlayView().startPlayer();
            }
        });
    }

    /*
    *
    * 设置静音
    *
    * */
    @ReactMethod
    public void setMute () {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("playersss", Us.getInstance().getPiliPlayView() + "");
                Us.getInstance().getPiliPlayView().setMute();
            }
        });
    }

}
