package com.reactlibrary.push;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.app.FragmentManager;

import com.alivc.live.pusher.AlivcEncodeModeEnum;
import com.alivc.live.pusher.AlivcLivePushBGMListener;
import com.alivc.live.pusher.AlivcLivePushCameraTypeEnum;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePushStatsInfo;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.alivc.live.pusher.LogUtil;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.reactlibrary.R;
import com.reactlibrary.StorageContext;
import com.reactlibrary.push.Us;

import java.util.ArrayList;
import java.util.List;
import com.reactlibrary.push.StorageContextPush;

import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_LEFT;
import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_RIGHT;
import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT;

public class LivePush extends FrameLayout implements LifecycleEventListener {

    ThemedReactContext mContext;

    private static final String TAG = "LivePushActivity";
    private static final int FLING_MIN_DISTANCE = 50;
    private static final int FLING_MIN_VELOCITY = 0;
    private final long REFRESH_INTERVAL = 1000;
    private static final String URL_KEY = "url_key";
    private static final String ASYNC_KEY = "async_key";
    private static final String AUDIO_ONLY_KEY = "audio_only_key";
    private static final String ORIENTATION_KEY = "orientation_key";
    private static final String CAMERA_ID = "camera_id";
    private static final String FLASH_ON = "flash_on";
    public static final int REQ_CODE_PUSH = 0x1112;
    public boolean startPush = false;

    public SurfaceView mPreviewView;
    private ViewPager mViewPager;

    private RCTEventEmitter mEventEmitter;

    private List<Fragment> mFragmentList = new ArrayList<>();
//    private FragmentAdapter mFragmentAdapter;

    private GestureDetector mDetector;
    private ScaleGestureDetector mScaleDetector;
    private LivePushFragment mLivePushFragment;
    private PushTextStatsFragment mPushTextStatsFragment;
    private PushDiagramStatsFragment mPushDiagramStatsFragment;
    private AlivcLivePushConfig mAlivcLivePushConfig;

    private AlivcLivePusher mAlivcLivePusher = null;
    private String mPushUrl = null;

    private boolean mAsync = false;
    private boolean mAudioOnly = false;
    private int mOrientation = ORIENTATION_PORTRAIT.ordinal();

    private SurfaceStatus mSurfaceStatus = SurfaceStatus.UNINITED;
    private Handler mHandler = new Handler();
    private boolean isPause = false;

    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean mFlash = false;
    public boolean inPreviewing = false;
    AlivcLivePushStatsInfo alivcLivePushStatsInfo = null;

    Activity mActivity;

//    private ConnectivityChangedReceiver mChangedReceiver = new ConnectivityChangedReceiver();

    private int mNetWork = 0;

    //使用在java代码创建控件（无法加载XML文件中定义的控件属性）
    public LivePush(Context context) {
        super(context);
    }

    //由系统调用（上下文环境构造方法 + 带属性）
    public LivePush(ThemedReactContext context, AttributeSet attrs) {
        super(context, attrs);
        context.addLifecycleEventListener(this);
        this._init(context);
    }

    //由系统调用（上下文环境构造方法 + 带属性 + 布局文件中定义样式文件构造方法）
    public LivePush(ThemedReactContext context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        context.addLifecycleEventListener(this);
        this._init(context);
    }

    public LivePush(ThemedReactContext context) {
        super(context);
        context.addLifecycleEventListener(this);
        this._init(context);
    }

