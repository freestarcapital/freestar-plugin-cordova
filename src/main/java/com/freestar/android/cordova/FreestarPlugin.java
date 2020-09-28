package com.freestar.android.cordova;

import android.annotation.SuppressLint;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.freestar.android.ads.AdRequest;
import com.freestar.android.ads.ChocolateLogger;
import com.freestar.android.ads.ErrorCodes;
import com.freestar.android.ads.FreeStarAds;
import com.freestar.android.ads.InterstitialAd;
import com.freestar.android.ads.InterstitialAdListener;
import com.freestar.android.ads.LVDOAdUtil;
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

    private AdRequest adRequest;

    private Map<String, InterstitialAd> interstitialAdMap = new HashMap<>();
    private Map<String, RewardedAd> rewardedAdMap = new HashMap<>();
    private long lastRequestMillis;

    private static final String PLACEMENT = "placement";

    private static final String ACTION_INIT = "FREESTAR_INIT";

    private static final String ACTION_LOAD_INTERSTITIAL_AD = "LOAD_INTERSTITIAL_AD";
    private static final String ACTION_SHOW_INTERSTITIAL_AD = "SHOW_INTERSTITIAL_AD";

    private static final String ACTION_LOAD_REWARD_AD = "LOAD_REWARD_AD";
    private static final String ACTION_SHOW_REWARD_AD = "SHOW_REWARD_AD";
    private static final String ACTION_CHECK_REWARD_AD = "CHECK_REWARD_AD";

    private static final String ACTION_SET_USER_PARAMS = "SET_USER_PARAMS";
    private static final String ACTION_SET_APP_PARAMS = "SET_APP_PARAMS";
    private static final String ACTION_SET_TESTMODE_PARAMS = "SET_TESTMODE_PARAMS";

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        ChocolateLogger.i(TAG, "Init FreestarPlugin");
    }

    private String valueFromOptions(String key, JSONObject options) {
        String value = "";
        if (options != null && options.has(key)) {
            value = options.optString(key);
        }
        return value;
    }

    public boolean execute(String action, final JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        ChocolateLogger.i(TAG, "FreestarPlugin Action Received :" + action);

        if (ACTION_LOAD_INTERSTITIAL_AD.equals(action)) {

            synchronized (this) {
                if (!canRequest()) {
                    Log.e(TAG, "Cannot LOAD_INTERSTITIAL_AD while another ad is in progress ");
                    return false;
                }
                markRequest();
            }

            JSONObject options = inputs.optJSONObject(0);

            final String placement = valueFromOptions(PLACEMENT, options);

            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    InterstitialAd interstitialAd = new InterstitialAd(cordova.getActivity(), interstitialAdListener);
                    interstitialAdMap.put(placement, interstitialAd);
                    interstitialAd.loadAd(getAdRequest(), placement);
                }
            });

        } else if (ACTION_SHOW_INTERSTITIAL_AD.equals(action)) {

            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    JSONObject options = inputs.optJSONObject(0);
                    final String placement = valueFromOptions(PLACEMENT, options);
                    InterstitialAd interstitialAd = interstitialAdMap.get(placement+"");
                    if (interstitialAd != null) {
                        try {
                            interstitialAd.show();
                        }catch(Exception e) {
                            ChocolateLogger.e(TAG,"", e);
                        }
                    }
                }
            });

        } else if (ACTION_LOAD_REWARD_AD.equals(action)) {

            synchronized (this) {
                if (!canRequest()) {
                    Log.e(TAG, "Cannot LOAD_REWARD_AD while another ad is in progress ");
                    return false;
                }
                markRequest();
            }

            JSONObject options = inputs.optJSONObject(0);
            final String placement = valueFromOptions(PLACEMENT, options);

            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    RewardedAd rewardedAd = new RewardedAd(cordova.getActivity(), rewardedAdListener);
                    rewardedAdMap.put(placement, rewardedAd);
                    rewardedAd.loadAd(getAdRequest(), placement);
                }
            });

        } else if (ACTION_SHOW_REWARD_AD.equals(action)) {

            JSONObject options = inputs.optJSONObject(0);
            final String placement = valueFromOptions(PLACEMENT, options);

            final String SECRET = valueFromOptions("SECRET", options);
            final String USERID = valueFromOptions("USERID", options);
            final String REWARDNAME = valueFromOptions("REWARDNAME", options);
            final String REWARDAMOUNT = valueFromOptions("REWARDAMOUNT", options);

            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    RewardedAd rewardedAd = rewardedAdMap.get(placement+"");
                    if (rewardedAd != null && rewardedAd.isReady()) {
                        try {
                            rewardedAd.showRewardAd(SECRET, USERID, REWARDNAME, REWARDAMOUNT);
                        } catch (Exception e) {
                            rewardedAdListener.onRewardedVideoFailed("", ErrorCodes.INTERNAL_ERROR);
                        }
                    }
                }
            });

        } else if (ACTION_CHECK_REWARD_AD.equals(action)) {

            JSONObject options = inputs.optJSONObject(0);
            final String placement = valueFromOptions(PLACEMENT, options);
            RewardedAd rewardedAd = rewardedAdMap.get(placement+"");

            boolean result = rewardedAd != null && rewardedAd.isReady();

            if (result) {
                callbackContext.success();
            } else {
                callbackContext.error(0);
            }

        } else if (ACTION_SET_USER_PARAMS.equals(action)) {

            JSONObject options = inputs.optJSONObject(0);
            if (options == null) {
                return false;
            }
            final String age = valueFromOptions("AGE", options);
            final String birthDate = valueFromOptions("BIRTHDATE", options);
            final String gender = valueFromOptions("GENDER", options);
            final String maritalStatus = valueFromOptions("MARITALSTATUS", options);

            final String ethnicity = valueFromOptions("ETHNICITY", options);
            final String dmaCode = valueFromOptions("DMACODE", options);
            final String postal = valueFromOptions("POSTAL", options);
            final String curPostal = valueFromOptions("POSTAL", options);

            final String latitude = valueFromOptions("LATITUDE", options);
            final String longitude = valueFromOptions("LONGITUDE", options);

            setAdRequestUserParams(age, birthDate, gender, maritalStatus,
                    ethnicity, dmaCode, postal, curPostal, latitude, longitude);

        } else if (ACTION_SET_APP_PARAMS.equals(action)) {

            JSONObject options = inputs.optJSONObject(0);
            if (options == null) {
                return false;
            }

            final String appName = valueFromOptions("APPNAME", options);
            final String pubName = valueFromOptions("PUBNAME", options);
            final String appDomain = valueFromOptions("APPDOMAIN", options);
            final String pubDomain = valueFromOptions("PUBDOMAIN", options);

            final String storeUrl = valueFromOptions("STOREURL", options);
            final String iabCategory = valueFromOptions("IABCATEGORY", options);

            setAdRequestAppParams(appName, pubName, appDomain, pubDomain, storeUrl, iabCategory);

        } else if (ACTION_SET_TESTMODE_PARAMS.equals(action)) {

            JSONObject options = inputs.optJSONObject(0);
            if (options == null) {
                return false;
            }

            final boolean isEnabled = options.has("TESTMODE") && options.optBoolean("TESTMODE");
            final String hashID = valueFromOptions("HASHID", options);

            setTestModeEnabled(isEnabled, hashID);
        } else if (ACTION_INIT.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            if (options == null) {
                return false;
            }

            final String key = valueFromOptions("apikey", options);

            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    FreeStarAds.init(cordova.getActivity(), key, new AdRequest(cordova.getActivity()));
                }
            });
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

    private String appendPlacement(String placement) {
        return appendPlacement(placement, -1);
    }

    private String appendPlacement(String placement, int errorCode) {
        JSONObject args = new JSONObject();
        try {
            if (placement != null) {
                args.put("placement", placement);
            }
            if (errorCode >= 0) {
                args.put("error", ErrorCodes.getErrorDescription(errorCode));
            }
        }catch (Exception e) {
            //ignore
        }
        return args.toString();
    }

    //////////////////////////FULLSCREEN_INTERSTITIAL CALLBACKS//////////////////////////
    private InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
        @Override
        public void onInterstitialLoaded(String placement) {
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onInterstitialLoaded',"+
                    appendPlacement(placement) +
                    ");");
        }

        @Override
        public void onInterstitialFailed(String placement, int errorCode) {
            resetRequest();
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onInterstitialFailed'," +
                    appendPlacement(placement, errorCode) +
                    ");");
        }

        @Override
        public void onInterstitialShown(String placement) {
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onInterstitialShown'," +
                    appendPlacement(placement) +
                    ");");
        }

        @Override
        public void onInterstitialDismissed(final String placement) {
            resetRequest();
            clear(placement);
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onInterstitialDismissed'," +
                    appendPlacement(placement) +
                    ");");
        }

        @Override
        public void onInterstitialClicked(String placement) {
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onInterstitialClicked'," +
                    appendPlacement(placement) +
                    ");");
        }
    };
    //////////////////////////////////////////////////////////////////////////////

    //////////////////////////REWARD CALLBACKS//////////////////////////
    private RewardedAdListener rewardedAdListener = new RewardedAdListener() {

        @Override
        public void onRewardedVideoLoaded(String placement) {
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoLoaded'," +
                    appendPlacement(placement) +
                    ");");
        }

        @Override
        public void onRewardedVideoFailed(String placement, int errorCode) {
            resetRequest();
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoFailed'," +
                    appendPlacement(placement, errorCode) +
                    ");");
        }

        @Override
        public void onRewardedVideoShown(String placement) {
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoShown'," +
                    appendPlacement(placement) +
                    ");");
        }

        @Override
        public void onRewardedVideoShownError(String placement, int errorCode) {
            resetRequest();
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoShownError'," +
                    appendPlacement(placement, errorCode) +
                    ");");
        }

        @Override
        public void onRewardedVideoDismissed(final String placement) {
            resetRequest();
            clear(placement);
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoDismissed'," +
                    appendPlacement(placement) +
                    ");");
        }

        @Override
        public void onRewardedVideoCompleted(String placement) {
            resetRequest();
            sendCallbackToCordova("javascript:cordova.fireDocumentEvent('onRewardedVideoCompleted'," +
                    appendPlacement(placement) +
                    ");");
        }
    };
    //////////////////////////////////////////////////////////////////////////////

    ////////////////////AD REQUEST PARAMETERS////////////////////////////////
    private void setAdRequestUserParams(String age, String birthDate, String gender, String maritalStatus,
                                        String ethnicity, String dmaCode, String postal, String curPostal,
                                        String latitude, String longitude) {
        Log.i(TAG, "Cordova User Params Set.");
        getAdRequest();

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

    private void setAdRequestAppParams(String appName, String pubName,
                                       String appDomain, String pubDomain,
                                       String storeUrl, String iabCategory) {
        Log.i(TAG, "Cordova App Params Set.");
        getAdRequest();

        adRequest.setAppName(appName);
        adRequest.setRequester(pubName);

        adRequest.setAppDomain(appDomain);
        adRequest.setPublisherDomain(pubDomain);

        adRequest.setAppStoreUrl(storeUrl);
        adRequest.setCategory(iabCategory);
    }

    private void setTestModeEnabled(boolean isEnabled, String hashID) {
        Log.i(TAG, "Cordova Test Params Set.");
        getAdRequest();

        FreeStarAds.enableLogging(isEnabled);
        FreeStarAds.enableTestAds(isEnabled);

        adRequest.setTestModeEnabled(isEnabled);

        Set<String> testID = new HashSet<String>();
        testID.add(hashID);
        adRequest.setTestDevices(testID);
    }

    private AdRequest getAdRequest() {
        if (adRequest == null) {
            adRequest = new AdRequest(cordova.getActivity());
        }

        return adRequest;
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

    void resetRequest() {
        lastRequestMillis = 0;
    }

    private void markRequest() {
        lastRequestMillis = System.currentTimeMillis();
    }

    private boolean canRequest() {
        return System.currentTimeMillis() - lastRequestMillis > 5000L;
    }

    void clear(String placement) {
        try {
            InterstitialAd interstitialAd = interstitialAdMap.get(placement+"");
            if (interstitialAd != null) {
                interstitialAd.destroyView();
                interstitialAdMap.remove(placement+"");
                Log.i(TAG, "Interstitial ad cleared.");
            }
            RewardedAd rewardedAd = rewardedAdMap.get(placement+"");
            if (rewardedAd != null) {
                rewardedAd.destroyView();
                rewardedAdMap.remove(placement+"");
                Log.i(TAG, "Rewarded ad cleared.");
            }
        }catch (Exception e) {
            Log.e(TAG, "clear() failed ",e);
        }
    }
}
