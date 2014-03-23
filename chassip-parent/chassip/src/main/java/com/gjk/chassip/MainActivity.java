package com.gjk.chassip;

import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.gjk.chassip.database.DatabaseManager;
import com.gjk.chassip.database.DatabaseManager.DataChangeListener;
import com.gjk.chassip.database.PersistentObject;
import com.gjk.chassip.database.objects.Group;
import com.gjk.chassip.database.objects.GroupMember;
import com.gjk.chassip.database.objects.User;
import com.gjk.chassip.net.GetGroupMembersTask;
import com.gjk.chassip.net.GetMultipleGroupsTask;
import com.gjk.chassip.net.TaskResult;
import com.gjk.chassip.net.HTTPTask.HTTPTaskListener;
import com.gjk.chassip.service.ChassipService;
import com.google.common.collect.Lists;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import static com.gjk.chassip.helper.DatabaseHelper.*;

/**
 * Activity for chats. This extends {@link SlidingFragmentActivity} and implements {@link Service}.
 * 
 * @author gpl
 * 
 */
public class MainActivity extends SlidingFragmentActivity implements ServiceConnection, DataChangeListener {

	private final String LOGTAG = getClass().getSimpleName();

	private ViewPager mViewPager;
	private ActionBar mActionBar;
	private ThreadPagerAdapter mThreadPagerAdapter;
	// private AddChatTask mAddChatTask;
	// private AddThreadTask mAddThreadTask;
	// private AddInstantMessageTask mAddInstantMessagrTask;
	// private AddMemberTask mMemberTask;
	// private AlertDialog mDialog;
	private ChatsDrawerFragment mChatsDrawerFragment;
	private ThreadsDrawerFragment mThreadsDrawerFragment;

	private Messenger mServiceMessenger = null;
	boolean mIsBound;

	private Context mCtx;
	private final Messenger mClientMessager = new Messenger(new IncomingMessageHandler());

	private ServiceConnection mConnection = this;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gjk.chassip.DrawerActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Debug.waitForDebugger();

		mCtx = this;

		// Listen for table changes
		Application.get().getDatabaseManager().registerDataChangeListener(User.TABLE_NAME, this);
		Application.get().getDatabaseManager().registerDataChangeListener(Group.TABLE_NAME, this);
		Application.get().getDatabaseManager().registerDataChangeListener(GroupMember.TABLE_NAME, this);
		try {
			setAccountUser(getIntent().getExtras());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String fullName = getAccountUserFullName();
		String message1 = String.format(Locale.getDefault(), "Welcome, %s! You're swagged out!", fullName);
		Toast.makeText(getApplicationContext(), message1, Toast.LENGTH_SHORT).show();
		// mAccountUserId = getAccountUserId(mCtx);

		// Instantiate sliding menu
		final SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);

		// set the Behind View
		setBehindContentView(R.layout.chats_drawer);

		// commit/get drawer fragments
		if (savedInstanceState == null) {

			mChatsDrawerFragment = new ChatsDrawerFragment() {
				@Override
				public void onListItemClick(ListView l, View v, int position, long id) {
					super.onListItemClick(l, v, position, id);
					sm.toggle();
					toggleChat(this.getChatIdFromPosition(position));
				}
			};
			mThreadsDrawerFragment = new ThreadsDrawerFragment() {
				@Override
				public void onListItemClick(ListView l, View v, int position, long id) {
					super.onListItemClick(l, v, position, id);
					sm.toggle();
					// TODO: fill me in!!
				}
			};

			getSupportFragmentManager().beginTransaction().replace(R.id.chats_menu_frame, mChatsDrawerFragment)
					.commit();

			sm.setSecondaryMenu(getLayoutInflater().inflate(R.layout.threads_drawer, null));
			getSupportFragmentManager().beginTransaction().replace(R.id.threads_menu_frame, mThreadsDrawerFragment)
					.commit();

		} else {

			mChatsDrawerFragment = (ChatsDrawerFragment) this.getSupportFragmentManager().findFragmentById(
					R.id.chats_menu_frame);
			mThreadsDrawerFragment = (ThreadsDrawerFragment) this.getSupportFragmentManager().findFragmentById(
					R.id.threads_menu_frame);
		}

		// customize
		sm.setShadowWidthRes(R.dimen.shadow_width);
		// sm.setShadowDrawable(R.drawable.shadow);
		// sm.setSecondaryShadowDrawable(R.drawable.shadowright);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

		// instantiate new view pager
		mViewPager = new ViewPager(this);

