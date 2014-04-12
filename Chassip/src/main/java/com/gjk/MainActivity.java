package com.gjk;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.gjk.database.DatabaseManager.DataChangeListener;
import com.gjk.database.PersistentObject;
import com.gjk.database.objects.Group;
import com.gjk.database.objects.GroupMember;
import com.gjk.helper.DatabaseHelper;
import com.gjk.helper.GeneralHelper;
import com.gjk.net.AddMemberTask;
import com.gjk.net.AddSideChatMembersTask;
import com.gjk.net.AddWhisperMembersTask;
import com.gjk.net.GetGroupMembersTask;
import com.gjk.net.GetMessageTask;
import com.gjk.net.GetMultipleGroupsTask;
import com.gjk.net.GetSideChatMembersTask;
import com.gjk.net.GetWhisperMembersTask;
import com.gjk.net.HTTPTask.HTTPTaskListener;
import com.gjk.net.NotifyGroupInviteesTask;
import com.gjk.net.NotifySideChatInviteesTask;
import com.gjk.net.NotifyWhisperInviteesTask;
import com.gjk.net.TaskResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.gjk.helper.DatabaseHelper.addGroupMember;
import static com.gjk.helper.DatabaseHelper.addGroupMembers;
import static com.gjk.helper.DatabaseHelper.addGroupMessages;
import static com.gjk.helper.DatabaseHelper.addGroups;
import static com.gjk.helper.DatabaseHelper.getAccountUserFullName;
import static com.gjk.helper.DatabaseHelper.getAccountUserId;
import static com.gjk.helper.DatabaseHelper.getGroup;
import static com.gjk.helper.DatabaseHelper.getGroupMember;
import static com.gjk.helper.DatabaseHelper.getGroupMembers;
import static com.gjk.helper.DatabaseHelper.getGroups;
import static com.gjk.helper.DatabaseHelper.getLastStoredMessageId;
import static com.gjk.helper.DatabaseHelper.setAccountUser;

/**
 * Activity for chats. This extends {@link SlidingFragmentActivity} and implements {@link Service}.
 * 
 * @author gpl
 * 
 */
