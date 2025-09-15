package com.hiteshjangid.lenden.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SystemUiHiderHoneycomb extends SystemUiHiderBase {

    private static final int SYSTEM_UI_FLAG_VISIBLE = View.SYSTEM_UI_FLAG_VISIBLE;
    private static final int SYSTEM_UI_FLAG_LOW_PROFILE = View.SYSTEM_UI_FLAG_LOW_PROFILE;

    private int mShowFlags;
    private int mHideFlags;
    private int mTestFlags;
    private boolean mVisible = true;

    protected SystemUiHiderHoneycomb(Activity activity, View anchorView, int flags) {
        super(activity, anchorView, flags);
        initializeFlags();
    }

    @Override
    public void setup() {
        mAnchorView.setOnSystemUiVisibilityChangeListener(mSystemUiVisibilityChangeListener);
    }

    @Override
    public void hide() {
        mAnchorView.setSystemUiVisibility(mHideFlags);
    }

    @Override
    public void show() {
        mAnchorView.setSystemUiVisibility(mShowFlags);
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    private void initializeFlags() {
        mShowFlags = SYSTEM_UI_FLAG_VISIBLE;
        mHideFlags = SYSTEM_UI_FLAG_LOW_PROFILE;
        mTestFlags = SYSTEM_UI_FLAG_LOW_PROFILE;

        if (hasFlag(FLAG_FULLSCREEN)) {
            mShowFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            mHideFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (hasFlag(FLAG_HIDE_NAVIGATION)) {
            mShowFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            mHideFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            mTestFlags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
    }

    private boolean hasFlag(int flag) {
        return (mFlags & flag) != 0;
    }

    private void setFullScreenFlags(boolean show) {
        int windowFlag = show ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        mActivity.getActionBar().setHideOnContentScrollEnabled(!show);
        mActivity.getWindow().setFlags(windowFlag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private View.OnSystemUiVisibilityChangeListener mSystemUiVisibilityChangeListener
            = new View.OnSystemUiVisibilityChangeListener() {
        @Override
        public void onSystemUiVisibilityChange(int vis) {
            boolean newVisibility = (vis & mTestFlags) == 0;
            if (newVisibility != mVisible) {
                mVisible = newVisibility;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    setFullScreenFlags(mVisible);
                }
                mOnVisibilityChangeListener.onVisibilityChange(mVisible);
            }
        }
    };
}
