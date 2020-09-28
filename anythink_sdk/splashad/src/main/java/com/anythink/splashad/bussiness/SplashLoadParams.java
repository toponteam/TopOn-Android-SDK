package com.anythink.splashad.bussiness;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.common.FormatLoadParams;
import com.anythink.splashad.api.ATSplashAdListener;

public class SplashLoadParams extends FormatLoadParams {
    Activity activity;
    ViewGroup containerView;
    ATSplashAdListener listener;
}
