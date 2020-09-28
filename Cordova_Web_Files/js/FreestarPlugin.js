//////////////////////////////COMMON METHODS FOR ALL AD TYPES//////////////////////////////
var callbackFunction;           //Store the function reference for Ad Event Callback

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
            log("Success");
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

function setAdRequestAppParams(appName, pubName, appDomain, pubDomain, storeUrl, iabCategory) {
    log("setAdRequestAppParams");

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Success");
        },
        "FreestarPlugin",
        ACTION_SET_APP_PARAMS,
        [{
         	"APPNAME":appName,
         	"PUBNAME":pubName,
         	"APPDOMAIN":appDomain,
         	"PUBDOMAIN":pubDomain,
         	"STOREURL":storeUrl,
            "IABCATEGORY":iabCategory
         }]
    );
}

//////////////////////////////INITIALIZE FREESTAR ADS SDK//////////////////////////////
var ACTION_INIT = "FREESTAR_INIT";
function initializeFreestarSDK(apikey) {
    log("Freestar Init");

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Success");
        },
        "FreestarPlugin",
        ACTION_INIT,
        [{"apikey":apikey}]
    );
}


function setTestModeEnabled(isEnabled, hashID) {
    log("setTestModeEnabled");

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Success");
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

var INTERSTITIAL_AD_LOADED = false;

registerInterstitialAdEvents();

function loadInterstitialAdFromSDK(placement) {
    log("loadInterstitialAdFromSDK");
    INTERSTITIAL_AD_LOADED = false;

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Success");
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
            log("Success");
        },
        "FreestarPlugin",
        ACTION_SHOW_INTERSTITIAL_AD,
        [{"placement":placement}]
    );
}

function registerInterstitialAdEvents() {
    document.addEventListener('onInterstitialLoaded', function(data) {
        log("onInterstitialLoaded");
        INTERSTITIAL_AD_LOADED = true;

        callbackFunction("onInterstitialLoaded", data);
    });

    document.addEventListener('onInterstitialFailed', function(data) {
        log("onInterstitialFailed");
        INTERSTITIAL_AD_LOADED = false;

        callbackFunction("onInterstitialFailed", data);
    });

    document.addEventListener('onInterstitialShown', function(data) {
        log("onInterstitialShown");

        callbackFunction("onInterstitialShown", data);
    });

    document.addEventListener('onInterstitialDismissed', function(data) {
        log("FreestarPlugin.js:" + "onInterstitialDismissed");

        callbackFunction("onInterstitialDismissed", data);
    });

    document.addEventListener('onInterstitialClicked', function(data) {
        log("onInterstitialClicked");

        callbackFunction("onInterstitialClicked", data);
    });
}

//////////////////////////////SHOW PREROLL AD//////////////////////////////

var ACTION_SHOW_PREROLL_AD = "SHOW_PREROLL_AD";
var ACTION_PREROLL_AD_EVENT = "PREROLL_AD_EVENT";

var PREROLL_AD_WIDTH;
var PREROLL_AD_HEIGHT;
var PREROLL_AD_CONTAINER;
var PREROLL_AD_VIDEO_ELEMENT;

var NO_OF_PARTNER = 0;
var TOTAL_FAILED_PARTNER = 0;

function showPrerollAdFromSDK(width, height, adDiv, videoElement) {

    PREROLL_AD_WIDTH = width;
    PREROLL_AD_HEIGHT = height;
    PREROLL_AD_CONTAINER = adDiv;
    PREROLL_AD_VIDEO_ELEMENT = videoElement;

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Success");
        },
        "FreestarPlugin",
        ACTION_SHOW_PREROLL_AD,
        [{"width":PREROLL_AD_WIDTH, "height":PREROLL_AD_HEIGHT}]
    );
}

function prerollAdLoadedFromSDK(urls) {
    loadPrerollAdInDiv(urls);
}

function prerollAdFailedFromSDK(error) {
    callbackFunction("AdError");

    playMainContent();
}

