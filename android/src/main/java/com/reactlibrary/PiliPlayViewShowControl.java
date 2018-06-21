package com.reactlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.react.uimanager.ThemedReactContext;
import com.transitionseverywhere.TransitionManager;

import com.pili.pldroid.player.widget.PLVideoTextureView;

import java.lang.reflect.Constructor;

import static com.reactlibrary.CommonUtil.getActionBarHeight;
import static com.reactlibrary.CommonUtil.getStatusBarHeight;
import static com.reactlibrary.CommonUtil.hideNavKey;
import static com.reactlibrary.CommonUtil.hideSupportActionBar;
import static com.reactlibrary.CommonUtil.showNavKey;
import static com.reactlibrary.CommonUtil.showSupportActionBar;

/**
 * Created by seatell on 2017/12/26.
 */

public class PiliPlayViewShowControl extends RelativeLayout{

    PiliPlayView mPiliPlayView;

    PLVideoTextureView mVideoView;

    public static final int SMALL_ID = 84778;

    public static final int FULLSCREEN_ID = 85597;

    Boolean mLockLand = false;

    //保存系统状态ui
    public int mSystemUiVisibility;

    //是否隐藏虚拟按键
    public boolean mHideKey = true;

    //满屏填充暂停为徒
    protected Bitmap mFullPauseBitmap;

    //是否需要在利用window实现全屏幕的时候隐藏actionbar
    public boolean mActionBar = false;

    //是否需要在利用window实现全屏幕的时候隐藏statusbar
    public boolean mStatusBar = false;

    //当前item框的屏幕位置
    protected int[] mListItemRect;

    //当前item的大小
    protected int[] mListItemSize;

    //是否使用全屏动画效果
    protected boolean mShowFullAnimation = true;

    //当前是否全屏
    protected boolean mIfCurrentIsFullscreen = false;

    public OrientationUtils mOrientationUtils;

    PiliPlayViewShowControl(ThemedReactContext context) {
        super(context);
        this.mPiliPlayView = Us.getInstance().getPiliPlayView();
        this.mVideoView = mPiliPlayView.getPLVideoTextureView();
    }
    /**
     * 将自定义的效果也设置到全屏
     *
     * @param context
     * @param actionBar 是否有actionBar，有的话需要隐藏
     * @param statusBar 是否有状态bar，有的话需要隐藏
     * @return
     */

    public void startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {

    }


    /**
     * 利用window层播放全屏效果
     *
     * @param context
     * @param actionBar 是否有actionBar，有的话需要隐藏
     * @param statusBar 是否有状态bar，有的话需要隐藏
     */
    @SuppressWarnings("ResourceType, unchecked")
    public void startWindowFullscreenw(final Context context, final boolean actionBar, final boolean statusBar) {
        Log.d("keyide", "我点击了全屏-total------7");
        mSystemUiVisibility = StorageContext.getInstance().getActivity().getWindow().getDecorView().getSystemUiVisibility();

        hideSupportActionBar(context, actionBar, statusBar);

        if (mHideKey) {
            hideNavKey(context);
        }

        this.mActionBar = actionBar;

        this.mStatusBar = statusBar;

        mListItemRect = new int[2];

        mListItemSize = new int[2];

        final ViewGroup vp = getViewGroup();

        removeVideo(vp, FULLSCREEN_ID);

        //处理暂停的逻辑
        pauseFullCoverLogic();

//        if (mTextureViewContainer.getChildCount() > 0) {
//            mTextureViewContainer.removeAllViews();
//        }


        saveLocationStatus(context, statusBar, actionBar);

        boolean hadNewConstructor = true;

//        try {
//            GSYBaseVideoPlayer.this.getClass().getConstructor(Context.class, Boolean.class);
//        } catch (Exception e) {
//            hadNewConstructor = false;
//        }

        try {
            //通过被重载的不同构造器来选择
            final PiliPlayView _PiliPlayView = new PiliPlayView(StorageContext.getInstance().getContext());

            _PiliPlayView.setId(FULLSCREEN_ID);
//            gsyVideoPlayer.setIfCurrentIsFullscreen(true);
//            gsyVideoPlayer.setVideoAllCallBack(mVideoAllCallBack);

            cloneParams(mPiliPlayView, _PiliPlayView);

//            if (gsyVideoPlayer.getFullscreenButton() != null) {
//                gsyVideoPlayer.getFullscreenButton().setImageResource(getShrinkImageRes());
//                gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (mBackFromFullScreenListener == null) {
//                            clearFullscreenLayout();
//                        } else {
//                            mBackFromFullScreenListener.onClick(v);
//                        }
//                    }
//                });
//            }
//
//            if (gsyVideoPlayer.getBackButton() != null) {
//                gsyVideoPlayer.getBackButton().setVisibility(VISIBLE);
//                gsyVideoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (mBackFromFullScreenListener == null) {
//                            clearFullscreenLayout();
//                        } else {
//                            Log.i("axasxasx","我在非全屏下点击了");
//                            mBackFromFullScreenListener.onClick(v);
//                        }
//                    }
//                });
//            }

            final FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            final FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setBackgroundColor(Color.BLACK);

            if (mShowFullAnimation) {
                int a = getWidth();
                int b = getHeight();
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(a, b);
                lp.setMargins(mListItemRect[0], mListItemRect[1], 0, 0);
                frameLayout.addView(_PiliPlayView, lp);

                vp.addView(frameLayout, lpParent);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TransitionManager.beginDelayedTransition(vp);
                        resolveFullVideoShow(context, _PiliPlayView, frameLayout);
                    }
                }, 300);
            } else {
                int a = getWidth();
                int b = getHeight();
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(getWidth(), getHeight());
                Log.d("LayoutParams","---" + a + "-----" + b);
                frameLayout.addView(_PiliPlayView, lp);
                vp.addView(frameLayout, lpParent);
                _PiliPlayView.setVisibility(INVISIBLE);
                frameLayout.setVisibility(INVISIBLE);
                resolveFullVideoShow(context, _PiliPlayView, frameLayout);
            }