    public void _init (ThemedReactContext context) {

        this.mContext = context;
        mActivity = StorageContextPush.getInstance().getActivity();

        if(mActivity == null) {
            return;
        }
        //mActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        LayoutInflater.from(context).inflate(R.layout.live_camera, this);
        LayoutInflater.from(context).inflate(R.layout.activity_push, this);
//        mActivity.setContentView(R.layout.activity_push);
        mViewPager = (ViewPager) findViewById(R.id.tv_pager);
        initPreview();
        initViewPager();
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAsync = mActivity.getIntent().getBooleanExtra(ASYNC_KEY, false);
        mAudioOnly = mActivity.getIntent().getBooleanExtra(AUDIO_ONLY_KEY, false);
        mOrientation = mActivity.getIntent().getIntExtra(ORIENTATION_KEY, ORIENTATION_PORTRAIT.ordinal());
        mCameraId = mActivity.getIntent().getIntExtra(CAMERA_ID, Camera.CameraInfo.CAMERA_FACING_FRONT);
        mFlash = mActivity.getIntent().getBooleanExtra(FLASH_ON, false);
        setOrientation(mOrientation);
        initView();
        mEventEmitter = context.getJSModule(RCTEventEmitter.class);
        //mAlivcLivePushConfig = (AlivcLivePushConfig) mActivity.getIntent().getSerializableExtra(AlivcLivePushConfig.Config);
        mAlivcLivePushConfig = PushConfig.getInstance().setContext(this.mContext);
        mAlivcLivePusher = new AlivcLivePusher();
        PushConfig.getInstance().init(this,mAlivcLivePusher);
        mPushUrl = PushConfig.getInstance().rtmpUrl;
        mAlivcLivePushConfig.setCameraType(AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK);

        try {
            Log.d("resolution","设置分辨率v2");
            mAlivcLivePusher.init(mActivity.getApplicationContext(),mAlivcLivePushConfig);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            showDialog(context, e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            showDialog(context, e.getMessage());
        }

        mLivePushFragment = new LivePushFragment().newInstance(mPushUrl, mAsync, mAudioOnly, mCameraId, mFlash);
        mLivePushFragment.setAlivcLivePusher(mAlivcLivePusher);
        mLivePushFragment.setStateListener(mStateListener);
        mPushTextStatsFragment = new PushTextStatsFragment();
        mPushDiagramStatsFragment = new PushDiagramStatsFragment();
        mScaleDetector = new ScaleGestureDetector(mActivity.getApplicationContext(), mScaleGestureDetector);
        mDetector = new GestureDetector(mActivity.getApplicationContext(), mGestureDetector);
        mNetWork = NetWorkUtils.getAPNType(context);


//        Button Btn1 = (Button)findViewById(R.id.button1);//获取按钮资源
//        Btn1.setOnClickListener(new Button.OnClickListener(){//创建监听
//            public void onClick(View v) {
//                Log.d("dianji","我点击了" + mPreviewView + "-----");
//                mAlivcLivePusher.startPush(mPushUrl);
//            }
//        });
    }

    public void initView() {
        mPreviewView = (SurfaceView) findViewById(R.id.preview_view);
        mPreviewView.getHolder().addCallback(mCallback);
    }

    public void setRtmpUrl (String url) {
        mPushUrl = url;
    }

    public void startPush () {
        Log.d("rtmpUrl",mPushUrl + "----" + "我开始直播了");
        mAlivcLivePusher.startPush(mPushUrl);
        startPush = true;
    }

    public void destoryPush () {
        mAlivcLivePusher.destroy();
    }

    public String getPushUrl () {
        return mAlivcLivePusher.getPushUrl();
    }

    public void pausePush() {
        mAlivcLivePusher.pause();
    }

    public Boolean isPushing () {
        return  mAlivcLivePusher.isPushing();
    }

    public void pauseBGM() {
        mAlivcLivePusher.pauseBGM();
    }

    public void startBGMAsync(String targetMusic) {
        mAlivcLivePusher.startBGMAsync(targetMusic);
    }

    public void setBeautyOn(boolean beauty) {
        mAlivcLivePusher.setBeautyOn(beauty);
    }

    public void stopBGMAsync() {
        mAlivcLivePusher.stopBGMAsync();
    }

    public void restartPush() {
        mAlivcLivePusher.restartPush();
    }

    public void restartPushAsync () {
        //mAlivcLivePusher.reconnectPushAsync();
    }

    public void setMute(boolean mute) {
        mAlivcLivePusher.setMute(mute);
    }

    public void setBeautyWhite(int beautyWhite) {
        Log.d("beautyWhite",beautyWhite + "------beautyWhite");
        if(isPushing() || inPreviewing) {
            mAlivcLivePusher.setBeautyWhite(beautyWhite);
        }
    }

    public void setLivePushInfoListener(AlivcLivePushInfoListener mPushInfoListener) {
        mAlivcLivePusher.setLivePushInfoListener(mPushInfoListener);
    }

    public void setPushErrorListener(AlivcLivePushErrorListener mPushErrorListener) {
        mAlivcLivePusher.setLivePushErrorListener(mPushErrorListener);
    }

    public void setLivePushNetworkListener(AlivcLivePushNetworkListener mPushNetworkListener) {
        mAlivcLivePusher.setLivePushNetworkListener(mPushNetworkListener);
    }

    public void setBeautySaturation(int setBeautySaturation) {
        Log.d("setBeautySaturation",setBeautySaturation + "------setBeautySaturation");
        if(isPushing() || inPreviewing) {
            //mAlivcLivePusher.setBeautySaturation(setBeautySaturation);
        }
    }

    public void setBeautyBuffing(int beautyBuffing) {
        Log.d("beautyBuffing",beautyBuffing + "------beautyBuffing");
        if(isPushing() || inPreviewing) {
            mAlivcLivePusher.setBeautyBuffing(beautyBuffing);
        }
    }

    public void setZoom(int zoom) {
        Log.d("zoom","我设置了缩放");
        mAlivcLivePusher.setZoom(zoom);
    }

    public void setBeautyBrightness(int beautyBrightness) {
        Log.d("beautyBrightness",beautyBrightness + "------beautyBrightness");
        if(isPushing() || inPreviewing) {
            mAlivcLivePusher.setBeautyBrightness(beautyBrightness);
        }
    }

    public void setBeautyRuddy(int beautyRuddy) {
        Log.d("beautyRuddy",beautyRuddy + "------beautyRuddy");
        if(isPushing() || inPreviewing) {
            mAlivcLivePusher.setBeautyBrightness(beautyRuddy);
        }
    }
    public void  setPreviewOrientation(AlivcPreviewOrientationEnum orientation) {
        mAlivcLivePusher.setPreviewOrientation(orientation);
    }

    public void resumePush () {
        mAlivcLivePusher.resume();
    }

    public void resumeAsync () {
        mAlivcLivePusher.resumeAsync();
    }

    public void resumeBGM () {
        mAlivcLivePusher.resumeBGM();
    }

    public void stopPreview() {
        mAlivcLivePusher.stopPreview();
    }

    public void setFlash(boolean flash) {
        mAlivcLivePusher.setFlash(flash);
    }

    public void setAudioDenoise (boolean noise) {
        Log.d("mPushBGMListener","我降了噪音------");
        mAlivcLivePusher.setAudioDenoise(noise);
    }

    public void setAutoFocus(boolean autoFocus) {
        mAlivcLivePusher.setAutoFocus(autoFocus);
    }

    public void switchCamera() {
        mAlivcLivePusher.switchCamera();
    }

    public void setLivePushBGMListener(AlivcLivePushBGMListener mPushBGMListener) {
        Log.d("mPushBGMListener",mPushBGMListener + "-----mPushBGMListener");
        if(mAlivcLivePusher != null) {
            mAlivcLivePusher.setLivePushBGMListener(mPushBGMListener);
        }
    }

    SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            if(mSurfaceStatus == SurfaceStatus.UNINITED) {
                mSurfaceStatus = SurfaceStatus.CREATED;
                if(mAlivcLivePusher != null) {
                    try {
                        if(mAsync) {
                            mAlivcLivePusher.startPreviewAysnc(mPreviewView);
                            inPreviewing = true;
                        } else {
                            mAlivcLivePusher.startPreview(mPreviewView);
                            inPreviewing = true;
                        }
                    } catch (IllegalArgumentException e) {
                        e.toString();
                    } catch (IllegalStateException e) {
                        e.toString();
                    }
                }
            } else if(mSurfaceStatus == SurfaceStatus.DESTROYED) {
                mSurfaceStatus = SurfaceStatus.RECREATED;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            mSurfaceStatus = SurfaceStatus.CHANGED;
            if(mLivePushFragment != null) {
                mLivePushFragment.setSurfaceView(mPreviewView);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            mSurfaceStatus = SurfaceStatus.DESTROYED;
        }
    };

    public void initViewPager () {

        mFragmentList.add(mPushTextStatsFragment);
        mFragmentList.add(mLivePushFragment);
        mFragmentList.add(mPushDiagramStatsFragment);
//        mFragmentAdapter = new FragmentAdapter(Us.getInstance().getSupportFragmentManager(), mFragmentList) ;
//        Log.d("mActivityssss",mFragmentAdapter + "----" + Us.getInstance().getSupportFragmentManager() + "++++" + mViewPager);
//        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setCurrentItem(1);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(((ViewPager)view).getCurrentItem() == 1) {
                    if (motionEvent.getPointerCount() >= 2) {
                        mScaleDetector.onTouchEvent(motionEvent);
                    } else if (motionEvent.getPointerCount() == 1) {
                        mDetector.onTouchEvent(motionEvent);
                    }
                }
                return false;
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int arg0) {
                if(arg0 == 1) {
                    mHandler.removeCallbacks(mRunnable);
                } else {
                    mHandler.post(mRunnable);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void initPreview () {
        Log.d("pushssss","我执行了这里");
        Us.getInstance().setLivePusher(this);



    }

    public AlivcLivePusher getLivePusher() {
        return this.mAlivcLivePusher;
    }

    private float scaleFactor = 1.0f;
    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureDetector = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            if(scaleGestureDetector.getScaleFactor() > 1) {
                scaleFactor += 0.5;
            } else {
                scaleFactor -= 2;
            }
            if(scaleFactor <= 1) scaleFactor = 1;
            try{
                if(scaleFactor >= mAlivcLivePusher.getMaxZoom()) scaleFactor = mAlivcLivePusher.getMaxZoom();
                mAlivcLivePusher.setZoom((int)scaleFactor);

            } catch (IllegalStateException e) {

            }
            return false;
        }
        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

        }
    };

    private GestureDetector.OnGestureListener mGestureDetector = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            if (mPreviewView.getWidth() > 0 && mPreviewView.getHeight() > 0) {
                float x = motionEvent.getX() / mPreviewView.getWidth();
                float y = motionEvent.getY() / mPreviewView.getHeight();
                try{
                    mAlivcLivePusher.focusCameraAtAdjustedPoint(x, y, true);
                } catch (IllegalStateException e) {

                }
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            if(motionEvent == null || motionEvent1 == null) {
                return false;
            }
            if (motionEvent.getX() - motionEvent1.getX() > FLING_MIN_DISTANCE
                    && Math.abs(v) > FLING_MIN_VELOCITY) {
                // Fling left
            } else if (motionEvent1.getX() - motionEvent.getX() > FLING_MIN_DISTANCE
                    && Math.abs(v) > FLING_MIN_VELOCITY) {
                // Fling right
            }
            return false;
        }
    };

    public SurfaceView getPreviewView() {
        return this.mPreviewView;
    }


