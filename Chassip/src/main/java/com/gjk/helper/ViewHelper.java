package com.gjk.helper;

import java.util.ArrayList;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

public class ViewHelper {

	public static ArrayList<View> findViewsById(int id, View parent, ArrayList<View> outViews) {
		if(parent.getId() == id) {
			outViews.add(parent);
		}
		else if(parent instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup)parent;
			int len = vg.getChildCount();
			for (int i = 0; i < len; i++) {
				View v = vg.getChildAt(i);
				outViews = ViewHelper.findViewsById(id, v, outViews);
			}
		}
		return outViews;
	}

	public static String getUniqueId(View view) {
		int id = view.getId();
		String resourceName = "";
		Resources res = view.getContext().getResources();
		if (id > 0 && res != null) {
			try {
				String pkgname;
				switch (id&0xff000000) {
				case 0x7f000000:
					pkgname="app";
					break;
				case 0x01000000:
					pkgname="android";
					break;
				default:
					pkgname = res.getResourcePackageName(id);
					break;
				}
				String typename = res.getResourceTypeName(id);
				String entryname = res.getResourceEntryName(id);
				resourceName = new StringBuilder().append(pkgname).append(":").append(typename).append("/").append(entryname).toString();
			}catch (Resources.NotFoundException e) {}
		}
		return new StringBuilder(view.getClass().getName() + "@").append(Integer.toHexString(System.identityHashCode(view))).append(resourceName.isEmpty() ? "" : "?" + resourceName).toString();
	}
}