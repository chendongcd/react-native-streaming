package com.reactlibrary.push;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.alivc.live.pusher.AlivcEncodeModeEnum;
import com.alivc.live.pusher.AlivcLivePushBGMListener;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.alivc.live.pusher.AlivcVideoEncodeGopEnum;
import com.alivc.live.pusher.AlivcAudioChannelEnum;
import com.alivc.live.pusher.WaterMarkInfo;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.reactlibrary.PiliPlayerViewManager;

import java.util.ArrayList;

import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT;

/**
 * Created by seatell on 2017/12/27.
 */

public class PushConfig extends AlivcLivePushConfig {
    private static final String TAG = "PushConfigActivity";

    LivePush mLivePush;

    private RCTEventEmitter mEventEmitter;

    private static PushConfig mPushConfig = new PushConfig();

    AlivcLivePusher mAlivcLivePusher;

    String stateMessage;

    public static PushConfig getInstance () {
        return mPushConfig;
    }

    public void init (LivePush mLivePush,AlivcLivePusher mAlivcLivePusher) {
        this.mAlivcLivePusher = mAlivcLivePusher;
        mPushConfig.mLivePush = mLivePush;
//        mLivePush.setLivePushInfoListener(mPushInfoListener);
//        mLivePush.setLivePushErrorListener(mPushErrorListener);
//        mLivePush.setLivePushNetworkListener(mPushNetworkListener);
        setLivePushInfoListener(mPushInfoListener);
        setPushErrorListener(mPushErrorListener);
        //setLivePushNetworkListener(mPushNetworkListener);
        mLivePush.setLivePushBGMListener(mPushBGMListener);
        //setConnectRetryCount(10);
    }

    public String rtmpUrl;

    private AlivcResolutionEnum mDefinition = AlivcResolutionEnum.RESOLUTION_540P;
    private static final int REQ_CODE_PERMISSION = 0x1111;
    private static final int PROGRESS_0 = 0;
    private static final int PROGRESS_16 = 16;
    private static final int PROGRESS_20 = 20;
    private static final int PROGRESS_33 = 33;
    private static final int PROGRESS_40 = 40;
    private static final int PROGRESS_50 = 50;
    private static final int PROGRESS_60 = 60;
    private static final int PROGRESS_66 = 66;
    private static final int PROGRESS_75 = 75;
    private static final int PROGRESS_80 = 80;
    private static final int PROGRESS_100 = 100;

    private static final int PROGRESS_AUDIO_320 = 30;
    private static final int PROGRESS_AUDIO_441 = 70;
    private InputMethodManager manager;

    private LinearLayout mPublish;
    private SeekBar mResolution;
    private SeekBar mAudioRate;
    private SeekBar mFps;
    private SeekBar mMinFps;
    private TextView mResolutionText;
    private TextView mAudioRateText;
    private TextView mWaterPosition;
    private TextView mFpsText;
    private TextView mMinFpsText;

    private EditText mUrl;
    private EditText mTargetRate;
    private EditText mMinRate;
    private EditText mInitRate;
    private EditText mRetryInterval;
    private EditText mRetryCount;

    private Switch mWaterMark;
    private Switch mPushMirror;
    private Switch mPreviewMirror;
    private Switch mHardCode;
    private Switch mCamera;
    private Switch mAudioOnly;
    private Switch mAutoFocus;
    private Switch mBeautyOn;
    private Switch mAsync;
    private Switch mFlash;
    private Switch mLog;
    private ImageView mQr;
    private ImageView mBack;
    private RadioGroup mAudioRadio;
    private RadioGroup mGop;
    private RadioGroup mOrientation;

    //美颜相关数据
    private SeekBar mSaturationBar;
    private SeekBar mBrightnessBar;
    private SeekBar mWhiteBar;
    private SeekBar mSkinBar;
    private SeekBar mRuddyBar;

    private TextView mSaturation;
    private TextView mBrightness;
    private TextView mWhite;
    private TextView mSkin;
    private TextView mRuddy;

    private LinearLayout mWaterLinear;

    private AlivcLivePushConfig mAlivcLivePushConfig;
    private boolean mAsyncValue = true;
    private boolean mAudioOnlyPush = false;
    private AlivcPreviewOrientationEnum mOrientationEnum = ORIENTATION_PORTRAIT;

    private ArrayList<WaterMarkInfo> waterMarkInfos = new ArrayList<>();

    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean isFlash = false;

