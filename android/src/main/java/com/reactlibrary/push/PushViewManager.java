
package com.reactlibrary.push;

import android.util.Log;

import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import com.facebook.react.bridge.ReadableArray;
import com.reactlibrary.PiliPlayerViewManager;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * 圆形组件基础类管理器
 */
public class PushViewManager extends SimpleViewManager<LivePush> {

    /**
     * 设置js引用名
     *
     * @return String
     */

    private boolean started = false;/*默认设置没有打开预览*/

    private int beautyState = 0;

    private String camera = "back";

    private int flash = 0;

    public ArrayList<String> musicArr = new ArrayList();

    public PushViewManager() {
        super();
        Log.d("hehe", "我执行了PUS和vIEWmANAGER");
    }

    /*暴露外部回调函数枚举*/
    public enum Events {

        _onInfo("onInfo"),NetworkSlow("onNetworkSlow"), Connect("onConnectSuccess"), ConnectError("onConnectError"), ConnectTimeout("onConnectTimeout");
        private final String name;

        Events(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Override
    public String getName() {
        return "RCTStreaming";
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants () {
        MapBuilder.Builder builder = MapBuilder.builder();
        for(PushViewManager.Events events : PushViewManager.Events.values()) {
            builder.put(events.getName(),MapBuilder.of("registrationName",events.getName()));
        }
        return builder.build();
    }

    /**
     * 创建UI组件实例
     *
     * @param reactContext
     * @return CircleView
     */
    @Override
    protected LivePush createViewInstance(ThemedReactContext reactContext) {
        return new LivePush(reactContext);
    }

    @ReactProp(name = "rtmpURL")
    public void setRtmpURL(LivePush mLivePush, @Nullable String rtmpURL) {
        Log.d("rtmpURL", rtmpURL + "-----");
        PushConfig.getInstance().setRtmpURL(rtmpURL);
    }

    @ReactProp(name = "orientation")
    public void setPreviewOrientation(LivePush mLivePush, @Nullable Boolean orientation) {
        if (orientation) {
            PushConfig.getInstance().setPreviewOrientation(AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_RIGHT);
            return;
        } else{
            PushConfig.getInstance().setPreviewOrientation(AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT);
        }
    }
    /*
    * 改变美颜程度
    * 0-100|int
    * */
    @ReactProp(name = "beautyWhite")
    public void setBeautyWhite(LivePush mLivePush, int beautyWhite) {
        Log.d("beautyWhite", beautyWhite + "");
        PushConfig.getInstance().setBeautyWhite(beautyWhite);
    }

    /*
    * 改变美颜饱和度
    * 0-100|int
    * */
    @ReactProp(name = "beautySaturation")
    public void setBeautySaturation(LivePush mLivePush, int beautySaturation) {
        Log.d("beautySaturation", beautySaturation + "");
        PushConfig.getInstance().setBeautySaturation(beautySaturation);
    }

    /*
    * 改变磨皮程度
    * 0-100|int
    * */
    @ReactProp(name = "beautyBuffing")
    public void setBeautyBuffing(LivePush mLivePush, int beautyBuffing) {
        Log.d("beautyBuffing", beautyBuffing + "");
        PushConfig.getInstance().setBeautyBuffing(beautyBuffing);
    }

    /*
    * 改变美颜亮度
    * 0-100|int
    * */
    @ReactProp(name = "beautyBrightness")
    public void setBeautyBrightness(LivePush mLivePush, int beautyBrightness) {
        Log.d("beautyBrightness", beautyBrightness + "");
        PushConfig.getInstance().setBeautyBrightness(beautyBrightness);
    }

    /*
    * 改变美颜红润
    * 0-100|int
    * */
    @ReactProp(name = "beautyRuddy")
    public void setBeautyRuddy(LivePush mLivePush, int beautyRuddy) {
        Log.d("beautyRuddy", beautyRuddy + "");
        PushConfig.getInstance().setBeautyRuddy(beautyRuddy);
    }


    /*
    *立即切换到 targetMusic 此播放地址
    *targetMusic
    * */
//    @ReactProp(name = "targetMusic")
//    public void setPlayingMusic(LivePush mLivePush, String targetMusic) {
////        for (int i = 0; i < musiclist.size(); i++) {
////            if (!musicArr.contains(musiclist.getString(i))) {
////                musicArr.add(musiclist.getString(i));
////            }
////        }
//        if(targetMusic.equals("")) {
//            return;
//        }
//        if(!musicArr.contains(targetMusic)) {
//            musicArr.add(targetMusic);
//            PushConfig.getInstance().setMusicList(musicArr);
//        }
//        PushConfig.getInstance().setTargetMusic(targetMusic);
//    }
//
//
//    @ReactProp(name = "started")
//    public void startPreview (LivePush circleView,@Nullable boolean startPreview) {
////        Log.i("CircleView",circleView + "");
////        Log.i("CircleView",circleView.getInstance() + "");
////        circleView.getInstance().startPreview();
//        Log.i("mConfigure",startPreview + "");
//        if(this.started == startPreview) {
//            return;
//        }else {
//            this.started = startPreview;
//            if(startPreview) {
//                startStreaming(circleView);
//            }else {
//                stopStreaming(circleView);
//            }
//        }
//    }
//
//    @ReactProp(name = "resolution")
//    public void setVideoResolution (CircleView circleView,@Nullable int videoResolution) {
//        circleView.setVideoResolution(videoResolution);
//    }
//
//
//
//    @ReactProp(name = "frameRate")
//    public void setFrameRate (CircleView circleView,@Nullable int frameRate) {
//        circleView.setFrameRate(frameRate);
//    }
//
//    @ReactProp(name = "flash")
//    public void setFlash (CircleView circleView,@Nullable int flash) {
//        if(this.flash == flash){
//            return;
//        }
//        this.flash = flash;
//        circleView.setFlash(flash);
//    }
//
//    @ReactProp(name = "camera")
//    public void setCameraFacing (CircleView circleView,@Nullable String camera) {
//        if(this.camera == camera){
//            return;
//        }
//        this.camera = camera;
//        circleView.setCameraFacing(camera);
//    }
//
//    @ReactProp(name = "config")
//    public void setConfig (CircleView circleView,@Nullable ReadableMap config) {
//        circleView.setConfig(config);
//    }
//
//
//
//
//    @ReactProp(name = "beauty")
//    public void setBeauty (CircleView circleView,@Nullable int beauty) {
//        if(beautyState == beauty) {
//            return;
//        }
//        beautyState = beauty;
//        circleView.setBeauty(beauty);
//    }
//
//    @Override
//    @Nullable
//    public Map getExportedCustomDirectEventTypeConstants () {
//        MapBuilder.Builder builder = MapBuilder.builder();
//        for(Events events : Events.values()) {
//            builder.put(events.getName(),MapBuilder.of("registrationName",events.getName()));
//        }
//        return builder.build();
//    }
//
//    public void startStreaming (CircleView circleView) {
//        circleView.startStreaming();
//    }
//
//    public void stopStreaming (CircleView circleView) {
//        circleView.stopStreaming();
//    }
}