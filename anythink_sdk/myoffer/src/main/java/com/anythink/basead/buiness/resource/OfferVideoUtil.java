/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness.resource;

import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import java.io.FileDescriptor;

public class OfferVideoUtil {


    public static class Size {
        public int width;
        public int height;
    }


    public static Size getVideoSize(String path) {

        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Size size = null;
        try {
            size = new Size();

            //1.Init
            MediaMetadataRetriever mMetadataRetriever = new MediaMetadataRetriever();
            //2.Source Path
            mMetadataRetriever.setDataSource(path);
            //3.Get video width
            String width = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            //4.Get video height
            String height = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

            mMetadataRetriever.release();

            size.width = Integer.parseInt(width);
            size.height = Integer.parseInt(height);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return size;
    }

    public static Size getVideoSize(FileDescriptor fd) {

        if (fd == null) {
            return null;
        }
        Size size = null;
        try {
            size = new Size();

            //1.Init
            MediaMetadataRetriever mMetadataRetriever = new MediaMetadataRetriever();
            //2.Source Path
            mMetadataRetriever.setDataSource(fd);
            //3.Get video width
            String width = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            //4.Get video height
            String height = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

            mMetadataRetriever.release();

            size.width = Integer.parseInt(width);
            size.height = Integer.parseInt(height);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return size;
    }


    public static Size getAdaptiveVideoSize(String path, int destWidth, int destHeight) {

        Size videoSize = getVideoSize(path);
        if (videoSize == null) {
            return null;
        }

        int videoWidth = videoSize.width;
        int videoHeight = videoSize.height;

        float ratio_video = videoWidth * 1f / videoHeight;
        float ratio_dest = destWidth * 1f / destHeight;


        if (ratio_video < ratio_dest) {
            videoSize.height = destHeight;
            videoSize.width = (int) (videoSize.height * ratio_video);

        } else {
            videoSize.width = destWidth;
            videoSize.height = (int) (videoSize.width / ratio_video);

        }
        return videoSize;
    }

    public static Size getAdaptiveVideoSize(FileDescriptor fd, int destWidth, int destHeight) {

        Size videoSize = getVideoSize(fd);
        if (videoSize == null) {
            return null;
        }

        int videoWidth = videoSize.width;
        int videoHeight = videoSize.height;

        float ratio_video = videoWidth * 1f / videoHeight;
        float ratio_dest = destWidth * 1f / destHeight;


        if (ratio_video < ratio_dest) {
            videoSize.height = destHeight;
            videoSize.width = (int) (videoSize.height * ratio_video);

        } else {
            videoSize.width = destWidth;
            videoSize.height = (int) (videoSize.width / ratio_video);

        }
        return videoSize;
    }
}
