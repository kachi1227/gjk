package com.gjk.chassip.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import android.content.Context;

public class DataHelper {
	
	public static String getTextFromResource(Context c, int resId) {
		return getTextFromStream(c.getResources().openRawResource(resId));
	}
	
	public static void copyRawResourceToFile(Context c, int resId, String outputFilePath) throws IOException {
		copyRawResourceToFile(c, resId, new File(outputFilePath));
	}
	
	public static void copyRawResourceToFile(Context c, int resId, File outputFile) throws IOException {
		File f = outputFile;
		f.getParentFile().mkdirs();
		if (f.exists()) {
			f.delete();
		}
		f.createNewFile();
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
		InputStream in = c.getResources().openRawResource(resId);
		copyFromStreamToStream(in, out, true, true);
	}
	
	public static void copyFromStreamToFile(InputStream from, File to, int bufferSize) throws IOException {
		if (!to.exists()) {
			to.getParentFile().mkdirs();
			to.createNewFile();
		}
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(to));
		copyFromStreamToStream(from, out, true, true, bufferSize);
	}
	
	public static void copyFromStreamToFile(InputStream from, File to) throws IOException {
		copyFromStreamToFile(from, to, 1024);
	}
	
	public static void copyFromFileToStream(File from, OutputStream to, boolean flush, boolean close, int bufferSize) throws IOException {
		FileInputStream fis = new FileInputStream(from);
		copyFromStreamToStream(fis, to, flush, close, bufferSize);
		if (!close) {
			fis.close();
		}
	}
	
	public static void copyFromFileToFile(File from, File to, int bufferSize) throws IOException {
		FileInputStream fis = new FileInputStream(from);
		copyFromStreamToFile(fis, to, bufferSize);
	}
	
	public static void copyFromFileToFile(File from, File to) throws IOException {
		copyFromFileToFile(from, to, 1024);
	}

	public static void copyFromFileToStream(File from, OutputStream to, boolean flush, boolean close) throws IOException {
		copyFromFileToStream(from, to, flush, close, 1024);
	}
	
	//Uses default 1024 byte buffer
	public static void copyFromStreamToStream(InputStream from, OutputStream to, boolean flush, boolean close) throws IOException {
		copyFromStreamToStream(from, to, flush, close, 1024);
	}
	
	//Copy data from input stream to output stream
	//This method closes both streams when finished
	public static void copyFromStreamToStream(InputStream from, OutputStream to, boolean flush, boolean close, int bufferSize) throws IOException {
		try {
			int bytesRead = 0;
			int offset = 0;
			byte[] data = new byte[bufferSize];
			
			while ((bytesRead = from.read(data, offset, bufferSize)) >= 0) {
				to.write(data, offset, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				if (flush) {
					to.flush();
				}
				if (close) {
					to.close();
					from.close();
				}
			} catch (Exception e2) {
				e.printStackTrace();
			}
			throw e;
		}
		if (flush) {
			to.flush();
		}
		if (close) {
			to.close();
			from.close();
		}
	}
	
	public static void copyTextToFile(String from, File to) throws IOException {
		copyTextToStream(from, new FileOutputStream(to), true, true);
	}
	
	public static void copyTextToStream(String from, OutputStream to, boolean flush, boolean close) throws IOException {
		to.write(from.getBytes());
		if (flush) {
			to.flush();
		}
		if (close) {
			to.close();
		}
	}
	
	public static String getTextFromStream(InputStream in) {
		return getTextFromStream(in, 1024);
	}
	
	public static String getTextFromStream(InputStream in, int bufferSize) {
		char[] buffer = new char[bufferSize];
		InputStreamReader r = new InputStreamReader(in);
		StringBuilder sb = new StringBuilder();
		int charsRead;
		try {
			while ((charsRead = r.read(buffer, 0, bufferSize)) > 0) {
				sb.append(buffer, 0, charsRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static void deleteRecursively(File fileOrDirectoryToDelete) {
		if (!fileOrDirectoryToDelete.exists())
			return;
		if (fileOrDirectoryToDelete.isDirectory()) {
			File[] contents = fileOrDirectoryToDelete.listFiles();
			for (int i = 0; i<contents.length; i++) {
				deleteRecursively(contents[i]);
			}
		}
		fileOrDirectoryToDelete.delete();
	}
	

}
