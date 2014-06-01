package com.gjk.utils.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.gjk.Application;
import com.gjk.helper.ViewHelper;
import com.gjk.utils.LogWriter;
import com.gjk.utils.NumberUtil;
import com.gjk.views.CacheImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BitmapLoader {

	private static HashMap<String, SparseArray<Set<String>>> sUrlToBitmapMap = new HashMap<String, SparseArray<Set<String>>>();

	public static final int MAX_IMAGE_WIDTH = Application.get().getResources().getDisplayMetrics().widthPixels;
	public static final int MAX_IMAGE_HEIGHT = Application.get().getResources().getDisplayMetrics().widthPixels;
	public static final int MAX_BITMAP_SIZE = MAX_IMAGE_WIDTH * MAX_IMAGE_HEIGHT * 4;
	public static final int TYPE_RESOURCE = 0;
	public static final int TYPE_URI = 1;


	public static Drawable getBitmapDrawableFromAsset(View view, Object resource, int type) {
		Bitmap b = getSampledBitmapFromAsset(view, resource, type).getBitmap();
		return b != null ? new BitmapDrawable(view.getResources(), b) : null;
	}

	public static Bitmap getBitmapFromAsset(View view, Object resource, int type) {
		return getSampledBitmapFromAsset(view, resource, type, null).getBitmap();
	}
	
	public static Bitmap getBitmapFromAsset(View view, Object resource, int type, int[] imageDimensions) {
		return getSampledBitmapFromAsset(view, resource, type, imageDimensions).getBitmap();
	}
	
	
	public static SampledBitmap getSampledBitmapFromAsset(View view, Object resource, int type) {
		return getSampledBitmapFromAsset(view, resource, type, null);
	}
	

	public static SampledBitmap getSampledBitmapFromAsset(View view, Object resource, int type, int[] imageDimensions) {
		return getSampledBitmapFromAsset(view, resource, resource.toString(), type, imageDimensions);
	}

	public static SampledBitmap getSampledBitmapFromAsset(View view, Object resource, String cacheKey, int type, int[] imageDimensions) {

		InputStream buffer = null;

		try {
			buffer = (type == TYPE_RESOURCE ? view.getContext().getResources().openRawResource((Integer)resource) :
				(type == TYPE_URI ? new FileInputStream(new File(((Uri)resource).getPath())) : null));
			BitmapFactory.Options options = new BitmapFactory.Options();

			getBitmapDimensions(new BufferedInputStream(buffer), options);
			if(imageDimensions == null) {
				imageDimensions = new int[2];
				getEstimatedImageDimensions(view, imageDimensions);

				//rare case for when we're cropping and the values are very skewed. I.e. width=480 & height=45
				if(view instanceof ImageView && ((ImageView)view).getScaleType() == ScaleType.CENTER_CROP || ((ImageView)view).getScaleType() == ScaleType.CENTER_INSIDE) {
					final int height = options.outHeight;
					final int width = options.outWidth;

					if(imageDimensions[0] > imageDimensions[1] && (double)imageDimensions[1]/(double)imageDimensions[0] < .5) { //width greater than height AND the ratio is greater than 2:1
						double ratio = (double)imageDimensions[0]/(double)width;
						imageDimensions[1] = (int)(height * ratio);						

					} else if(imageDimensions[1] > imageDimensions[0] && (double)imageDimensions[0]/(double)imageDimensions[1] < .5) {//height greater than width AND ratio is greater than 2:1
						double ratio = (double)imageDimensions[1]/(double)height;
						imageDimensions[0] = (int)(width * ratio);
					}					
				}
			}
			
			options.inDither = false;
			options.inSampleSize = calculateInSampleSize(options, imageDimensions[0], imageDimensions[1]);			
			options.inPurgeable = true;
			options.inInputShareable = true;
			SampledBitmap sampledBitmap = Application.get().getSampledBitmap(cacheKey);
			
			if(sampledBitmap != null && sampledBitmap.getSampleSize() <= options.inSampleSize)
				buffer.close();
			else {

				options.inJustDecodeBounds = false;
				buffer.close();
				//reset inputstream, since we cannot reuse;
				buffer = (type == TYPE_RESOURCE ? view.getContext().getResources().openRawResource((Integer)resource) :
					(type == TYPE_URI ? new FileInputStream(new File(((Uri)resource).getPath())) : null));
				Bitmap b = BitmapFactory.decodeStream(buffer, null, options);
				if(b != null && type == TYPE_URI) {
					ExifInterface exif = new ExifInterface(((Uri)resource).getPath());
					int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
					if(orientation > ExifInterface.ORIENTATION_NORMAL ) {
						Matrix matrix = getRectifyingMatrixForExifOrientation(orientation);
						b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
					}
					
				}
				buffer.close();
				sampledBitmap = new SampledBitmap(b, options.inSampleSize);
				//TODO this could be why the size is reporting inconstent results. Because we're changing the size of the fly here. 
				//shouldnt we recycle the bitmap that we're about to replace here too? And make all other images use this new bitmap?
				
			}
			
			
			return sampledBitmap;

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(buffer != null){
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private static void getEstimatedImageDimensions(View view, int[] dimensions) {

		if(view.getHeight() != 0 && view.getWidth() != 0) {
			dimensions[0] = view.getWidth();
			dimensions[1] = view.getHeight();
		} else {
			LayoutParams lp = view.getLayoutParams();
			LayoutParams parentLp = ((View)view.getParent()).getLayoutParams();
			int childHeightSpec = ViewGroup.getChildMeasureSpec(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0, lp.height == LayoutParams.MATCH_PARENT && parentLp != null ? parentLp.height : lp.height);
			int childWidthSpec = ViewGroup.getChildMeasureSpec(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0, lp.width == LayoutParams.MATCH_PARENT && parentLp != null ? parentLp.width : lp.width);
			view.measure(childWidthSpec, childHeightSpec);
			dimensions[0] = view.getMeasuredWidth() > 0 ? view.getMeasuredWidth() : MAX_IMAGE_WIDTH;
			dimensions[1] = view.getMeasuredHeight() > 0 ? view.getMeasuredHeight() : MAX_IMAGE_HEIGHT;
		}
	}

	public static void getBitmapDimensions(InputStream stream, BitmapFactory.Options options) {
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(stream, null, options);	
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			int heightRatio = Math.round((float)height / (float)reqHeight);
			int widthRatio = Math.round((float)width / (float)reqWidth);
			inSampleSize = Math.max(Math.min(heightRatio, widthRatio), 1);
		}

		int highestOne = NumberUtil.log2(Integer.highestOneBit(inSampleSize));
		int low = (int)Math.pow(2, highestOne);
		int high = (int)Math.pow(2, highestOne + 1);
		return calculateClampedSampleSize((int)Math.pow(2, highestOne + (Math.abs(inSampleSize - low) < Math.abs(inSampleSize - high) ? 0 : 1)), width, height);
	}
	
	private static int calculateClampedSampleSize(int proposedSampleSize, int bitmapWidth, int bitmapHeight) {
		
		int newSampleSize = proposedSampleSize;
		int originalBitmapSize = bitmapWidth * bitmapHeight * 4;
		int totalBitmapSize = originalBitmapSize/(newSampleSize * newSampleSize);
		if(totalBitmapSize > MAX_BITMAP_SIZE) {
			double ratio = (double)originalBitmapSize/(double)MAX_BITMAP_SIZE;
			double roughSampleSize = Math.sqrt(ratio);
			roughSampleSize = (int)Math.ceil(roughSampleSize);
			int highestOne = NumberUtil.log2(Integer.highestOneBit((int)roughSampleSize));
			int low = (int)Math.pow(2, highestOne);
			int high = (int)Math.pow(2, highestOne + 1);
			newSampleSize = (int)Math.pow(2, highestOne + (Math.abs(roughSampleSize - low) < Math.abs(roughSampleSize - high) ? 0 : 1));
			LogWriter.writeLog(Log.DEBUG, "BITMAP_CLAMP", "Clampingggg");
		}
		
		return newSampleSize;
	}
	
	private static Matrix getRectifyingMatrixForExifOrientation(int orientation) {
		Matrix matrix = new Matrix();
		switch(orientation) {
			case ExifInterface.ORIENTATION_UNDEFINED:
			case ExifInterface.ORIENTATION_NORMAL:
				break;
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: // left right reversed mirror
				matrix.setScale(-1, 1);
				break; 
			case ExifInterface.ORIENTATION_ROTATE_180:
				matrix.postRotate(180);
				break;
			case ExifInterface.ORIENTATION_FLIP_VERTICAL:  // upside down mirror
				matrix.setScale(1, -1);
				break;
			case ExifInterface.ORIENTATION_TRANSPOSE:  // flipped about top-left <--> bottom-right axis
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:  // rotate 90 cw to right it
				matrix.postRotate(90);
				break;
			case ExifInterface.ORIENTATION_TRANSVERSE:  // flipped about top-right <--> bottom-left axis
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:  // rotate 270 to right it
				matrix.postRotate(270);
				break;
		}
		
		return matrix;
	}

	public synchronized static void trackBitmap(View view, String url, SampledBitmap sampledBitmap, boolean shouldCache) {
			if(sampledBitmap == null || sampledBitmap.getBitmap() == null)
				return;
			
			SparseArray<Set<String>> viewMap = sUrlToBitmapMap.get(url);
			if(viewMap == null) {
				viewMap = new SparseArray<Set<String>>();
				sUrlToBitmapMap.put(url, viewMap);
			}
			
			Set<String> viewSet = viewMap.get(sampledBitmap.getSampleSize()) != null ? 
					viewMap.get(sampledBitmap.getSampleSize()) : new HashSet<String>();
			
			String uniqueId = ViewHelper.getUniqueId(view);
			
			viewSet.add(uniqueId);
			viewMap.put(sampledBitmap.getSampleSize(), viewSet);
			if(view instanceof CacheImageView) {
				LogWriter.writeLog(Log.DEBUG, "OOM_BITMAP", "Sampled Bitmap with URL: " + url + ". Used by " + uniqueId + ". Total views referencing it: " + viewSet.size());
			}
			
			if(shouldCache)
					Application.get().addSampledBitmapToMemory(url, sampledBitmap);
	}
	
	public synchronized static boolean isBitmapReferenced(String url) {
		return sUrlToBitmapMap.get(url) != null;
	}

    public synchronized static void clearCache() {
        sUrlToBitmapMap.clear();
    }
	
	public synchronized static void cleanupBitmap(String url, int sampleSize, View view) {
		SparseArray<Set<String>> viewMap = sUrlToBitmapMap.get(url);
		Set<String> viewSet;
		if(viewMap != null && (viewSet = viewMap.get(sampleSize)) != null) {
			int lowestSampleSize = viewMap.keyAt(0);
			String uniqueId = ViewHelper.getUniqueId(view);
			boolean success = viewSet.remove(uniqueId);
			if(success)
				LogWriter.writeLog(Log.DEBUG, "OOM_BITMAP", "Sampled Bitmap with URL: " + ((CacheImageView)view).getUrl() + " was removed from " + uniqueId + ". Total views referencing it: " + viewSet.size());
			else
				LogWriter.writeLog(Log.DEBUG, "OOM_BITMAP", "False positive. Could not remove Sampled Bitmap with URL: " + ((CacheImageView)view).getUrl() + " from " + uniqueId + ". Total views referencing it: " + viewSet.size());
			if(viewSet.isEmpty()) {
				Bitmap bitmap = null;
				if(view instanceof ImageView) {
					Drawable drawable = ((ImageView)view).getDrawable();
					if(drawable instanceof BitmapDrawable && (bitmap = ((BitmapDrawable)drawable).getBitmap()) != null) {
						((ImageView)view).setImageBitmap(null);
						((ImageView)view).setImageDrawable(null);
						bitmap = ((BitmapDrawable)drawable).getBitmap();
						//bitmap.recycle();
					}
				} else if(view.getBackground() instanceof BitmapDrawable && (bitmap = ((BitmapDrawable)view.getBackground()).getBitmap()) != null) {
					view.setBackgroundDrawable(null);
					//bitmap.recycle();
				}

				viewMap.remove(sampleSize);
				if(viewMap.size() == 0)
					sUrlToBitmapMap.remove(url);
				//if this bitmap is not in our cache, recycle. else do nothing
				SampledBitmap test = Application.get().getSampledBitmap(url);
				if(Application.get().getSampledBitmap(url) == null || sampleSize > lowestSampleSize) 
					bitmap.recycle();

				LogWriter.writeLog(Log.DEBUG, "OOM_BITMAP", "Bitmap with URL: " + ((CacheImageView)view).getUrl() + " has been recycled.");
			}
		}
	}
}
