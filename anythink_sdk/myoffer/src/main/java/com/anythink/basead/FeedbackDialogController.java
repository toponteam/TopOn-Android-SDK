/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonUtil;

public class FeedbackDialogController {

    private Context mContext;
    private Dialog mDialog;
    private View mRoot;
    private EditText mEditText;
    private ImageView mCloseBtn;
    private TextView mCommitBtn;

    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    private TextView mTextView4;
    private TextView mTextView5;
    private TextView mTextView6;
    private TextView mTextView7;
    private TextView mTextView8;
    private TextView mTextView9;

    public BaseAdContent mBaseAdContent;
    public BaseAdRequestInfo mBaseAdRequestInfo;

    private boolean mIsHintShowing;

    private FeedbackDialogListener mListener;

    public void showDialog(Context context, BaseAdContent baseAdContent, BaseAdRequestInfo baseAdRequestInfo, FeedbackDialogListener listener) {

        try {
            mContext = context;
            mBaseAdContent = baseAdContent;
            mBaseAdRequestInfo = baseAdRequestInfo;
            mListener = listener;

            int widthPixels = context.getResources().getDisplayMetrics().widthPixels;
            int heightPixels = context.getResources().getDisplayMetrics().heightPixels;
            if (widthPixels > heightPixels) {//landscape
                mRoot = LayoutInflater.from(context).inflate(CommonUtil.getResId(context, "myoffer_feedback_land", "layout"), null, false);
            } else {
                mRoot = LayoutInflater.from(context).inflate(CommonUtil.getResId(context, "myoffer_feedback", "layout"), null, false);
            }

            init();

            showDialog(widthPixels, heightPixels);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void showDialog(int widthPixels, int heightPixels) {
        mDialog = new Dialog(mContext, CommonUtil.getResId(mContext, "myoffer_feedback_dialog", "style"));
        mDialog.setContentView(mRoot);
        mDialog.setCancelable(true);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mListener != null) {
                    mListener.onClosed();
                }
            }
        });

        Window window = mDialog.getWindow();
        if (window != null) {

            if (widthPixels > heightPixels) {//landscape
                window.setLayout(CommonUtil.dip2px(mContext, 280), CommonUtil.dip2px(mContext, 320));
            } else {
                window.setLayout(CommonUtil.dip2px(mContext, 300), CommonUtil.dip2px(mContext, 426));
            }
        }

        mDialog.show();
    }

    public boolean isDialogShowing() {
        return mDialog != null && mDialog.isShowing();
    }

    private void init() {
        mCloseBtn = (ImageView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_iv_close", "id"));
        mEditText = (EditText) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_et", "id"));
        mCommitBtn = (TextView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_tv_commit", "id"));

        mTextView1 = (TextView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_tv_1", "id"));
        mTextView2 = (TextView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_tv_2", "id"));
        mTextView3 = (TextView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_tv_3", "id"));
        mTextView4 = (TextView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_tv_4", "id"));
        mTextView5 = (TextView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_tv_5", "id"));
        mTextView6 = (TextView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_tv_6", "id"));
        mTextView7 = (TextView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_tv_7", "id"));
        mTextView8 = (TextView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_tv_8", "id"));
        mTextView9 = (TextView) mRoot.findViewById(CommonUtil.getResId(mContext, "myoffer_feedback_tv_9", "id"));

        registerLister();
    }

    private void registerLister() {
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        mTextView1.setOnClickListener(mOnClickListener);
        mTextView2.setOnClickListener(mOnClickListener);
        mTextView3.setOnClickListener(mOnClickListener);
        mTextView4.setOnClickListener(mOnClickListener);
        mTextView5.setOnClickListener(mOnClickListener);
        mTextView6.setOnClickListener(mOnClickListener);
        mTextView7.setOnClickListener(mOnClickListener);
        mTextView8.setOnClickListener(mOnClickListener);
        mTextView9.setOnClickListener(mOnClickListener);

        mCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText != null) {
                    String submitMsg = mEditText.getText().toString();
                    if (TextUtils.isEmpty(submitMsg)) {
                        if (!mIsHintShowing) {
                            mIsHintShowing = true;
                            mEditText.setCursorVisible(false);
                            mEditText.setHint(CommonUtil.getResId(mContext, "myoffer_feedback_hint", "string"));
                            mEditText.setHintTextColor(Color.parseColor("#999999"));
                            mEditText.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mIsHintShowing = false;
                                    mEditText.setCursorVisible(true);
                                    mEditText.setHint("");
                                }
                            }, 1500);
                        }
                    } else {
                        AgentEventManager.feedbackAgent(mBaseAdContent, mBaseAdRequestInfo, "0", submitMsg);

                        FeedbackDialogController.this.close();

                        if (mListener != null) {
                            mListener.onFeedback();
                        }
                    }

                }
            }
        });

    }

    private void close() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                if (mListener != null) {
                    mListener.onClosed();
                }
            }
        }, 30);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof TextView) {
                TextView TextView = (TextView) v;

                AgentEventManager.feedbackAgent(mBaseAdContent, mBaseAdRequestInfo, TextView.getTag().toString(), "");

                FeedbackDialogController.this.close();

                if (mListener != null) {
                    mListener.onFeedback();
                }
            }
        }
    };

    public void destroy() {
        mContext = null;
        mBaseAdContent = null;
        mBaseAdRequestInfo = null;
        mListener = null;
    }


    public interface FeedbackDialogListener {
        void onFeedback();

        void onClosed();
    }

}
