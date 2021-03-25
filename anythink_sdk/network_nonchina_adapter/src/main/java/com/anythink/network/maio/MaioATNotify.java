/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.maio;

public interface MaioATNotify {
    public void notifyLoaded();

    public void notifyLoadFail(String code, String msg);

    public void notifyPlayStart();

    public void notifyClick();

    public void notifyClose();

    public void notifyPlayEnd(boolean isReward);

    public void notifyPlayFail(String code, String msg);
}
