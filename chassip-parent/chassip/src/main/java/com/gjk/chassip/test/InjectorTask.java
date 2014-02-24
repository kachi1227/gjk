package com.gjk.chassip.test;

import java.io.IOException;

import android.os.AsyncTask;

public class InjectorTask extends AsyncTask<Void, Void, Void> {
	
	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			InjectorDeux.getInstance().next();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
}