//    public class FragmentAdapter extends FragmentPagerAdapter {
//
//        List<Fragment> fragmentList = new ArrayList<>();
//        public FragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
//            super(fm);
//            this.fragmentList = fragmentList;
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            return fragmentList.get(position);
//        }
//
//        @Override
//        public int getCount() {
//            return fragmentList.size();
//        }
//
//    }

    public interface PauseState {
        void updatePause(boolean state);
    }

    public void setOrientation(int orientation) {
        if(orientation == ORIENTATION_PORTRAIT.ordinal()) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if(orientation == ORIENTATION_LANDSCAPE_HOME_RIGHT.ordinal()) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if(orientation == ORIENTATION_LANDSCAPE_HOME_LEFT.ordinal()) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }

    private void showDialog(Context context, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(mActivity.getString(R.string.dialog_title));
        dialog.setMessage(message);
        dialog.setNegativeButton(mActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mActivity.finish();
            }
        });
        dialog.show();
    }

    private PauseState mStateListener = new PauseState() {
        @Override
        public void updatePause(boolean state) {
            isPause = state;
        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(TAG, "====== mRunnable run ======");

            new AsyncTask<AlivcLivePushStatsInfo, Void, AlivcLivePushStatsInfo>() {
                @Override
                protected AlivcLivePushStatsInfo doInBackground(AlivcLivePushStatsInfo... alivcLivePushStatsInfos) {
                    try {
                        alivcLivePushStatsInfo = mAlivcLivePusher.getLivePushStatsInfo();
                    } catch (IllegalStateException e) {

                    }
                    return alivcLivePushStatsInfo;
                }

                @Override
                protected void onPostExecute(AlivcLivePushStatsInfo alivcLivePushStatsInfo) {
                    super.onPostExecute(alivcLivePushStatsInfo);
                    if(mPushTextStatsFragment != null && mViewPager.getCurrentItem() == 0) {
                        mPushTextStatsFragment.updateValue(alivcLivePushStatsInfo);
                    } else if (mPushDiagramStatsFragment != null && mViewPager.getCurrentItem() == 2) {
                        mPushDiagramStatsFragment.updateValue(alivcLivePushStatsInfo);
                    }
                    mHandler.postDelayed(mRunnable, REFRESH_INTERVAL);
                }
            }.execute();
        }
    };
    @Override
    public void onHostResume() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PushConfig.getInstance().resumeAsyncOUT();
            }
        });
    }
    @Override
    public void onHostPause() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PushConfig.getInstance().pausePush();
            }
        });
    }
    @Override
    public void onHostDestroy() {
        StorageContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PushConfig.getInstance().destoryPush();
            }
        });
    }
}
