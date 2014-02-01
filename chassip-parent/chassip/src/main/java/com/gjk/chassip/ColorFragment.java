package com.gjk.chassip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Test class. Will be removed...
 * @author gpl
 *
 */
public class ColorFragment extends Fragment {
	
	private int mColorRes = -1;
	
	public ColorFragment() { 
		super();
		setColor(R.color.white);
		setRetainInstance(true);
	}
	
	public void setColor(int colorRes) {
		mColorRes = colorRes;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null)
			mColorRes = savedInstanceState.getInt("mColorRes");
		int color = getResources().getColor(mColorRes);
		// construct the RelativeLayout
		RelativeLayout v = new RelativeLayout(getActivity());
		v.setBackgroundColor(color);		
		return v;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("mColorRes", mColorRes);
	}
	
	@Override
	public String toString() {
		if (mColorRes==R.color.red)
			return "Red";
		else if (mColorRes==R.color.green)
			return "Green";
		else if (mColorRes==R.color.blue)
			return "Blue";
		else if (mColorRes==R.color.white)
			return "White";
		else if (mColorRes==R.color.black)
			return "Black";
		else
			return "Ooops";
				
	}
	
}
