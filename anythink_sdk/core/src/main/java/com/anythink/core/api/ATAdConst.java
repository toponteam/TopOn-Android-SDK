/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

public class ATAdConst {
    public static class KEY {
        public static final String AD_WIDTH = "key_width";
        public static final String AD_HEIGHT = "key_height";
        public static final String AD_ORIENTATION = "ad_orientation";
        public static final String AD_SOUND = "ad_sound";
        public static final String USER_ID = "user_id";
        public static final String USER_CUSTOM_DATA = "user_custom_data";
        public static final String REWARD_NAME = "reward_name";
        public static final String REWARD_AMOUNT = "reward_amount";


        public static final String AD_IS_SUPPORT_DEEP_LINK = "ad_is_support_deep_link";

        public static final String AD_CLICK_CONFIRM_STATUS = "ad_click_confirm_status";
    }

    public static class NETWORK_CUSTOM_KEY {
        public static final String OFFER_ID = "offer_id";
        public static final String CREATIVE_ID = "creative_id";
        public static final String IS_DEEPLINK_OFFER = "is_deeplink";

        public static final String NETWORK_ID = "network_id";
        public static final String NETWORK_UNIT_ID = "network_unit_id";
        public static final String NETWORK_ECPM = "network_ecpm";
    }

    public static final int ORIENTATION_VERTICAL = 1;
    public static final int ORIENTATION_HORIZONTAL = 2;
}
