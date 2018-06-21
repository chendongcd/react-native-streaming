package com.reactlibrary.push;

/**
 * Created by seatell on 2017/12/18.
 */

import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.reactlibrary.StorageContext;

import java.util.ArrayList;

public class StreamingModule extends ReactContextBaseJavaModule {
    private Context mContext;
    private boolean flash = false;
    public ArrayList<String> musicArr = new ArrayList();

    public StreamingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
    }

    @Override
    public String getName() {
        return "StreamingManager";
    }

    @ReactMethod
    public void startPush() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Us.getInstance().getCircleView().closeStreaming();
//                Log.d("startPush","看见你兄啊的我的青蛙打网球");
                System.out.println("startPush-----");
                PushConfig.getInstance().startPush();
            }
        });
    }

    /*
    *
    * 清除推流
    * */
    @ReactMethod
    public void closeStreaming() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Us.getInstance().getCircleView().closeStreaming();
                PushConfig.getInstance().destoryPush();
            }
        });
    }

    /*
    *
    * 暂停推流
    * */
    @ReactMethod
    public void pausePush() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Us.getInstance().getCircleView().closeStreaming();
                PushConfig.getInstance().pausePush();
            }
        });
    }

    @ReactMethod
    public void stopPreview() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PushConfig.getInstance().stopPreviewOUT();
            }
        });
    }

    @ReactMethod
    public void switchCamera() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("switchCamera");
                //Us.getInstance().getCircleView().closeStreaming();
                PushConfig.getInstance().switchCamera();
            }
        });
    }

    @ReactMethod
    public void setFlash() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("flash==aa"+flash);
                flash=!flash;
                System.out.println("flash==bb"+flash);
;                PushConfig.getInstance().setFlash(flash);
            }
        });
    }

    @ReactMethod
    public void setBeautyOn(final boolean beauty) {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PushConfig.getInstance().setBeautyOn(beauty);
            }
        });
    }

    @ReactMethod
    public void setTargetMusic(final String targetMusic) {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("targetMusic", targetMusic + "-----aaa");
                PushConfig.getInstance().setTargetMusic(targetMusic);
            }
        });
    }


    /*
     * musicList
     * 同时支持单个歌曲路径String和一个数组
     * */
    @ReactMethod
    public void addMusic(final String music) {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!musicArr.contains(music)) {
                    musicArr.add(music);
                    PushConfig.getInstance().setMusicList(musicArr);
                }
            }
        });
    }

    /*
    * musicList
    * 同时支持单个歌曲路径String和一个数组
    * */
    @ReactMethod
    public void addMusic(final ReadableArray musiclist) {

        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int lastMusicArrLength = musicArr.size();
                for (int i = 0; i < musiclist.size(); i++) {
                    if (!musicArr.contains(musiclist.getString(i))) {
                        musicArr.add(musiclist.getString(i));
                    }
                }
                int nextMusicArrLength = musicArr.size();
                if (nextMusicArrLength > lastMusicArrLength) {
                    PushConfig.getInstance().setMusicList(musicArr);
                }
            }
        });
    }

    /*
    *
    * 降噪音
    * */
    @ReactMethod
    public void setAudioDenoise(boolean audioDenoise) {
        PushConfig.getInstance().setAudioDenoiseOUT(audioDenoise);
    }

    /*
    *
    * 设置分辨率
    * 默认540p
    * 最好加一个setTmeout
    * 尽量先设置分辨率 在开始推流
    * */
    @ReactMethod
    public void setResolution(String resolution) {
        PushConfig.getInstance().setResolutionOUT(resolution);
    }

    /*
    *
    * 设置解码模式
    * true为硬解码 false为软解码
    *
    * */
    @ReactMethod
    public void setVideoEncodeMode(boolean encodeMode) {
        PushConfig.getInstance().setVideoEncodeModeOUT(encodeMode);
    }

    /*
    *
    * 设置GOP
    * @params int | s
    *
    * */
    @ReactMethod
    public void setVideoEncodeGop(int encodeGop) {
        PushConfig.getInstance().setVideoEncodeGopOUT(encodeGop);
    }

    /*
    *
    * 设置单声道
    * @params int
    *
    * */
    @ReactMethod
    public void setAudioChannels(int audioChannels) {
        PushConfig.getInstance().setAudioChannelsOUT(audioChannels);
    }

    /*
    *
    * 设置视频编码码率
    * @params int | (100,5000)
    *
    * */
    @ReactMethod
    public void setTargetVideoBitrate(int videoBitrate) {
        PushConfig.getInstance().setTargetVideoBitrateOUT(videoBitrate);
    }

    /*
    * 最小码率
    * @params int | (100,5000)
    * */
    @ReactMethod
    public void setMinVideoBitrate(int minBitrate) {
        PushConfig.getInstance().setMinVideoBitrateOUT(minBitrate);
    }

    /*
    * 设置推流镜像
    *
    * */
    @ReactMethod
    public void setPushMirror(boolean pushMirror) {
        PushConfig.getInstance().setPushMirrorOUT(pushMirror);
    }

    /*
    * 停止播放背景音乐
    * stopBGMAsync
    * */

    @ReactMethod
    public void stopBGMAsync() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PushConfig.getInstance().stopBGMAsyncOUT();
            }
        });
    }

    /*
    *
    * 设置静音
    * */
    @ReactMethod
    public void setMute(boolean mute) {
        PushConfig.getInstance().setMuteOUT(mute);
    }

    /*
    * 重新开始推流
    *
    *
    * */
    @ReactMethod
    public void restartPush() {
        PushConfig.getInstance().restartPushOUT();
    }

    /*
    * 重新开始推流
    * */
    @ReactMethod
    public void restartPushAsync() {
        PushConfig.getInstance().restartPushAsyncOUT();
    }

    /*
    * 恢复推流
    * */
    @ReactMethod
    public void resume() {
        PushConfig.getInstance().resumeOUT();
    }

    /*
    * 恢复推流（异步）
    * */
    @ReactMethod
    public void resumeAsync() {
        PushConfig.getInstance().resumeAsyncOUT();
    }

    /*
    * 自动对焦
    * */
    @ReactMethod
    public void setAutoFocus(boolean autoFocus) {
        PushConfig.getInstance().setAutoFocusOUT(autoFocus);
    }

    /*
    * 设置缩放
    * int
    * */
    @ReactMethod
    public void setZoom(int zoom) {
        PushConfig.getInstance().setZoomOUT(zoom);
    }
    /*
    * 设置曝光度
    * 【0~100）
    * */
    @ReactMethod
    public void setExposure(int exposure) {
        Log.d("exposure", exposure + "");
        PushConfig.getInstance().setExposureOUT(exposure);
    }
}

