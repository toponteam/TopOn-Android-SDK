package com.anythink.core.common.entity;

import android.text.TextUtils;

import com.anythink.core.common.net.CommonNoticeUrlLoader;

public class S2SHBResponse {
    public String bidId;
    public String curreny;
    public double price;
    public String winNoticeUrl;
    public String lossNoticeUrl;
    public String networkUnitId;
    public String networkFirmId;
    public int isSuccess;
    public int errorCode;
    public String errorMsg;

    public long outDateTime;

    public void sendWinNotice() {
        if (!TextUtils.isEmpty(winNoticeUrl)) {
            new CommonNoticeUrlLoader(winNoticeUrl).start(0, null);
        }
    }
}
