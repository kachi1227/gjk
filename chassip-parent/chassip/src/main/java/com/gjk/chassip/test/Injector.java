package com.gjk.chassip.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.gjk.chassip.ChatActivity;
import com.gjk.chassip.InstantMessage;
import com.gjk.chassip.model.ThreadType;
import com.gjk.chassip.model.User;

import android.app.Activity;
import android.content.res.AssetManager;

/**
 * 
 * @author gpl
 */
public class Injector extends Activity {

	private ChatActivity mChat;
    private BufferedReader mBufferedReader;
    private boolean mInitialized;
	
	private static Injector sInstance;
	
	private Injector() {
		mInitialized = false;
	}
	
	public static synchronized Injector getInstance() {
		if (sInstance == null) {
			sInstance = new Injector();
		}
		return sInstance;
	}

	public void initialize(ChatActivity chat) {
		this.mChat = chat;
		AssetManager am = chat.getApplicationContext().getAssets();
		InputStream is;
		try {
			is = am.open("test.txt");
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
			String line = mBufferedReader.readLine();
			if (line != null) {
				String[] lineSplit = line.split("`");
				if (lineSplit[0].equals("message")) {
					long chatId = Long.valueOf(lineSplit[1]);
					long threadId = Long.valueOf(lineSplit[2]);
					User user = new User(lineSplit[3]);
					String message = lineSplit[4];
					mChat.addInstantMessage(chatId, new InstantMessage(threadId, user, message));
				}
				else if (lineSplit[0].equals("addmembers")) {
					long chatId = Long.valueOf(lineSplit[1]);
					long threadId = Long.valueOf(lineSplit[2]);
					for (String strUser : lineSplit[3].split(",")) {
						mChat.addMember(chatId, threadId, new User(strUser));
					}
				}
				else if (lineSplit[0].equals("addchat")) {
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
 