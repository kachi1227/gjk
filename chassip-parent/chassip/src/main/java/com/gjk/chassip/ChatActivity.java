package com.gjk.chassip;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.gjk.chassip.model.ChatManager;
import com.gjk.chassip.model.ThreadType;
import com.gjk.chassip.model.User;
import com.gjk.chassip.service.InjectorTrois;
import com.google.common.collect.Lists;

/**
 * Activity for chats. This extends {@link DrawerActivity}.
 * @author gpl
 *
 */
@SuppressLint("DefaultLocale")
public class ChatActivity extends DrawerActivity implements ServiceConnection  {

	private final String LOGTAG = getClass().getSimpleName();
	
	private ActionBar mActionBar;
	private ViewPager mViewPager;
	private ThreadPagerAdapter mThreadPagerAdapter;
	private AddChatTask mAddChatTask;
	private AddThreadTask mAddThreadTask;
	private AddInstantMessageTask mAddInstantMessagrTask;
	private AddMemberTask mMemberTask;
	private AlertDialog mJoinThreadDialog;
	
	private Messenger mServiceMessenger = null;
	boolean mIsBound;

	private final Messenger mClientMessager = new Messenger(new IncomingMessageHandler());

	private ServiceConnection mConnection = this;
	
	/**
	 * Check if the service is running. If the service is running 
	 * when the activity starts, we want to automatically bind to it.
	 */
	private void automaticBind() {
		if (InjectorTrois.isRunning()) {
			doBindService();
		}
	}
	
//	/**
//	 * Send data to the service
//	 * @param intvaluetosend The data to send
//	 */
//	private void sendMessageToService(int intvaluetosend) {
//		if (mIsBound) {
//			if (mServiceMessenger != null) {
//				try {
//					Message msg = Message.obtain(null, InjectorTrois.MSG_SET_INT_VALUE, intvaluetosend, 0);
//					msg.replyTo = mClientMessager;
//					mServiceMessenger.send(msg);
//				} catch (RemoteException e) {
//				}
//			}
//		}
//	}

	/**
	 * Bind this Activity to MyService
	 */
	private void doBindService() {
		bindService(new Intent(this, InjectorTrois.class), mConnection, Context.BIND_AUTO_CREATE);
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
					Message msg = Message.obtain(null, InjectorTrois.MSG_UNREGISTER_CLIENT);
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
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mServiceMessenger = new Messenger(service);
		Log.i(LOGTAG, "onServiceConnected()");
		try {
			Message msg = Message.obtain(null, InjectorTrois.MSG_REGISTER_CLIENT);
			msg.replyTo = mClientMessager;
			mServiceMessenger.send(msg);
		} 
		catch (RemoteException e) {
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
	 * Handle incoming messages from MyService
	 */
	private class IncomingMessageHandler extends Handler {		
		@Override
		public void handleMessage(Message msg) {
			// Log.d(LOGTAG,"IncomingHandler:handleMessage");
			switch (msg.what) {
			case InjectorTrois.MSG_NEW_MESSAGE:
				Log.i(LOGTAG, "MSG_NEW_MESSAGE");
				msg.getData().getLong("chat_id");
				InstantMessage im = new InstantMessage(
						msg.getData().getLong("chat_id"),
						msg.getData().getLong("thread_id"),
						new User(msg.getData().getString("user_name")),
						msg.getData().getString("message"));
				addInstantMessage(im);
				break;
			case InjectorTrois.MSG_NEW_MEMBERS:
				Log.i(LOGTAG, "MSG_NEW_MEMBERS");
				long chatId2 = msg.getData().getLong("chat_id");
				long threadId2 = msg.getData().getLong("thread_id");
				String[] userNames2 = msg.getData().getStringArray("user_names");
				addMembers(chatId2, threadId2, User.getUsers(userNames2));
				break;
			case InjectorTrois.MSG_NEW_THREAD:
				Log.i(LOGTAG, "MSG_NEW_THREAD");
				long chatId3 = msg.getData().getLong("chat_id");
				long threadId3 = msg.getData().getLong("thread_id");
				ThreadType type3 = ThreadType.getFromValue(msg.getData().getInt("thread_type"));
				String[] userNames3 = msg.getData().getStringArray("user_names");
				joinThread(chatId3, threadId3, type3, User.getUsers(userNames3));
				break;
			case InjectorTrois.MSG_NEW_CHAT:
				Log.i(LOGTAG, "MSG_NEW_CHAT");
				long chatId4 = msg.getData().getLong("chat_id");
				long threadId4 = msg.getData().getLong("thread_id");
				String[] userNames4 = msg.getData().getStringArray("user_names");
				joinChat(chatId4, threadId4, User.getUsers(userNames4));
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private void pauseService() {
		Log.i(LOGTAG, "pauseService()");
		try {
			Message msg = Message.obtain(null, InjectorTrois.MSG_PAUSE);
			msg.replyTo = mClientMessager;
			mServiceMessenger.send(msg);
		} 
		catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		} 
	}
	
	private void goService() {
		Log.i(LOGTAG, "goService()");
		try {
			Message msg = Message.obtain(null, InjectorTrois.MSG_GO);
			msg.replyTo = mClientMessager;
			mServiceMessenger.send(msg);
		} 
		catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		} 
	}
	
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
		
		String message = String.format("Using ID=%d", getIntent().getExtras().getLong(Constants.ID));
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		
		// instantiate new view pager
		mViewPager = new ViewPager(this);
		
		// get action bar tabs
		mActionBar = getSupportActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mViewPager.setId(R.id.pager);
		mThreadPagerAdapter = new ThreadPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mThreadPagerAdapter);
		setContentView(mViewPager);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				mActionBar.setSelectedNavigationItem(position);
			}
		});
		mViewPager.setOffscreenPageLimit(Constants.OFFSCREEN_PAGE_LIMIT); 
		
		startService(new Intent(ChatActivity.this, InjectorTrois.class));
		doBindService();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
