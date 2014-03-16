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
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			return ((User) obj).getName().equals(mName);
		}
		return false;
	}
}
