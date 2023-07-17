package com.hiteshjangid.lenden.expenseincome.util;

import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SystemUiHiderBase extends SystemUiHider {
    private boolean mVisible = true;

    protected SystemUiHiderBase(Activity activity, View anchorView, int flags) {
        super(activity, anchorView, flags);
    }

    @Override
    public void setup() {
        Window window = mActivity.getWindow();
        if ((mFlags & FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES) == 0) {
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    @Override
    public void hide() {
        Window window = mActivity.getWindow();
        if ((mFlags & FLAG_FULLSCREEN) != 0) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        mOnVisibilityChangeListener.onVisibilityChange(false);
        mVisible = false;
    }

    @Override
    public void show() {
        Window window = mActivity.getWindow();
        if ((mFlags & FLAG_FULLSCREEN) != 0) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        mOnVisibilityChangeListener.onVisibilityChange(true);
        mVisible = true;
    }
}
