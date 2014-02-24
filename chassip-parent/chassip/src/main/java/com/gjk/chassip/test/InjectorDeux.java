package com.gjk.chassip.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.gjk.chassip.ChatActivity;
import com.gjk.chassip.InstantMessage;
import com.gjk.chassip.model.ThreadType;
import com.gjk.chassip.model.User;
import com.gjk.chassip.model.ChatManager;
import com.google.common.collect.Lists;


import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * 
 * @author gpl
 */
public class InjectorDeux extends Activity {

	private ChatActivity mChat;
    private BufferedReader mBufferedReader;
    private boolean mInitialized;
    private MessageLoop mMessageLoop;
    
	private static InjectorDeux sInstance;
	
	private InjectorDeux() {
		mInitialized = false;
	}
	
	public static synchronized InjectorDeux getInstance() {
		if (sInstance == null) {
			sInstance = new InjectorDeux();
		}
		return sInstance;
	}

	public void initialize(ChatActivity chat) {
		this.mChat = chat;
		AssetManager am = chat.getApplicationContext().getAssets();
		InputStream is;
		try {
			is = am.open("testdeux.txt");
			InputStreamReader inputStreamReader = new InputStreamReader(is);
		    mBufferedReader = new BufferedReader(inputStreamReader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mInitialized = true;
	}
	
	public void next() throws IOException {
		if (mInitialized) {
			if (mMessageLoop != null && mMessageLoop.isInMessageLoop()) {
				mChat.addInstantMessage(mMessageLoop.getChatId(), mMessageLoop.getInstantMessage());
				mMessageLoop.checkTime();
			}
			else {
				String line = mBufferedReader.readLine();
				if (line != null) {
					String[] lineSplit = line.split("`");
					if (lineSplit[0].equals("messages")) {
						Log.d("Injector", "Adding message");
						long chatId = Long.valueOf(lineSplit[1]);
						String[] strThreadIds = lineSplit[2].split(",");
						Long[] threadIds = new Long[strThreadIds.length];
						for (int i=0; i<strThreadIds.length; i++) {
							threadIds[i] = Long.valueOf(strThreadIds[i]);
						}
						int duration = Integer.valueOf(lineSplit[3]);
						mMessageLoop = new MessageLoop(chatId, threadIds, duration);
					}
					else if (lineSplit[0].equals("addmembers")) {
						Log.d("Injector", "Adding members");
						long chatId = Long.valueOf(lineSplit[1]);
						long threadId = Long.valueOf(lineSplit[2]);
						for (String strUser : lineSplit[3].split(",")) {
							mChat.addMembers(chatId, threadId, new User(strUser));
						}
					}
					else if (lineSplit[0].equals("addchat")) {
						Log.d("Injector", "Adding new chat");
						long chatId = Long.valueOf(lineSplit[1]);
						long threadId = Long.valueOf(lineSplit[2]);
						String[] strUsers = lineSplit[3].split(",");
						User[] users = new User[strUsers.length];
						for (int i=0; i<strUsers.length; i++) {
							users[i] = new User(strUsers[i]);
						}
						mChat.joinChat(chatId, threadId, users);
					}
					else if (lineSplit[0].equals("addthread")) {
						Log.d("Injector", "Adding new thread");
						long chatId = Long.valueOf(lineSplit[1]);
						long threadId = Long.valueOf(lineSplit[2]);
						ThreadType type = ThreadType.valueOf(lineSplit[3]);
						String[] strUsers = lineSplit[4].split(",");
						User[] users = new User[strUsers.length];
						for (int i=0; i<strUsers.length; i++) {
							users[i] = new User(strUsers[i]);
						}
						mChat.joinThread(chatId, threadId, type, users);
					}
				}
			}
		}
	}
	
	private class MessageLoop {
		
		private final List<String> mMessages = Lists.newArrayList(
				"One hand in the air if you don't really care",
				"Two hands in the air if you don't really care",
				"It's like that sometimes, I mean ridiculous",
				"It's like that sometimes, this shit ridiculous",
				"One hand in the air if you don't really care",
				"Middle finger in the air if you don't really care",
				"It's like that sometimes, man, ridiculous",
				"Life can be sometimes ridiculous",
				"I'm so appalled, Spalding ball",
				"Balding, Donald Trump taking dollars from y'all",
				"Baby, you're fired, your girlfriend hired",
				"But if you don't mind, I'mma keep you on call",
				"We above the law, we don't give a fuck about y'all",
				"I got dogs that'll chew a fucking hole through the wall",
				"But since they all lovers, I need more rubbers",
				"And if I don't use rubbers, need more covers",
				"Housekeeping, I mean goddamn",
				"One time let it be a bad bitch sweeping",
				"That know we get O's like Cheerios",
				"That know because they seen us in the videos",
				"That know the day that you play me",
				"Would be the same day MTV play videos",
				"That was a little joke, voila",
				"Praises due to the most high, Allah",
				"Praises due to the most fly, Prada",
				"Baby, I'm magic, tada",
				"Address me as your highness, high as United",
				"Thirty-thousand feet up and you are not invited",
				"Niggas be writing bullshit like they gotta work",
				"Niggas is going through real shit, man, they out of work",
				"That's why another goddamn dance track gotta hurt",
				"That's why I'd rather spit something that got a purp",
				"Champagne wishes, thirty white bitches",
				"I mean this shit is fucking ridiculous",
				"Five star dishes, different exotic fishes",
				"Man this shit is fucking ridiculous",
				"How should I begin this? I'm just so offended",
				"How am I even mentioned by all these fucking beginners?",
				"I'm so appalled, I might buy the mall",
				"Just to show niggas how much more I have in store",
				"I'm fresher than you all, so I don't have to pause",
				"All of y'all can suck my balls through my drawers",
				"Dark Knight feeling, die and be a hero",
				"Or live long enough to see yourself become a villain",
				"I went from the favorite to the most hated",
				"But would you rather be underpaid or overrated?",
				"Moral victories is for minor league coaches",
				"And 'Ye already told you we major, you cockroaches",
				"Show me where the boats is, Ferrari Testarossas",
				"And Hammer went broke so you know I'm more focused",
				"I lost 30 mil, so I spent another 30",
				"Cause unlike Hammer, thirty million can't hurt me",
				"Fucking insane, the fuck am I saying?",
				"Not only am I fly, I'm fucking not playing",
				"All these little bitches too big for they britches",
				"Burning they little bridges, fucking ridiculous",
				"Success is what you make it, take it how it come",
				"A half a mil in twenties like a billion where I'm from",
				"An arrogant drug dealer, the legend I become",
				"CNN said I'd be dead by 21",
				"Blackjack, I just pulled an ace",
				"As you looking at the king in his face",
				"Everything I dream, motherfuckers, I'm watching it take shape",
				"While to you I'm just a young rich nigga that lacks faith",
				"Range Rove, leather roof, love war, fuck a truce",
				"Still move a bird like I'm in bed with Mother Goose",
				"Them hoes coming in a baker's dozen",
				"Claiming they was with me when they know they really wasn't",
				"I keep the city's best, never said she was the brightest",
				"So if you had her too, it don't affect me in the slightest",
				"I never met a bitch that didn't need a little guidance",
				"So I dismiss her past until she disappoints your highness",
				"I speak the gospel, hostile",
				"Tony doing time for what he did to nostrils",
				"Paranoid mind, I'm still under the watchful",
				"Eye of the law, aspire for more",
				"Them kilos came, we gave you Bobby Brown jaw",
				"Flaws ain't flaws when it's you that makes the call",
				"Flow similar to the legends of the falls",
				"Spill it, I own you all, yeah",
				"Hah, I am so outrageous",
				"I wear my pride on my sleeve like a bracelet",
				"If God had a iPod, I'd be on his playlist",
				"My phrases amazes the faces in places",
				"The favorite, hah, my cup overrunneth with hundreds",
				"Dummy, damn, it's hard not for me to waste it",
				"The new Commandment: Thou shalt not hate, kid",
				"My movement is like the civil rights, I'm Ralph David",
				"Abernathy, so call my lady Rosa Parks",
				"I am nothing like them niggas, baby, those are marks",
				"I met this girl on Valentine's Day, fucked her in May",
				"She found out about April, so she chose to march",
				"Hah, damn another broken heart",
				"I keep bitches by the twos, nigga, Noah's ark",
				"I got a seven on me, I call my 'dro Lamar",
				"Plus a Trojan in my pocket, Matt Leinart",
				"G-A-T in the Pathfinder",
				"Cause you haters got PhDs",
				"Y'all just some major haters and some math minors",
				"Tiger Woods, don't make me grab iron",
				"Ayo, champagne wishes and thirty white bitches",
				"You know the shit is fucking ridiculous",
				"Cars for the missus and furs for the mistress",
				"You know that shit is fucking ridiculous");
		
		private long mChatId;
		private List<Long> mThreadIds;
		private long mDuration;
		private boolean mInMessageLoop;
		private long mStartTime;
		
		public MessageLoop(long chatId, Long[] threadIds, long duration) {
			mChatId = chatId;
			mThreadIds = Arrays.asList(threadIds);
			mDuration = duration;
			mInMessageLoop = true;
			mStartTime = System.currentTimeMillis();
		}
		
		public InstantMessage getInstantMessage() {
			long randomThreadId = mThreadIds.get(new Random().nextInt(mThreadIds.size()));
			List<User> members = Lists.newArrayList(ChatManager.getInstance().getChat(mChatId).getThreadFragment(randomThreadId).getMembers());
			User randomUser = members.get(new Random().nextInt(members.size()));
			String randomMessage = mMessages.get(new Random().nextInt(mMessages.size()));
			return new InstantMessage(randomThreadId, randomUser, randomMessage);
		}
		
		public long getChatId() {
			return mChatId;
		}
		
		public boolean isInMessageLoop() {
			return mInMessageLoop;
		}
		
		public void checkTime() {
			mInMessageLoop = (System.currentTimeMillis() - mStartTime) < mDuration;
		}
	}
}
 