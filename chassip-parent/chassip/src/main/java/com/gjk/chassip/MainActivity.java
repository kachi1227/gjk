package com.gjk.chassip;

import java.util.List;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import com.gjk.chassip.account.AccountManager;
import com.gjk.chassip.model.Chat;
import com.gjk.chassip.model.ChatManager;
import com.gjk.chassip.model.ImManagerFactory;
import com.gjk.chassip.model.ThreadType;
import com.gjk.chassip.model.User;
import com.gjk.chassip.service.ChassipService;
import com.google.common.collect.Lists;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import static com.gjk.chassip.Constants.*;

/**
 * Activity for chats. This extends {@link SlidingFragmentActivity} and implements {@link Service}.
 * 
 * @author gpl
 * 
 */
public class MainActivity extends SlidingFragmentActivity implements ServiceConnection {

	private final String LOGTAG = getClass().getSimpleName();

	private ChatManager mChatManager = ChatManager.getInstance();

	private ViewPager mViewPager;
	private ActionBar mActionBar;
	private ThreadPagerAdapter mThreadPagerAdapter;
	private AddChatTask mAddChatTask;
	private AddThreadTask mAddThreadTask;
	private AddInstantMessageTask mAddInstantMessagrTask;
	private AddMemberTask mMemberTask;
	private AlertDialog mJoinThreadDialog;
	private ChatsDrawerFragment mChatsDrawerFragment;
	private ThreadsDrawerFragment mThreadsDrawerFragment;

	private Messenger mServiceMessenger = null;
	boolean mIsBound;

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

		String firstName = getIntent().getExtras().getString(FIRST_NAME);
		String lastName = getIntent().getExtras().getString(LAST_NAME);
		long chassipId = getIntent().getExtras().getLong(CHASSIP_ID);
		String gcmId = getIntent().getExtras().getString(GCM_ID);
		String message1 = String
				.format(Locale.getDefault(), "Welcome, %s %s! You're swagged out!", firstName, lastName);
		String message2 = String.format(Locale.getDefault(), "Your Chassip ID is: %d", chassipId);
		String message3 = String.format(Locale.getDefault(), "Your GCM ID is: %s", gcmId);
		Toast.makeText(getApplicationContext(), message1, Toast.LENGTH_SHORT).show();
		Toast.makeText(getApplicationContext(), message2, Toast.LENGTH_SHORT).show();
		Toast.makeText(getApplicationContext(), message3, Toast.LENGTH_SHORT).show();

		AccountManager.getInstance().setUser(new User(firstName + " " + lastName));

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
			Message msg = Message.obtain(null, ChassipService.MSG_REGISTER_CLIENT);
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

