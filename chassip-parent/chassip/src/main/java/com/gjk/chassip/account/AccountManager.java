package com.gjk.chassip.account;

import com.gjk.chassip.model.User;

public class AccountManager {
	
	private static AccountManager sInstance;
	
	private User thisUser; 
	
	public synchronized static AccountManager getInstance() {
		if (sInstance == null) {
			sInstance = new AccountManager();
		}
		return sInstance;
	}
	
	public void setUser(User user) {
		thisUser = user;
	}
	
	public User getUser() {
		return thisUser;
	}
}
