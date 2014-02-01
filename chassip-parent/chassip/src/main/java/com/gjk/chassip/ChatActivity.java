package com.gjk.chassip;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.ActionBar;

/**
 * Activity for chats. This extends {@link DrawerActivity}.
 * @author gpl
 *
 */
public class ChatActivity extends DrawerActivity {

	ActionBar mActionBar;
	ViewPager mViewPager;
	
	public ChatActivity() {
		super(R.string.app_name);
	}

	/*
	 * (non-Javadoc)
	 * @see com.gjk.chassip.DrawerActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// instantiate new view pager
		mViewPager = new ViewPager(this);
		
		// get action bar tabs
		mActionBar = getSupportActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mViewPager.setId(R.id.pager);
		mViewPager.setAdapter(new ColorPagerAdapter(getSupportFragmentManager()));
		setContentView(mViewPager);

		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				mActionBar.setSelectedNavigationItem(position);
			}

		});
		
		mViewPager.setCurrentItem(0);
		
	}
	
	/**
	 * Implementation of {@link ActionBar.TabListener}
	 * @author gpl
	 *
	 */
	private class TabListener implements ActionBar.TabListener {
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            // show the given tab
            mViewPager.setCurrentItem(tab.getPosition());
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // hide the given tab
        	return;
        }

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // probably ignore this event
        	return;
        }
	}

	/**
	 * Implementation of {@link FragmentPagerAdapter}
	 * @author gpl
	 *
	 */
	private class ColorPagerAdapter extends FragmentPagerAdapter {
		
		private TabListener tabListener;
		private ArrayList<Fragment> mFragments;

		private final int[] COLORS = new int[] {
			R.color.red,
			R.color.green,
			R.color.blue,
			R.color.white,
			R.color.black
		};
		
		public ColorPagerAdapter(FragmentManager fm) {
			super(fm);
			mFragments = new ArrayList<Fragment>();
			tabListener = new TabListener();
			for (int color : COLORS) {
				ColorFragment frag = new ColorFragment();
				frag.setColor(color);
				mFragments.add(frag);
				mActionBar.addTab(
						mActionBar.newTab()
						.setText(frag.toString())
						.setTabListener(tabListener));
			}
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

	}

}
