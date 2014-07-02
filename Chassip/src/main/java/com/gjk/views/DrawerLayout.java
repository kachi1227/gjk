package com.gjk.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author gpl
 */
public class DrawerLayout extends android.support.v4.widget.DrawerLayout {

    private static final String LOGTAG = "DrawerLayout";

    Map<Long, View> mViews;

    public DrawerLayout(Context context) {
        super(context);
        mViews = Maps.newHashMap();
    }

    public DrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViews = Maps.newHashMap();
    }

    public DrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mViews = Maps.newHashMap();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isDrawerOpen(Gravity.RIGHT)) {
            for (Long convoId : mViews.keySet()) {
                View view = mViews.get(convoId);
                if (view != null) {
                    if (view.isShown()) {
                        if (isPointInsideView(ev.getRawX(), ev.getRawY(), view)) {
                            return false;
                        }
                    } else {
                        view.setOnCreateContextMenuListener(null);
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev);// && shouldIntercept();
    }

    @Override
    public void openDrawer(View drawerView) {
        closeBothDrawers();
        super.openDrawer(drawerView);
    }

    @Override
    public void openDrawer(int gravity) {
        closeBothDrawers();
        super.openDrawer(gravity);
    }

    public void registerView(long convoId, View view) {
        mViews.put(convoId, view);
    }

    public void unregisterViews(long convoId) {
        View view = mViews.get(convoId);
        if (view != null) {
            mViews.get(convoId).setOnCreateContextMenuListener(null);
        }
        mViews.remove(convoId);
    }

    public void unregisterViews() {
        for (View view : mViews.values()) {
            if (view != null) {
                view.setOnCreateContextMenuListener(null);
            }
        }
        mViews.clear();
    }

    public void closeBothDrawers() {
        closeDrawer(Gravity.LEFT);
        closeDrawer(Gravity.RIGHT);
    }

    private boolean isPointInsideView(float x, float y, View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect.contains((int) x, (int) y);
    }
}
