package com.reactlibrary;


import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.pili.pldroid.player.PLMediaPlayer;

import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.reactlibrary.push.PushViewManager;

public class PiliPlayView extends FrameLayout implements PLMediaPlayer.OnErrorListener, LifecycleEventListener {

    private ThemedReactContext mContext;

    public PiliPlayViewShowControl mPiliPlayViewShowControl;

    private RCTEventEmitter mEventEmitter;

    private int mRotation = 0;

    //正常
    public static final int CURRENT_STATE_NORMAL = 0;
    //准备中
    public static final int CURRENT_STATE_PREPAREING = 1;
    //播放中
    public static final int CURRENT_STATE_PLAYING = 2;
    //开始缓冲
    public static final int CURRENT_STATE_PLAYING_BUFFERING_START = 3;
    //暂停
    public static final int CURRENT_STATE_PAUSE = 5;
    //自动播放结束
    public static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    //错误状态
    public static final int CURRENT_STATE_ERROR = 7;

    //当前的播放状态
    protected int mCurrentState = -1;

    //全屏按键
    protected ImageView mFullscreenButton;

    //是否全屏
    Boolean mIfCurrentIsFullscreen = false;

    DisplayMetrics displayMetrics;

    public final static int MEDIA_CODEC_SW_DECODE = 0;
    public final static int MEDIA_CODEC_HW_DECODE = 1;
    public final static int MEDIA_CODEC_AUTO = 2;

    public OrientationUtils orientationUtils;

    public PLVideoTextureView mVideoView;

    View mView;

    String errorMsg = "";

    String infoMsg = "";

    private AVOptions mOptions = new AVOptions();

    PiliPlayView(ThemedReactContext context) {
        super(context);
        this.mContext = context;

        context.addLifecycleEventListener(this);

        initView(context);

        StorageContext.getInstance().setContext(context);

        initParams(context);
    }

    public void initView(ThemedReactContext context) {
        LayoutInflater.from(context).inflate(R.layout.activity_pl_video_texture, this);

        mVideoView = (PLVideoTextureView) findViewById(R.id.VideoView);
        mFullscreenButton = (ImageView) findViewById(R.id.fullscreen);


        Us.getInstance().setPiliPlayView(this);

        mVideoView.setOnErrorListener(this);
        StorageContext.getInstance().setContext(context);

        initParams(context);

    }

    /**
     * 是否全屏
     */
    public boolean isIfCurrentIsFullscreen() {
        return mIfCurrentIsFullscreen;
    }

    /*
    *
    * 获取播放器TextView
    *
    * */

    public PLVideoTextureView getPLVideoTextureView() {
        return mVideoView;
    }

    public int getCurrentState() {
        return mCurrentState;
    }


    public void initParams(final ThemedReactContext context) {

        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, mContext);

        setLoading(findViewById(R.id.LoadingView));

        setCoverImage(findViewById(R.id.CoverView));


        mEventEmitter = mContext.getJSModule(RCTEventEmitter.class);

        mVideoView.setOnInfoListener(mOnInfoListener);

        mVideoView.setOnCompletionListener(mOnCompletionListener);

        getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                Log.d("cccc", "我执行了全屏按钮1111---dianji " + mIfCurrentIsFullscreen);
                orientationUtils.resolveByClick();