function loadPrerollAdInDiv(urls) {
    urls = JSON.parse(urls);
    NO_OF_PARTNER = urls.length;
    TOTAL_FAILED_PARTNER = 0;

    for(var url in urls) {
        log("Url from SDK : " + urls[url]);

        if (urls[url].indexOf("caller=mpsdk") != -1) {
            log("Freestar Player Starting");

            // URL FOR TESTING  :  "https://srv2.vdopia.com/adserver/html5/inwapads/?output=sdkvast&caller=mpsdk&adFormat=preroll&ak=a31006151c434a3205f10250f8676566&version=2.0&fullscreen=0&showClose=1&container=androidWebNot&sleepAfter=0&target_params=sex=Male|birthday=1988-01-21|age=27|maritalStatus=single|ethnicity=asian|postalcode=110096|currpostal=201301|dmacode=807|emailhash+5DF956EDA0E58FF2696E02D9A45A2414|geoType=2&adSize=[vdo_adSize]&adType=[vdo_adtype]&isExpandable=[vdo_expandable]&di=8f0da3f7-9a56-4ecd-a6a0-0b2ec1fe75f2&dimm=[vdo_dimm]&dims=[vdo_dims]&diim=[vdo_diim]&diis=[vdo_diis]&dium=[vdo_dium]&dius=[vdo_dius]&mraid=[vdo_mraid]&requester=Vdopia&dif=dpid&ua=Mozilla/5.0+(Linux;+Android+5.0;+Nexus+5+Build/LRX21O;+wv)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Version/4.0+Chrome/51.0.2704.81+Mobile+Safari/537.36&cb=1470724313&type=app&appDomain=vdopia.com&appBundle=chocolateApp&appName=VdopiaSampleApp&appStoreUrl=play.google.com&category=prerollad&dnt=1&pos=0&linearity=1&publisherdomain=vdopia.com&devicemodel=Nexus+5&deviceos=android&deviceosv=5.0",
            // PARTNER NAME, VAST URL, AD CONTAINER DIV, MAIN CONTENT VIDEO TAG, VIDEO DIV WIDTH, VIDEO DIV HEIGHT, FUNCTION TO GET EVENTS
            new sdk_chocolate(
                "freestar",
                urls[url],
                PREROLL_AD_CONTAINER, PREROLL_AD_VIDEO_ELEMENT,
                300, 250,
                onAdEventVdopia);
        } else {
            log("IMA Player Starting");

            // URL FOR TESTING  :  "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=",
            // PARTNER NAME, VAST URL, AD CONTAINER DIV, MAIN CONTENT VIDEO TAG, VIDEO DIV WIDTH, VIDEO DIV HEIGHT,
            setupIMA(
                "google",
                urls[url],
                PREROLL_AD_CONTAINER, PREROLL_AD_VIDEO_ELEMENT,
                300, 250);
        }
    }
}

function showPrerollAdInDiv(partner) {
    log("showPrerollAdInDiv for Winner " + partner);

    callbackFunction("AdLoaded");

    if (partner.toLowerCase() === "google") {
        if (playAds) {
            playAds();
        }
    } else {
        if (vdopiaEvent) {
            vdopiaEvent();
        }
    }
}

var vdopiaEvent;        //This variable is required to save the function which start the Ad after mediation
function onAdEventVdopia(event) {
    console.log("Preroll Event Fired : " + event.type);

    if (event.type == "AdLoaded") {
        vdopiaEvent = event.startAd;            //Save function to start the AD in above created variable
        prerollAdEventFromJSPlayer("chocolate", "AdLoaded");
    } else if (event.type == "AdError") {
        prerollAdEventFromJSPlayer("chocolate", "AdError");
    } else if (event.type == 'AdEnded') {
        prerollAdEventFromJSPlayer("chocolate", "AdEnded");
    } else if (event.type == 'AdClicked') {
        prerollAdEventFromJSPlayer("chocolate", "AdClicked");
    }
}