//            gsyVideoPlayer.addTextureView();
//
//            GSYVideoManager.instance().setLastListener(this);
//            GSYVideoManager.instance().setListener(gsyVideoPlayer);
//
//            checkoutState();
//            return gsyVideoPlayer;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ViewGroup getViewGroup() {
        return (ViewGroup) (CommonUtil.scanForActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
    }

    /**
     * 移除没用的
     */
    private void removeVideo(ViewGroup vp, int id) {
        View old = vp.findViewById(id);
        if (old != null) {
            if (old.getParent() != null) {
                ViewGroup viewGroup = (ViewGroup) old.getParent();
                vp.removeView(viewGroup);
            }
        }
    }

    /**
     * 全屏的暂停的时候返回页面不黑色
     */
    private void pauseFullCoverLogic() {
//        if (mPiliPlayView.getCurrentState() == PiliPlayView.CURRENT_STATE_PAUSE && mPiliPlayView != null
//                && (mFullPauseBitmap == null || mFullPauseBitmap.isRecycled()) && mShowPauseCover) {
//            try {
////                initCover();
//            } catch (Exception e) {
//                e.printStackTrace();
//                mFullPauseBitmap = null;
//            }
//        }
    }

    /**
     * 保存大小和状态
     */
    private void saveLocationStatus(Context context, boolean statusBar, boolean actionBar) {
        getLocationOnScreen(mListItemRect);
        int statusBarH = getStatusBarHeight(context);
        int actionBerH = getActionBarHeight(StorageContext.getInstance().getActivity());
        if (statusBar) {
            mListItemRect[1] = mListItemRect[1] - statusBarH;
        }
        if (actionBar) {
            mListItemRect[1] = mListItemRect[1] - actionBerH;
        }
        mListItemSize[0] = getWidth();
        mListItemSize[1] = getHeight();
    }

    /**
     * 克隆切换参数
     *
     * @param from
     * @param to
     */
    protected void cloneParams(PiliPlayView from, PiliPlayView to) {
//        to.setLooping(from.isLooping());
//        to.setSpeed(from.getSpeed(), from.mSoundTouch);
//        to.setIsTouchWigetFull(from.mIsTouchWigetFull);
//        to.mHadPlay = from.mHadPlay;
//        to.mEffectFilter = from.mEffectFilter;
//        to.mCacheFile = from.mCacheFile;
//        to.mFullPauseBitmap = from.mFullPauseBitmap;
//        to.mNeedShowWifiTip = from.mNeedShowWifiTip;
//        to.mShrinkImageRes = from.mShrinkImageRes;
//        to.mEnlargeImageRes = from.mEnlargeImageRes;
//        to.mRotate = from.mRotate;
//        to.mShowPauseCover = from.mShowPauseCover;
//        to.mDismissControlTime = from.mDismissControlTime;
//        to.mSeekRatio = from.mSeekRatio;
//        to.mNetChanged = from.mNetChanged;
//        to.mNetSate = from.mNetSate;
//        to.mRotateWithSystem = from.mRotateWithSystem;
//        to.mBackUpPlayingBufferState = from.mBackUpPlayingBufferState;
//        to.mRenderer = from.mRenderer;
//        to.mBackFromFullScreenListener = from.mBackFromFullScreenListener;
//        to.setUp(from.mOriginUrl, from.mCache, from.mCachePath, from.mMapHeadData, from.mTitle);
//        to.setStateAndUi(from.mCurrentState);
    }


    /**
     * 全屏
     */
    protected void resolveFullVideoShow(Context context, final PiliPlayView _PiliPlayView, final FrameLayout frameLayout) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) _PiliPlayView.getLayoutParams();
        lp.setMargins(0, 0, 0, 0);
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        _PiliPlayView.setLayoutParams(lp);
//        _PiliPlayView.setIfCurrentIsFullscreen(true);
        mOrientationUtils = new OrientationUtils(_PiliPlayView,context);
        mOrientationUtils.setEnable(true);
        mOrientationUtils.setRotateWithSystem(true);
        _PiliPlayView.orientationUtils = mOrientationUtils;

        if (isShowFullAnimation()) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mLockLand && mOrientationUtils.getIsLand() != 1) {
                        mOrientationUtils.resolveByClick();
                    }
                    _PiliPlayView.setVisibility(VISIBLE);
                    frameLayout.setVisibility(VISIBLE);
                }
            }, 300);
        } else {
            if (mLockLand) {
                mOrientationUtils.resolveByClick();
            }
            _PiliPlayView.setVisibility(VISIBLE);
            frameLayout.setVisibility(VISIBLE);
        }


//        if (mVideoAllCallBack != null) {
//            Debuger.printfError("onEnterFullscreen");
//            mVideoAllCallBack.onEnterFullscreen(mOriginUrl, mTitle, gsyVideoPlayer);
//        }
//        mIfCurrentIsFullscreen = true;
//
//        checkoutState();
    }

    public boolean isShowFullAnimation() {
        return mShowFullAnimation;
    }
}
