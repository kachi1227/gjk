package com.gjk.chassip;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.gjk.chassip.model.ChatManager;
import com.gjk.chassip.model.ThreadType;
import com.gjk.chassip.model.User;
import com.gjk.chassip.test.InjectorDeux;
import com.gjk.chassip.test.InjectorTask;
import com.google.common.collect.Lists;

/**
 * Activity for chats. This extends {@link DrawerActivity}.
 * @author gpl
 *
 */
public class ChatActivity extends DrawerActivity {

	private ActionBar mActionBar;
	private ViewPager mViewPager;
	private ThreadPagerAdapter mThreadPagerAdapter;
	private Handler mHandler;
	private Thread mInjectorThread;
	private AddChatTask mAddChatTask;
	private AddThreadTask mAddThreadTask;
	private AddInstantMessageTask mAddInstantMessagrTask;
	private AddMemberTask mMemberTask;
	
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
		
//		android.os.Debug.waitForDebugger();
		
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
		
		InjectorDeux.getInstance().initialize(this);
		mHandler = new Handler();
		mInjectorThread = new Thread() {
			public void run() {
				new InjectorTask().execute();
				mHandler.postDelayed(this, Constants.INJECTOR_PERIOD);
			}
		};
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		mHandler.removeCallbacks(mInjectorThread);
		mHandler.postDelayed(mInjectorThread, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();

		mHandler.removeCallbacks(mInjectorThread);
	}
	
	public void joinChat(final long chatId, final long threadId, final User[] users) {
    	mThreadPagerAdapter.addChat(chatId, threadId, users);
	}
	
	public void addInstantMessage(long chatId,  InstantMessage im) {
		mThreadPagerAdapter.addInstantMessage(chatId, im);
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
        	mThreadPagerAdapter.addThread(chatId, threadId, type, members);
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
		
		public ThreadPagerAdapter(FragmentManager fm) {
			super(fm);
			mFragments = Lists.newLinkedList();
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
		
		public void addChat(long chatId, long threadId, User[] members) {
			mAddChatTask = new AddChatTask(chatId, threadId, members);
			mAddChatTask.execute();
		}
		
		public void addThread(long chatId, long threadId, ThreadType type, User[] members) {
			mAddThreadTask = new AddThreadTask(chatId, threadId, type, members);
			mAddThreadTask.execute();
		}
		
		public void addInstantMessage(long chatId, InstantMessage im) {
			mAddInstantMessagrTask = new AddInstantMessageTask(chatId, im);
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
			
			addToThreadsDrawer(frag);
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
			Thread.currentThread().setName(this.getClass().getSimpleName());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mMainChatFrag = new ThreadFragment(mChatId, mThreadId, ThreadType.MAIN_CHAT, mUsers);
			ChatManager.getInstance().addChat(mChatId, mMainChatFrag);
			mThreadPagerAdapter.addTab(mMainChatFrag);
			addToChatsDrawer(ChatManager.getInstance().getChat(mChatId));
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
			Thread.currentThread().setName(this.getClass().getSimpleName());
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

		private long mChatId;
		private InstantMessage mIm;
		
		public AddInstantMessageTask(long chatId, InstantMessage im) {
			mChatId = chatId;
			mIm = im;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			ChatManager.getInstance().addInstantMessage(mChatId, mIm);
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
			Thread.currentThread().setName(this.getClass().getSimpleName());
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
