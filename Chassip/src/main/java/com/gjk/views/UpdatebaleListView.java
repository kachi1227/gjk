package com.gjk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * // http://jmsliu.com/1529/rss-reader-android-app-tutorial-4-drag-to-refresh-in-listview-android-example.html
 *
 * @author gpl
 */
public class UpdatebaleListView extends ListView {

    private Boom mBoom = null;

    public interface Boom {
        boolean onTouchEvent(MotionEvent ev);
    }

    public UpdatebaleListView(Context context) {
        super(context);
    }

    public UpdatebaleListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UpdatebaleListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setBoom(Boom boom) {
        mBoom = boom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mBoom != null) {
            return mBoom.onTouchEvent(ev) || super.onTouchEvent(ev);
        } else {
            return super.onTouchEvent(ev);
        }
    }
}
