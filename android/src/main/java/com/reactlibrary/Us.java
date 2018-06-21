package com.reactlibrary;

import android.content.Context;
import android.view.View;


/**
 * Created by seatell on 2017/11/7.
 */

public class Us {
    public static final Us cc = new Us();

    View mView = null;


    public static Context mContext;

//    CircleView mCircleView;

    PiliPlayView mPiliPlayView;


    public static Us getInstance () {
        return  cc;
    }

    public static void set(Context mContext) {
        Us.getInstance().mContext = mContext;
    }
    public static Context getContext () {
        if(Us.getInstance().mContext!=null) {
            return Us.getInstance().mContext;
        }
        return null;
    }

    public static void set(View mView) {
        Us.getInstance().mView = mView;
    }
    public static View get () {
        if(Us.getInstance().mView!=null) {
            return Us.getInstance().mView;
        }
        return null;
    }

//    public static void setCircleView(CircleView mCircleView) {
//        Us.getInstance().mCircleView = mCircleView;
//    }
//
//    public static CircleView getCircleView () {
//        return Us.getInstance().mCircleView;
//    }

    public static void setPiliPlayView(PiliPlayView mPiliPlayView) {
        Us.getInstance().mPiliPlayView = mPiliPlayView;
    }

    public static PiliPlayView getPiliPlayView () {
        return Us.getInstance().mPiliPlayView;
    }
}
