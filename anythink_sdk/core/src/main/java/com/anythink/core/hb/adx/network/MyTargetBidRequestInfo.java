/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb.adx.network;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.common.base.Const;
import com.anythink.core.hb.adx.BidRequest;
import com.anythink.core.strategy.PlaceStrategy;

import org.json.JSONObject;

public class MyTargetBidRequestInfo extends BaseNetworkInfo {
    int bannerWidth = 0;
    int bannerHeight = 0;

    String appId;
    String slotId;
    int firmId;
    String buyerUid;
    ATBaseAdAdapter adapter;

    public MyTargetBidRequestInfo(Context context, String format, PlaceStrategy.UnitGroupInfo unitGroupInfo, ATBaseAdAdapter adapter) {
        super(format);
        try {
            try {
                Looper.prepare();
            } catch (Throwable e) {

            }
            JSONObject contentObject = new JSONObject(unitGroupInfo.content);
            String appId = contentObject.optString("app_id");
            String slotId = contentObject.optString("slot_id");
            String size = contentObject.optString("size");

            this.appId = appId;
            this.slotId = slotId;
            this.firmId = unitGroupInfo.networkType;
            this.adapter = adapter;
            buyerUid = adapter.getBiddingToken(context);

            if (format.equals(Const.FORMAT.BANNER_FORMAT) && !TextUtils.isEmpty(size)) {
                String[] sizes = size.split("x");
                if (sizes.length == 2) {
                    bannerWidth = Integer.parseInt(sizes[0]);
                    bannerHeight = Integer.parseInt(sizes[1]);
                }
            }

        } catch (Exception e) {

        }

    }

    @Override
    public String getRequestInfoId() {
        return slotId;
    }

    @Override
    public String getSDKVersion() {
        return adapter.getNetworkSDKVersion();
    }

    @Override
    public JSONObject toRequestJSONObject() {
        JSONObject networkObject = new JSONObject();
        try {
            networkObject.put(BidRequest.APP_ID, appId);
            networkObject.put(BidRequest.UNIT_ID, slotId);
            networkObject.put(BidRequest.NW_FIRM_ID, firmId);
            networkObject.put(BidRequest.BUYERUID, buyerUid);
            networkObject.put(BidRequest.FORMAT, format);

            if (TextUtils.equals(format, Const.FORMAT.BANNER_FORMAT)) {
                networkObject.put(BidRequest.AD_WIDTH, bannerWidth);
                networkObject.put(BidRequest.AD_HEIGHT, bannerHeight);
            }
        } catch (Exception e) {

        }
        return networkObject;
    }

}
