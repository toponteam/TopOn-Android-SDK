package com.anythink.core.common.entity;

import android.text.TextUtils;

import com.anythink.core.api.ATRewardInfo;
import com.anythink.core.common.net.TrackingV2Loader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Z on 2018/1/10.
 */

public class AdTrackingInfo extends TrackerInfo {


    public static final int AUTO_REQUEST = 1;
    public static final int HANDLE_REQUEST = 2;
    public static final int READY_CHECK = 3;
    public static final int UPSTATUS_REQUEST = 4;
    public static final int CACHE_NUM_REQUEST = 5;

    public static final int NORMAL_CALLBACK = 0;
    public static final int SHORT_OVERTIME_CALLBACK = 1;
    public static final int LONG_OVERTIME_ERROR_CALLBACK = 2;

    public static final int NO_SHOW_CACHE = 0;
    public static final int SHOW_HIGH_LEVEL_CACHE = 1;
    public static final int SHOW_LOW_LEVEL_CACHE = 2;


    private int mGroupId;

    private int mNetworkType; //Mediation type
    private String mSourceType; //Source type: image or video
    private String mNetworkContent; //AdSource placement info
    private int mHourlyFrequency; //Hourly impression
    private int mDailyFrequency; //Daily impression
    private String mNetworkList; //Waterfall list
    private int mRequestNetworkNum; //The number of requesting AdSource
    private int mRefresh; //Refresh type, 0：non-refresh，1：refresh
    private int mLevel; //requestLevel
    private int mImpressionLevel; //impression Level
    private boolean mIsDefaultNetwork;
    private long mDataFillTime; //Ad data fill time
    private long mFillTime; //Fill time
    private String mUnitGroupUnitId; //Adsource id

    public String mUserId = "";
    public int mAutoRequest = 0;
    public int mLoadStatus = 0;
    public int mFlag = 0;
    public int mProgress;

    public String mNetworkVersion;

    public String mShowCurrentRequestId; //Current RequestId

    int mBidType; //Bid type
    double mBidPrice; //Bid price

    public int mMyOfferShowType;//1:Default MyOffer，0：Normal MyOffer

    int mShowSwitch;
    int mClickTkSwtich;

    public String mScenario; //Scenario of impression

    private String mNetworkPlacementId;
    private String mShowId;

    private int mEcpmLevel;
    private String mEcpmPrecision;

    private String mCountry;
    private String mCurrency;

    private ATRewardInfo mPlacementRewardInfo;

    private Map<String, ATRewardInfo> mScenarioRewardMap;
    private Map<String, Object> mCustomRule;

    protected int adTypeDayShowTime; //format day show time
    protected int adTypeHourShowTime; //format hour show time
    protected int placementDayShowTime; //placement day show time
    protected int placementHourShowTime; //placement hour show time

    private long mHBWaitingToRequestTime;
    private long mHBBidTimeout;

    public long getmHBWaitingToRequestTime() {
        return mHBWaitingToRequestTime;
    }

    public void setmHBWaitingToRequestTime(long mHBWaitingToRequestTime) {
        this.mHBWaitingToRequestTime = mHBWaitingToRequestTime;
    }

    public long getmHBBidTimeout() {
        return mHBBidTimeout;
    }

    public void setmHBBidTimeout(long mHBBidTimeout) {
        this.mHBBidTimeout = mHBBidTimeout;
    }

    public void setAdTypeDayShowTime(int adTypeDayShowTime) {
        this.adTypeDayShowTime = adTypeDayShowTime;
    }

    public void setAdTypeHourShowTime(int adTypeHourShowTime) {
        this.adTypeHourShowTime = adTypeHourShowTime;
    }

    public void setPlacementDayShowTime(int placementDayShowTime) {
        this.placementDayShowTime = placementDayShowTime;
    }

    public void setPlacementHourShowTime(int placementHourShowTime) {
        this.placementHourShowTime = placementHourShowTime;
    }


