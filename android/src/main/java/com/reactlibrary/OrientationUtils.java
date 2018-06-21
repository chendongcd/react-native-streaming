package com.reactlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.WindowManager;


/**
 * 处理屏幕旋转的的逻辑
 * Created by yangqiao on 2017/12/26.
 */

public class OrientationUtils {

    private Activity activity;
    private PiliPlayView mPiliPlayView;
    private OrientationEventListener orientationEventListener;

    private int screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    //表明当前屏幕旋转状态 0表示横屏 1表示竖屏
    private int mIsLand;

    private boolean mClick, mClickLand, mClickPort;
    private boolean mEnable = true;
    private boolean mRotateWithSystem = true; //是否跟随系统


    Context mContext;

    /**
     * @param mPiliPlayView
     */
    public OrientationUtils(PiliPlayView mPiliPlayView) {
        this.mPiliPlayView = mPiliPlayView;
        init();
    }

    public OrientationUtils(PiliPlayView mPiliPlayView, Context context) {
        this.mPiliPlayView = mPiliPlayView;
        this.mContext = context;
        init();
    }

    private void init() {
        activity = StorageContext.getInstance().getActivity();
        orientationEventListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int rotation) {
                Log.d("hahaha","我执行了这里的函数22222");
                //是否开启了自动适应屏幕 0 === 没开启 1=== 开启
                boolean autoRotateOn = (Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
                if (!autoRotateOn && mRotateWithSystem) {
                    if (mIsLand == 0) {
                        return;
                    }
                }
                // 设置竖屏
                if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
                    if (mClick) {
                        if (mIsLand > 0 && !mClickLand) {
                            return;
                        } else {
                            mClickPort = true;
                            mClick = false;
                            mIsLand = 0;
                        }
                    } else {
                        if (mIsLand > 0) {
                            screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                            Log.d("hahaha","我走了这里2");
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            StorageContext.getInstance().getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            if (mPiliPlayView.isIfCurrentIsFullscreen()) {
//                                mPiliPlayView.getFullscreenButton().setImageResource(mPiliPlayView.getShrinkImageRes());
                            } else {
//                                mPiliPlayView.getFullscreenButton().setImageResource(mPiliPlayView.getEnlargeImageRes());
                            }
                            mIsLand = 0;
                            mClick = false;
                        }
                    }
                }
                // 设置横屏
                else if (((rotation >= 230) && (rotation <= 310))) {
                    if (mClick) {
//                        Log.d("keyide", "我点击了全屏2222----" + gsyVideoPlayer.getCurrentState());
                        if (!(mIsLand == 1) && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = 1;
                        }
                    } else {
                        if (!(mIsLand == 1)) {
                            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            StorageContext.getInstance().getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                            gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
                            mIsLand = 1;
                            mClick = false;
                        }
                    }
                }
                // 设置反向横屏
                else if (rotation > 30 && rotation < 95) {
                    if (mClick) {
                        if (!(mIsLand == 2) && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = 2;
                        }
                    } else if (!(mIsLand == 2)) {
                        screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        Log.d("hahaha","我走了这里4");
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        StorageContext.getInstance().getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                        gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
                        mIsLand = 2;
                        mClick = false;
                    }
                }
            }
        };
        orientationEventListener.enable();
    }

    /**
     * 点击切换的逻辑，比如竖屏的时候点击了就是切换到横屏不会受屏幕的影响
     */
    public void resolveByClick() {
        mClick = true;
        if (mIsLand == 0) {
            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            Log.d("hahaha","我走了这里5");
           activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            StorageContext.getInstance().getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mPiliPlayView.rotatePlay(180);
//            gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
            mIsLand = 1;
            mClickLand = false;
        } else {
            screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            Log.d("hahaha","我走了这里6");
            mPiliPlayView.rotatePlay(0);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            StorageContext.getInstance().getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            if (gsyVideoPlayer.isIfCurrentIsFullscreen()) {
//                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
//            } else {
//                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getEnlargeImageRes());
//            }
            mIsLand = 0;
            mClickPort = false;
        }
    }

    /**
     * 列表返回的样式判断。因为立即旋转会导致界面跳动的问题
     */
    public int backToProtVideo() {
        if (mIsLand > 0) {
            mClick = true;
            Log.d("hahaha","我走了这里1");
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            if (gsyVideoPlayer != null)
//                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getEnlargeImageRes());
            mIsLand = 0;
            mClickPort = false;
            return 500;
        }
        return 0;
    }


    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
        if (mEnable) {
            orientationEventListener.enable();
        } else {
            orientationEventListener.disable();
            Log.d("hahaha","我禁止了屏幕旋转");
        }
    }

    public void releaseListener() {
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
    }

    public boolean isClick() {
        return mClick;
    }

    public void setClick(boolean Click) {
        this.mClick = mClick;
    }

    public boolean isClickLand() {
        return mClickLand;
    }

    public void setClickLand(boolean ClickLand) {
        this.mClickLand = ClickLand;
    }

    public int getIsLand() {
        return mIsLand;
    }

    public void setIsLand(int IsLand) {
        this.mIsLand = IsLand;
    }


    public boolean isClickPort() {
        return mClickPort;
    }

    public void setClickPort(boolean ClickPort) {
        this.mClickPort = ClickPort;
    }

    public int getScreenType() {
        return screenType;
    }

    public void setScreenType(int screenType) {
        this.screenType = screenType;
    }


    public boolean isRotateWithSystem() {
        return mRotateWithSystem;
    }

    /**
     * 是否更新系统旋转，false的话，系统禁止旋转也会跟着旋转
     * @param rotateWithSystem 默认true
     */
    public void setRotateWithSystem(boolean rotateWithSystem) {
        this.mRotateWithSystem = rotateWithSystem;
    }
}
