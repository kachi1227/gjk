package com.gjk.chassip;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * Activity class for left and right drawers, and any settings. Extends
 * {@link SlidingFragmentActivity}. 
 * @author gpl
 *
 */
public class DrawerActivity extends SlidingFragmentActivity {

	private int mTitleRes;
	protected ListFragment leftDrawerFragment;
	protected ListFragment rightDrawerFragment;

	/**
	 * Constructor
	 * @param titleRes
	 */
	public DrawerActivity(int titleRes) {
		mTitleRes = titleRes;
	}

	/*
	 * (non-Javadoc)
	 * @see com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set Title
		setTitle(mTitleRes);

		// Instantiate sliding menu
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		
		// set the Behind View
		setBehindContentView(R.layout.left_drawer);
		
		// commit/get drawer fragments
		if (savedInstanceState == null) {
			leftDrawerFragment = new SampleListFragment();
			rightDrawerFragment = new SampleListFragment();
			
			getSupportFragmentManager()
	        .beginTransaction()
	        .replace(R.id.left_menu_frame, leftDrawerFragment)
	        .commit();
			
			sm.setSecondaryMenu(getLayoutInflater().inflate(R.layout.right_drawer, null));
			getSupportFragmentManager()
	        .beginTransaction()
	        .replace(R.id.right_menu_frame, rightDrawerFragment)
	        .commit();
			
		} else {
			leftDrawerFragment = (ListFragment)this.getSupportFragmentManager().findFragmentById(R.id.left_menu_frame);
			rightDrawerFragment = (ListFragment)this.getSupportFragmentManager().findFragmentById(R.id.right_menu_frame);
			
		}

		// customize
		sm.setShadowWidthRes(R.dimen.shadow_width);
		//sm.setShadowDrawable(R.drawable.shadow);
		//sm.setSecondaryShadowDrawable(R.drawable.shadowright);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
