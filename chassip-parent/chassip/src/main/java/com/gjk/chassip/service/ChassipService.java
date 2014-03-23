package com.gjk.chassip.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.mutable.MutableInt;
import org.json.JSONObject;

import com.gjk.chassip.MainActivity;
import com.gjk.chassip.ThreadType;
import com.google.common.collect.Lists;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.gjk.chassip.helper.DataHelper;
import com.gjk.chassip.helper.DatabaseHelper;
import com.gjk.chassip.helper.DatabaseHelper.*;

/**
 * 
 * @author gpl
 */
public class ChassipService extends Service {

//	private NotificationManager mNotificationManager;
	private BufferedReader mBufferedReader;
	private boolean mInitialized;
	private MessageLoop mMessageLoop;

	private Timer mTimer = new Timer();
	private static boolean isRunning = false;

	private static Messenger sClient = null; // one single client

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_NEW_CHAT = 3;
	public static final int MSG_NEW_THREAD = 4;
	public static final int MSG_NEW_GROUP_MEMBER = 5;
	public static final int MSG_NEW_GROUP_MEMBER_LAST = 6;
	public static final int MSG_NEW_MESSAGE = 7;
	public static final int MSG_PAUSE = 8;
	public static final int MSG_GO = 9;

	private final Messenger mServiceMessenger = new Messenger(new IncomingMessageHandler()); // Target we publish for
																								// clients to send
	private final static String LOGTAG = "ChassipService";

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(LOGTAG, "Service Started.");
		// showNotification();