//		automaticBind();
//		mHandler.removeCallbacks(mInjectorThread);
//		mHandler.postDelayed(mInjectorThread, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onPause()
	+ */
	@Override
	protected void onPause() {
		super.onPause();

//		mHandler.removeCallbacks(mInjectorThread);
	}
	
	public void joinChat(final long chatId, final long threadId, final User[] users) {
		mThreadPagerAdapter.removeThreadsFromView();
		mThreadPagerAdapter.addChat(chatId, threadId, users);
	}
	
	public void addInstantMessage(InstantMessage im) {
		mThreadPagerAdapter.addInstantMessage(im);
	}
	
	public void addMember(long chatId, long threadId, User user) {
		mThreadPagerAdapter.addMember(chatId, threadId, user);
	}
	
	public void addMembers(long chatId, long threadId, User... users) {
		for (User user : users) {
			addMember(chatId, threadId, user);
		}
	}
	
	public void joinThread(final long chatId, final long threadId, final ThreadType type, final User[] members) {
		
		if (ChatManager.getInstance().chatExists(chatId)) {
			
//			pauseService();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// Add the buttons
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	        	@Override
				public void onClick(DialogInterface dialog, int which) {
	        		mThreadPagerAdapter.addThread(chatId, threadId, type, members);
//	        		goService();
	        	}
			});
			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
