/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.impression;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Impression tracker used to call {@link ImpressionInterface#recordImpression(View)} when a
 * percentage of a native ad has been on screen for a duration of time.
 */
public class ImpressionTracker {

    private static final int PERIOD = 250;

    // Object tracking visibility of added views
    private final VisibilityTracker mVisibilityTracker;

    // All views and ads being tracked for impressions
    private final Map<View, ImpressionInterface> mTrackedViews;

    // Visible views being polled for time on screen before tracking impression
    private final Map<View, TimestampWrapper<ImpressionInterface>> mPollingViews;

    // Handler for polling visible views
    private final Handler mPollHandler;

    // Runnable to run on each visibility loop
    private final PollingRunnable mPollingRunnable;

    // Object to check actual visibility
    private final VisibilityTracker.VisibilityChecker mVisibilityChecker;

    // Listener for when a view becomes visible or non visible
    private VisibilityTracker.VisibilityTrackerListener mVisibilityTrackerListener;

    public ImpressionTracker( final Context context) {
        this(new WeakHashMap<View, ImpressionInterface>(),
                new WeakHashMap<View, TimestampWrapper<ImpressionInterface>>(),
                new VisibilityTracker.VisibilityChecker(),
                new VisibilityTracker(context),
                new Handler(Looper.getMainLooper()));
    }

    public ImpressionTracker( final Context context, int delayTime) {
        this(new WeakHashMap<View, ImpressionInterface>(),
                new WeakHashMap<View, TimestampWrapper<ImpressionInterface>>(),
                new VisibilityTracker.VisibilityChecker(),
                new VisibilityTracker(context, delayTime),
                new Handler(Looper.getMainLooper()));
    }

    ImpressionTracker( final Map<View, ImpressionInterface> trackedViews,
             final Map<View, TimestampWrapper<ImpressionInterface>> pollingViews,
             final VisibilityTracker.VisibilityChecker visibilityChecker,
             final VisibilityTracker visibilityTracker,
             final Handler handler) {
        mTrackedViews = trackedViews;
        mPollingViews = pollingViews;
        mVisibilityChecker = visibilityChecker;
        mVisibilityTracker = visibilityTracker;

        mVisibilityTrackerListener = new VisibilityTracker.VisibilityTrackerListener() {
            @Override
            public void onVisibilityChanged( final List<View> visibleViews,  final List<View> invisibleViews) {
                for (final View view : visibleViews) {
                    // It's possible for native ad to be null if the view was GC'd from this class
                    // but not from VisibilityTracker
                    // If it's null then clean up the view from this class
                    final ImpressionInterface impressionInterface = mTrackedViews.get(view);
                    if (impressionInterface == null) {
                        removeView(view);
                        continue;
                    }

                    // If the native ad is already polling, don't recreate it
                    final TimestampWrapper<ImpressionInterface> polling = mPollingViews.get(view);
                    if (polling != null && impressionInterface.equals(polling.mInstance)) {
                        continue;
                    }

                    // Add a new polling view
                    mPollingViews.put(view, new TimestampWrapper<ImpressionInterface>(impressionInterface));
                }

                for (final View view : invisibleViews) {
                    mPollingViews.remove(view);
                }
                scheduleNextPoll();
            }
        };
        mVisibilityTracker.setVisibilityTrackerListener(mVisibilityTrackerListener);

        mPollHandler = handler;
        mPollingRunnable = new PollingRunnable();
    }

    /**
     * Tracks the given view for impressions.
     */
    public void addView(final View view,  final ImpressionInterface impressionInterface) {
        // View is already associated with the same native ad
        if (mTrackedViews.get(view) == impressionInterface) {
            return;
        }

        // Clean up state if view is being recycled and associated with a different ad
        removeView(view);

        if (impressionInterface.isImpressionRecorded()) {
            return;
        }

        mTrackedViews.put(view, impressionInterface);
        mVisibilityTracker.addView(view, impressionInterface.getImpressionMinPercentageViewed(),
                impressionInterface.getImpressionMinVisiblePx());
    }

    public void removeView(final View view) {
        mTrackedViews.remove(view);
        removePollingView(view);
        mVisibilityTracker.removeView(view);
    }

    /**
     * Immediately clear all views. Useful for when we re-request ads for an ad placer
     */
    public void clear() {
        mTrackedViews.clear();
        mPollingViews.clear();
        mVisibilityTracker.clear();
        mPollHandler.removeMessages(0);
    }

    public void destroy() {
        clear();
        mVisibilityTracker.destroy();
        mVisibilityTrackerListener = null;
    }

    void scheduleNextPoll() {
        // Only schedule if there are no messages already scheduled.
        if (mPollHandler.hasMessages(0)) {
            return;
        }

        mPollHandler.postDelayed(mPollingRunnable, PERIOD);
    }

    private void removePollingView(final View view) {
        mPollingViews.remove(view);
    }

    class PollingRunnable implements Runnable {
        // Create this once to avoid excessive garbage collection observed when calculating
        // these on each pass.

        private final ArrayList<View> mRemovedViews;

        PollingRunnable() {
            mRemovedViews = new ArrayList<View>();
        }

        @Override
        public void run() {
            for (final Map.Entry<View, TimestampWrapper<ImpressionInterface>> entry : mPollingViews.entrySet()) {
                final View view = entry.getKey();
                final TimestampWrapper<ImpressionInterface> timestampWrapper = entry.getValue();

                // If it's been visible for the min impression time, trigger the callback
                if (!mVisibilityChecker.hasRequiredTimeElapsed(
                        timestampWrapper.mCreatedTimestamp,
                        timestampWrapper.mInstance.getImpressionMinTimeViewed())) {
                    continue;
                }

                timestampWrapper.mInstance.recordImpression(view);
                timestampWrapper.mInstance.setImpressionRecorded();

                // Removed in a separate loop to avoid a ConcurrentModification exception.
                mRemovedViews.add(view);
            }

            for (View view : mRemovedViews) {
              removeView(view);
            }
            mRemovedViews.clear();

            if (!mPollingViews.isEmpty()) {
                scheduleNextPoll();
            }
        }
    }

    @Deprecated
    VisibilityTracker.VisibilityTrackerListener getVisibilityTrackerListener() {
        return mVisibilityTrackerListener;
    }
}
