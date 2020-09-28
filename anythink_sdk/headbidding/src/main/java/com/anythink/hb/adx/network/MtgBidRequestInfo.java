package com.anythink.hb.adx.network;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.anythink.core.common.base.Const;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.hb.adx.BidRequest;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.mtgbid.out.BidManager;
import com.mintegral.msdk.out.BannerSize;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGConfiguration;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class MtgBidRequestInfo extends BaseNetworkInfo {
    int bannerWidth = 0;
    int bannerHeight = 0;

    public MtgBidRequestInfo(Context context, String format, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        try {
            try {
                Looper.prepare();
            } catch (Throwable e) {

            }
            JSONObject contentObject = new JSONObject(unitGroupInfo.content);
            String appid = contentObject.optString("appid");
            String appkey = contentObject.optString("appkey");
            String unitid = contentObject.optString("unitid");
            String size = contentObject.optString("size");

            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, String> map = sdk.getMTGConfigurationMap(appid, appkey);
            sdk.init(map, context.getApplicationContext());

            setAppId(appid);
            setUnitId(unitid);
            setFirmId(unitGroupInfo.networkType);
            setFormat(format);
            setBuyerUid(BidManager.getBuyerUid(context));

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
    public String getSDKVersion() {
        try {
            Class mtgConfiguration = Class.forName("com.mintegral.msdk.out.MTGConfiguration");
            for (Field field : mtgConfiguration.getFields()) {
                field.setAccessible(true);
                if (field.getType().toString().endsWith("java.lang.String") && Modifier.isStatic(field.getModifiers())) {
                    String verisonName = field.get(mtgConfiguration).toString();
                    if (verisonName.startsWith("MAL")) {
                        return verisonName;
                    }
                }

            }
            return MTGConfiguration.SDK_VERSION;
        } catch (Throwable e) {

        }
        return "";
    }

    @Override
    public boolean checkNetworkSDK() {
        try {
            Class bidManageClass = BidManager.class;
            MIntegralSDKFactory.getMIntegralSDK();
            return true;
        } catch (Throwable e) {

        }
        return false;
    }

    @Override
    public JSONObject toRequestJSONObject() {
        JSONObject networkObject = new JSONObject();
        try {
            networkObject.put(BidRequest.NETWORK_SDK_VERSION, getSDKVersion());
            networkObject.put(BidRequest.UNIT_ID, getUnitId());
            networkObject.put(BidRequest.APP_ID, getAppId());
            networkObject.put(BidRequest.NW_FIRM_ID, getFirmId());
            networkObject.put(BidRequest.BUYERUID, getBuyerUid());
            networkObject.put(BidRequest.FORMAT, getFormat());

            if (getFormat().equals(Const.FORMAT.BANNER_FORMAT)) {
                networkObject.put(BidRequest.AD_WIDTH, bannerWidth);
                networkObject.put(BidRequest.AD_HEIGHT, bannerHeight);
            }
        } catch (Exception e) {

        }
        return networkObject;
    }

}
