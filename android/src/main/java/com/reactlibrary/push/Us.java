package com.reactlibrary.push;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.reactlibrary.PiliPlayView;


/**
 * Created by seatell on 2017/11/7.
 */

public class Us extends AppCompatActivity {
    public static final Us cc = new Us();

    View mView = null;


    public static Context mContext;


    LivePush mLivePush;


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


    public static void setLivePusher(LivePush mLivePush) {
        Us.getInstance().mLivePush = mLivePush;
    }

    public static LivePush getLivePusher () {
        return Us.getInstance().mLivePush;
    }

    public static FragmentManager getFramgmentmanager () {
        return Us.getInstance().getSupportFragmentManager();
    }
}
