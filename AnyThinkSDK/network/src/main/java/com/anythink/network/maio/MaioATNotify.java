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
