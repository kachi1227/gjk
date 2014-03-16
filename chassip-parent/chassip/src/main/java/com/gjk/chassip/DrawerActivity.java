package com.gjk.chassip;

import android.os.Bundle;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import com.gjk.chassip.model.Chat;

/**
 * Activity class for left and right drawers, and any settings. Extends
 * {@link SlidingFragmentActivity}. 
 * @author gpl
 *
 */
public class DrawerActivity extends SlidingFragmentActivity {

	private int mTitleRes;
	private ChatsDrawerFragment mLeftDrawerFragment;
	private ThreadsDrawerFragment mRightDrawerFragment;

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
		setBehindContentView(R.layout.chats_drawer);
		
		// commit/get drawer fragments
		if (savedInstanceState == null) {
			mLeftDrawerFragment = new ChatsDrawerFragment();
			mRightDrawerFragment = new ThreadsDrawerFragment();
			
			getSupportFragmentManager()
	        .beginTransaction()
	        .replace(R.id.chats_menu_frame, mLeftDrawerFragment)
	        .commit();
			
			sm.setSecondaryMenu(getLayoutInflater().inflate(R.layout.threads_drawer, null));
			getSupportFragmentManager()
	        .beginTransaction()
	        .replace(R.id.threads_menu_frame, mRightDrawerFragment)
	        .commit();
			
		} else {
			mLeftDrawerFragment = (ChatsDrawerFragment)this.getSupportFragmentManager().findFragmentById(R.id.chats_menu_frame);
			mRightDrawerFragment = (ThreadsDrawerFragment)this.getSupportFragmentManager().findFragmentById(R.id.threads_menu_frame);
			
		}

		// customize
		sm.setShadowWidthRes(R.dimen.shadow_width);
//		sm.setShadowDrawable(R.drawable.shadow);
//		sm.setSecondaryShadowDrawable(R.drawable.shadowright);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public void addToChatsDrawer(Chat chat) {
		mLeftDrawerFragment.addChat(chat);
	}
	
	public void updateChatsDrawer() {
		mLeftDrawerFragment.updateView();
	}
	
	public void addToThreadDrawer(ThreadFragment frag) {
		mRightDrawerFragment.addThread(frag);
	}
	
	public void removeThreadFromDrawer(ThreadFragment frag) {
		mRightDrawerFragment.removeThread(frag);
	}
	
	public void updateThreadsDrawer() {
		mRightDrawerFragment.updateView();
	}
}
