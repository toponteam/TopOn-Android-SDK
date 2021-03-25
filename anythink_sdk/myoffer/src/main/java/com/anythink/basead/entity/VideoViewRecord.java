/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.entity;

public class VideoViewRecord {
    public int videoLength;
    public int videoStartTime;
    public int videoEndTime;
    public int isVideoPlayInStart; //Is video playing in first frame? 1: Yes 2: No
    public int isVideoPlayInEnd; //Is video playing end in last frame? 1: Yes 2: No

    public long videoStartUTCMillTime;
    public long videoEndUTCMillTime;
    public int videoCurrentMillPosition; //video current progress, unit: millsecond
    public int videoDirectTrackingProgress;
    /**
     * 1 - Play in the advertising exposure area；
     * 2 - Full screen and vertical screen, showing only video；
     * 3 - Full-screen vertical screen, video displayed at the top of the screen, and ads displayed at the bottom of the screen to promote the landing page (refer to the appendix ad rendering style example, only applicable to ads whose interactive type is to open the web page. The landing page is obtained from the click report return data, see click for details Report response data part)；
     * 4 - Full screen horizontal screen, only video is displayed；
     * 0 - Other developer custom scenes
     */
    public static final int PORTRAIT_VIDEO_FULLSCREEN = 1;
    public static final int LANDSCAPE_VIDEO_FULLSCREEN = 4;
    public int viodePlayScence;

    /**
     * 1 - Play for the first time；
     * 2 - Resume playing after pausing；
     * 3 - Restart playback。(only native)
     */
    public static final int FIRST_PLAY_VIDEO_TYPE = 1;
    public static final int RE_PLAY_VIDEO_TYPE = 2;
    public int videoPlayType;

    /**
     * 1 - Auto play (recommended when the internet connection is wifi or 4G, set the video to auto play)；
     * 2 - Click to play
     */
    public static final int AUTO_PLAY_BEHAVIOR = 1;
    public static final int HANDLE_PLAY_BEHAVIOR = 2;
    public int videoPlayBehavior;

    /**
     * 0 - normal playback；
     * 1 - The video is loading; (only Native ads exist)
     * 2 - Playback error。
     */
    public static final int CORRECT_PLAY_STATUS = 0;
    public static final int ERROR_PLAY_STATUS = 2;
    public int videoPlayStatus;


    @Override
    public String toString() {
        return "VideoViewRecord{" +
                "videoLength=" + videoLength +
                ", videoStartTime=" + videoStartTime +
                ", videoEndTime=" + videoEndTime +
                ", isVideoPlayInStart=" + isVideoPlayInStart +
                ", isVideoPlayInEnd=" + isVideoPlayInEnd +
                ", viodePlayScence=" + viodePlayScence +
                ", videoPlayType=" + videoPlayType +
                ", videoPlayBehavior=" + videoPlayBehavior +
                ", videoPlayStatus=" + videoPlayStatus +
                '}';
    }
}
