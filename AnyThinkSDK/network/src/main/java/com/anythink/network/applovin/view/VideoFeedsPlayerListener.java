package com.anythink.network.applovin.view;


/**
 * play callback
 *
 * @author simon
 */
public interface VideoFeedsPlayerListener {

    /**
     * start play
     */
    void onPlayStarted(int allDuration);

    /**
     * play completed
     */
    void onPlayCompleted();

    /**
     * play failed
     *
     * @param errorStr Failed message
     */
    void onPlayError(String errorStr);

    /**
     * Playback progress
     */
    void onPlayProgress(int curPlayPosition, int allDuration);

    /**
     * start buffer
     *
     * @param bufferMsg
     */
    void OnBufferingStart(String bufferMsg);

    /**
     * end buffer
     */
    void OnBufferingEnd();

    /**
     * setDataSourceError
     *
     * @param errorStr
     */
    void onPlaySetDataSourceError(String errorStr);

    /***
     * restart
     * @param curPlayPosition
     */
    void onPalyRestart(int curPlayPosition, int allDuration);

    /***
     * pause
     */
    void onPalyPause(int curPlayPosition);

    /***
     * resume
     */
    void onPalyResume(int curPlayPosition);

    /***
     * status of sound
     * @param soundopen
     */
    void onSoundStat(boolean soundopen);

//

    /***
     * close playback
     */
    void onPlayClose();

    /***
     * click in endScreen
     */
    void onAdClicked();

    /***
     * close endScreen
     */
    void closeADView();

    /***
     * init callback
     */
    void onInitCallBack(boolean intiState);

}
