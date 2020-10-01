//////////////////////////////COMMON METHODS FOR ALL AD TYPES//////////////////////////////

function log(message) {
    if (true) {                 //Make it false to disable log
        console.log("FreestarPlugin.js: " + message)
    }
}

function setAdRequestUserParams(age, birthDate, gender, maritalStatus,
                                ethnicity, dmaCode, postal, curPostal, latitude, longitude) {
    log("setAdRequestUserParams");

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Fail")
        },
        "FreestarPlugin",
        ACTION_SET_USER_PARAMS,
        [{
         	"AGE":age,
         	"BIRTHDATE":birthDate,
         	"GENDER":gender,
         	"MARITALSTATUS":maritalStatus,
         	"ETHNICITY":ethnicity,
            "DMACODE":dmaCode,
            "POSTAL":postal,
            "CURPOSTAL":curPostal,
            "LATITUDE":latitude,
            "LONGITUDE":longitude
         }]
    );
}


//////////////////////////////FREESTAR ADS TEST MODE//////////////////////////////
function setTestModeEnabled(isEnabled, hashID) {
    log("setTestModeEnabled");

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Fail")
        },
        "FreestarPlugin",
        ACTION_SET_TESTMODE_PARAMS,
        [{
         	"TESTMODE":isEnabled,
         	"HASHID":hashID
         }]
    );
}

//////////////////////////////SHOW INTERSTITIAL AD//////////////////////////////

var ACTION_LOAD_INTERSTITIAL_AD = "LOAD_INTERSTITIAL_AD";
var ACTION_SHOW_INTERSTITIAL_AD = "SHOW_INTERSTITIAL_AD";

function loadInterstitialAdFromSDK(placement) {
    log("loadInterstitialAdFromSDK");
    INTERSTITIAL_AD_LOADED = false;

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Fail")
        },
        "FreestarPlugin",
        ACTION_LOAD_INTERSTITIAL_AD,
        [{"placement":placement}]
    );
}

function showInterstitialAdFromSDK(placement) {
    log("showInterstitialAdFromSDK");

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Fail")
        },
        "FreestarPlugin",
        ACTION_SHOW_INTERSTITIAL_AD,
        [{"placement":placement}]
    );
}

//////////////////////////////SHOW REWARD AD//////////////////////////////
var ACTION_LOAD_REWARD_AD = "LOAD_REWARD_AD";
var ACTION_SHOW_REWARD_AD = "SHOW_REWARD_AD";
var ACTION_CHECK_REWARD_AD = "CHECK_REWARD_AD";

var ACTION_SET_USER_PARAMS = "SET_USER_PARAMS";
var ACTION_SET_APP_PARAMS = "SET_APP_PARAMS";
var ACTION_SET_TESTMODE_PARAMS = "SET_TESTMODE_PARAMS";

function loadRewardAdFromSDK(placement) {
    log("loadRewardAdFromSDK");

    cordova.exec(
        function(result) {
            log("Success")
        },
        function(result) {
            log("Fail");
        },
        "FreestarPlugin",
        ACTION_LOAD_REWARD_AD,
        [{"placement":placement}]
    );
}

function showRewardAdFromSDK(placement, secret, userid, rewardName, rewardAmount) {
    log("showRewardAdFromSDK");

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Fail")
        },
        "FreestarPlugin",
        ACTION_SHOW_REWARD_AD,
        [{
            "placement":placement,
         	"SECRET":secret,
         	"USERID":userid,
         	"REWARDNAME":rewardName,
         	"REWARDAMOUNT":rewardAmount
         }]
    );
}

function checkRewardAdStatusFromSDK(placement) {
    log("checkRewardAdStatusFromSDK");

    cordova.exec(
        function(result) {
            log("checkRewardAdStatus Success");
            //REWARD_AD_LOADED = true;
        },
        function(result) {
            log("checkRewardAdStatus Failed");
            //REWARD_AD_LOADED = false;
        },
        "FreestarPlugin",
        ACTION_CHECK_REWARD_AD,
        [{"placement":placement}]
    );
}

var FreestarAds = {};

FreestarAds.loadRewardedAd = function(placement) {
	console.log("FreestarPlugin.js: load rewarded.  placement: " + placement);
	loadRewardAdFromSDK(placement);
}

FreestarAds.loadInterstitialAd = function(placement) {
   console.log("FreestarPlugin.js: load interstitial.  placement: " + placement);
	loadInterstitialAdFromSDK(placement);
}

FreestarAds.showRewardedAd = function(placement) {
	console.log("FreestarPlugin.js: show rewarded.  placement: " + placement);
	showRewardAdFromSDK(placement);
}

FreestarAds.showInterstitialAd = function(placement) {
   console.log("FreestarPlugin.js: show interstitial.  placement: " + placement);
	showInterstitialAdFromSDK(placement);
}