    public String getmNetworkPlacementId() {
        return mNetworkPlacementId;
    }

    public void setmNetworkPlacementId(String mNetworkPlacementId) {
        this.mNetworkPlacementId = mNetworkPlacementId;
    }

    public String getmShowId() {
        return mShowId;
    }

    public void setmShowId(String mShowId) {
        this.mShowId = mShowId;
    }

    public int getmEcpmLevel() {
        return mEcpmLevel;
    }

    public void setmEcpmLevel(int mEcpmLevel) {
        this.mEcpmLevel = mEcpmLevel;
    }

    public String getmEcpmPrecision() {
        return mEcpmPrecision;
    }

    public void setmEcpmPrecision(String mEcpmPrecision) {
        this.mEcpmPrecision = mEcpmPrecision;
    }

    public String getmCountry() {
        return mCountry;
    }

    public void setmCountry(String mCountry) {
        this.mCountry = mCountry;
    }

    public String getmCurrency() {
        return mCurrency;
    }

    public void setmCurrency(String mCurrency) {
        this.mCurrency = mCurrency;
    }

    public Map<String, ATRewardInfo> getmScenarioRewardMap() {
        return mScenarioRewardMap;
    }

    public void setmScenarioRewardMap(Map<String, ATRewardInfo> mScenarioRewardMap) {
        this.mScenarioRewardMap = mScenarioRewardMap;
    }

    public ATRewardInfo getmPlacementRewardInfo() {
        return mPlacementRewardInfo;
    }

    public void setmPlacementRewardInfo(ATRewardInfo mPlacementRewardInfo) {
        this.mPlacementRewardInfo = mPlacementRewardInfo;
    }

    public Map<String, Object> getmCustomRule() {
        return mCustomRule;
    }

    public void setmCustomRule(Map<String, Object> mCustomRule) {
        this.mCustomRule = mCustomRule;
    }

    public String getmScenario() {
        return mScenario;
    }

    public void setmScenario(String mScenario) {
        this.mScenario = mScenario;
    }

    public int getmShowTkSwitch() {
        return mShowSwitch;
    }

    public void setmShowTkSwitch(int mShowSwitch) {
        this.mShowSwitch = mShowSwitch;
    }

    public int getmClickTkSwtich() {
        return mClickTkSwtich;
    }

    public void setmClickTkSwtich(int mClickTkSwtich) {
        this.mClickTkSwtich = mClickTkSwtich;
    }

    public int getMyOfferShowType() {
        return mMyOfferShowType;
    }

    public void setMyOfferShowType(int type) {
        mMyOfferShowType = type;
    }

    public int getmBidType() {
        return mBidType;
    }

    public void setmBidType(int mBidType) {
        this.mBidType = mBidType;
    }

    public double getmBidPrice() {
        return mBidPrice;
    }

    public void setmBidPrice(double mBidPrice) {
        this.mBidPrice = mBidPrice;
    }

    public String getmNetworkVersion() {
        return mNetworkVersion;
    }

    public void setmNetworkVersion(String mNetworkVersion) {
        this.mNetworkVersion = mNetworkVersion;
    }

    public String getmUnitGroupUnitId() {
        return mUnitGroupUnitId;
    }

    public void setmUnitGroupUnitId(String mUnitGroupUnitId) {
        this.mUnitGroupUnitId = mUnitGroupUnitId;
    }

    public boolean ismIsDefaultNetwork() {
        return mIsDefaultNetwork;
    }

    public void setmIsDefaultNetwork(boolean mIsDefaultNetwork) {
        this.mIsDefaultNetwork = mIsDefaultNetwork;
    }

    public int getRequestLevel() {
        return mLevel;
    }

    public void setRequestLevel(int mLevel) {
        this.mLevel = mLevel;
    }

    public int getImpressionLevel() {
        return mImpressionLevel;
    }

    public void setImpressionLevel(int impressionLevel) {
        this.mImpressionLevel = impressionLevel;
    }

