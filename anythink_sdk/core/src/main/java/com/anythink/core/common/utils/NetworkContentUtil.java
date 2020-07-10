package com.anythink.core.common.utils;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkContentUtil {

    private static Map<Integer, String> sNetworkKeyMap = new HashMap<>();

    static {
        sNetworkKeyMap.put(1, "unit_id");
        sNetworkKeyMap.put(2, "unit_id");
        sNetworkKeyMap.put(3, "unit_id");
        sNetworkKeyMap.put(4, "ad_space");//flurry
        sNetworkKeyMap.put(5, "zone_id");
        sNetworkKeyMap.put(6, "unitid");
        sNetworkKeyMap.put(7, "unitid");
        sNetworkKeyMap.put(8, "unit_id");
        sNetworkKeyMap.put(9, "location");
        sNetworkKeyMap.put(10, "placement_name");
        sNetworkKeyMap.put(11, "instance_id");
        sNetworkKeyMap.put(12, "placement_id");
        sNetworkKeyMap.put(13, "placement_id");
        sNetworkKeyMap.put(14, "zone_id");
        sNetworkKeyMap.put(15, "slot_id");
//        sNetworkKeyMap.put(16, "app_id");//uniplay
        sNetworkKeyMap.put(17, "slot_id");//oneway
        sNetworkKeyMap.put(19, "slot_id");
        sNetworkKeyMap.put(21, "placement_id");
        sNetworkKeyMap.put(22, "ad_place_id");
        sNetworkKeyMap.put(23, "spot_id");
        sNetworkKeyMap.put(24, "zone_id");
        sNetworkKeyMap.put(25, "ad_tag");
        sNetworkKeyMap.put(26, "placement_id");
//        sNetworkKeyMap.put(27, "app_key");//luomi
        sNetworkKeyMap.put(28, "position_id");
        sNetworkKeyMap.put(29, "placement_id");
        sNetworkKeyMap.put(35, "my_oid");
        sNetworkKeyMap.put(36, "unit_id");
        sNetworkKeyMap.put(37, "spot_id");
    }

    public static String getNetworkPlacementId(int networkFirmId, String networkContentJson) {
        if (TextUtils.isEmpty(networkContentJson)) {
            return null;
        }

        String key = sNetworkKeyMap.get(networkFirmId);
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject(networkContentJson);
            return jsonObject.optString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
