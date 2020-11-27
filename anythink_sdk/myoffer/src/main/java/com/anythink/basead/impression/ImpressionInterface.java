/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.impression;

import android.view.View;

/**
 * This interface should be implemented by native ad formats that want to make use of the
 * {@link ImpressionTracker} to track impressions.
 */
public interface ImpressionInterface {
    int getImpressionMinPercentageViewed();
    Integer getImpressionMinVisiblePx();
    int getImpressionMinTimeViewed();
    void recordImpression(View view);
    boolean isImpressionRecorded();
    void setImpressionRecorded();
}
