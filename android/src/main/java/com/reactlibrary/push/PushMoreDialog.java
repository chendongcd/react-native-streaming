package com.reactlibrary.push;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.alivc.live.pusher.AlivcLivePusher;
import com.reactlibrary.R;
import com.reactlibrary.push.Us;

public class PushMoreDialog extends DialogFragment implements View.OnClickListener{

    private Button mShare;
    private EditText mTargetRate;
    private EditText mMinRate;

    private Switch mAutoFocus;
    private Switch mPushMirror;
    private Switch mPreviewMirror;

    private AlivcLivePusher mAlivcLivePusher = null;
    private String mPushUrl = "";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = inflater.inflate(R.layout.push_more, container);
        mShare = (Button) view.findViewById(R.id.share);
        mShare.setOnClickListener(this);
        mTargetRate = (EditText) view.findViewById(R.id.target_rate_edit);
        mMinRate = (EditText) view.findViewById(R.id.min_rate_edit);

        mPushMirror = (Switch) view.findViewById(R.id.push_mirror_switch);
        mPreviewMirror = (Switch) view.findViewById(R.id.preview_mirror_switch);
        mAutoFocus = (Switch) view.findViewById(R.id.autofocus_switch);
        mPushMirror.setChecked(SharedPreferenceUtils.isPushMirror(getActivity().getApplicationContext()));
        mPreviewMirror.setChecked(SharedPreferenceUtils.isPreviewMirror(getActivity().getApplicationContext()));
        mAutoFocus.setChecked(SharedPreferenceUtils.isAutoFocus(getActivity().getApplicationContext()));
        mPushMirror.setOnCheckedChangeListener(onCheckedChangeListener);
        mPreviewMirror.setOnCheckedChangeListener(onCheckedChangeListener);
        mAutoFocus.setOnCheckedChangeListener(onCheckedChangeListener);
        mTargetRate.setText(String.valueOf(SharedPreferenceUtils.getTargetBit(getActivity().getApplicationContext())));
        mMinRate.setText(String.valueOf(SharedPreferenceUtils.getMinBit(getActivity().getApplicationContext())));

        mTargetRate.setHint(String.valueOf(SharedPreferenceUtils.getHintTargetBit(getActivity().getApplicationContext())));
        mMinRate.setHint(String.valueOf(SharedPreferenceUtils.getHintMinBit(getActivity().getApplicationContext())));
        return view;
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        super.onResume();

        DisplayMetrics dpMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(dpMetrics);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();

        p.width = dpMetrics.widthPixels;
//        p.height = dpMetrics.heightPixels/2;
        getDialog().getWindow().setAttributes(p);
    }

    public void setAlivcLivePusher(AlivcLivePusher alivcLivePusher) {
        this.mAlivcLivePusher = alivcLivePusher;
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if(mAlivcLivePusher == null) {
                if(getActivity() != null) {
                    mAlivcLivePusher = Us.getInstance().getLivePusher().getLivePusher();
                }

                if(mAlivcLivePusher == null) {
                    return;
                }
            }

            try{
                if(id == R.id.push_mirror_switch) {
                    mAlivcLivePusher.setPushMirror(isChecked);
                    SharedPreferenceUtils.setPushMirror(getActivity().getApplicationContext(), isChecked);
                } else if(id == R.id.preview_mirror_switch) {
                    mAlivcLivePusher.setPreviewMirror(isChecked);
                    SharedPreferenceUtils.setPreviewMirror(getActivity().getApplicationContext(), isChecked);
                } else if(id == R.id.autofocus_switch) {
                    mAlivcLivePusher.setAutoFocus(isChecked);
                    SharedPreferenceUtils.setAutofocus(getActivity().getApplicationContext(), isChecked);
                }
            } catch (IllegalStateException e) {
                Common.showDialog(getActivity(), e.getMessage());
            }

        }
    };

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        try {
            int targetRate = -1;
            int minRate = -1;
            boolean isRight = true;

            if(!mTargetRate.getText().toString().isEmpty()) {
                targetRate = Integer.valueOf(mTargetRate.getText().toString());
            } else {
                targetRate = Integer.valueOf(mTargetRate.getHint().toString());
            }

            SharedPreferenceUtils.setTargetBit(getActivity().getApplicationContext(), targetRate);
            if(!mMinRate.getText().toString().isEmpty()) {
                minRate = Integer.valueOf(mMinRate.getText().toString());
            } else {
                minRate = Integer.valueOf(mMinRate.getHint().toString());
            }

            SharedPreferenceUtils.setMinBit(getActivity().getApplicationContext(), minRate);
            if(targetRate != -1) {
                if(targetRate < 100 || targetRate > 5000) {
                    isRight = false;
                }
            }

            if(minRate != -1) {
                if(minRate < 100 || targetRate > 5000) {
                    isRight = false;
                }
            }

            if(minRate != 1 && targetRate != 1) {
                if(minRate > targetRate) {
                    isRight = false;
                }
            }

            if(isRight) {
                if(targetRate != -1) {
                    mAlivcLivePusher.setTargetVideoBitrate(targetRate);
                }
                if(minRate != -1) {
                    mAlivcLivePusher.setMinVideoBitrate(minRate);
                }
            } else {
                Common.showDialog(getActivity(), getString(R.string.bite_error));
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
    }
    public void setPushUrl(String url) {
        this.mPushUrl = url;
    }
}