    public AlivcLivePushConfig setContext(ThemedReactContext context) {
        mEventEmitter = context.getJSModule(RCTEventEmitter.class);

        return mPushConfig;
    }

    public void changeStateMessage(String msg) {
        this.stateMessage = msg;
        WritableMap infoMessage = Arguments.createMap();
        infoMessage.putString("msg",msg);
        Log.d("mPushInfoListener",msg + "------1");
        mEventEmitter.receiveEvent(mLivePush.getId(), PushViewManager.Events._onInfo.getName(), infoMessage);
    }

    private AlivcLivePushBGMListener mPushBGMListener = new AlivcLivePushBGMListener() {
        @Override
        public void onStarted() {
            Log.d("mPushBGMListener",mPushBGMListener + "----------mPushBGMListeneronStarted");

        }

        @Override
        public void onStoped() {
            Log.d("mPushBGMListener",mPushBGMListener + "----------mPushBGMListeneronStoped");
        }

        @Override
        public void onPaused() {
            Log.d("mPushBGMListener",mPushBGMListener + "----------mPushBGMListeneronPaused");
        }

        @Override
        public void onResumed() {
            Log.d("mPushBGMListener",mPushBGMListener + "----------mPushBGMListeneronResumed");
        }

        @Override
        public void onProgress(final long progress, final long duration) {
//            StorageContextPush.getInstance().getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
////                    if(mMusicDialog != null) {
////                        mMusicDialog.updateProgress(progress, duration);
////                    }
//                }
//            });
            Log.d("mPushBGMListener",mPushBGMListener + "----------mPushBGMListeneronProgress");
        }

        @Override
        public void onCompleted() {
            Log.d("mPushBGMListener",mPushBGMListener + "----------mPushBGMListeneronCompleted");
        }

        @Override
        public void onDownloadTimeout() {
            Log.d("mPushBGMListener",mPushBGMListener + "----------mPushBGMListeneroonDownloadTimeout");
        }

        @Override
        public void onOpenFailed() {
            Log.d("mPushBGMListener",mPushBGMListener + "----------mPushBGMListeneroonDownloadTimeout");
        }
    };


    AlivcLivePushInfoListener mPushInfoListener = new AlivcLivePushInfoListener() {
        @Override
        public void onPreviewStarted(AlivcLivePusher pusher) {
            //showToast(getString(R.string.start_preview));
            Log.d("mPushInfoListener","onPreviewStarted");
            changeStateMessage("previewStart");
        }

        @Override
        public void onPreviewStoped(AlivcLivePusher pusher) {
//            showToast(getString(R.string.stop_preview));
            Log.d("mPushInfoListener","onPreviewStoped");
            changeStateMessage("PreviewStop");
        }

        @Override
        public void onPushStarted(AlivcLivePusher pusher) {
//            showToast(getString(R.string.start_push));
            Log.d("mPushInfoListener","onPushStarted");
            changeStateMessage("PushStart");
        }

        @Override
        public void onPushPauesed(AlivcLivePusher pusher) {
//            showToast(getString(R.string.pause_push));
            Log.d("mPushInfoListener","onPushPauesed");
            changeStateMessage("PushPause");
        }

        @Override
        public void onPushResumed(AlivcLivePusher pusher) {
//            showToast(getString(R.string.resume_push));
            Log.d("mPushInfoListener","onPushResumed");
            changeStateMessage("PushResume");
        }

        @Override
        public void onPushStoped(AlivcLivePusher pusher) {
//            showToast(getString(R.string.stop_push));
            Log.d("mPushInfoListener","onPushStoped");
            changeStateMessage("PushStop");
        }

        /**
         * 推流重启通知
         *
         * @param pusher AlivcLivePusher实例
         */
        @Override
        public void onPushRestarted(AlivcLivePusher pusher) {
//            showToast(getString(R.string.restart_success));
            Log.d("mPushInfoListener","onPushRestarted");
            changeStateMessage("PushRestart");
        }

        @Override
        public void onFirstFramePreviewed(AlivcLivePusher pusher) {
//            showToast(getString(R.string.first_frame));
            Log.d("mPushInfoListener","onFirstFramePreviewed");
            changeStateMessage("FirstFramePreview");
        }

        @Override
        public void onDropFrame(AlivcLivePusher pusher, int countBef, int countAft) {
//            showToast(getString(R.string.drop_frame) + ", 丢帧前："+countBef+", 丢帧后："+countAft);
            Log.d("mPushInfoListener","onDropFrame");
            changeStateMessage("DropFrame");
        }

        @Override
        public void onAdjustBitRate(AlivcLivePusher pusher, int curBr, int targetBr) {
            Log.d("mPushInfoListener","onDropFrame");
//            showToast(getString(R.string.adjust_bitrate) + ", 当前码率："+curBr+"Kps, 目标码率："+targetBr+"Kps");
            changeStateMessage("AdjustBitRate");
        }

        @Override
        public void onAdjustFps(AlivcLivePusher pusher, int curFps, int targetFps) {
            Log.d("mPushInfoListener","onDropFrame");
            changeStateMessage("AdjustFps");
//            showToast(getString(R.string.adjust_fps) + ", 当前帧率："+curFps+", 目标帧率："+targetFps);
        }
    };

