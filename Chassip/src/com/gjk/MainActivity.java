package com.gjk;

import com.crashlytics.android.Crashlytics;
import org.json.JSONArray;
import org.json.JSONException;

import com.gjk.net.GetMessageTask;
import com.gjk.net.SendMessageTask;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
		setContentView(R.layout.main);
        try {
			new GetMessageTask(this, null, 4, 1, new JSONArray("[4, -1]"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //new SendMessageTask(this, null, 4, 1, "Testing from the app", null, null, null, 2, 3L);
               
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
