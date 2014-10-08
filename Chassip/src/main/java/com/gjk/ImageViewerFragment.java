package com.gjk;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.Toast;
import android.graphics.PointF;
import android.util.FloatMath;
import android.util.Log;
import android.view.View.OnTouchListener;

import com.gjk.helper.GeneralHelper;
import com.gjk.views.CacheImageView;
//import com.gjk.views.TouchImageView;


public class ImageViewerFragment extends Fragment implements OnTouchListener{

    private CacheImageView image;

    private static final String TAG = "Touch";
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();


    //state of image
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    public ImageViewerFragment(){};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.image_viewer, container, false);
        //v.setHovered(true);
        return v;

    }

    @Override
    public void onStart() {
        super.onStart();
        image = (CacheImageView) getView().findViewById(R.id.displayedImage);
        /*Context context = getActivity().getApplicationContext();
        String text = "creating..";
        int duration = Toast.LENGTH_SHORT;
        GeneralHelper.reportMessage(context, "imageViewer", text, true);*/
        String imgUrl =  getArguments().getString("imgUrl");
        image.setVisibility(View.VISIBLE);
        image.configure(Constants.BASE_URL + imgUrl, 0, false);
        image.setOnTouchListener(this);
        matrix = image.getImageMatrix();


    }

    public boolean onTouch (View v, MotionEvent event){
        //handle touch event here
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        // Dump touch event to log
        //dumpEvent(event);

        switch(event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:// first finger down only

                savedMatrix.setScale((float) mid.x, (float) mid.y);
                savedMatrix.set(image.getImageMatrix());
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:// first and second finger down
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if(oldDist > 10f){
                    savedMatrix.setScale(mid.x, (float) mid.y);
                    savedMatrix.set(image.getImageMatrix());
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_UP:// first finger lifted
            case MotionEvent.ACTION_POINTER_UP:// second finger lifted
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_MOVE:
                if(mode == DRAG){

                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                }
                else if (mode == ZOOM){
                    // pinch zooming

                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if(newDist > 10f){
                        matrix.set(savedMatrix);
                        float scale = newDist/oldDist;

                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        //do transformation
        view.setImageMatrix(matrix);
        //view.setScaleType(ImageView.ScaleType.FIT_CENTER);

        return true;
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.d(TAG, sb.toString());
    }


    private float spacing(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt( x*x + y*y );
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }




}