                if (this != null) {
                    //resolveLayout(context);
//                    ViewGroup mViewGroup = (ViewGroup)getParent();
//                    DisplayMetrics dm = new DisplayMetrics();
//                    StorageContext.getInstance().getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
//
//                    final LayoutParams lpParent = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                    final FrameLayout frameLayout = new FrameLayout(context);
//
//                    int a = getWidth();
//                    int b = getHeight();
//
//                    LayoutParams lp = new LayoutParams(a, b);
//
//
//                    params.width = px2dip(context, dm.widthPixels);
//                    params.height = px2dip(context, dm.heightPixels);
//                    mViewGroup.setLayoutParams(params);
                    Log.d("cccc", "我执行了全屏按钮1111---dianji----我执行了这里 " + mIfCurrentIsFullscreen);
                    if (!mIfCurrentIsFullscreen) {
                        startFullScreen(context);
                    } else {
                        exitFullScreen();
                    }
                }
            }
        });
        mPiliPlayViewShowControl = new PiliPlayViewShowControl(context);
    }

    public void startFullScreen(ThemedReactContext context) {
        if (mIfCurrentIsFullscreen == true) return;
        Log.d("cccc", "我执行了全屏按钮1111");
        hideSupportActionBar(context, true);
        ViewGroup contentView = (ViewGroup) StorageContext.getInstance().getActivity().findViewById(android.R.id.content);
        if (mIfCurrentIsFullscreen == false) {
            contentView.removeView(this);
        } else {
            if (this.getParent() != null) {
                ((ViewGroup) getParent()).removeView(this);
            }
        }
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        if (this.getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
        contentView.addView(this, params);
        mIfCurrentIsFullscreen = true;
//            mController.onPlayModeChanged(mCurrentMode);

    }

    public boolean exitFullScreen() {
        if (!mIfCurrentIsFullscreen) {
            Log.d("cccc", "我执行了全屏按钮33333");
            ViewGroup contentView = (ViewGroup) StorageContext.getInstance().getActivity().findViewById(android.R.id.content);
            if (this.getParent() != null) {
                ((ViewGroup) getParent()).removeView(this);
            }
            contentView.removeView(this);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            contentView.addView(this, params);

            mIfCurrentIsFullscreen = false;
            return true;
        }
        return false;
    }

    /**
     * 全屏
     */
    protected void resolveFullVideoShow(Context context, final FrameLayout frameLayout) {
        LayoutParams lp = (LayoutParams) this.getLayoutParams();
        lp.setMargins(0, 0, 0, 0);
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        setLayoutParams(lp);
        final OrientationUtils mOrientationUtils = new OrientationUtils(this, context);
//        this.mOrientationUtils = mOrientationUtils;

        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mOrientationUtils.getIsLand() != 1) {
                    mOrientationUtils.resolveByClick();
                }
                setVisibility(VISIBLE);
                frameLayout.setVisibility(VISIBLE);
            }
        }, 300);
    }

    private ViewGroup getViewGroup() {
        return (ViewGroup) (CommonUtil.scanForActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
    }

    public void resolveLayout(ThemedReactContext context) {
        ViewGroup mViewGroup = (ViewGroup) getParent();
        DisplayMetrics dm = new DisplayMetrics();
        StorageContext.getInstance().getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        FrameLayout layout = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(px2dip(context, dm.widthPixels), px2dip(context, dm.heightPixels));
//        ImageView imageView=new ImageView(context);
        PiliPlayView mPiliPlayView = new PiliPlayView(context);
        layout.addView(mPiliPlayView, params);
        ViewGroup.LayoutParams p = mPiliPlayView.getLayoutParams();
        p.width = 200;
        p.height = 200;
        mPiliPlayView.setLayoutParams(p);
    }

    /*
    *
    * 设置封面
    *
    * */
    public void setCoverImage(View coverView) {
        mVideoView.setCoverView(coverView);
    }

    /*
    *
    * 设置loading显示
    *
    * */
    public void setLoading(View loadingView) {
        loadingView.setVisibility(View.VISIBLE);
        mVideoView.setBufferingIndicator(loadingView);
    }

    /**
     * 获取屏幕的宽
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /*
    *
    * 改变屏幕狂傲
    * */
    public void changeSize(int width, int height) {
        ViewGroup.LayoutParams linearParams = this.getLayoutParams();
//        ViewGroup.LayoutParams linearParams = getLayout();
        linearParams.height = height;
        linearParams.width = width;
        this.setLayoutParams(linearParams);
        requestLayout();
    }

    public void setMute() {
        mVideoView.setVolume(0.0f, 0.0f);
    }


    public FrameLayout.LayoutParams getLayout() {
        FrameLayout.LayoutParams layoutParams = new
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        return layoutParams;
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取屏幕的高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 获取全屏按键
     */
    public ImageView getFullscreenButton() {
        return mFullscreenButton;
    }


    /*
    *
    * 改变屏幕大小
    *
    * */
    public void rotatePlay(int deg) {
        mVideoView.setDisplayOrientation(deg);
    }

    public void changeErrorMsg(String msg) {

        this.errorMsg = msg;
        WritableMap errorMessage = Arguments.createMap();
        errorMessage.putString("msg", msg);
        mEventEmitter.receiveEvent(getId(), PiliPlayerViewManager.Events._onErrorPlayer.getName(), errorMessage);
    }

    public boolean onError(PLMediaPlayer mp, int errorCode) {
        if (errorCode == PLMediaPlayer.MEDIA_ERROR_UNKNOWN) {
            changeErrorMsg("未知错误");
        } else if (errorCode == PLMediaPlayer.ERROR_CODE_OPEN_FAILED) {
            changeErrorMsg("播放器打开错误");
        } else if (errorCode == PLMediaPlayer.ERROR_CODE_IO_ERROR) {
            changeErrorMsg("网络异常");
        } else if (errorCode == PLMediaPlayer.ERROR_CODE_SEEK_FAILED) {
            changeErrorMsg("拖动失败");
        } else if (errorCode == PLMediaPlayer.ERROR_CODE_HW_DECODE_FAILURE) {
            changeErrorMsg("硬解失败");
        }
        return false;
    }

    public void InfoMsg(String msg) {
        this.infoMsg = msg;
        WritableMap errorMessage = Arguments.createMap();
        errorMessage.putString("msg", msg);
        mEventEmitter.receiveEvent(getId(), PiliPlayerViewManager.Events._onInfo.getName(), errorMessage);
    }

    private PLMediaPlayer.OnInfoListener mOnInfoListener = new PLMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
            Log.i("mOnInfoListener", "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    InfoMsg("未知消息");
                    break;
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    InfoMsg("开始缓冲");
                    break;
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    InfoMsg("停止缓冲");
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    InfoMsg("第一帧视频已成功渲染");
                    break;
                case PLMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                    InfoMsg("第一帧音频已成功播放");
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    Log.i("mOnInfoListener", "video frame rendering, ts = " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    Log.i("mOnInfoListener", "audio frame rendering, ts = " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_GOP_TIME:
                    InfoMsg("获取视频的I帧间隔");
                    break;
                case PLMediaPlayer.MEDIA_INFO_SWITCHING_SW_DECODE:
                    InfoMsg("硬解失败，自动切换软解");
                    break;
                case PLMediaPlayer.MEDIA_INFO_METADATA:
                    InfoMsg("读取到 metadata 信息");
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_BITRATE:

                case PLMediaPlayer.MEDIA_INFO_VIDEO_FPS:
//                    updateStatInfo();
                    break;
                case PLMediaPlayer.MEDIA_INFO_LOOP_DONE:
                    InfoMsg("loop 中的一次播放完成");
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    InfoMsg("获取到视频的播放角度");
                    break;
                case PLMediaPlayer.MEDIA_INFO_CONNECTED:
                    InfoMsg("连接成功");
                    break;
                default:
                    break;
            }
            return true;
        }
    };


    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer plMediaPlayer) {
            Log.i("Completed", "Play Completed !");
            WritableMap errorMessage = Arguments.createMap();
            errorMessage.putString("msg", "播放完成");
            mEventEmitter.receiveEvent(getId(), PiliPlayerViewManager.Events._onCompletion.getName(), errorMessage);
        }
    };

    public void startPlay() {
        RequestPlayerParamsBuilder playerInstance = RequestPlayerParamsBuilder.getInstance();
        /* 包括硬解码 软解码 自动解码*/
        setMediaPlayerDecode(playerInstance.mediaDecode);
        /*设置延迟时间，默认0s*/
        setPlayerTimeout(playerInstance.timeout);
        /*设置缓存*/
        setPlayerCacheBuffer(playerInstance.cache);
        /*设置是否允许直播*/
        setPlayerLiving(playerInstance.live);
        /*将设置参数添加入mVideoView*/
        mVideoView.setAVOptions(mOptions);
        /*设置播放地址*/
        setVideoPlayerPath(playerInstance.videoPlayerPath);
        startPlayer();
    }

    /*直播参数设置函数B*/

    public void setVideoPlayerPath(String videoPath) {
        if (videoPath == "") {
            return;
        }
        mVideoView.setVideoPath(videoPath);
    }

    public void setMediaPlayerDecode(int mediaPlayerDecode) {
        mOptions.setInteger(AVOptions.KEY_MEDIACODEC, mediaPlayerDecode);
    }

    public void setPlayerTimeout(int timeout) {
        mOptions.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, timeout);
    }

    public void setPlayerCacheBuffer(int cache) {
        mOptions.setInteger(AVOptions.KEY_CACHE_BUFFER_DURATION, cache);
    }

