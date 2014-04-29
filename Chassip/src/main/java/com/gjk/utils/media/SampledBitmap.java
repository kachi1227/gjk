package com.gjk.utils.media;

import android.graphics.Bitmap;

public class SampledBitmap {

	private Bitmap mBitmap;
	private int mSampleSize;
	//private int mReferenceCount;  
	
	public SampledBitmap(Bitmap bitmap, int sampleSize) {
		mBitmap = bitmap;
		mSampleSize = sampleSize;
		//mReferenceCount = referenceCount;
	}
	
	public void setBitmap(Bitmap b) {
		mBitmap = b;
	}
	
	public Bitmap getBitmap() {
		return mBitmap;
	}
	
	public void setSampleSize(int sampleSize) {
		mSampleSize = sampleSize;
	}
	
	public int getSampleSize() {
		return mSampleSize;
	}
}