    public int getmHourlyFrequency() {
        return mHourlyFrequency;
    }

    public void setmHourlyFrequency(int mHourlyFrequency) {
        this.mHourlyFrequency = mHourlyFrequency;
    }

    public int getmDailyFrequency() {
        return mDailyFrequency;
    }

    public void setmDailyFrequency(int mDailyFrequency) {
        this.mDailyFrequency = mDailyFrequency;
    }

    public String getmNetworkList() {
        return mNetworkList;
    }

    public void setmNetworkList(String mNetworkList) {
        this.mNetworkList = mNetworkList;
    }

    public int getmRequestNetworkNum() {
        return mRequestNetworkNum;
    }

    public void setmRequestNetworkNum(int mRequestNetworkNum) {
        this.mRequestNetworkNum = mRequestNetworkNum;
    }

    public int getmRefresh() {
        return mRefresh;
    }

    public void setmRefresh(int mRefresh) {
        this.mRefresh = mRefresh;
    }


    public String getmNetworkContent() {
        return mNetworkContent;
    }

    public void setmNetworkContent(String mNetworkContent) {
        this.mNetworkContent = mNetworkContent;
    }


    public int getmNetworkType() {
        return mNetworkType;
    }


    public void setmNetworkType(int mNetworkType) {
        this.mNetworkType = mNetworkType;
    }

    public String getmSourceType() {
        return mSourceType;
    }

    public void setmSourceType(String mSourceType) {
        this.mSourceType = mSourceType;
    }


    public int getmGroupId() {
        return mGroupId;
    }

    public void setmGroupId(int mGroupId) {
        this.mGroupId = mGroupId;
    }

    public void setCurrentRequestId(String currentRequestId) {
        mShowCurrentRequestId = currentRequestId;
    }


    public void setUserInfo(String userid) {

        if (!TextUtils.isEmpty(userid)) {
            mUserId = userid;
        }
    }

    public String getUserInfo() {
        return mUserId;
    }

    public void setRequestType(int autoRequest) {
        mAutoRequest = autoRequest;
    }

    public int getRequestType() {
        return mAutoRequest;
    }


    public void setLoadStatus(int status) {
        mLoadStatus = status;
    }

    public int getLoadStatus() {
        return mLoadStatus;
    }


    public void setFlag(int flag) {
        mFlag = flag;
    }

    public long getDataFillTime() {
        return mDataFillTime;
    }

    public void setDataFillTime(long mDataFillTime) {
        this.mDataFillTime = mDataFillTime;
    }

    public void setFillTime(long fillTime) {
        mFillTime = fillTime;
    }

    public long getFillTime() {
        return mFillTime;
    }


    public int getmProgress() {
        return mProgress;
    }

    public void setmProgress(int mProgress) {
        this.mProgress = mProgress;
    }

    /***----------------------------------------Anythink SDK Tracking-----------------------------------------------**/
    boolean mIsLoad;
    int mReason;
    String mAsResult;

    public boolean ismIsLoad() {
        return mIsLoad;
    }

    public void setmIsLoad(boolean mIsLoad) {
        this.mIsLoad = mIsLoad;
    }

    public int getmReason() {
        return mReason;
    }

    public void setmReason(int mReason) {
        this.mReason = mReason;
    }

    public String getmAsResult() {
        return mAsResult;
    }

    public void setmAsResult(String mAsResult) {
        this.mAsResult = mAsResult;
    }


    /***----------------------------------------headbidding Tracking-----------------------------------------------**/
    long mHbStartTime;
    long mHbEndTime;
    String mHbResultList;

    public long getmHbStartTime() {
        return mHbStartTime;
    }

    public void setmHbStartTime(long mHbStartTime) {
        this.mHbStartTime = mHbStartTime;
    }

    public long getmHbEndTime() {
        return mHbEndTime;
    }

    public void setmHbEndTime(long mHbEndTime) {
        this.mHbEndTime = mHbEndTime;
    }

