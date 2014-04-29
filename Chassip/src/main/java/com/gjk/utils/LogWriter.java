package com.gjk.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class LogWriter {

	private static boolean DEBUG = true;
	
	public static void writeLog(int type, String tag, String msg) {
		if(DEBUG) {
			switch(type) {
				case Log.ASSERT:
					Log.wtf(tag, msg);
					break;
				case Log.DEBUG:
					Log.d(tag, msg);
					break;
				case Log.ERROR:
					Log.e(tag, msg);
					break;
				case Log.INFO:
					Log.i(tag, msg);
					break;
				case Log.VERBOSE:
					Log.v(tag, msg);
					break;
				case Log.WARN:
					Log.w(tag, msg);
					break;
			}
		}
	}

}