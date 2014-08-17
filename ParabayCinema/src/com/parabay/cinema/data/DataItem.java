package com.parabay.cinema.data;

import android.graphics.Bitmap;

import com.parabay.cinema.media.data.MediaItem;

public class DataItem {

	private int mType;
	private String mPath;
	private Object mData;
	
	private String mDescription;
	private String mCategory;
	private String mOwner;
	
    private MediaItem mItem;
    private int mIndex;
    
	public DataItem(MediaItem item, int index, Bitmap bitmap) {
		
        this.mData = bitmap;
        this.mItem = item;
        this.mIndex = index;
	}

	public int getType() {
		return mType;
	}

	public void setType(int mType) {
		this.mType = mType;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String mPath) {
		this.mPath = mPath;
	}

	public Object getData() {
		return mData;
	}

	public void setData(Object mData) {
		this.mData = mData;
	}

	public MediaItem getItem() {
		return mItem;
	}

	public void setItem(MediaItem mItem) {
		this.mItem = mItem;
	}

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int mIndex) {
		this.mIndex = mIndex;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String mDescription) {
		this.mDescription = mDescription;
	}

	public String getCategory() {
		return mCategory;
	}

	public void setCategory(String mCategory) {
		this.mCategory = mCategory;
	}

	public String getOwner() {
		return mOwner;
	}

	public void setOwner(String mOwner) {
		this.mOwner = mOwner;
	}
	
	public String toString() {
    	StringBuilder result = new StringBuilder();
        String NEW_LINE = "\n";

        result.append(this.getClass().getName() + " Object {" + NEW_LINE);
        result.append(" item: " + this.mItem.toString() + NEW_LINE);
        result.append(" index: " + String.valueOf(this.mIndex) + NEW_LINE );
        result.append("}");

        return result.toString();
    }
}
