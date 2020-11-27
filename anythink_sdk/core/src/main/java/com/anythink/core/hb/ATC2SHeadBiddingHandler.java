/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb;

import android.text.TextUtils;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.ATHeadBiddingRequest;
import com.anythink.core.common.entity.BaseBiddingResult;
import com.anythink.core.common.entity.BiddingResult;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.core.common.utils.CustomAdapterFactory;
import com.anythink.core.common.utils.NetworkLogUtil;
import com.anythink.core.hb.callback.BiddingCallback;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ATC2SHeadBiddingHandler extends BaseHeadBiddingHandler {

    public static final String TAG = ATC2SHeadBiddingHandler.class.getSimpleName();

    private List<PlaceStrategy.UnitGroupInfo> mProcessSets;//record current request
    private List<PlaceStrategy.UnitGroupInfo> mResultLists;

    private BiddingCallback mBiddingCallback;

    private long mStartBiddingTime;
    private AtomicBoolean isFinishHB = new AtomicBoolean(false);

    public ATC2SHeadBiddingHandler(ATHeadBiddingRequest request) {
        super(request);

        mProcessSets = Collections.synchronizedList(new ArrayList<>(this.mRequest.hbList));
        mResultLists = Collections.synchronizedList(new ArrayList<PlaceStrategy.UnitGroupInfo>(3));
    }

    @Override
    protected void startBidRequest(final BiddingCallback biddingCallback) {
        this.mBiddingCallback = biddingCallback;

        List<PlaceStrategy.UnitGroupInfo> c2sHbList = this.mRequest.hbList;
        int size = c2sHbList.size();

        mStartBiddingTime = System.currentTimeMillis();

        for (int i = 0; i < size; i++) {
            final PlaceStrategy.UnitGroupInfo unitGroupInfo = c2sHbList.get(i);
            ATBaseAdAdapter adapter = CustomAdapterFactory.createAdapter(unitGroupInfo);

            if (adapter == null) {
                ATBiddingResult failedC2SBidingResult = getFailedC2SBidingResult(unitGroupInfo.adapterClassName + "not exist!");
                handleResult(false, failedC2SBidingResult, unitGroupInfo);

            } else {

                try {
                    ATBiddingListener bidListener = new ATBiddingListener() {

                        @Override
                        public void onC2SBidResult(ATBiddingResult bidResult) {
                            handleResult(bidResult.isSuccess, bidResult, unitGroupInfo);
                        }
                    };

                    CommonLogUtil.i(TAG, "start c2s bid request: " + adapter.getNetworkName());
                    Map<String, Object> serviceExtras = CommonUtil.jsonObjectToMap(unitGroupInfo.content);
                    if (!adapter.startBiddingRequest(mRequest.context, serviceExtras, bidListener)) {
                        ATBiddingResult failedC2SBidingResult = getFailedC2SBidingResult("This network don't support head bidding in current TopOn's version.");
                        handleResult(false, failedC2SBidingResult, unitGroupInfo);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    ATBiddingResult failedC2SBidingResult = getFailedC2SBidingResult(e.getMessage());
                    handleResult(false, failedC2SBidingResult, unitGroupInfo);
                }
            }
        }
    }

    @Override
    protected void processUnitGrouInfo(PlaceStrategy.UnitGroupInfo unitGroupInfo, BaseBiddingResult biddingResult, long bidUseTime) {

        if (biddingResult.isSuccess) {
            unitGroupInfo.bidUseTime = bidUseTime;//bid use time
            unitGroupInfo.ecpm = biddingResult.price;
            unitGroupInfo.payload = biddingResult.token;
            unitGroupInfo.sortType = 0;//normal bidrequest

            NetworkLogUtil.headbidingLog(Const.LOGKEY.SUCCESS, mRequest.placementId, CommonSDKUtil.getFormatString(String.valueOf(mRequest.format)), unitGroupInfo);

            /**Add Bid Cache**/
            BiddingResult ats2SBiddingResult = new BiddingResult(true, unitGroupInfo.ecpm, unitGroupInfo.payload, "", "", "");
            ats2SBiddingResult.outDateTime = unitGroupInfo.getBidTokenAvailTime() + System.currentTimeMillis();
            ats2SBiddingResult.expireTime = unitGroupInfo.getBidTokenAvailTime();

            //save hb cache
            BiddingCacheManager.getInstance().addCache(unitGroupInfo.unitId, ats2SBiddingResult);

//            HiBidCache hiBidCache = new HiBidCache();
//            hiBidCache.payLoad = unitGroupInfo.payload;
//            hiBidCache.price = unitGroupInfo.ecpm;
//            hiBidCache.outDateTime = unitGroupInfo.getBidTokenAvailTime() + System.currentTimeMillis();
//            HBC2SCacheManager.getInstance().addCache(unitGroupInfo.unitId, hiBidCache);
        } else {
            unitGroupInfo.bidUseTime = bidUseTime;//bid use time
            unitGroupInfo.ecpm = 0;
            unitGroupInfo.sortType = -1;
            unitGroupInfo.level = -1;
            if (TextUtils.isEmpty(biddingResult.errorMsg)) {
                unitGroupInfo.setErrorMsg("bid error");
            } else {
                unitGroupInfo.setErrorMsg(biddingResult.errorMsg);
            }

            NetworkLogUtil.headbidingLog(Const.LOGKEY.FAIL, mRequest.placementId, CommonSDKUtil.getFormatString(String.valueOf(mRequest.format)), unitGroupInfo);
        }
    }

    private synchronized void handleResult(boolean isSuccess, ATBiddingResult ATBiddingResult, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        ATC2SHeadBiddingHandler.this.processUnitGrouInfo(unitGroupInfo, ATBiddingResult, System.currentTimeMillis() - mStartBiddingTime);

        if (!isFinishHB.get()) {
            mResultLists.add(unitGroupInfo);
            mProcessSets.remove(unitGroupInfo);
            if (mBiddingCallback != null) {

                if (isSuccess) {
                    mBiddingCallback.onBiddingSuccess(mResultLists);
                } else {
                    mBiddingCallback.onBiddingFailed(mResultLists);
                }
            }
            mResultLists.remove(unitGroupInfo);

            if (mProcessSets.size() == 0) {//All results have been returned, notify ATHeadBiddingHandler of the end
                if (mBiddingCallback != null) {
                    mBiddingCallback.onBiddingFinished();
                }
            }
        }
    }

    private ATBiddingResult getFailedC2SBidingResult(String errorMsg) {
        return ATBiddingResult.fail(errorMsg);
    }

    @Override
    public synchronized void onTimeout() {
        if (!isFinishHB.get()) {
            isFinishHB.set(true);

            CommonLogUtil.i(TAG, "c2s bid request timeout");
            //bid timeout, return all remainder
            for (PlaceStrategy.UnitGroupInfo unitGroupInfo : mProcessSets) {
                mResultLists.add(unitGroupInfo);

                ATBiddingResult failedC2SBidingResult = getFailedC2SBidingResult("bid timeout!");
                ATC2SHeadBiddingHandler.this.processUnitGrouInfo(unitGroupInfo, failedC2SBidingResult, System.currentTimeMillis() - mStartBiddingTime);
            }
            mProcessSets.clear();

            if (mBiddingCallback != null) {
                mBiddingCallback.onBiddingFailed(mResultLists);
            }
            mResultLists.clear();

            if (mBiddingCallback != null) {
                mBiddingCallback.onBiddingFinished();
            }
            mBiddingCallback = null;
        }
    }

}
