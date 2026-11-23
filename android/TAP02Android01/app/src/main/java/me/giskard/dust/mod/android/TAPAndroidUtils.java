package me.giskard.dust.mod.android;

import android.content.Context;
import android.content.res.AssetManager;

public class TAPAndroidUtils {
    private static Context APP_CTX;

    public static void setAppCtx(Context appCtx) {
        APP_CTX = appCtx;
    }
    public static Context getAppCtx() {
        return APP_CTX;
    }

    private static AssetManager ASSET_MANAGER;

    public static void setAssets(AssetManager assets) {
        ASSET_MANAGER = assets;
    }
    public static AssetManager getAssets() {
        return ASSET_MANAGER;
    }
}
