package com.freestar.android.cordova;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.freestar.android.ads.AdRequest;
import com.freestar.android.ads.AdSize;
import com.freestar.android.ads.BannerAd;
import com.freestar.android.ads.BannerAdListener;

import java.lang.ref.WeakReference;

/**
 * Shows banner ad in a popup window; for Unity
 */
class PopupBannerAd {

    private BannerAd bannerAd;
    private int location;
    private WindowManager wm;
    private WeakReference<Activity> activity;

    public PopupBannerAd(Activity activity) {
        wm = activity.getWindowManager();
        bannerAd = new BannerAd(activity);
        this.activity = new WeakReference<>(activity);
    }

    public void loadBannerAd(AdRequest adRequest, AdSize adSize, String placement,
                             int popupBannerAdLocation, BannerAdListener bannerAdListener) {
        this.location = popupBannerAdLocation;
        bannerAd.setAdSize(adSize);
        bannerAd.setBannerAdListener(bannerAdListener);
        bannerAd.loadAd(adRequest, placement);
    }

    public boolean isShowing() {
        return bannerAd != null && bannerAd.isAttachedToWindow();
    }

    public void onResume() {
        try {
            bannerAd.onResume();
        }catch (Throwable t) {
            //ignored
        }
    }
    public void onPause() {
        try {
            bannerAd.onPause();
        }catch (Throwable t) {
            //ignored
        }
    }

    public void destroy() {
        try {
            wm.removeView(bannerAd);
        }catch (Throwable e) {
            //ignored
        }
        try {
            if (bannerAd != null) {
                bannerAd.destroyView();
                bannerAd = null;
            }
        }catch (Throwable t) {
            //ignored
        }
    }

    public void showBannerAd(View bannerAdView) {

        if (activity == null || activity.get() == null || activity.get().isFinishing()
                || activity.get().isDestroyed()) {
            return;
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
        params.flags =
                // this is to keep button presses going to the background window
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        // this is to enable the notification to receive touch events
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.format = PixelFormat.TRANSLUCENT;
        if (location == FreestarConstants.BANNER_AD_POSITION_TOP) {
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        } else if (location == FreestarConstants.BANNER_AD_POSITION_MIDDLE) {
            params.gravity = Gravity.CENTER;
        } else {
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        }
        try {
            wm.addView(bannerAdView, params);
        }catch (Throwable t) {
            //ignored
        }
    }
}
