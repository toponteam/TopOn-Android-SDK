/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.anythink.core.common.utils.CommonUtil;

public class FeedbackTextView extends AutoResizeTextView {

    public FeedbackTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context context) {
        setBackgroundResource(CommonUtil.getResId(context, "myoffer_bg_feedback_textview", "drawable"));
    }


}