function prerollAdEventFromJSPlayer(partner, event) {
    log("prerollAdLoaded " + partner + " Event : " + event);

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Failed");
        },
        "FreestarPlugin",
        ACTION_PREROLL_AD_EVENT,
        [{"partner":partner, "event":event}]
    );

    if (event === "AdError") {
        TOTAL_FAILED_PARTNER++;
        log("prerollAdFailed " + TOTAL_FAILED_PARTNER + " :: " + NO_OF_PARTNER);

        if (TOTAL_FAILED_PARTNER == NO_OF_PARTNER) {
            callbackFunction("AdError");
            playMainContent();
        }
    } else if (event === "AdClicked") {
        callbackFunction("AdClicked");
    } else if (event === "AdEnded") {
        callbackFunction("AdEnded");
        playMainContent();
    }
}

function playMainContent() {
    PREROLL_AD_CONTAINER.style.display = "none";

    PREROLL_AD_VIDEO_ELEMENT.load();
    PREROLL_AD_VIDEO_ELEMENT.play();
}

//////////////////////////////SHOW REWARD AD//////////////////////////////
var ACTION_LOAD_REWARD_AD = "LOAD_REWARD_AD";
var ACTION_SHOW_REWARD_AD = "SHOW_REWARD_AD";
var ACTION_CHECK_REWARD_AD = "CHECK_REWARD_AD";

var ACTION_SET_USER_PARAMS = "SET_USER_PARAMS";
var ACTION_SET_APP_PARAMS = "SET_APP_PARAMS";
var ACTION_SET_TESTMODE_PARAMS = "SET_TESTMODE_PARAMS";

var REWARD_AD_LOADED = false;

registerRewardAdEvents();

function loadRewardAdFromSDK(placement) {
    log("loadRewardAdFromSDK");

    REWARD_AD_LOADED = false;

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Success");
        },
        "FreestarPlugin",
        ACTION_LOAD_REWARD_AD,
        [{"placement":placement}]
    );
}

function showRewardAdFromSDK(placement, secret, userid, rewardName, rewardAmount) {
    log("showRewardAdFromSDK");

    REWARD_AD_LOADED = false;

    cordova.exec(
        function(result) {
            log("Success");
        },
        function(result) {
            log("Success");
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
    log("showRewardAdFromSDK");

    cordova.exec(
        function(result) {
            log("checkRewardAdStatus Success");
            REWARD_AD_LOADED = true;
        },
        function(result) {
            log("checkRewardAdStatus Failed");
            REWARD_AD_LOADED = false;
        },
        "FreestarPlugin",
        ACTION_CHECK_REWARD_AD,
        [{"placement":placement}]
    );
}

function isRewardAdAvailable() {
    log("isRewardAdAvailable " + REWARD_AD_LOADED);
    return REWARD_AD_LOADED;
}

function registerRewardAdEvents() {
    document.addEventListener('onRewardedVideoLoaded', function(data) {
        log("onRewardedVideoLoaded");
        REWARD_AD_LOADED = true;

        callbackFunction("onRewardedVideoLoaded", data);
    });

    document.addEventListener('onRewardedVideoFailed', function(data) {
        log("onRewardedVideoFailed");
        REWARD_AD_LOADED = false;

        callbackFunction("onRewardedVideoFailed", data);
    });

    document.addEventListener('onRewardedVideoShown', function(data) {
        log("onRewardedVideoShown");

        callbackFunction("onRewardedVideoShown", data);
    });

    document.addEventListener('onRewardedVideoShownError', function(data) {
        log("onRewardedVideoShownError");

        callbackFunction("onRewardedVideoShownError", data);
    });

    document.addEventListener('onRewardedVideoDismissed', function(data) {
        log("onRewardedVideoDismissed");

        callbackFunction("onRewardedVideoDismissed", data);
    });

    document.addEventListener('onRewardedVideoCompleted', function(data) {
        log("onRewardedVideoCompleted");

        callbackFunction("onRewardedVideoCompleted", data);
    });
}