		// get action bar tabs
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mViewPager.setId(R.id.pager);
		mThreadPagerAdapter = new ThreadPagerAdapter(getSupportFragmentManager(), mViewPager, mActionBar);
		setContentView(mViewPager);
		mViewPager.setOffscreenPageLimit(Constants.OFFSCREEN_PAGE_LIMIT);

		startService(new Intent(MainActivity.this, ChassipService.class));
		doBindService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// automaticBind();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// mHandler.removeCallbacks(mInjectorThread);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mServiceMessenger = new Messenger(service);
		Log.i(LOGTAG, "onServiceConnected()");
		try {
			android.os.Message msg = android.os.Message.obtain(null, ChassipService.MSG_REGISTER_CLIENT);
			msg.replyTo = mClientMessager;
			mServiceMessenger.send(msg);
		} catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// This is called when the connection with the service has been unexpectedly disconnected - process crashed.
		mServiceMessenger = null;
		Log.i(LOGTAG, "onServiceDisconnected()");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			doUnbindService();
		} catch (Throwable t) {
			Log.e(LOGTAG, "Failed to unbind from the service", t);
		}
	}

	/**
	 * Check if the service is running. If the service is running when the activity starts, we want to automatically
	 * bind to it.
	 */
	@SuppressWarnings("unused")
	private void automaticBind() {
		if (ChassipService.isRunning()) {
			doBindService();
		}
	}

	/**
	 * Bind this Activity to MyService
	 */
	private void doBindService() {
		bindService(new Intent(this, ChassipService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		Log.i(LOGTAG, "doBindService()");
	}

	/**
	 * Un-bind this Activity to MyService
	 */
	private void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with it, then now is the time to unregister.
			if (mServiceMessenger != null) {
				try {
					android.os.Message msg = android.os.Message.obtain(null, ChassipService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mClientMessager;
					mServiceMessenger.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
			Log.i(LOGTAG, "doUnbindService()");
		}
	}

	@SuppressWarnings("unused")
	private void pauseService() {
		Log.i(LOGTAG, "pauseService()");
		try {
			android.os.Message msg = android.os.Message.obtain(null, ChassipService.MSG_PAUSE);
			msg.replyTo = mClientMessager;
			mServiceMessenger.send(msg);
		} catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		}
	}

	@SuppressWarnings("unused")
	private void goService() {
		Log.i(LOGTAG, "goService()");
		try {
			android.os.Message msg = android.os.Message.obtain(null, ChassipService.MSG_GO);
			msg.replyTo = mClientMessager;
			mServiceMessenger.send(msg);
		} catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		}
	}

	private void fetchGroups() {
		new GetMultipleGroupsTask(this, new HTTPTaskListener() {

			@Override
			public void onTaskComplete(TaskResult result) {

				if (result.getResponseCode() == 1) {
					JSONArray response = (JSONArray) result.getExtraInfo();
					try {
						addGroups(response);
					} catch (Exception e) {
						handleGetGroupsError(e);
					}
				} else {
					handleGetGroupsFail(result);
				}
			}
		}, getAccountUserId());
	}

	private void fetchGroupMembers(final Group chat) {
		new GetGroupMembersTask(this, new HTTPTaskListener() {

			@Override
			public void onTaskComplete(TaskResult result) {

				if (result.getResponseCode() == 1) {
					JSONArray response = (JSONArray) result.getExtraInfo();
					try {
						addGroupMembers(response, chat.getGlobalId());
					} catch (Exception e) {
						handleGetGroupMembersError(e);
					}

				} else {
					handleGetGroupMembersFail(result);
				}
			}
		}, chat.getGlobalId());
	}

	private void handleGetGroupsFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Getting Groups failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Getting Groups failed: %s", result.getMessage()));
	}

	private void handleGetGroupsError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Getting Groups errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Getting Groups errored: %s", e.getMessage()));
	}

	private void handleGetGroupMembersFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Getting Chat Members failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Getting Chat Members failed: %s", result.getMessage()));
	}

	private void handleGetGroupMembersError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Getting Chat Members errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Getting Chat Members errored: %s", e.getMessage()));
	}

	private void showLongToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void toggleChat(Group chat) {
		if (ChatsDrawerFragment.getCurrentChat() == null || chat != ChatsDrawerFragment.getCurrentChat()) {
			mThreadPagerAdapter.setChat(chat);
		}
	}

	private void addToThreadDrawer(ThreadFragment frag) {
		mThreadsDrawerFragment.addThread(frag);
	}

	private void removeAllThreadsFromDrawer() {
		mThreadsDrawerFragment.removeAllThreads();
	}

	/**
	 * Handle incoming messages from MyService
	 */
	@SuppressLint("HandlerLeak")
	private class IncomingMessageHandler extends Handler {
		@Override
		public void handleMessage(android.os.Message msg) {

			try {

				JSONObject json = bundleToJson(msg.getData());
				Log.d(LOGTAG, "Handling message: " + json);

				switch (msg.what) {
				case ChassipService.MSG_NEW_MESSAGE:
					addGroupMessage(json);
					break;
				case ChassipService.MSG_NEW_GROUP_MEMBER:
//					addGroupMember(json, false);
					break;
				case ChassipService.MSG_NEW_GROUP_MEMBER_LAST:
//					addGroupMember(json, true);
					break;
				case ChassipService.MSG_NEW_THREAD:
					break;
				case ChassipService.MSG_NEW_CHAT:
					addGroup(json);
					break;
				default:
					super.handleMessage(msg);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * Implementation of {@link FragmentPagerAdapter}
	 * 
	 * @author gpl
	 * 
	 */
	private class ThreadPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener,
			ActionBar.TabListener {

		private List<ThreadFragment> mCurrrentThreads;
		private ViewPager mPager;
		private ActionBar mBar;

		protected ThreadPagerAdapter(FragmentManager fm, ViewPager vp, ActionBar ab) {
			super(fm);
			mPager = vp;
			mPager.setAdapter(this);
			mPager.setOnPageChangeListener(this);
			mBar = ab;
			mCurrrentThreads = Lists.newLinkedList();
		}

		@Override
		public int getCount() {
			return mCurrrentThreads == null ? 0 : mCurrrentThreads.size();
		}

		@Override
		public Fragment getItem(int position) {
			return mCurrrentThreads.get(position);
		}

		@Override
		public int getItemPosition(Object item) {
			return POSITION_NONE;
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			mPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			mBar.setSelectedNavigationItem(position);
		}

		protected void setChat(Group chat) {
			clear();
			ChatsDrawerFragment.setCurrentChat(chat);
			ThreadFragment mainFrag = generateMainThreadFragment(chat);
			mCurrrentThreads.add(mainFrag);
			notifyDataSetChanged();
			ThreadFragment[] sideConvoFrags = generateSideConvoThreadFragments(chat);
			for (ThreadFragment frag : sideConvoFrags) {
				mCurrrentThreads.add(frag);
				notifyDataSetChanged();
			}
			ThreadFragment[] whisperFrags = generateWhisperThreadFragments(chat);
			for (ThreadFragment frag : whisperFrags) {
				mCurrrentThreads.add(frag);
				notifyDataSetChanged();
			}
			for (ThreadFragment frag : mCurrrentThreads) {
				Tab tab = mBar.newTab().setText(frag.getName()).setTabListener(this);
				mBar.addTab(tab);
				addToThreadDrawer(frag);
			}
			mPager.setCurrentItem(0);
		}

		private ThreadFragment generateMainThreadFragment(Group chat) {
			return new ThreadFragment(chat.getGlobalId(), mCurrrentThreads.size(), ThreadType.MAIN_CHAT, chat.getName(), mChatsDrawerFragment.getMembers(chat.getGlobalId()));
		}

		private ThreadFragment[] generateSideConvoThreadFragments(Group chat) {
			// JSONObject sidechats = new JSONObject(chat.getSideChats());
			// GroupMember[] members = getGroupMembers(mCtx, chat.getGlobalId());
			return new ThreadFragment[] {};
		}

		private ThreadFragment[] generateWhisperThreadFragments(Group chat) {
			// JSONObject sidechats = new JSONObject(chat.getWhispers());
			// GroupMember[] members = getGroupMembers(mCtx, chat.getGlobalId());
			return new ThreadFragment[] {};
		}

		private void clear() {
			mBar.removeAllTabs();
			removeAllThreadsFromDrawer();
			mCurrrentThreads.clear();
			notifyDataSetChanged();
		}
	}

	@Override
	public void onDataChanged(PersistentObject o) {
		if (o.getTableName().equals(User.TABLE_NAME)) {
			fetchGroups();
		} else if (o.getTableName().equals(Group.TABLE_NAME)) {
			Group g = (Group) o;
			if (ChatsDrawerFragment.getCurrentChat() == null) {
				toggleChat(g);
			}
			fetchGroupMembers(g);
		}
	}
}
