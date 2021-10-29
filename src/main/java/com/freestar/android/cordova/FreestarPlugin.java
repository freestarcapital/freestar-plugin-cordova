package com.freestar.android.cordova;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.freestar.android.ads.AdRequest;
import com.freestar.android.ads.AdSize;
import com.freestar.android.ads.BannerAdListener;
import com.freestar.android.ads.ChocolateLogger;
import com.freestar.android.ads.ErrorCodes;
import com.freestar.android.ads.FreeStarAds;
import com.freestar.android.ads.InterstitialAd;
import com.freestar.android.ads.InterstitialAdListener;
import com.freestar.android.ads.RewardedAd;
import com.freestar.android.ads.RewardedAdListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FreestarPlugin extends CordovaPlugin {

    private static final String TAG = "FreestarCordovaPlugin";
    private boolean isChooserEnabled;

    private static final String API_META_KEY = "com.freestar.android.ads.API_KEY";
    private static final String PLACEMENT = "placement";
    private static final String BANNER_AD_SIZE = "banner_ad_size";
    private static final String BANNER_AD_POSITION = "banner_ad_position";

    private static final String ACTION_LOAD_INTERSTITIAL_AD = "LOAD_INTERSTITIAL_AD";
    private static final String ACTION_SHOW_INTERSTITIAL_AD = "SHOW_INTERSTITIAL_AD";

    private static final String ACTION_LOAD_REWARD_AD = "LOAD_REWARD_AD";
    private static final String ACTION_SHOW_REWARD_AD = "SHOW_REWARD_AD";
    private static final String ACTION_SHOW_BANNER_AD = "SHOW_BANNER_AD";
    private static final String ACTION_CLOSE_BANNER_AD = "CLOSE_BANNER_AD";
    private static final String ACTION_SET_USER_PARAMS = "SET_USER_PARAMS";
    private static final String ACTION_SET_TESTMODE_PARAMS = "SET_TESTMODE_PARAMS";
    private static final String ACTION_SET_ENABLE_PARTNER_CHOOSER = "SET_ENABLE_PARTNER_CHOOSER";

    private AdRequest adRequest;
    private Map<String, InterstitialAd> interstitialAdMap = new HashMap<>();
    private Map<String, RewardedAd> rewardedAdMap = new HashMap<>();
    private Map<String, PopupBannerAd> popupBannerAdMap = new HashMap<>();
    private long lastFullscreenMillis;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        adRequest = new AdRequest(cordova.getActivity());        
        ChocolateLogger.i(TAG, "Init FreestarPlugin");
        try {
            Context context = webView.getContext();
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(webView.getContext().getPackageName(), PackageManager.GET_META_DATA);
            String apiKey = app.metaData.getString(API_META_KEY);
            FreeStarAds.init(context, apiKey);
        } catch (Exception e) {
            //this will be bad since Freestar won't be able to initialize
            ChocolateLogger.e(TAG, "init failed", e);
        }
    }

    private String stringFrom(JSONObject options, String key, String defaultString) {
        String value = defaultString;
        if (options != null && options.has(key)) {
            value = options.optString(key, defaultString);
        }
        return value;
    }

    private int intFrom(JSONObject options, String key, int defaultInt) {
        int intValue = defaultInt;
        if (options != null && options.has(key)) {
            intValue = options.optInt(key, defaultInt);
        }
        return intValue;
    }

    private void loadInterstitialAd(String placement) {
        InterstitialAd interstitialAd = new InterstitialAd(cordova.getActivity(), interstitialAdListener);
        interstitialAdMap.put(placement, interstitialAd);
        interstitialAd.loadAd(adRequest, placement);
        interstitialAd.loadAd(adRequest);
    }

    private void loadRewardedAd(String placement) {
        RewardedAd rewardedAd = new RewardedAd(cordova.getActivity(), rewardedAdListener);
        rewardedAdMap.put(placement, rewardedAd);
        rewardedAd.loadAd(adRequest, placement);
    }

    private void loadBannerAd(final String placement, final int bannerAdSize, final int bannerAdPosition) {
        PopupBannerAd popupBannerAd = popupBannerAdMap.get(placement + "" + bannerAdSize);
        if (popupBannerAd != null && popupBannerAd.isShowing()) {
            ChocolateLogger.w(TAG, "show banner. already showing. placement: "
                    + placement + " bannerAdSize: " + bannerAdSize);
        } else {
            popupBannerAd = new PopupBannerAd(cordova.getActivity());
            final PopupBannerAd finalPopupBannerAd = popupBannerAd;
            popupBannerAdMap.put(placement + "" + bannerAdSize, popupBannerAd);
            popupBannerAd.loadBannerAd(adRequest,
                    from(bannerAdSize),
                    placement,
                    bannerAdPosition,
                    new BannerAdListener() {
                        @Override
                        public void onBannerAdLoaded(View bannerAdView, String placement) {
                            try {
                                finalPopupBannerAd.showBannerAd(bannerAdView);
                                sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onBannerAdShowing'," +
                                        callbackParamsFrom(placement, bannerAdSize, -1) +
                                        ");");
                            } catch (Throwable t) {
                                ChocolateLogger.e(TAG, "loadBannerAd failed", t);
                            }
                        }

                        @Override
                        public void onBannerAdFailed(View bannerAdView, String placement, int errorCode) {
                            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onBannerAdFailed'," +
                                    callbackParamsFrom(placement, bannerAdSize, errorCode) +
                                    ");");
                        }

                        @Override
                        public void onBannerAdClicked(View bannerAdView, String placement) {
                            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onBannerAdClicked'," +
                                    callbackParamsFrom(placement, bannerAdSize, -1) +
                                    ");");
                        }

                        @Override
                        public void onBannerAdClosed(View bannerAdView, String placement) {
                            //not implemented
                        }
                    });
        }
    }

    private AdSize from(int bannerAdSize) {
        if (bannerAdSize == FreestarConstants.BANNER_AD_SIZE_320x50) {
            return AdSize.BANNER_320_50;
        } else if (bannerAdSize == FreestarConstants.BANNER_AD_SIZE_300x250) {
            return AdSize.MEDIUM_RECTANGLE_300_250;
        } else {
            return AdSize.LEADERBOARD_728_90;
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                for (PopupBannerAd popupBannerAd : popupBannerAdMap.values()) {
                    popupBannerAd.onResume();
                }
            }
        });
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                for (PopupBannerAd popupBannerAd : popupBannerAdMap.values()) {
                    popupBannerAd.onPause();
                }
            }
        });
    }

    public boolean execute(String action, final JSONArray inputs, CallbackContext callbackContext) throws JSONException {

        ChocolateLogger.i(TAG, "FreestarPlugin Action Received :" + action);

        if (ACTION_SHOW_BANNER_AD.equals(action)) {

            JSONObject options = inputs.optJSONObject(0);
            final String placement = stringFrom(options, PLACEMENT, "");
            final int bannerAdSize = intFrom(options, BANNER_AD_SIZE, FreestarConstants.BANNER_AD_SIZE_320x50);
            final int bannerAdPosition = intFrom(options, BANNER_AD_POSITION, FreestarConstants.BANNER_AD_POSITION_BOTTOM);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (isChooserEnabled) {
                        MediationPartners.choosePartners(cordova.getActivity(), adRequest, MediationPartners.ADTYPE_BANNER, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadBannerAd(placement, bannerAdSize, bannerAdPosition);
                            }
                        });
                    } else {
                        loadBannerAd(placement, bannerAdSize, bannerAdPosition);
                    }
                }
            });

        } else if (ACTION_CLOSE_BANNER_AD.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            final String placement = stringFrom(options, PLACEMENT, "");
            final int bannerAdSize = intFrom(options, BANNER_AD_SIZE, FreestarConstants.BANNER_AD_SIZE_320x50);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    PopupBannerAd popupBannerAd = popupBannerAdMap.get(placement + "" + bannerAdSize);
                    if (popupBannerAd != null && popupBannerAd.isShowing()) {
                        popupBannerAd.destroy();
                    }
                }
            });
        } else if (ACTION_LOAD_INTERSTITIAL_AD.equals(action)) {

            synchronized (this) {
                if (!canFullscreenRequest()) {
                    Log.e(TAG, "Cannot LOAD_INTERSTITIAL_AD while another ad is in progress ");
                    return false;
                }
                markFullscreenRequest();
            }

            cordova.getActivity().runOnUiThread(new Runnable() {
                JSONObject options = inputs.optJSONObject(0);
                final String placement = stringFrom(options, PLACEMENT, "");

                public void run() {
                    if (isChooserEnabled) {
                        MediationPartners.choosePartners(cordova.getActivity(), adRequest, MediationPartners.ADTYPE_INTERSTITIAL, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadInterstitialAd(placement);
                            }
                        });
                    } else {
                        loadInterstitialAd(placement);
                    }
                }
            });

        } else if (ACTION_SHOW_INTERSTITIAL_AD.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            final String placement = stringFrom(options, PLACEMENT, "");
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    InterstitialAd interstitialAd = interstitialAdMap.get(placement + "");
                    if (interstitialAd != null) {
                        try {
                            interstitialAd.show();
                        } catch (Exception e) {
                            interstitialAdListener.onInterstitialFailed(placement, ErrorCodes.INTERNAL_ERROR);
                            ChocolateLogger.e(TAG, "interstitial failed. internal error.", e);
                        }
                    } else {
                        interstitialAdListener.onInterstitialFailed(placement, ErrorCodes.INVALID_REQUEST);
                        ChocolateLogger.e(TAG, "show interstitial failed.  interstitial ad is null.");
                    }
                }
            });

        } else if (ACTION_LOAD_REWARD_AD.equals(action)) {

            synchronized (this) {
                if (!canFullscreenRequest()) {
                    Log.e(TAG, "Cannot LOAD_REWARD_AD while another ad is in progress ");
                    return false;
                }
                markFullscreenRequest();
            }

            JSONObject options = inputs.optJSONObject(0);
            final String placement = stringFrom(options, PLACEMENT, "");
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (isChooserEnabled) {
                        MediationPartners.choosePartners(cordova.getActivity(), adRequest, MediationPartners.ADTYPE_REWARDED, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadRewardedAd(placement);
                            }
                        });
                    } else {
                        loadRewardedAd(placement);
                    }
                }
            });

        } else if (ACTION_SHOW_REWARD_AD.equals(action)) {

            JSONObject options = inputs.optJSONObject(0);
            final String placement = stringFrom(options, PLACEMENT, "");

            final String SECRET = stringFrom(options, "SECRET", "");
            final String USERID = stringFrom(options, "USERID", "");
            final String REWARDNAME = stringFrom(options, "REWARDNAME", "");
            final String REWARDAMOUNT = stringFrom(options, "REWARDAMOUNT", "");

            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    RewardedAd rewardedAd = rewardedAdMap.get(placement + "");
                    if (rewardedAd != null) {
                        try {
                            rewardedAd.showRewardAd(SECRET, USERID, REWARDNAME, REWARDAMOUNT);
                        } catch (Exception e) {
                            rewardedAdListener.onRewardedVideoFailed(placement, ErrorCodes.INTERNAL_ERROR);
                            ChocolateLogger.e(TAG, "rewarded failed. internal error.", e);
                        }
                    } else {
                        rewardedAdListener.onRewardedVideoFailed(placement, ErrorCodes.INVALID_REQUEST);
                        ChocolateLogger.e(TAG, "show rewarded failed.  reward ad is null.");
                    }
                }
            });

        } else if (ACTION_SET_USER_PARAMS.equals(action)) {

            JSONObject options = inputs.optJSONObject(0);
            if (options == null) {
                return false;
            }
            final String age = stringFrom(options, "AGE", "");
            final String birthDate = stringFrom(options, "BIRTHDATE", "");
            final String gender = stringFrom(options, "GENDER", "");
            final String maritalStatus = stringFrom(options, "MARITALSTATUS", "");

            final String ethnicity = stringFrom(options, "ETHNICITY", "");
            final String dmaCode = stringFrom(options, "DMACODE", "");
            final String postal = stringFrom(options, "POSTAL", "");
            final String curPostal = stringFrom(options, "POSTAL", "");

            final String latitude = stringFrom(options, "LATITUDE", "");
            final String longitude = stringFrom(options, "LONGITUDE", "");

            setAdRequestUserParams(age, birthDate, gender, maritalStatus,
                    ethnicity, dmaCode, postal, curPostal, latitude, longitude);

        } else if (ACTION_SET_TESTMODE_PARAMS.equals(action)) {

            JSONObject options = inputs.optJSONObject(0);
            if (options == null) {
                return false;
            }

            final boolean isEnabled = options.optBoolean("TESTMODE", false);
            final String hashID = stringFrom(options, "HASHID", "");

            setTestModeEnabled(isEnabled, hashID);
        } else if (ACTION_SET_ENABLE_PARTNER_CHOOSER.equals(action)) {

            JSONObject options = inputs.optJSONObject(0);
            if (options == null) {
                return false;
            }

            isChooserEnabled = options.optBoolean("ENABLED", false);
        }

        return true;
    }

    private void sendCallbackToCordova(final String jsString) {
        ChocolateLogger.i(TAG, "Cordova Callback : " + jsString);

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(jsString);
            }
        });
    }

    private String callbackParamsFrom(String placement, int bannerAdSize, int errorCode) {
        JSONObject args = new JSONObject();
        try {
            if (placement != null) {
                args.put(PLACEMENT, placement);
            }
            if (bannerAdSize >= 0) {
                args.put(BANNER_AD_SIZE, bannerAdSize);
            }
            if (errorCode >= 0) {
                args.put("error", ErrorCodes.getErrorDescription(errorCode));
            }
        } catch (Exception e) {
            //ignore
        }
        return args.toString();
    }

    //////////////////////////FULLSCREEN_INTERSTITIAL CALLBACKS//////////////////////////
    private InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
        @Override
        public void onInterstitialLoaded(String placement) {
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onInterstitialLoaded'," +
                    callbackParamsFrom(placement, -1, -1) +
                    ");");
        }

        @Override
        public void onInterstitialFailed(String placement, int errorCode) {
            resetFullscreenRequest();
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onInterstitialFailed'," +
                    callbackParamsFrom(placement, -1, errorCode) +
                    ");");
        }

        @Override
        public void onInterstitialShown(String placement) {
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onInterstitialShown'," +
                    callbackParamsFrom(placement, -1, -1) +
                    ");");
        }

        @Override
        public void onInterstitialDismissed(final String placement) {
            resetFullscreenRequest();
            cleanupFullscreenAds(placement);
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onInterstitialDismissed'," +
                    callbackParamsFrom(placement, -1, -1) +
                    ");");
        }

        @Override
        public void onInterstitialClicked(String placement) {
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onInterstitialClicked'," +
                    callbackParamsFrom(placement, -1, -1) +
                    ");");
        }
    };
    //////////////////////////////////////////////////////////////////////////////

    //////////////////////////REWARD CALLBACKS//////////////////////////
    private RewardedAdListener rewardedAdListener = new RewardedAdListener() {

        @Override
        public void onRewardedVideoLoaded(String placement) {
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoLoaded'," +
                    callbackParamsFrom(placement, -1, -1) +
                    ");");
        }

        @Override
        public void onRewardedVideoFailed(String placement, int errorCode) {
            resetFullscreenRequest();
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoFailed'," +
                    callbackParamsFrom(placement, -1, errorCode) +
                    ");");
        }

        @Override
        public void onRewardedVideoShown(String placement) {
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoShown'," +
                    callbackParamsFrom(placement, -1, -1) +
                    ");");
        }

        @Override
        public void onRewardedVideoShownError(String placement, int errorCode) {
            resetFullscreenRequest();
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoShownError'," +
                    callbackParamsFrom(placement, -1, errorCode) +
                    ");");
        }

        @Override
        public void onRewardedVideoDismissed(final String placement) {
            resetFullscreenRequest();
            cleanupFullscreenAds(placement);
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoDismissed'," +
                    callbackParamsFrom(placement, -1, -1) +
                    ");");
        }

        @Override
        public void onRewardedVideoCompleted(String placement) {
            resetFullscreenRequest();
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoCompleted'," +
                    callbackParamsFrom(placement, -1, -1) +
                    ");");
        }
    };
    //////////////////////////////////////////////////////////////////////////////

    ////////////////////AD REQUEST PARAMETERS////////////////////////////////
    private void setAdRequestUserParams(String age, String birthDate, String gender, String maritalStatus,
                                        String ethnicity, String dmaCode, String postal, String curPostal,
                                        String latitude, String longitude) {
        Log.i(TAG, "Cordova User Params Set.");
        adRequest.setAge(age);
        adRequest.setBirthday(getDate(birthDate));

        if (TextUtils.isEmpty(gender)) {
            gender = "";
        }
        adRequest.setGender(gender);

        if (TextUtils.isEmpty(maritalStatus)) {
            maritalStatus = "";
        }

        adRequest.setMaritalStatus(maritalStatus);

        adRequest.setEthnicity(ethnicity);
        adRequest.setDmaCode(dmaCode);
        adRequest.setPostalCode(postal);
        adRequest.setCurrPostal(curPostal);

        if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
            try {
                Log.i(TAG, "Cordova Location Params Set.");
                Location location = new Location("");
                location.setLatitude(Double.valueOf(latitude));
                location.setLongitude(Double.valueOf(longitude));
                adRequest.setLocation(location);
            } catch (NumberFormatException e) {
                Log.i(TAG, "Cordova Location Invalid.");
            }
        }
    }

    private void setTestModeEnabled(boolean isEnabled, String hashID) {
        Log.i(TAG, "Cordova Test Params Set.");

        FreeStarAds.enableLogging(isEnabled);
        FreeStarAds.enableTestAds(isEnabled);

        adRequest.setTestModeEnabled(isEnabled);

        Set<String> testID = new HashSet<String>();
        testID.add(hashID);
        adRequest.setTestDevices(testID);
    }

    private Date getDate(String dateString) {
        try {
            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            return df.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
    //////////////////////////////////////////////////////////////////////////////

    private void resetFullscreenRequest() {
        lastFullscreenMillis = 0;
    }

    private void markFullscreenRequest() {
        lastFullscreenMillis = System.currentTimeMillis();
    }

    private boolean canFullscreenRequest() {
        //don't load any other fullscreen ad within 5 seconds
        //of another fullscreen ad request
        return System.currentTimeMillis() - lastFullscreenMillis > 5000L;
    }

    private void cleanupFullscreenAds(String placement) {
        try {
            InterstitialAd interstitialAd = interstitialAdMap.get(placement + "");
            if (interstitialAd != null) {
                interstitialAd.destroyView();
                interstitialAdMap.remove(placement + "");
                Log.i(TAG, "Interstitial ad cleared.");
            }
            RewardedAd rewardedAd = rewardedAdMap.get(placement + "");
            if (rewardedAd != null) {
                rewardedAd.destroyView();
                rewardedAdMap.remove(placement + "");
                Log.i(TAG, "Rewarded ad cleared.");
            }
        } catch (Exception e) {
            Log.e(TAG, "clear() failed ", e);
        }
    }
}
