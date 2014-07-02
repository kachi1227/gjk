package com.gjk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author gpl
 */
public class HorizontalScrollView extends android.widget.HorizontalScrollView {

    public HorizontalScrollView(Context context) {
        super(context);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//
//                return false;
//            default:
        return super.onInterceptTouchEvent(ev);
//        }
    }
}
