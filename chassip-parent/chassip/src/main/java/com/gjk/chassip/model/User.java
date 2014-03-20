package com.gjk.chassip.model;

/**
 * 
 * @author gpl
 */
public class User {
	
	private String mName;

	public User(String name) {
		this.mName = name;
	}
	
	public User() {}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.mName = name;
	}
	
	public static User[] getUsers(String[] userNames) {
		User[] users = new User[userNames.length];
		for (int i=0; i<userNames.length; i++) {
			users[i] = new User(userNames[i]);
		}
		return users;
	}
	
	public static String getUserStrs(User[] userNames) {
		String users = userNames[0].getName();
		for (int i=1; i<userNames.length-1; i++) {
			users = users + ", " + userNames[i].getName();
		}
		if (userNames.length == 2) {
			users = users + " and " + userNames[userNames.length-1].getName();
		}
		else if (userNames.length > 2) {
			users = users + ", and " + userNames[userNames.length-1].getName();
		}
		return users;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			return ((User) obj).getName().equals(mName);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("name=%s", mName);
	}
}