public class MainActivity extends SlidingFragmentActivity implements DataChangeListener,
		LoginDialog.NoticeDialogListener, RegisterDialog.NoticeDialogListener {

	private final static String LOGTAG = "MainActivity";

    private Context mCtx;

	private ViewPager mViewPager;
	private ActionBar mActionBar;
	private ThreadPagerAdapter mThreadPagerAdapter;
	private ChatsDrawerFragment mChatsDrawerFragment;
	private ThreadsDrawerFragment mThreadsDrawerFragment;

	private LoginDialog mLoginDialog;
	private RegisterDialog mRegDialog;

	// TODO: Temporary!!
	private final static HashMap<String, Long> mapping = Maps.newHashMap();
	private List<Long> mSelectedMembers;

	@Override
	public void onNewIntent(Intent i) {
		Log.d(LOGTAG, "Swag");
		if (i.getExtras() != null && i.getExtras().containsKey("group_id")) {
			long chatId = i.getExtras().getLong("group_id");
			Application.get().getPreferences().edit().putLong("current_group_id", chatId).commit();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gjk.DrawerActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Debug.waitForDebugger();

        mCtx = this;
        Crashlytics.start(mCtx);

		// Instantiate sliding menu
		final SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);

		// set the Behind View
		setBehindContentView(R.layout.chats_drawer);

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
		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mViewPager.setId(R.id.pager);
		mThreadPagerAdapter = new ThreadPagerAdapter(getSupportFragmentManager(), mViewPager, mActionBar);
		setContentView(mViewPager);
		mViewPager.setOffscreenPageLimit(Constants.OFFSCREEN_PAGE_LIMIT);

		if (savedInstanceState == null) {

			mChatsDrawerFragment = new ChatsDrawerFragment() {
				@Override
				public void onListItemClick(ListView l, View v, int position, long id) {
					super.onListItemClick(l, v, position, id);
					sm.toggle();
					toggleChat((Group) l.getItemAtPosition(position));
				}
			};
			mThreadsDrawerFragment = new ThreadsDrawerFragment() {
				@Override
				public void onListItemClick(ListView l, View v, int position, long id) {
					super.onListItemClick(l, v, position, id);
					final ThreadFragment frag = (ThreadFragment) l.getItemAtPosition(position);
					String message = "Are you sure you'd like to add more members to " + frag.getName() + "?";
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage(message).setPositiveButton("Yes", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mSelectedMembers = new ArrayList<Long>(); // Where we track the selected items
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							// Set the dialog title
							builder.setTitle(R.string.add_members_to_chat_title)
							// Specify the list array, the items to be selected by default (null for none),
							// and the listener through which to receive callbacks when items are selected
									.setMultiChoiceItems(R.array.contacts, null,
											new DialogInterface.OnMultiChoiceClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which, boolean isChecked) {
													String name = getResources().getStringArray(R.array.contacts)[which];
													if (isChecked) {
														// If the user checked the item, add it to the selected items
														mSelectedMembers.add(mapping.get(name));
													} else if (mSelectedMembers.contains(which)) {
														// Else, if the item is already in the array, remove it
														mSelectedMembers.remove(mapping.get(name));
													}
												}
											})
									// Set the action buttons
									.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int id) {
											// User clicked OK, so save the mSelectedItems results somewhere
											// or return them to the component that opened the dialog
											if (frag.getThreadType() == ThreadType.MAIN_CHAT) {
												addChatMembers();
											} else if (frag.getThreadType() == ThreadType.SIDE_CONVO) {
												addSideConvoMembers(frag);
											} else if (frag.getThreadType() == ThreadType.WHISPER) {
												addWhisperMembers(frag);
											}
										}
									}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int id) {
										}
									});
							// Create the AlertDialog
							AlertDialog dialog2 = builder.create();
							dialog2.setCanceledOnTouchOutside(true);
							dialog2.show();
						}
					}).setNegativeButton("No", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
					sm.toggle();
				}
			};
			getSupportFragmentManager().beginTransaction().replace(R.id.chats_menu_frame, mChatsDrawerFragment)
					.commit();
			sm.setSecondaryMenu(getLayoutInflater().inflate(R.layout.threads_drawer, null));
			getSupportFragmentManager().beginTransaction().replace(R.id.threads_menu_frame, mThreadsDrawerFragment)
					.commit();
		}

		Application.get().getDatabaseManager().registerDataChangeListener(Group.TABLE_NAME, this);
		Application.get().getDatabaseManager().registerDataChangeListener(Group.TABLE_NAME, mThreadPagerAdapter);
		Application.get().getDatabaseManager().registerDataChangeListener(GroupMember.TABLE_NAME, this);

		mLoginDialog = new LoginDialog();
		mRegDialog = new RegisterDialog();

		if (mapping.isEmpty()) {
			mapping.put("Greg", 3L);
			mapping.put("Jeff", 8L);
			mapping.put("Kachi", 6L);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	private void addChatMembers() {
		final long[] members = new long[mSelectedMembers.size()];
		for (int i = 0; i < mSelectedMembers.size(); i++) {
			members[i] = mSelectedMembers.get(i);
		}
		new AddMemberTask(this, new HTTPTaskListener() {
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					loadMembers(members);
				} else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
                }
			}
		}, ChatsDrawerFragment.getCurrentChat().getGlobalId(), members);
	}

	private void loadMembers(final long[] members) {
		new GetGroupMembersTask(this, new HTTPTaskListener() {
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					JSONArray response = (JSONArray) result.getExtraInfo();
					try {
						DatabaseHelper.addGroupMembers(response, ChatsDrawerFragment.getCurrentChat().getGlobalId());
						notifyNewChatMembers(members);
					} catch (Exception e) {
                        GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());
					}
				} else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
                }
			}
		}, ChatsDrawerFragment.getCurrentChat().getGlobalId());
	}

	private void notifyNewChatMembers(final long[] members) {
		new NotifyGroupInviteesTask(this, new HTTPTaskListener() {
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					Log.i(LOGTAG, "Notified group invitees");
				} else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
                }
			}
		}, DatabaseHelper.getAccountUserId(), ChatsDrawerFragment.getCurrentChat().getGlobalId(), members);
	}

	private void addSideConvoMembers(final ThreadFragment frag) {
		final long[] members = new long[mSelectedMembers.size()];
		for (int i = 0; i < mSelectedMembers.size(); i++) {
			members[i] = mSelectedMembers.get(i);
		}
		new AddSideChatMembersTask(this, new HTTPTaskListener() {
			long[] ids = members;

			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					for (long id : ids) {
						frag.addMember(getGroupMember(id));
					}
					mThreadsDrawerFragment.updateView();
					notifyNewSideConvoMembers(frag.getThreadId());
				} else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());				}
			}
		}, frag.getThreadId(), members);
	}

	private void notifyNewSideConvoMembers(long id) {
		Set<GroupMember> ms = mThreadPagerAdapter.getMainThread().getMembers();
		long[] members = new long[ms.size()];
		int index = 0;
		for (GroupMember m : ms) {
			members[index++] = m.getGlobalId();
		}
		new NotifySideChatInviteesTask(this, new HTTPTaskListener() {
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					Log.i(LOGTAG, "Notified side convo invitees");
				} else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());				}
			}
		}, DatabaseHelper.getAccountUserId(), id, members);
	}

	private void addWhisperMembers(final ThreadFragment frag) {
		final long[] members = new long[mSelectedMembers.size()];
		for (int i = 0; i < mSelectedMembers.size(); i++) {
			members[i] = mSelectedMembers.get(i);
		}
		new AddWhisperMembersTask(this, new HTTPTaskListener() {
			long[] ids = members;

			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					for (long id : ids) {
						frag.addMember(getGroupMember(id));
					}
					mThreadsDrawerFragment.updateView();
					notifyNewWhisperMembers(frag.getThreadId());
				} else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
                }
			}
		}, frag.getThreadId(), members);
	}

	private void notifyNewWhisperMembers(long id) {
		Set<GroupMember> ms = mThreadPagerAdapter.getMainThread().getMembers();
		long[] members = new long[ms.size()];
		int index = 0;
		for (GroupMember m : ms) {
			members[index++] = m.getGlobalId();
		}
		new NotifyWhisperInviteesTask(this, new HTTPTaskListener() {
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					Log.i(LOGTAG, "Notified side convo invitees");
				} else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
				}
			}
		}, DatabaseHelper.getAccountUserId(), id, members);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Application.get().activityResumed();
		if (!Application.get().getPreferences().contains(Constants.JSON)
				|| !Application.get().getPreferences().contains(Constants.PROPERTY_REG_ID)) {
			if (!mLoginDialog.isAdded() && !mRegDialog.isAdded()) {
				mLoginDialog.show(getSupportFragmentManager(), "LoginDialog");
			}
		} else {
			getGroupsFromDb();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Application.get().activityPaused();
		if (ChatsDrawerFragment.getCurrentChat() != null) {
			Application.get().getPreferences().edit()
					.putLong("current_group_id", ChatsDrawerFragment.getCurrentChat().getGlobalId()).commit();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Application.get().getDatabaseManager().unregisterDataChangeListener(Group.TABLE_NAME, this);
		Application.get().getDatabaseManager().unregisterDataChangeListener(Group.TABLE_NAME, mThreadPagerAdapter);
		Application.get().getDatabaseManager().unregisterDataChangeListener(GroupMember.TABLE_NAME, this);
	}

	private void getGroupsFromDb() {
		List<Group> groups = getGroups();
		for (Group g : groups) {
			mChatsDrawerFragment.addChat(g);
		}
		if (!groups.isEmpty()) {
			long chatId;
			if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("group_id")) {
				chatId = getIntent().getExtras().getLong("group_id");
			} else {
				chatId = Application.get().getPreferences().getLong("current_group_id", groups.get(0).getGlobalId());
			}
			toggleChat(getGroup(chatId));
		}
	}

	private void fetchGroups() {
		new GetMultipleGroupsTask(this, new HTTPTaskListener() {
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					JSONArray response = (JSONArray) result.getExtraInfo();
					try {
						if (response.length() == 0) {
							getSlidingMenu().toggle();
						} else {
							addGroups(response);
						}
					} catch (Exception e) {
                        GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());					}
				} else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());				}
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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mThreadsDrawerFragment.updateView();
							}
						});
					} catch (Exception e) {
                        GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());
					}
				} else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
				}
			}
		}, chat.getGlobalId());
	}

	private void fetchGroupMessages(final Group chat) {
		JSONArray jsonArray = new JSONArray();
		long id = getLastStoredMessageId(chat.getGlobalId());
		try {
			jsonArray.put(0, id).put(1, -1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		new GetMessageTask(this, new HTTPTaskListener() {
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					JSONArray messages = (JSONArray) result.getExtraInfo();
					try {
						addGroupMessages(messages);
					} catch (Exception e) {
                        GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());
					}
				} else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
				}
			}
		}, getAccountUserId(), chat.getGlobalId(), jsonArray);
	}

	private void toggleChat(Group chat) {
		// if (ChatsDrawerFragment.getCurrentChat() == null || chat != ChatsDrawerFragment.getCurrentChat()) {
		mChatsDrawerFragment.unnotifyGroup(chat);
		mThreadPagerAdapter.setChat(chat);
		// }
	}

	private void addToThreadDrawer(ThreadFragment frag) {
		mThreadsDrawerFragment.addThread(frag);
	}

	private void removeAllThreadsFromDrawer() {
		mThreadsDrawerFragment.removeAllThreads();
	}

	/**
	 * Implementation of {@link FragmentPagerAdapter}
	 * 
	 * @author gpl
	 * 
	 */
	private class ThreadPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener,
			ActionBar.TabListener, DataChangeListener {

		private List<ThreadFragment> mCurrrentThreads;
		private String mWhispers;
		private String mSideConvos;
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
            try {
			    mBar.setSelectedNavigationItem(position);
            }
            finally {}
		}

		protected void setChat(Group chat) {
			clear();
			ChatsDrawerFragment.setCurrentChat(chat);
			ThreadFragment[] mainFrag = new ThreadFragment[] { generateMainThreadFragment(chat) };
			ThreadFragment[] sideConvoFrags = generateSideConvoThreadFragments(chat);
			ThreadFragment[] whisperFrags = generateWhisperThreadFragments(chat);
			addThreads(GeneralHelper.concatAll(mainFrag, sideConvoFrags, whisperFrags));
			mPager.setCurrentItem(0);
		}

		protected ThreadFragment getMainThread() {
			return mCurrrentThreads.get(0);
		}

		private void addThreads(ThreadFragment... frags) {
			for (ThreadFragment frag : frags) {
				mCurrrentThreads.add(frag);
				notifyDataSetChanged();
				Tab tab = mBar.newTab().setText(frag.getArguments().getString("name")).setTabListener(this);
				mBar.addTab(tab);
				addToThreadDrawer(frag);
				mThreadsDrawerFragment.updateView();
			}
		}

		private ThreadFragment generateMainThreadFragment(Group chat) {
			Bundle b = new Bundle();
			b.putLong("chatId", chat.getGlobalId());
			b.putLong("threadId", 0);
			b.putInt("threadType", ThreadType.MAIN_CHAT.getValue());
			b.putString("name", chat.getName());
			ThreadFragment frag = new ThreadFragment();
			frag.setArguments(b);
            frag.addMembers(getGroupMembers(chat.getGlobalId()));
			return frag;
		}

		private ThreadFragment[] generateSideConvoThreadFragments(Group chat) {
			String sideConvosStr = chat.getSideChats();
			mSideConvos = sideConvosStr;
			if (sideConvosStr.isEmpty()) {
				return new ThreadFragment[] {};
			}
			String[] sideConvosStrSplit = sideConvosStr.split("\\|");
			ThreadFragment[] frags = new ThreadFragment[sideConvosStrSplit.length];
			int index = 0;
			for (String sideConvoStr : sideConvosStrSplit) {
				frags[index++] = generateThreadFragment(sideConvoStr, ThreadType.SIDE_CONVO, chat.getGlobalId());
			}
			return frags;
		}

		private ThreadFragment[] generateWhisperThreadFragments(Group chat) {
			String whispersStr = chat.getWhispers();
			mWhispers = whispersStr;
			if (whispersStr.isEmpty()) {
				return new ThreadFragment[] {};
			}
			String[] whispersStrSplit = whispersStr.split("\\|");
			ThreadFragment[] frags = new ThreadFragment[whispersStrSplit.length];
			int index = 0;
			for (String whisperStr : whispersStrSplit) {
				frags[index++] = generateThreadFragment(whisperStr, ThreadType.WHISPER, chat.getGlobalId());
			}
			return frags;
		}

		private ThreadFragment generateThreadFragment(String thread, ThreadType type, final long chatId) {
			String[] sideConvoStrSplit = thread.split(":");
			long id = Long.valueOf(sideConvoStrSplit[0]);
			String name = sideConvoStrSplit[1];
			Bundle b = new Bundle();
			b.putLong("chatId", chatId);
			b.putLong("threadId", id);
			b.putInt("threadType", type.getValue());
			b.putString("name", name);
			final ThreadFragment frag = new ThreadFragment();
			frag.setArguments(b);
			if (type == ThreadType.SIDE_CONVO) {
				new GetSideChatMembersTask(getApplicationContext(), new HTTPTaskListener() {
					@Override
					public void onTaskComplete(TaskResult result) {
						if (result.getResponseCode() == 1) {
							try {
								JSONArray response = (JSONArray) result.getExtraInfo();
								for (int i = 0; i < response.length(); i++) {
									GroupMember m = addGroupMember(response.getJSONObject(i), chatId, true);
									frag.addMember(m);
									mThreadsDrawerFragment.updateView();
								}
							} catch (Exception e) {
                                GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());
							}
						} else {
                            GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
						}
					}
				}, id);
			} else {
				new GetWhisperMembersTask(getApplicationContext(), new HTTPTaskListener() {
					@Override
					public void onTaskComplete(TaskResult result) {
						if (result.getResponseCode() == 1) {
							try {
								JSONArray response = (JSONArray) result.getExtraInfo();
								for (int i = 0; i < response.length(); i++) {
									GroupMember m = addGroupMember(response.getJSONObject(i), chatId, true);
									frag.addMember(m);
									mThreadsDrawerFragment.updateView();
								}
							} catch (Exception e) {
                                GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());
							}
						} else {
                            GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
						}
					}
				}, id);
			}
			return frag;
		}

		private void clear() {
			mBar.removeAllTabs();
			removeAllThreadsFromDrawer();
			mCurrrentThreads.clear();
			notifyDataSetChanged();
		}

		@Override
		public void onDataChanged(PersistentObject o) {
			if (o.getTableName().equals(Group.TABLE_NAME)) {
				final Group g = (Group) o;
				if (ChatsDrawerFragment.getCurrentChat() != null) {
					if (ChatsDrawerFragment.getCurrentChat().getGlobalId() == g.getGlobalId()) {
						if (!g.getSideChats().equals(mSideConvos)) {
							String[] threads = mSideConvos.isEmpty() ? new String[] { g.getSideChats() } : g
									.getSideChats().substring(mSideConvos.length() + 1).split("\\|");
							for (String thread : threads) {
								addThreads(generateThreadFragment(thread, ThreadType.SIDE_CONVO, g.getGlobalId()));
							}
							mSideConvos = g.getSideChats();
						}
						if (!g.getWhispers().equals(mWhispers)) {
							String[] threads = mWhispers.isEmpty() ? new String[] { g.getWhispers() } : g.getWhispers()
									.substring(mWhispers.length() + 1).split("\\|");
							for (String thread : threads) {
								addThreads(generateThreadFragment(thread, ThreadType.WHISPER, g.getGlobalId()));
							}
							mWhispers = g.getWhispers();
						}
					}
				}
			}
		}
	}

	@Override
	public void onDataChanged(PersistentObject o) {
		if (o.getTableName().equals(Group.TABLE_NAME)) {
			final Group g = (Group) o;
			if (ChatsDrawerFragment.getCurrentChat() == null || g.getCreatorId() == getAccountUserId()) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						toggleChat(g);
					}
				});
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(50l);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					fetchGroupMembers(g);
					fetchGroupMessages(g);
				}
			}).start();
		}
	}

	@Override
	public void onDialogPositiveClick(LoginDialog dialog) {

		try {
			setAccountUser(dialog.getMyArguments());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String fullName = getAccountUserFullName();
		String message1 = String.format(Locale.getDefault(), "Welcome, %s! You're swagged out!", fullName);
		Toast.makeText(getApplicationContext(), message1, Toast.LENGTH_SHORT).show();

		getGroupsFromDb();
		fetchGroups();
	}

	@Override
	public void onDialogNegativeClick(LoginDialog dialog) {
		mLoginDialog.dismiss();
		mRegDialog.show(getSupportFragmentManager(), "RegisterDialog");
	}

	@Override
	public void onDialogPositiveClick(RegisterDialog dialog) {
		try {
			setAccountUser(dialog.getMyArguments());
		} catch (Exception e) {
			e.printStackTrace();
		}
		String fullName = getAccountUserFullName();
		String message1 = String.format(Locale.getDefault(), "Welcome, %s! You're swagged out!", fullName);
		Toast.makeText(getApplicationContext(), message1, Toast.LENGTH_SHORT).show();

		getGroupsFromDb();
		fetchGroups();
	}

	@Override
	public void onDialogNegativeClick(RegisterDialog dialog) {
		mRegDialog.dismiss();
		mLoginDialog.show(getSupportFragmentManager(), "LoginDialog");
	}
}
