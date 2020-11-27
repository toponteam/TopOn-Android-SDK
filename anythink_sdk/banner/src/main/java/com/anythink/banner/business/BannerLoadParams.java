/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.banner.business;

import android.content.Context;

import com.anythink.banner.api.ATBannerView;
import com.anythink.core.common.FormatLoadParams;

public class BannerLoadParams extends FormatLoadParams {
    InnerBannerListener listener;
    ATBannerView bannerView;
    Context context;
}
