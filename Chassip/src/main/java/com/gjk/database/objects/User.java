/**************************************************
 * User.java
 *
 * Created By Kachi Nwaobasi on 03/18/2014.
 * Copyright 2014 GJK. All rights reserved.
 **************************************************/

package com.gjk.database.objects;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import com.gjk.database.objects.base.BaseUser;

import org.json.JSONObject;


public class User extends BaseUser {

    public static User insertOrUpdate(SQLiteOpenHelper dbm, JSONObject json) throws Exception {
        long id = json.getLong("id");
        User user = findOneByGlobalId(dbm, id);
        if (user == null) {
            user = new User(dbm);
        }
        user.setGlobalId(id);
        if (!json.isNull("first_name"))
            user.setFirstName(json.getString("first_name"));
        if (!json.isNull("last_name"))
            user.setLastName(json.getString("last_name"));
        if (!json.isNull("name") && user.getFullName().trim().isEmpty())
            user.setFirstName(json.getString("name"));
        if (!json.isNull("bio"))
            user.setBio(json.getString("bio"));
        if (!json.isNull("image"))
            user.setImageUrl(json.getString("image"));
        user.save(false);
        return user;
    }

    public User(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
        super(dbm, c, skipOk);
    }

    public User(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
        super(dbm, jsonString, skipOk);
    }

    public User(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
        super(dbm, obj, skipOk);
    }

    public String getFullName() {
        return getFirstName() + (!getLastName().isEmpty() ? (" " + getLastName()) : "");
    }

    public User(SQLiteOpenHelper dbm) {
        super(dbm);
    }

    @Override
    protected void onBeforeDelete() {

    }

    @Override
    protected void onAfterDelete() {

    }

    @Override
    public String toString() {
        return getFullName();
    }

}