		isRunning = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOGTAG, "Received start id " + startId + ": " + intent);
		return START_NOT_STICKY;
	}

	/**
	 * Display a notification in the notification bar.
	 */
	private void sendNotification(String user, String msg) {
	    Intent intent = new Intent(this, MainActivity.class);
	    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

	    // Build notification
	    // Actions are just fake
	    Notification noti = new Notification.Builder(this)
	        .setContentTitle("New message from " + user)
	        .setContentText("msg")
	        .setContentIntent(pIntent).build();
	    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    // hide the notification after its selected
	    noti.flags |= Notification.FLAG_AUTO_CANCEL;

	    notificationManager.notify(0, noti);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(LOGTAG, "onBind");
		return mServiceMessenger.getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopSelf();
		return super.onUnbind(intent);
	}
	
	public static void createChat(JSONObject chat) {
		Log.d(LOGTAG, "Got create chat");
		sendMessage(new MutableInt(MSG_NEW_CHAT), DatabaseHelper.jsonToBundle(chat));
	}
	
	public static void addChatMembers(long chatId, String[] memberNames, long[] memberIds) {
		Bundle bundle = new Bundle();
		bundle.putLong("chat_id", chatId);
		bundle.putLong("thread_id", 1);
		bundle.putStringArray("user_names", memberNames);
		bundle.putLongArray("user_ids", memberIds);
		Log.d(LOGTAG, "Got add members");
		sendMessage(new MutableInt(MSG_NEW_GROUP_MEMBER), bundle);
	}
	
	public static void sendMessage(long chatId, long threadId, String from, String message, long time) {
		Bundle bundle = new Bundle();
		bundle.putLong("chat_id", chatId);
		bundle.putLong("thread_id", threadId);
		bundle.putString("user_name", from);
		bundle.putString("message", message);
		bundle.putLong("time", time);
		Log.d(LOGTAG, "Got send message");
		sendMessage(new MutableInt(MSG_NEW_MESSAGE), bundle);
	}

	/**
	 * Send the data to all clients.
	 * 
	 * @param intvaluetosend
	 *            The value to send.
	 */
	private static void sendMessage(MutableInt type, Bundle bundle) {
		synchronized (sClient) { 
			if (sClient != null) {
				try {
					// Send data as a String
					Message msg = Message.obtain(null, type.intValue());
					msg.setData(bundle);
					sClient.send(msg);
				} catch (RemoteException e) {
					// The client is dead. Remove it from the list.
					sClient = null;
					Log.d(LOGTAG, "Client is dead. FUCCCK");
				}
			} else {
				Log.d(LOGTAG, "No client to send message to bro");
			}
		}
	}

	public static boolean isRunning() {
		return isRunning;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTimer != null) {
			mTimer.cancel();
		}
		Log.i(LOGTAG, "Service Stopped.");
		isRunning = false;
	}

	public void initialize() {
		AssetManager am = getApplicationContext().getAssets();
		InputStream is;
		try {
			is = am.open("simulation.txt");
			InputStreamReader inputStreamReader = new InputStreamReader(is);
			mBufferedReader = new BufferedReader(inputStreamReader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mInitialized = true;
	}

//	public boolean next(MutableInt msgType, Bundle bundle) throws IOException {
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if (mInitialized) {
//			if (mMessageLoop != null && mMessageLoop.isInMessageLoop()) {
//				msgType.setValue(MSG_NEW_MESSAGE);
//				Message randomCleanGarbage = mMessageLoop.getInstantMessage();
//				if (randomCleanGarbage == null) {
//					return false;
//				}
//				bundle.putLong("chat_id", randomCleanGarbage.getChatId());
//				bundle.putLong("thread_id", randomCleanGarbage.getThreadId());
//				bundle.putString("user_name", randomCleanGarbage.getUser().getName());
//				bundle.putString("message", randomCleanGarbage.getIm());
//				bundle.putLong("time", randomCleanGarbage.getTime());
//				mMessageLoop.checkTime();
//				return true;
//			} else {
//				String line = mBufferedReader.readLine();
//				if (line != null) {
//					String[] lineSplit = line.split("`");
//					if (lineSplit[0].equals("messages")) {
//						Log.d(LOGTAG, "Adding message");
//						int duration = Integer.valueOf(lineSplit[1]);
//						mMessageLoop = new MessageLoop(duration);
//						return false;
//					} else if (lineSplit[0].equals("addmembers")) {
//						Log.d(LOGTAG, "Adding members");
//						long chatId = Long.valueOf(lineSplit[1]);
//						long threadId = Long.valueOf(lineSplit[2]);
//						String[] userNames = lineSplit[3].split(",");
//						msgType.setValue(MSG_NEW_GROUP_MEMBER);
//						bundle.putLong("chat_id", chatId);
//						bundle.putLong("thread_id", threadId);
//						bundle.putStringArray("user_names", userNames);
//						return true;
//					} else if (lineSplit[0].equals("addchat")) {
//						Log.d(LOGTAG, "Adding new chat");
//						long chatId = Long.valueOf(lineSplit[1]);
//						long threadId = Long.valueOf(lineSplit[2]);
//						String[] userNames = lineSplit[3].split(",");
//						msgType.setValue(MSG_NEW_CHAT);
//						bundle.putLong("chat_id", chatId);
//						bundle.putLong("thread_id", threadId);
//						bundle.putStringArray("user_names", userNames);
//						return true;
//					} else if (lineSplit[0].equals("addthread")) {
//						Log.d(LOGTAG, "Adding new thread");
//						long chatId = Long.valueOf(lineSplit[1]);
//						long threadId = Long.valueOf(lineSplit[2]);
//						ThreadType type = ThreadType.valueOf(lineSplit[3]);
//						String[] userNames = lineSplit[4].split(",");
//						msgType.setValue(MSG_NEW_THREAD);
//						bundle.putLong("chat_id", chatId);
//						bundle.putLong("thread_id", threadId);
//						bundle.putInt("thread_type", type.getValue());
//						bundle.putStringArray("user_names", userNames);
//						return true;
//					}
//				}
//			}
//		}
//		return false;
//	}

	private class MessageLoop {

//		private final List<String> mMessages = Lists.newArrayList("One hand in the air if you don't really care",
//				"Two hands in the air if you don't really care", "It's like that sometimes, I mean ridiculous",
//				"It's like that sometimes, this shit ridiculous", "One hand in the air if you don't really care",
//				"Middle finger in the air if you don't really care", "It's like that sometimes, man, ridiculous",
//				"Life can be sometimes ridiculous", "I'm so appalled, Spalding ball",
//				"Balding, Donald Trump taking dollars from y'all", "Baby, you're fired, your girlfriend hired",
//				"But if you don't mind, I'mma keep you on call", "We above the law, we don't give a fuck about y'all",
//				"I got dogs that'll chew a fucking hole through the wall",
//				"But since they all lovers, I need more rubbers", "And if I don't use rubbers, need more covers",
//				"Housekeeping, I mean goddamn", "One time let it be a bad bitch sweeping",
//				"That know we get O's like Cheerios", "That know because they seen us in the videos",
//				"That know the day that you play me", "Would be the same day MTV play videos",
//				"That was a little joke, voila", "Praises due to the most high, Allah",
//				"Praises due to the most fly, Prada", "Baby, I'm magic, tada",
//				"Address me as your highness, high as United", "Thirty-thousand feet up and you are not invited",
//				"Niggas be writing bullshit like they gotta work",
//				"Niggas is going through real shit, man, they out of work",
//				"That's why another goddamn dance track gotta hurt",
//				"That's why I'd rather spit something that got a purp", "Champagne wishes, thirty white bitches",
//				"I mean this shit is fucking ridiculous", "Five star dishes, different exotic fishes",
//				"Man this shit is fucking ridiculous", "How should I begin this? I'm just so offended",
//				"How am I even mentioned by all these fucking beginners?", "I'm so appalled, I might buy the mall",
//				"Just to show niggas how much more I have in store",
//				"I'm fresher than you all, so I don't have to pause",
//				"All of y'all can suck my balls through my drawers", "Dark Knight feeling, die and be a hero",
//				"Or live long enough to see yourself become a villain", "I went from the favorite to the most hated",
//				"But would you rather be underpaid or overrated?", "Moral victories is for minor league coaches",
//				"And 'Ye already told you we major, you cockroaches",
//				"Show me where the boats is, Ferrari Testarossas",
//				"And Hammer went broke so you know I'm more focused", "I lost 30 mil, so I spent another 30",
//				"Cause unlike Hammer, thirty million can't hurt me", "Fucking insane, the fuck am I saying?",
//				"Not only am I fly, I'm fucking not playing", "All these little bitches too big for they britches",
//				"Burning they little bridges, fucking ridiculous", "Success is what you make it, take it how it come",
//				"A half a mil in twenties like a billion where I'm from",
//				"An arrogant drug dealer, the legend I become", "CNN said I'd be dead by 21",
//				"Blackjack, I just pulled an ace", "As you looking at the king in his face",
//				"Everything I dream, motherfuckers, I'm watching it take shape",
//				"While to you I'm just a young rich nigga that lacks faith",
//				"Range Rove, leather roof, love war, fuck a truce",
//				"Still move a bird like I'm in bed with Mother Goose", "Them hoes coming in a baker's dozen",
//				"Claiming they was with me when they know they really wasn't",
//				"I keep the city's best, never said she was the brightest",
//				"So if you had her too, it don't affect me in the slightest",
//				"I never met a bitch that didn't need a little guidance",
//				"So I dismiss her past until she disappoints your highness", "I speak the gospel, hostile",
//				"Tony doing time for what he did to nostrils", "Paranoid mind, I'm still under the watchful",
//				"Eye of the law, aspire for more", "Them kilos came, we gave you Bobby Brown jaw",
//				"Flaws ain't flaws when it's you that makes the call", "Flow similar to the legends of the falls",
//				"Spill it, I own you all, yeah", "Hah, I am so outrageous",
//				"I wear my pride on my sleeve like a bracelet", "If God had a iPod, I'd be on his playlist",
//				"My phrases amazes the faces in places", "The favorite, hah, my cup overrunneth with hundreds",
//				"Dummy, damn, it's hard not for me to waste it", "The new Commandment: Thou shalt not hate, kid",
//				"My movement is like the civil rights, I'm Ralph David", "Abernathy, so call my lady Rosa Parks",
//				"I am nothing like them niggas, baby, those are marks",
//				"I met this girl on Valentine's Day, fucked her in May",
//				"She found out about April, so she chose to march", "Hah, damn another broken heart",
//				"I keep bitches by the twos, nigga, Noah's ark", "I got a seven on me, I call my 'dro Lamar",
//				"Plus a Trojan in my pocket, Matt Leinart", "G-A-T in the Pathfinder", "Cause you haters got PhDs",
//				"Y'all just some major haters and some math minors", "Tiger Woods, don't make me grab iron",
//				"Ayo, champagne wishes and thirty white bitches", "You know the shit is fucking ridiculous",
//				"Cars for the missus and furs for the mistress", "You know that shit is fucking ridiculous");
//
//		private long mDuration;
//		private boolean mInMessageLoop;
//		private long mStartTime;
//		private Random mRandom;
//
//		public MessageLoop(long duration) {
//			mDuration = duration;
//			mInMessageLoop = true;
//			mStartTime = System.currentTimeMillis();
//			mRandom = new Random();
//		}
//
//		public Message getInstantMessage() {
//			Long[] chatIds = ChatManager.getInstance().getChatIds();
//			if (chatIds == null || chatIds.length == 0) {
//				return null;
//			}
//			long randomChatId = chatIds[mRandom.nextInt(chatIds.length)];
//			long[] threadIds = ChatManager.getInstance().getChat(randomChatId).getThreadIds();
//			if (threadIds == null || threadIds.length == 0) {
//				return null;
//			}
//			long randomThreadId = threadIds[mRandom.nextInt(threadIds.length)];
//			List<User> members = Lists.newArrayList(ChatManager.getInstance().getChat(randomChatId)
//					.getThreadFragment(randomThreadId).getMembers());
//			User randomUser = members.get(mRandom.nextInt(members.size()));
//			String randomMessage = mMessages.get(mRandom.nextInt(mMessages.size()));
//			return new InstantMessage(randomChatId, randomThreadId, randomUser, randomMessage, System.currentTimeMillis());
//		}
//
//		public boolean isInMessageLoop() {
//			return mInMessageLoop;
//		}
//
//		public void checkTime() {
//			mInMessageLoop = (System.currentTimeMillis() - mStartTime) < mDuration;
//		}
	}

	// ////////////////////////////////////////
	// Nested classes
	// ///////////////////////////////////////

	/**
	 * The task to run...
	 */
	private class SimTask extends TimerTask {
		@Override
		public void run() {
			Log.i(getClass().getSimpleName(), "Timer doing work.");
			try {
				MutableInt msgType = new MutableInt();
				Bundle bundle = new Bundle();
//				boolean gotMessage = next(msgType, bundle);
//				if (gotMessage) {
//					sendNotification(bundle.getString("user_name"), bundle.getString("message"));
//					sendMessage(msgType, bundle);
//				}

			} catch (Throwable t) { // you should always ultimately catch all exceptions in timer tasks.
				Log.e(getClass().getSimpleName(), "Timer Tick Failed.", t);
			}
		}
	}

	/**
	 * Handle incoming messages from MainActivity
	 */
	@SuppressLint("HandlerLeak")
	private class IncomingMessageHandler extends Handler { // Handler of incoming messages from clients.
		@Override
		public void handleMessage(Message msg) {
			Log.d(getClass().getSimpleName(), "handleMessage: " + msg.what);
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				sClient = msg.replyTo;
				initialize();
				mTimer.scheduleAtFixedRate(new SimTask(), 0, 3000L);
				break;
			case MSG_UNREGISTER_CLIENT:
				sClient = null;
				mTimer.cancel();
				break;
			case MSG_PAUSE:
				mTimer.cancel();
				break;
			case MSG_GO:
				mTimer = new Timer();
				mTimer.scheduleAtFixedRate(new SimTask(), 0, 3000L);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
}