//    public void setPlayerPreferFormat (int preferFormat) {
//        // 设置偏好的视频格式，设置后会加快对应格式视频流的加载速度，但播放其他格式会出错
//        mOptions.setInteger(AVOptions.KEY_PREFER_FORMAT, PREFER_FORMAT_M3U8);
//    }

    /*设置是否允许直播*/
    public void setPlayerLiving(boolean live) {
        if (live) {
            mOptions.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        } else {
            mOptions.setInteger(AVOptions.KEY_LIVE_STREAMING, 0);
        }
    }

    public void hideSupportActionBar(Context context, boolean flag) {
        if (flag) {
            StorageContext.getInstance().getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            StorageContext.getInstance().getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }



    /*停止播放视频*/
    public void stopPlayerback() {
        mVideoView.stopPlayback();
    }

    /*暂停播放*/

    public void playerPause() {
        mVideoView.pause();
    }

    public void startPlayer() {
        if (!RequestPlayerParamsBuilder.getInstance().videoPlayerPath.equals("")) {
            mVideoView.start();
        }
    }

//    public void releasePlayer() {
//        mVideoView.release();
//    }



    /*直播参数设置函数E*/


    public void removePlayerView() {
        mView = findViewById(R.id.VideoView);
        if (null != mView) {
            ViewGroup parent = (ViewGroup) mView.getParent();
            parent.removeView(mView);
        }
    }


    /*直播参数设置类*/
    public static class RequestPlayerParamsBuilder {

        public final static int MEDIA_CODEC_SW_DECODE = 0;
        public final static int MEDIA_CODEC_HW_DECODE = 1;
        public final static int MEDIA_CODEC_AUTO = 2;

//        public final static int PREFER_FORMAT_M3U8 = 1;
//        public final static int PREFER_FORMAT_MP4 = 2;
//        public final static int PREFER_FORMAT_FLV = 3;

        String videoPlayerPath = "";
        //boolean videoPlay = true;

        int mediaDecode = AVOptions.MEDIA_CODEC_AUTO;

        int timeout = 0;

        int cache = 0;

        boolean live = true;

        //int preferFormat = 0;

        public static final RequestPlayerParamsBuilder instance = new RequestPlayerParamsBuilder();

        public static RequestPlayerParamsBuilder getInstance() {
            return instance;
        }

//        public void setVideoPlay (boolean videoPlay) {
//            this.videoPlay = videoPlay;
//        }

        public void setVideoPlayerPath(String videoPlayerPath) {
            this.videoPlayerPath = videoPlayerPath;
        }

        public void setMediaDecode(boolean mediaDecode) {
            if (mediaDecode) {
                this.mediaDecode = MEDIA_CODEC_SW_DECODE;
            } else {
                this.mediaDecode = MEDIA_CODEC_HW_DECODE;
            }
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public void setCacheBuffer(int cache) {
            this.cache = cache;
        }

        public void setLiving(boolean live) {
            this.live = live;
        }

//        public void setPreferFormat(int preferFlag) {
//            switch (preferFlag) {
//                case 1:
//                    this.preferFormat = PREFER_FORMAT_M3U8;
//                    break;
//                case 2:
//                    this.preferFormat = PREFER_FORMAT_MP4;
//                    break;
//                case 3:
//                    this.preferFormat = PREFER_FORMAT_FLV;
//                    break;
//            }
//        }
    }

    @Override
    public void onHostResume() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Us.getInstance().getPiliPlayView().startPlayer();
            }
        });
    }

    @Override
    public void onHostPause() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Us.getInstance().getPiliPlayView().playerPause();
            }
        });
    }

    @Override
    public void onHostDestroy() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Us.getInstance().getPiliPlayView().removePlayerView();
                Us.getInstance().getPiliPlayView().stopPlayerback();
            }
        });
    }
}
