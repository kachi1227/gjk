package com.gjk.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Properties;

import android.content.Context;
import android.os.Environment;

public class FileUtility {

	public static final int BUFFER_SIZE = 1024 * 20;

	public static  String readFileAsString(String path) {
		return new String(readFile(path));
	}

	public static byte[] readFile(String path) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] data;
		try {
			InputStream is = new FileInputStream(path);
			int read = 0;
			byte[] buf = new byte[BUFFER_SIZE];
			while ((read = is.read(buf)) != -1) {
				baos.write(buf,0,read);
			}

			data = baos.toByteArray();
			is.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			closeStream(baos);
		}

		return data;
	}

	public static  String readRawFileAsString(Context context, int id) {
		return new String(readRawFile(context,id));
	}



	public static byte[] readRawFile(Context context, int id) {
		InputStream is = context.getResources().openRawResource(id);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] data;
		try {
			int read = 0;
			byte[] buf = new byte[BUFFER_SIZE];
			while ((read = is.read(buf)) != -1) {
				baos.write(buf,0,read);
			}

			data = baos.toByteArray();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			closeStream(baos);
		}

		return data;
	}

	public static byte[] readResourceFile(String resourceName) {
		InputStream is = FileUtility.class.getResourceAsStream(resourceName);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] data;
		try {
			int read = 0;
			byte[] buf = new byte[BUFFER_SIZE];
			while ((read = is.read(buf)) != -1) {
				baos.write(buf,0,read);
			}

			data = baos.toByteArray();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			closeStream(baos);
		}

		return data;
	}

	public static Properties readResourceFileAsProperties(String resourceName) {
		InputStream is = FileUtility.class.getResourceAsStream(resourceName);
		try {
			Properties props = new Properties();
			props.load(is);
			return props;
		} catch (IOException ioe) { 
			throw new RuntimeException(ioe);
		}finally {
			closeStream(is);
		}

	}

	public static void closeReader(Reader reader) {
		try {
			if (reader != null) 
				reader.close();
		} catch (Exception e) { }
	}

	public static void closeStream(InputStream is) {
		try {
			if (is != null)
				is.close();
		} catch (IOException ioe) {
		}
	}

	public static void closeStream(OutputStream os) {
		try {
			if (os != null)
				os.close();
		} catch (IOException ioe) {
		}
	}

	public static void copyFile(File srcFile, File destFile) throws Exception {
		FileInputStream fis = new FileInputStream(srcFile);
		FileOutputStream fos = new FileOutputStream(destFile, false);
		byte[] buf = new byte[BUFFER_SIZE];
		int read;
		while ((read = fis.read(buf)) != -1) {
			fos.write(buf,0,read);
		}
		fos.close();
		fis.close();
		buf = null;
	}

	/**
	 * This function will copy files or directories from one location to another.
	 * note that the source and the destination must be mutually exclusive. This 
	 * function can not be used to copy a directory to a sub directory of itself.
	 * The function will also have problems if the destination files already exist.
	 * @param src -- A File object that represents the source for the copy
	 * @param dest -- A File object that represnts the destination for the copy.
	 * @throws java.io.IOException if unable to copy.
	 */
	public static void copyFiles(File src, File dest) throws IOException {
		//Check to ensure that the source is valid...
		if (!src.exists()) {
			throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath()+".");
		} else if (!src.canRead()) { //check to ensure we have rights to the source...
			throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath()+".");
		}
		//is this a directory copy?
		if (src.isDirectory()) 	{
			if (!dest.exists()) { //does the destination already exist?
				//if not we need to make it exist if possible (note this is mkdirs not mkdir)
				if (!dest.mkdirs()) {
					throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
				}
			}
			//get a listing of files...
			String list[] = src.list();
			//copy all the files in the list.
			for (int i = 0; i < list.length; i++)
			{
				File dest1 = new File(dest, list[i]);
				File src1 = new File(src, list[i]);
				copyFiles(src1 , dest1);
			}
		} else { 
			//This was not a directory, so lets just copy the file
			FileInputStream fin = null;
			FileOutputStream fout = null;
			byte[] buffer = new byte[BUFFER_SIZE]; //Buffer 4K at a time (you can change this).
			int bytesRead;
			try {
				//open the files for input and output
				fin =  new FileInputStream(src);
				fout = new FileOutputStream (dest);
				//while bytesRead indicates a successful read, lets write...
				while ((bytesRead = fin.read(buffer)) >= 0) {
					fout.write(buffer,0,bytesRead);
				}
			} catch (IOException e) { //Error copying file... 
				IOException wrapper = new IOException("copyFiles: Unable to copy file: " + 
						src.getAbsolutePath() + "to" + dest.getAbsolutePath()+".");
				wrapper.initCause(e);
				wrapper.setStackTrace(e.getStackTrace());
				throw wrapper;
			} finally { //Ensure that the files are closed (if they were open).
				if (fin != null) { fin.close(); }
				if (fout != null) { fout.close(); }
			}
		}
	}

	public static void deleteRecursive(String path) {
		if (path == null)
			return;

		deleteRecursive(new File(path));
	}

	public static void deleteRecursive(File fileToDelete) {
		if (!fileToDelete.exists()) {
			return;
		}

		if (fileToDelete.isDirectory()) {
			for (File f : fileToDelete.listFiles()) {
				deleteRecursive(f);
			}
		}

		fileToDelete.delete();
	}

	public static boolean canSaveToExternalFileDirectory() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()); //we can read and write to SDCard
	}
}
