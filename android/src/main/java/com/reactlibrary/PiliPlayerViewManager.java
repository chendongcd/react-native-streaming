
package com.reactlibrary;
import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nullable;


public class PiliPlayerViewManager extends SimpleViewManager<PiliPlayView> {

    /*暴露外部回调函数枚举*/
    public enum Events {

        _onErrorPlayer("onErrorPlayer"),_onInfo("onInfo"),_onCompletion("onCompletion");
        private final String name;

        Events(final String name) {
            this.name = name;
        }

        public String getName () {
            return name;
        }
    }

    /**
     * 设置js引用名
     * @return String
     */

    private boolean startFlag = false;

    private boolean isStop = false;

    @Override
    public String getName() {
        return "RCTPlayer";
    }

    /**
     * 创建UI组件实例
     * @param reactContext
     * @return CircleView
     */
    @Override
    protected PiliPlayView createViewInstance(ThemedReactContext reactContext) {
        return new PiliPlayView(reactContext);
    }


    @ReactProp(name = "source")
    public void setSource (PiliPlayView piliPlayView,ReadableMap source) {
        String videoPlayerPath = source.getString("uri");
        int timeout = source.getInt("timeout");
        boolean hardCodec = source.getBoolean("hardCodec");
        boolean live = source.getBoolean("live");

        PiliPlayView.RequestPlayerParamsBuilder playerInstance = PiliPlayView.RequestPlayerParamsBuilder.getInstance();

        Log.i("player",videoPlayerPath + "---------------videoPlayerPath");
        playerInstance.setVideoPlayerPath(videoPlayerPath);
        playerInstance.setTimeout(timeout);
        playerInstance.setMediaDecode(hardCodec);
        playerInstance.setLiving(live);
    }

    @ReactProp(name = "started")
    public void setSource (PiliPlayView piliPlayView,boolean started) {
        if(started) {
            piliPlayView.startPlay();
        }
    }

    @ReactProp(name = "stopPlayback")
    public void setStopPlayback (PiliPlayView piliPlayView,boolean stopPlayback) {
        this.isStop = stopPlayback;
        if(isStop) {
            piliPlayView.stopPlayerback();
        }
    }

    @ReactProp(name = "pause")
    public void setPause (PiliPlayView piliPlayView,boolean pause) {
        //this.isStop = stopPlayback;
        if(pause) {
            piliPlayView.playerPause();
        }
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants () {
        MapBuilder.Builder builder = MapBuilder.builder();
        for(PiliPlayerViewManager.Events events : PiliPlayerViewManager.Events.values()) {
            builder.put(events.getName(),MapBuilder.of("registrationName",events.getName()));
        }
        return builder.build();
    }
}