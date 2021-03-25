/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import java.io.Serializable;

public class BaseAdRequestInfo implements Serializable {
    public String bidId;
    public String placementId;
    public String adsourceId;
    public String requestId;
    public int requestAdNum = 1;
    public int networkFirmId;
    public String networkName;
    public int trafficGroupId;
    public int groupId;

    public BaseAdSetting baseAdSetting;
}