    public String getmHbResultList() {
        return mHbResultList;
    }

    public void setmHbResultList(String mHbResultList) {
        this.mHbResultList = mHbResultList;
    }

    /***----------------------------------------Difference Tracking Type---------------------------------------------**/


    @Override
    public JSONObject toJSONObject(int trakcingType) {
        JSONObject jsonObject = super.toJSONObject(trakcingType);
        try {
            jsonObject.put("nw_ver", mNetworkVersion);
            jsonObject.put("refresh", mRefresh);
            switch (trakcingType) {
                case TrackingV2Loader.AD_REQUEST_TYPE:
                    jsonObject.put("asid", mAsid);
                    jsonObject.put("unit_id", mUnitGroupUnitId);
                    jsonObject.put("nw_firm_id", mNetworkType);
                    jsonObject.put("gro_id", mGroupId);
                    jsonObject.put("auto_req", mAutoRequest);
                    jsonObject.put("aprn_auto_req", ismIsDefaultNetwork() ? 1 : 0);
                    jsonObject.put("bidtype", mBidType);
                    jsonObject.put("bidprice", String.valueOf(mBidPrice));
                    break;

                case TrackingV2Loader.AD_REQUEST_SUCCESS_TYPE:
                    jsonObject.put("asid", mAsid);
                    jsonObject.put("unit_id", mUnitGroupUnitId);
                    jsonObject.put("nw_firm_id", mNetworkType);
                    jsonObject.put("gro_id", mGroupId);
                    jsonObject.put("auto_req", mAutoRequest);
                    jsonObject.put("aprn_auto_req", ismIsDefaultNetwork() ? 1 : 0);
                    jsonObject.put("status", mLoadStatus);
                    jsonObject.put("filledtime", mFillTime);
                    jsonObject.put("flag", mFlag);
                    jsonObject.put("bidtype", mBidType);
                    jsonObject.put("bidprice", String.valueOf(mBidPrice));
                    break;

                case TrackingV2Loader.AD_SHOW_TYPE:
                    //Add by v5.5.5
                    jsonObject.put("ads", adTypeDayShowTime);
                    jsonObject.put("ahs", adTypeHourShowTime);
                    jsonObject.put("pds", placementDayShowTime);
                    jsonObject.put("phs", placementHourShowTime);

                case TrackingV2Loader.AD_CLICK_TYPE:
                    jsonObject.put("unit_id", mUnitGroupUnitId);
                    jsonObject.put("nw_firm_id", mNetworkType);
                    jsonObject.put("gro_id", mGroupId);
                    jsonObject.put("auto_req", mAutoRequest);
                    jsonObject.put("aprn_auto_req", ismIsDefaultNetwork() ? 1 : 0);

                    jsonObject.put("bidtype", mBidType);
                    jsonObject.put("bidprice", String.valueOf(mBidPrice));

                    //Add by v4.5.0
                    jsonObject.put("myoffer_showtype", mMyOfferShowType);
                    //Add by v5.4.7
                    if (!TextUtils.isEmpty(mScenario)) {
                        jsonObject.put("scenario", mScenario);
                    }

                    //Add by v5.5.6
                    jsonObject.put("ads", adTypeDayShowTime);
                    jsonObject.put("ahs", adTypeHourShowTime);
                    jsonObject.put("pds", placementDayShowTime);
                    jsonObject.put("phs", placementHourShowTime);

                    break;


                case TrackingV2Loader.AD_VIDEO_TYPE:
                    jsonObject.put("unit_id", mUnitGroupUnitId);
                    jsonObject.put("nw_firm_id", mNetworkType);
                    jsonObject.put("gro_id", mGroupId);
                    jsonObject.put("auto_req", mAutoRequest);
                    jsonObject.put("aprn_auto_req", ismIsDefaultNetwork() ? 1 : 0);
                    jsonObject.put("progress", mProgress);
                    jsonObject.put("bidtype", mBidType);
                    jsonObject.put("bidprice", String.valueOf(mBidPrice));
                    break;

                case TrackingV2Loader.AD_RV_START_TYPE:
                case TrackingV2Loader.AD_RV_CLOSE_TYPE:
                    jsonObject.put("unit_id", mUnitGroupUnitId);
                    jsonObject.put("nw_firm_id", mNetworkType);
                    jsonObject.put("gro_id", mGroupId);
                    jsonObject.put("auto_req", mAutoRequest);
                    jsonObject.put("aprn_auto_req", ismIsDefaultNetwork() ? 1 : 0);
                    jsonObject.put("bidtype", mBidType);
                    jsonObject.put("bidprice", String.valueOf(mBidPrice));
                    //Add by v5.4.7
                    if (!TextUtils.isEmpty(mScenario)) {
                        jsonObject.put("scenario", mScenario);
                    }
                    break;

                case TrackingV2Loader.AD_SDK_LOAD_TYPE:
                    jsonObject.put("isload", mIsLoad ? 1 : 0);
                    jsonObject.put("reason", mReason);
                    jsonObject.put("asid", mAsid);
                    jsonObject.put("gro_id", mGroupId);
                    break;
                case TrackingV2Loader.AD_SDK_LOAD_SUCCESS_TYPE:
                    jsonObject.put("loadtime", mFillTime);
                    jsonObject.put("gro_id", mGroupId);
                    if (mReason == 5) {
                        jsonObject.put("reason", mReason);
                    }

                    break;
                case TrackingV2Loader.AD_SDK_SHOW_TYPE:
                    jsonObject.put("unit_id", mUnitGroupUnitId);
                    jsonObject.put("nw_firm_id", mNetworkType);
                    jsonObject.put("gro_id", mGroupId);
                    jsonObject.put("bidtype", mBidType);
                    jsonObject.put("bidprice", String.valueOf(mBidPrice));
                    jsonObject.put("as_result", TextUtils.isEmpty(mAsResult) ? "[]" : new JSONArray(mAsResult));
                    jsonObject.put("new_req_id", mShowCurrentRequestId);
                    jsonObject.put("auto_req", mAutoRequest);

                    if (mShowCurrentRequestId == null && mRequestId == null) {
                        jsonObject.put("req_id_match", 0);
                    }

                    if (mShowCurrentRequestId != null && mRequestId != null) {
                        if (mShowCurrentRequestId.equals(mRequestId)) {
                            jsonObject.put("req_id_match", 0);
                        } else {
                            jsonObject.put("req_id_match", 1);
                        }
                    } else {
                        jsonObject.put("req_id_match", 1);
                    }

                    //Add by v4.5.0
                    jsonObject.put("myoffer_showtype", mMyOfferShowType);
                    //Add by v5.4.7
                    if (!TextUtils.isEmpty(mScenario)) {
                        jsonObject.put("scenario", mScenario);
                    }

                    //Add by v5.5.6
                    jsonObject.put("ads", adTypeDayShowTime);
                    jsonObject.put("ahs", adTypeHourShowTime);
                    jsonObject.put("pds", placementDayShowTime);
                    jsonObject.put("phs", placementHourShowTime);
                    break;
                case TrackingV2Loader.AD_HEADERBIDDING_TYPE:
                    jsonObject.put("asid", mAsid);
                    jsonObject.put("gro_id", mGroupId);
                    jsonObject.put("bidrequesttime", mHbStartTime);
                    jsonObject.put("bidresponsetime", mHbEndTime);
                    jsonObject.put("bidresponselist", TextUtils.isEmpty(mHbResultList) ? "[]" : new JSONArray(mHbResultList));
                    break;

                case TrackingV2Loader.ADSOURCE_SORT_TYPE:
                    jsonObject.put("asid", mAsid);
                    jsonObject.put("gro_id", mGroupId);
                    jsonObject.put("bidresponselist", TextUtils.isEmpty(mHbResultList) ? "[]" : new JSONArray(mHbResultList));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


}