	// /**
	// * Send data to the service
	// * @param intvaluetosend The data to send
	// */
	// private void sendMessageToService(int intvaluetosend) {
	// if (mIsBound) {
	// if (mServiceMessenger != null) {
	// try {
	// Message msg = Message.obtain(null, InjectorTrois.MSG_SET_INT_VALUE, intvaluetosend, 0);
	// msg.replyTo = mClientMessager;
	// mServiceMessenger.send(msg);
	// } catch (RemoteException e) {
	// }
	// }
	// }
	// }

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
					Message msg = Message.obtain(null, ChassipService.MSG_UNREGISTER_CLIENT);
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
			Message msg = Message.obtain(null, ChassipService.MSG_PAUSE);
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
			Message msg = Message.obtain(null, ChassipService.MSG_GO);
			msg.replyTo = mClientMessager;
			mServiceMessenger.send(msg);
		} catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		}
	}

	private void toggleChat(final long chatId) {
		if (chatId != mChatManager.getCurrentChatId()) {
			mThreadPagerAdapter.addChat(chatId, 0, null);
		}
	}

	private void joinNewChat(final long chatId, final long threadId, final User[] users) {

		if (!mChatManager.chatExists(chatId)) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// Add the buttons
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mThreadPagerAdapter.addChat(chatId, threadId, users);
				}
			});
			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			String message = getResources().getString(R.string.join_chat_message, User.getUserStrs(users));
			builder.setMessage(message).setTitle(R.string.join_chat_title);

			// Create the AlertDialog
			mJoinThreadDialog = builder.create();
			mJoinThreadDialog.setCanceledOnTouchOutside(true);
			mJoinThreadDialog.show();
		}
	}

	private void addInstantMessage(InstantMessage im) {
		mThreadPagerAdapter.addInstantMessage(im);
	}

	private void addMember(long chatId, long threadId, User user) {
		mThreadPagerAdapter.addMember(chatId, threadId, user);
	}

	private void addMembers(long chatId, long threadId, User... users) {
		for (User user : users) {
			addMember(chatId, threadId, user);
		}
	}

	private void joinThread(final long chatId, final long threadId, final ThreadType type, final User[] members) {

		if (mChatManager.chatExists(chatId)) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// Add the buttons
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mThreadPagerAdapter.addThread(chatId, threadId, type, members);
				}
			});
			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			if (type == ThreadType.SIDE_CONVO) {
				String message = getResources().getString(R.string.join_sideconvo_message, User.getUserStrs(members));
				builder.setMessage(message).setTitle(R.string.join_sideconvo_title);
			} else if (type == ThreadType.WHISPER) {
				String message = getResources().getString(R.string.join_whisper_message, User.getUserStrs(members));
				builder.setMessage(message).setTitle(R.string.join_whisper_title);
			} else {
				throw new RuntimeException("Whooooooa....");
			}

			// Create the AlertDialog
			mJoinThreadDialog = builder.create();
			mJoinThreadDialog.setCanceledOnTouchOutside(true);
			mJoinThreadDialog.show();
		}
	}

	private void addToChatsDrawer(Chat chat) {
		mChatsDrawerFragment.addChat(chat);
	}

	private void updateChatsDrawer() {
		mChatsDrawerFragment.updateView();
	}

	private void addToThreadDrawer(ThreadFragment frag) {
		mThreadsDrawerFragment.addThread(frag);
	}

	private void removeAllThreadsFromDrawer() {
		mThreadsDrawerFragment.removeAllThreads();
	}

	@SuppressWarnings("unused")
	private void removeThreadFromDrawer(ThreadFragment frag) {
		mThreadsDrawerFragment.removeThread(frag);
	}

	private void updateThreadsDrawer() {
		mThreadsDrawerFragment.updateView();
	}

	/**
	 * Handle incoming messages from MyService
	 */
	@SuppressLint("HandlerLeak")
	private class IncomingMessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// Log.d(LOGTAG,"IncomingHandler:handleMessage");
			switch (msg.what) {
			case ChassipService.MSG_NEW_MESSAGE:
				Log.i(LOGTAG, "MSG_NEW_MESSAGE");
				InstantMessage im = new InstantMessage(msg.getData().getLong("chat_id"), msg.getData().getLong(
						"thread_id"), ImManagerFactory.incrementTotalImCount(), new User(msg.getData().getString(
						"user_name")), msg.getData().getString("message"));
				addInstantMessage(im);
				break;
			case ChassipService.MSG_NEW_MEMBERS:
				Log.i(LOGTAG, "MSG_NEW_MEMBERS");
				long chatId2 = msg.getData().getLong("chat_id");
				long threadId2 = msg.getData().getLong("thread_id");
				String[] userNames2 = msg.getData().getStringArray("user_names");
				addMembers(chatId2, threadId2, User.getUsers(userNames2));
				break;
			case ChassipService.MSG_NEW_THREAD:
				Log.i(LOGTAG, "MSG_NEW_THREAD");
				long chatId3 = msg.getData().getLong("chat_id");
				long threadId3 = msg.getData().getLong("thread_id");
				ThreadType type3 = ThreadType.getFromValue(msg.getData().getInt("thread_type"));
				String[] userNames3 = msg.getData().getStringArray("user_names");
				joinThread(chatId3, threadId3, type3, User.getUsers(userNames3));
				break;
			case ChassipService.MSG_NEW_CHAT:
				Log.i(LOGTAG, "MSG_NEW_CHAT");
				long chatId4 = msg.getData().getLong("chat_id");
				long threadId4 = msg.getData().getLong("thread_id");
				String[] userNames4 = msg.getData().getStringArray("user_names");
				joinNewChat(chatId4, threadId4, User.getUsers(userNames4));
				break;
			default:
				super.handleMessage(msg);
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

		protected void clearTabs() {
			mBar.removeAllTabs();
			removeAllThreadsFromDrawer();
			mCurrrentThreads.clear();
			notifyDataSetChanged();
		}

		protected void displayCurrentTabs() {
			for (ThreadFragment frag : mChatManager.getCurrentChat().getThreads()) {
				addTab(frag);
			}
			mPager.setCurrentItem(0);
			notifyDataSetChanged();
		}

		protected void addChat(long chatId, long threadId, User[] members) {
			mAddChatTask = new AddChatTask(chatId, threadId, members);
			mAddChatTask.execute();
		}

		protected void addThread(long chatId, long threadId, ThreadType type, User[] members) {
			mAddThreadTask = new AddThreadTask(chatId, threadId, type, members);
			mAddThreadTask.execute();
		}

		protected void addInstantMessage(InstantMessage im) {
			mAddInstantMessagrTask = new AddInstantMessageTask(im);
			mAddInstantMessagrTask.execute();
		}

		protected void addMember(long chatId, long threadId, User user) {
			mMemberTask = new AddMemberTask(chatId, threadId, user);
			mMemberTask.execute();
		}

		protected void addTab(ThreadFragment frag) {

			String heading;
			switch (frag.getThreadType()) {
			case MAIN_CHAT:
				heading = "MAIN";
				break;
			case SIDE_CONVO:
				heading = "SIDE CONVO";
				break;
			case WHISPER:
				heading = "WHISPER";
				break;
			default:
				heading = "WTF...";
				break;
			}

			Tab tab = mBar
					.newTab()
					.setText(
							String.format(Locale.getDefault(), "%s - %d-%d", heading, frag.getChatId(),
									frag.getThreadId())).setTabListener(this);
			if (mChatManager.getCurrentChatId() == frag.getChatId()) {
				mBar.addTab(tab);
				mCurrrentThreads.add(frag);
				notifyDataSetChanged();
				mPager.setCurrentItem(mCurrrentThreads.size() - 1);
				addToThreadDrawer(frag);
			}
			notifyDataSetChanged();
		}
	}

	private class AddChatTask extends AsyncTask<Void, Void, Void> {

		private long mChatId;
		private long mThreadId;
		private User[] mUsers;
		private ThreadFragment mMainChatFrag;

		protected AddChatTask(long chatId, long threadId, User[] users) {
			mChatId = chatId;
			mThreadId = threadId;
			mUsers = users;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			Thread.currentThread().setName(getClass().getSimpleName());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mThreadPagerAdapter.clearTabs();
			if (mChatManager.chatExists(mChatId)) {
				mChatManager.setCurrentChat(mChatId);
				mThreadPagerAdapter.displayCurrentTabs();
			} else {
				mMainChatFrag = new ThreadFragment(mChatId, mThreadId, ThreadType.MAIN_CHAT, mUsers);
				mChatManager.addChat(mMainChatFrag);
				mThreadPagerAdapter.addTab(mMainChatFrag);
				addToChatsDrawer(mChatManager.getChat(mChatId));
			}
		}
	}

	private class AddThreadTask extends AsyncTask<Void, Void, Void> {

		private long mChatId;
		private long mThreadId;
		private ThreadType mType;
		private User[] mUsers;
		private ThreadFragment mThreadFrag;

		protected AddThreadTask(long chatId, long threadId, ThreadType type, User[] users) {
			mChatId = chatId;
			mThreadId = threadId;
			mType = type;
			mUsers = users;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			Thread.currentThread().setName(getClass().getSimpleName());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mThreadFrag = new ThreadFragment(mChatId, mThreadId, mType, mUsers);
			mChatManager.addThread(mThreadFrag);
			mThreadPagerAdapter.addTab(mThreadFrag);
		}
	}

	private class AddInstantMessageTask extends AsyncTask<Void, Void, Void> {

		private InstantMessage mIm;

		protected AddInstantMessageTask(InstantMessage im) {
			mIm = im;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			Thread.currentThread().setName(getClass().getSimpleName());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mChatManager.addInstantMessage(mIm);
		}
	}

	private class AddMemberTask extends AsyncTask<Void, Void, Void> {

		private long mChatId;
		private long mThreadId;
		private User mUser;

		protected AddMemberTask(long chatId, long threadId, User user) {
			mChatId = chatId;
			mThreadId = threadId;
			mUser = user;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			Thread.currentThread().setName(getClass().getSimpleName());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mChatManager.addMember(mChatId, mThreadId, mUser);
			ThreadFragment frag = mChatManager.getThreadFragment(mChatId, mThreadId);
			if (frag != null) {
				frag.addMember(mUser);
				String message = String.format(Locale.getDefault(), "Added new user: %s", mUser.getName());
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
			updateThreadsDrawer();
			updateChatsDrawer();
		}
	}
}
