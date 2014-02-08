package com.android.beta.chassip;


import java.io.UnsupportedEncodingException;
import java.security.KeyStore;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;



private String name = null;
private String bio = null;
private String stringBitMapAvi = null;

private int _id = null;
private String email = null;
private String password = null;
private String image = null;
//private String facebook_id = null
//private long date_joined = CURRENT_TIMESTAMP;



public void NewAccountRequest (String new_name, String new_bio, String new_avi){
	name = new_name;
	bio = new_bio;
	stringBitMapAvi = new_avi;
	JsonObject parcel = new JsonObject();
	parcel.put("name", name);
	parcel.put("bio", bio);
	parcel.put("avi", stringBitMapAvi)''
	
	//executeWithJson(String apiuri, parcel)

	
}

public  String _getName(){
	return this.name;	
};
public void _setName(String nameUpdate){
	//validate string here?
	//
	this.name = nameUpdate;
};

public 	String _getBio(){
	return this.bio;
}
public void _setBio(String bioUpdate){
	this.bio = bioUpdate;
};

public String _GetEmail(){
	return this.email;
};
public void _setEmail(String emailUpdate){
	//not sure if this type of data should be accompanied
	//by a token or authenticated
	this.email = emailUpdate;
};

//public void _updatePassword(){};