//					goService();
				}
			});
			
			if (type == ThreadType.SIDE_CONVO) {
				builder.setMessage(R.string.join_sideconvo_message)
			           .setTitle(R.string.join_sideconvo_title);
			}
			else if (type == ThreadType.WHISPER) {
				builder.setMessage(R.string.join_sideconvo_message)
			           .setTitle(R.string.join_sideconvo_title);
			}
			else {
				throw new RuntimeException("Whooooooa....");
			}
			
			// Create the AlertDialog
			mJoinThreadDialog = builder.create();
			mJoinThreadDialog.setCanceledOnTouchOutside(true);
			mJoinThreadDialog.show();
		}
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
	private class ThreadPagerAdapter extends FragmentPagerAdapter {
		
		private TabListener mTabListener;
		private List<ThreadFragment> mFragments;
		private List<Tab> mTabs;
		
		public ThreadPagerAdapter(FragmentManager fm) {
			super(fm);
			mFragments = Lists.newLinkedList();
			mTabs = Lists.newLinkedList();
			mTabListener = new TabListener();
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}
		
		@Override
	    public void destroyItem(ViewGroup container, int position, Object object) {
	        super.destroyItem(container, position, object);
	        FragmentManager manager = ((Fragment) object).getFragmentManager();
	        FragmentTransaction trans = manager.beginTransaction();
	        trans.remove((Fragment) object);
	        trans.commit();
	    }
		
		public void removeThreadsFromView() {
			mFragments.removeAll(mFragments);
			mTabs.removeAll(mTabs);
			mActionBar.removeAllTabs();
			if (ChatManager.getInstance().getNumberOfChats() > 0) {
				for (ThreadFragment frag : ChatManager.getInstance().getCurrentChat().getThreads()) {
					removeThreadFromDrawer(frag);
				}
			}
		}
		
		public void addChat(long chatId, long threadId, User[] members) {
			mAddChatTask = new AddChatTask(chatId, threadId, members);
			mAddChatTask.execute();
		}
		
		public void addThread(long chatId, long threadId, ThreadType type, User[] members) {
			mAddThreadTask = new AddThreadTask(chatId, threadId, type, members);
			mAddThreadTask.execute();
		}
		
		public void addInstantMessage(InstantMessage im) {
			mAddInstantMessagrTask = new AddInstantMessageTask(im);
			mAddInstantMessagrTask.execute();
		}
		
		public void addMember(long chatId, long threadId, User user) {
			mMemberTask = new AddMemberTask(chatId, threadId, user);
			mMemberTask.execute();
		}

		public void addTab(ThreadFragment frag) {
			
			mFragments.add(frag);
			
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
			
			Tab tab = mActionBar.newTab();
			mActionBar.addTab(tab
				.setText(heading)
				.setTabListener(mTabListener));
			mViewPager.setCurrentItem(mFragments.size() - 1);
			mTabs.add(tab);
			addToThreadDrawer(frag);
		}
	}
	
	private class AddChatTask extends AsyncTask<Void, Void, Void> {

		private long mChatId;
		private long mThreadId;
		private User[] mUsers;
		private ThreadFragment mMainChatFrag;
		
		public AddChatTask(long chatId, long threadId, User[] users) {
			mChatId = chatId;
			mThreadId = threadId;
			mUsers = users;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			Thread.currentThread().setName(LOGTAG);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (ChatManager.getInstance().chatExists(mChatId)) {
				for (ThreadFragment frag : ChatManager.getInstance().getChat(mChatId).getThreads()) {
					mThreadPagerAdapter.addTab(frag);
				}
			}
			else {
				mMainChatFrag = new ThreadFragment(mChatId, mThreadId, ThreadType.MAIN_CHAT, mUsers);
				ChatManager.getInstance().addChat(mChatId, mMainChatFrag);
				mThreadPagerAdapter.addTab(mMainChatFrag);
				addToChatsDrawer(ChatManager.getInstance().getChat(mChatId));
			}
		}
	}
	
	private class AddThreadTask extends AsyncTask<Void, Void, Void> {

		private long mChatId;
		private long mThreadId;
		private ThreadType mType;
		private User[] mUsers;
		private ThreadFragment mThreadFrag;
		
		public AddThreadTask(long chatId, long threadId, ThreadType type, User[] users) {
			mChatId = chatId;
			mThreadId = threadId;
			mType = type;
			mUsers = users;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			Thread.currentThread().setName(LOGTAG);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mThreadFrag = new ThreadFragment(mChatId, mThreadId, mType, mUsers);
			ChatManager.getInstance().addThread(mChatId, mThreadFrag);
			mThreadPagerAdapter.addTab(mThreadFrag);
		}
	}
	
	private class AddInstantMessageTask extends AsyncTask<Void, Void, Void> {

		private InstantMessage mIm;
		
		public AddInstantMessageTask(InstantMessage im) {
			mIm = im;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			Thread.currentThread().setName(LOGTAG);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			ChatManager.getInstance().addInstantMessage(mIm);
		}
	}

	private class AddMemberTask extends AsyncTask<Void, Void, Void> {

		private long mChatId;
		private long mThreadId;
		private User mUser;
		
		public AddMemberTask(long chatId, long threadId, User user) {
			mChatId = chatId;
			mThreadId = threadId;
			mUser = user;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			Thread.currentThread().setName(LOGTAG);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			ChatManager.getInstance().addMember(mChatId, mThreadId, mUser);
			ThreadFragment frag = ChatManager.getInstance().getThreadFragment(mChatId, mThreadId);
			if (frag != null) {
				frag.addMember(mUser);
				String message = String.format("Added new user: %s", mUser.getName());
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
			updateThreadsDrawer();
			updateChatsDrawer();
		}
	}
}
