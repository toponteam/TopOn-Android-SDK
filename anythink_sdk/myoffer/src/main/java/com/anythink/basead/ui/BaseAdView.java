package com.anythink.basead.ui;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.anythink.basead.buiness.OfferClickController;
import com.anythink.basead.entity.AdClickRecord;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.impression.ImpressionController;
import com.anythink.basead.impression.ImpressionTracker;
import com.anythink.basead.innerad.onlineapi.OnlineApiAdCacheManager;
import com.anythink.basead.innerad.utils.OwnOfferImpressionRecordManager;
import com.anythink.basead.myoffer.manager.MyOfferImpressionRecordManager;
import com.anythink.core.common.adx.AdxCacheController;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.OnlineApiOffer;
import com.anythink.core.common.entity.OwnBaseAdContent;
import com.anythink.core.hb.BiddingCacheManager;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdView extends RelativeLayout {

    BaseAdRequestInfo mBaseAdRequestInfo;
    BaseAdContent mBaseAdContent;

    ImpressionTracker mImpressionTracker;
    OfferClickController mOfferClickController;

    boolean hasRecordImpression;
    boolean hasDeleteCache;

    int recordTouchDownX;
    int recordTouchDownY;
    int recordTouchUpX;
    int recordTouchUpY;

    int recordTouchDownRelateX;
    int recordTouchDownRelateY;
    int recordTouchUpRelateX;
    int recordTouchUpRelateY;

    String mScenario;

    List<View> mClickViewLists;

    public BaseAdView(Context context, BaseAdRequestInfo baseAdRequestInfo, BaseAdContent baseAdContent, String scenario) {
        super(context);

        mBaseAdRequestInfo = baseAdRequestInfo;
        mBaseAdContent = baseAdContent;
        mScenario = scenario;

        mClickViewLists = new ArrayList<>();

        initContentView();
    }

    public BaseAdView(Context context, BaseAdRequestInfo baseAdRequestInfo, BaseAdContent baseAdContent) {
        this(context, baseAdRequestInfo, baseAdContent, "");
    }

    public BaseAdView(Context context) {
        super(context);
    }

    protected abstract void initContentView();

    protected abstract void notifyShow();

    protected abstract void notifyClick();

    protected abstract void notifyDeeplinkCallback(boolean isSuccess);

    protected void onClickStart() {
    }

    protected void onClickEnd() {
    }

    protected synchronized void onShow() {
        this.recordImpression();
    }

    protected void onClick() {

        if (mOfferClickController == null) {
            mOfferClickController = new OfferClickController(getContext(), mBaseAdRequestInfo, mBaseAdContent);
        }

        UserOperateRecord userOperateRecord = createUserOperateRecord();
        userOperateRecord.adClickRecord = getAdClickRecord();

        mOfferClickController.startClick(userOperateRecord, new OfferClickController.ClickStatusCallback() {
            @Override
            public void clickStart() {
                onClickStart();
            }

            @Override
            public void clickEnd() {
                onClickEnd();
            }

            @Override
            public void deeplinkCallback(boolean isSuccess) {
                notifyDeeplinkCallback(isSuccess);
            }

        });

        notifyClick();
    }

    protected void removeCache() {
        if (hasDeleteCache) {
            return;
        }
        hasDeleteCache = true;

        if (mBaseAdContent instanceof OnlineApiOffer) {
            OnlineApiAdCacheManager.getInstance().removeOnlineApiOfferInfo(getContext(), OnlineApiAdCacheManager.getInstance().getOnlineApiSaveId(mBaseAdRequestInfo));
        }

        try {
            if (mBaseAdContent instanceof AdxOffer) {
                BiddingCacheManager.getInstance().removeCache(mBaseAdRequestInfo.adsourceId, Const.NETWORK_FIRM.ADX_NETWORK_FIRM_ID);
                AdxCacheController.getInstance().removeAdxOfferInfo(getContext(), ((AdxOffer) mBaseAdContent).getBidId());
            }
        } catch (Throwable e) {

        }
    }

    protected void registerImpressionTracker(int delay, final Runnable action) {
        if (delay > 0) {
            mImpressionTracker = new ImpressionTracker(getContext(), delay);
        } else {
            mImpressionTracker = new ImpressionTracker(getContext());
        }

        mImpressionTracker.addView(this, new ImpressionController() {
            @Override
            public void recordImpression(View view) {
                if (action != null) {
                    action.run();
                }
            }
        });

    }

    protected void registerImpressionTracker(Runnable action) {
        this.registerImpressionTracker(0, action);
    }

    protected void recordImpression() {
        if (hasRecordImpression) {
            return;
        }
        hasRecordImpression = true;

        if (mBaseAdContent instanceof MyOfferAd) {
            MyOfferImpressionRecordManager.getInstance(getContext()).recordImpression((MyOfferAd) mBaseAdContent);
        } else if (mBaseAdContent instanceof OwnBaseAdContent) {
            OwnOfferImpressionRecordManager.getInstance().recordOfferImpression(getContext(), OwnOfferImpressionRecordManager.getRecordId(mBaseAdRequestInfo.placementId, mBaseAdRequestInfo.adsourceId), mBaseAdContent, mBaseAdRequestInfo.baseAdSetting);
        }

        notifyShow();
    }

    protected void destroy() {

        if (mOfferClickController != null) {
            mOfferClickController.cancelClick();
            mOfferClickController = null;
        }

        if (mImpressionTracker != null) {
            mImpressionTracker.destroy();
            mImpressionTracker = null;
        }

        mBaseAdRequestInfo = null;
        mBaseAdContent = null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                recordTouchDownX = (int) ev.getRawX();
                recordTouchDownY = (int) ev.getRawY();

                recordTouchDownRelateX = (int) ev.getX();
                recordTouchDownRelateY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                recordTouchUpX = (int) ev.getRawX();
                recordTouchUpY = (int) ev.getRawY();

                recordTouchUpRelateX = (int) ev.getX();
                recordTouchUpRelateY = (int) ev.getY();

                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    protected UserOperateRecord createUserOperateRecord() {
        UserOperateRecord userOperateRecord = new UserOperateRecord(mBaseAdRequestInfo.requestId, "");
        userOperateRecord.realWidth = getWidth();
        userOperateRecord.realHeight = getHeight();
        return userOperateRecord;
    }

    protected AdClickRecord getAdClickRecord() {
        AdClickRecord adClickRecord = new AdClickRecord();
        adClickRecord.clickDownX = recordTouchDownX;
        adClickRecord.clickDownY = recordTouchDownY;
        adClickRecord.clickUpX = recordTouchUpX;
        adClickRecord.clickUpY = recordTouchUpY;

        adClickRecord.clickRelateDownX = recordTouchDownRelateX;
        adClickRecord.clickRelateDownY = recordTouchDownRelateY;
        adClickRecord.clickRelateUpX = recordTouchUpRelateX;
        adClickRecord.clickRelateUpY = recordTouchUpRelateY;

        return adClickRecord;
    }


}
