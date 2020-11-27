/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;


public class AppRatingView extends LinearLayout {
    private Context mContext;
    private List<StarLevelView> viewList;
    public AppRatingView(Context context) {
        super(context);
        mContext = context;
    }

    public AppRatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public AppRatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setStarNum(int num){
        if(viewList == null){
            viewList = new ArrayList<StarLevelView>();
        }
        viewList.clear();
        removeAllViews();
        setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < num; i++) {
            StarLevelView starLevelView = new StarLevelView(getContext());
            LayoutParams params = new LayoutParams(dip2px(mContext, 17),
                    dip2px(mContext, 17));
            if (i != num - 1) {
                params.setMargins(0, 0, dip2px(getContext(), 8), 0);
            }
            starLevelView.setLayoutParams(params);

            addView(starLevelView);
            viewList.add(starLevelView);
        }
    }


    public void setRating(int num){
        for(int i = 0; i < viewList.size(); i++){
            StarLevelView starLevelView = viewList.get(i);
            if(i < num){
                starLevelView.setState(true);
            }else{
                starLevelView.setState(false);
            }
        }
    }
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

}