    AlivcLivePushErrorListener mPushErrorListener = new AlivcLivePushErrorListener() {
        @Override
        public void onSystemError(AlivcLivePusher livePusher, AlivcLivePushError error) {
            Log.d("AlivcLivePushError","onSystemError");
        }
        @Override
        public void onSDKError(AlivcLivePusher livePusher, AlivcLivePushError error) {
            if(error != null) {
                Log.d("AlivcLivePushError","onSDKError");
            }
        }
    };

    public ArrayList musicList = new ArrayList<String>();

    public void setRtmpURL(String rtmpUrl) {
        mPushConfig.rtmpUrl = rtmpUrl;
        mPushConfig.mLivePush.setRtmpUrl(rtmpUrl);
    }

    public void startPush () {
        System.out.println("startPush------rtmt + " + rtmpUrl);
        if(rtmpUrl != null) {
            System.out.println("rtmpUrl----" + rtmpUrl + "wo kaishi 推流了");
            mPushConfig.mLivePush.startPush();
        }
    }


    public void destoryPush () {
        if(isPushing()) {
            mPushConfig.mLivePush.destoryPush();
        }
    }

    public Boolean isPushing () {
        return mPushConfig.mLivePush.isPushing();
    }

    public void pausePush () {
        if(isPushing()) {
            mPushConfig.mLivePush.pausePush();
        }
    }

    public void switchCamera() {
        mPushConfig.mLivePush.switchCamera();
    }

    public void setFlash(boolean flash) {
        mPushConfig.mLivePush.setFlash(flash);
    }

    public void setPreviewOrientation(AlivcPreviewOrientationEnum orientation) {
        mPushConfig.mLivePush.setPreviewOrientation(orientation);
    }

    public void setBeautyOn(boolean beauty) {
        mPushConfig.mLivePush.setBeautyOn(beauty);
    }

    public void setBeautyWhite(int beautyWhite) {
        mPushConfig.mLivePush.setBeautyWhite(beautyWhite);
    }

    public void setBeautySaturation(int beautySaturation) {
        mPushConfig.mLivePush.setBeautySaturation(beautySaturation);
    }

    public void setBeautyBuffing(int beautyBuffing) {
        mPushConfig.mLivePush.setBeautyBuffing(beautyBuffing);
    }

    public void setBeautyBrightness(int beautyBrightness) {
        mPushConfig.mLivePush.setBeautyBrightness(beautyBrightness);
    }

    public void setBeautyRuddy(int beautyRuddy) {
        mPushConfig.mLivePush.setBeautyRuddy(beautyRuddy);
    }

    public void setMusicList(ArrayList<String> musicList) {
        this.musicList = musicList;
    }

    public void setTargetMusic(String targetMusic) {
        if(!this.musicList.contains(targetMusic)) {
            this.musicList.add(targetMusic);
        }
        Log.d("targetMusic",targetMusic + "-----");
        mPushConfig.mLivePush.startBGMAsync(targetMusic);
    }

    public void setAudioDenoiseOUT (boolean audioDenoise) {
        mPushConfig.mLivePush.setAudioDenoise(audioDenoise);
    }

