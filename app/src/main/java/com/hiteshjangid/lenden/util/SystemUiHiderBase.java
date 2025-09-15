package com.hiteshjangid.lenden.util;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;

public class SystemUiHiderBase extends SystemUiHider {

    private boolean mVisible = true;

    protected SystemUiHiderBase(Activity activity, View anchorView, int flags) {
        super(activity, anchorView, flags);
    }

    @Override
    public void setup() {
        if (!hasFlag(FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES)) {
            enableFullScreenLayout();
        }
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    @Override
    public void hide() {
        if (hasFlag(FLAG_FULLSCREEN)) {
            setFullScreenFlags(true);
            updateVisibilityState(false);
        }
    }

    @Override
    public void show() {
        if (hasFlag(FLAG_FULLSCREEN)) {
            setFullScreenFlags(false);
            updateVisibilityState(true);
        }
    }

    private void enableFullScreenLayout() {
        mActivity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void setFullScreenFlags(boolean fullScreen) {
        int windowFlag = fullScreen
                ? WindowManager.LayoutParams.FLAG_FULLSCREEN
                : 0;
        mActivity.getWindow().setFlags(
                windowFlag,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void updateVisibilityState(boolean visible) {
        mOnVisibilityChangeListener.onVisibilityChange(visible);
        mVisible = visible;
    }

    private boolean hasFlag(int flag) {
        return (mFlags & flag) != 0;
    }
}
