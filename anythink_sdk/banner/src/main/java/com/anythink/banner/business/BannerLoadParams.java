package com.anythink.banner.business;

import android.content.Context;

import com.anythink.banner.api.ATBannerView;
import com.anythink.core.common.FormatLoadParams;

public class BannerLoadParams extends FormatLoadParams {
    InnerBannerListener listener;
    ATBannerView bannerView;
    Context context;
}
