/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.entity;

import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;

/**
 * Start AdActivity Params
 */
public class AdActivityStartParams {
//    public String requestId;
    public int format;
    public String scenario;
    public BaseAdContent baseAdContent;
//    public String placementId;
//    public String adSourceId;
//    public BaseAdSetting baseAdSetting;
    public String eventId;
    public int orientation;
    public String targetUrl; //Only for webview landpage
    public BaseAdRequestInfo baseAdRequestInfo;
}