    public void setResolutionOUT(String resolution) {
        Log.d("resolution","设置分辨率v1");
        if(resolution.toUpperCase().equals("180P")) {
            mPushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_180P);
        }else if(resolution.toUpperCase().equals("240P")) {
            mPushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_240P);
        }else if(resolution.toUpperCase().equals("360P")) {
            mPushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_360P);
        }else if(resolution.toUpperCase().equals("480P")) {
            mPushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_480P);
        }else if(resolution.toUpperCase().equals("540P")) {
            mPushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_540P);
        }else if(resolution.toUpperCase().equals("720P")) {
            mPushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_720P);
        }else if(resolution.toUpperCase().equals("1080P")) {
            mPushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_1080P);
        }
    }

    public void setVideoEncodeModeOUT(boolean encodeMode) {
        if(encodeMode) {
            mPushConfig.setVideoEncodeMode(AlivcEncodeModeEnum.Encode_MODE_HARD);
        }else {
            Log.d("encode","软解码");
            mPushConfig.setVideoEncodeMode(AlivcEncodeModeEnum.Encode_MODE_SOFT);
        }
    }

    public void setVideoEncodeGopOUT(int setVideoEncodeGop) {
        if(setVideoEncodeGop == 1) {
            mPushConfig.setVideoEncodeGop(AlivcVideoEncodeGopEnum.GOP_ONE);
        }else if(setVideoEncodeGop == 2) {
            mPushConfig.setVideoEncodeGop(AlivcVideoEncodeGopEnum.GOP_TWO);
        }else if(setVideoEncodeGop == 3) {
            mPushConfig.setVideoEncodeGop(AlivcVideoEncodeGopEnum.GOP_THREE);
        }else if(setVideoEncodeGop == 4) {
            mPushConfig.setVideoEncodeGop(AlivcVideoEncodeGopEnum.GOP_FOUR);
        }else if(setVideoEncodeGop == 5) {
            mPushConfig.setVideoEncodeGop(AlivcVideoEncodeGopEnum.GOP_FIVE);
        }
    }
    public void setAudioChannelsOUT(int audioChannels) {
        if(audioChannels == 1) {
            mPushConfig.setAudioChannels(AlivcAudioChannelEnum.AUDIO_CHANNEL_ONE);
        }else if(audioChannels == 2) {
            mPushConfig.setAudioChannels(AlivcAudioChannelEnum.AUDIO_CHANNEL_TWO);
        }
    }

    public void setTargetVideoBitrateOUT(int videoBitrate) {
        mPushConfig.setTargetVideoBitrate(videoBitrate);
    }

    public void setMinVideoBitrateOUT(int minBitrate) {
        mPushConfig.setMinVideoBitrate(minBitrate);
    }

    public void setPushMirrorOUT(boolean pushMirror) {
        mPushConfig.setPushMirror(pushMirror);
    }

    public void stopPreviewOUT () {
        mPushConfig.mLivePush.stopPreview();
    }

    public void stopBGMAsyncOUT() {
        mPushConfig.mLivePush.stopBGMAsync();
    }

    public void setMuteOUT(boolean mute) {
        mPushConfig.mLivePush.setMute(mute);
    }

    public void restartPushOUT() {
        mPushConfig.mLivePush.restartPush();
    }

    public void restartPushAsyncOUT () {
        mPushConfig.mLivePush.restartPushAsync();
    }

    public void resumeOUT () {
        mPushConfig.mLivePush.resumePush();
    }

    public void setConnectRetryCount(int time) {
        mPushConfig.setConnectRetryCount(time);
    }

    public void resumeAsyncOUT () {
        if(mPushConfig.mLivePush.startPush) {
            mPushConfig.mLivePush.resumeAsync();
        }
    }

    public void setAutoFocusOUT(boolean autoFocus) {
        mPushConfig.mLivePush.setAutoFocus(autoFocus);
    }

    public void setLivePushInfoListener(AlivcLivePushInfoListener mPushInfoListener) {
        mPushConfig.mLivePush.setLivePushInfoListener(mPushInfoListener);
    }
    public void setPushErrorListener(AlivcLivePushErrorListener mPushErrorListener) {
        mPushConfig.mLivePush.setPushErrorListener(mPushErrorListener);
    }

    public void setLivePushNetworkListener(AlivcLivePushNetworkListener mPushNetworkListener) {
        mPushConfig.mLivePush.setLivePushNetworkListener(mPushNetworkListener);
    }
    public void setExposureOUT(int exposure) {
        mPushConfig.setExposure(exposure);
    }

    public void setZoomOUT(int zoom) {
        mPushConfig.mLivePush.setZoom(zoom);
    }
}
