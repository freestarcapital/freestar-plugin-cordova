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
function setPartnerChooserEnabled(isEnabled) {
    log("setPartnerChooserEnabled");

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Fail")
        },
        "FreestarPlugin",
        ACTION_SET_ENABLE_PARTNER_CHOOSER,
        [{
         	"ENABLED":isEnabled
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

const ACTION_LOAD_INTERSTITIAL_AD = "LOAD_INTERSTITIAL_AD";
const ACTION_SHOW_INTERSTITIAL_AD = "SHOW_INTERSTITIAL_AD";

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
const ACTION_LOAD_REWARD_AD = "LOAD_REWARD_AD";
const ACTION_SHOW_REWARD_AD = "SHOW_REWARD_AD";
const ACTION_CHECK_REWARD_AD = "CHECK_REWARD_AD";

const ACTION_SET_USER_PARAMS = "SET_USER_PARAMS";
const ACTION_SET_TESTMODE_PARAMS = "SET_TESTMODE_PARAMS";
const ACTION_SET_ENABLE_PARTNER_CHOOSER = "SET_ENABLE_PARTNER_CHOOSER";

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

//banner ads
const ACTION_SHOW_BANNER_AD = "SHOW_BANNER_AD";
const ACTION_CLOSE_BANNER_AD = "CLOSE_BANNER_AD";

function showBannerAdFromSDK(placement, bannerAdSize, bannerAdPosition) {
    log("showBannerAd");

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Fail")
        },
        "FreestarPlugin",
        ACTION_SHOW_BANNER_AD,
        [{
            "placement":placement,
         	"banner_ad_size":bannerAdSize,
         	"banner_ad_position":bannerAdPosition
         }]
    );
}

function closeBannerAdFromSDK(placement, bannerAdSize) {
    log("closeBannerAd");

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Fail")
        },
        "FreestarPlugin",
        ACTION_CLOSE_BANNER_AD,
        [{
            "placement":placement,
         	"banner_ad_size":bannerAdSize
         }]
    );
}


(function (global) {
   var FreestarPlugin = function () {};

   //supported banner ad sizes
   FreestarPlugin.prototype.BANNER_AD_SIZE_320x50 = 0;
   FreestarPlugin.prototype.BANNER_AD_SIZE_300x250 = 1;

   //supported banner ad positions
   FreestarPlugin.prototype.BANNER_AD_POSITION_BOTTOM = 0;
   FreestarPlugin.prototype.BANNER_AD_POSITION_CENTER = 1;
   FreestarPlugin.prototype.BANNER_AD_POSITION_TOP = 2;

   FreestarPlugin.prototype.loadRewardedAd = function(placement) {
      console.log("FreestarPlugin.js: load rewarded.  placement: " + placement);
      loadRewardAdFromSDK(placement);
   }

   FreestarPlugin.prototype.loadInterstitialAd = function(placement) {
      console.log("FreestarPlugin.js: load interstitial.  placement: " + placement);
      loadInterstitialAdFromSDK(placement);
   }

   FreestarPlugin.prototype.showRewardedAd = function(placement, SECRET, USERID, REWARDNAME, REWARDAMOUNT) {
      console.log("FreestarPlugin.js: show rewarded.  placement: " + placement);
      showRewardAdFromSDK(placement, SECRET, USERID, REWARDNAME, REWARDAMOUNT);
   }

   FreestarPlugin.prototype.showInterstitialAd = function(placement) {
      console.log("FreestarPlugin.js: show interstitial.  placement: " + placement);
      showInterstitialAdFromSDK(placement);
   }

   FreestarPlugin.prototype.showBannerAd = function(placement, bannerAdSize, bannerAdPosition) {
      console.log("FreestarPlugin.js: show banner ad.  placement: " + placement
                  + " adSize: " + bannerAdSize
                  + " adPosition: " + bannerAdPosition);
      showBannerAdFromSDK(placement, bannerAdSize, bannerAdPosition);
   }

   FreestarPlugin.prototype.closeBannerAd = function(placement, bannerAdSize) {
      console.log("FreestarPlugin.js: close banner ad.  placement: " + placement
                  + " adSize: " + bannerAdSize);
      closeBannerAdFromSDK(placement, bannerAdSize);
   }

   FreestarPlugin.prototype.setTestModeEnabled = function(isEnabled, hashID) {
      console.log("FreestarPlugin.js: setTestModeEnabled.  isEnabled: " + isEnabled
                  + " hashID: " + hashID);
      setTestModeEnabled(isEnabled, hashID);
   }

   FreestarPlugin.prototype.setPartnerChooserEnabled = function(isEnabled) {
      console.log("FreestarPlugin.js: setPartnerChooserEnabled.  isEnabled: " + isEnabled);
      setPartnerChooserEnabled(isEnabled);
   }

   FreestarPlugin.prototype.setAdRequestUserParams = function( age,
                                                               birthDate,
                                                               gender,
                                                               maritalStatus,
                                                               ethnicity,
                                                               dmaCode,
                                                               postal,
                                                               curPostal,
                                                               latitude,
                                                               longitude) {
      console.log("FreestarPlugin.js: setAdRequestUserParams. age: " + age
                                                               + " birthDate: " + birthDate
                                                               + " gender: " + gender
                                                               + " maritalStatus: " + maritalStatus
                                                               + " dmaCode: " + dmaCode
                                                               + " postal: " + postal
                                                               + " curPostal: " + curPostal
                                                               + " latitude: " + latitude
                                                               + " longitude: " + longitude);
      setAdRequestUserParams(age, birthDate, gender, maritalStatus,
                                      ethnicity, dmaCode, postal, curPostal, latitude, longitude);
   }

  global.cordova.addConstructor(function () {
      if (!global.Cordova) {
          global.Cordova = global.cordova;
      }

      if (!global.plugins) {
          global.plugins = {};
      }

      global.plugins.freestarPlugin = new FreestarPlugin();
  });
} (window));
