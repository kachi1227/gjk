package com.gjk.helper;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gjk.Constants;
import com.gjk.R;
import com.gjk.database.objects.User;
import com.gjk.views.CacheImageView;

import java.util.ArrayList;

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

    public static void resetUserListView(FragmentActivity fa, Cursor c, ListView listView, Checker checker) {
        listView.setAdapter(new UsersAdapter(fa, c, checker));
    }

    private static class UsersAdapter extends CursorAdapter {

        private final Checker mChecker;

        public UsersAdapter(FragmentActivity a, Cursor cursor, Checker checker) {
            super(a, cursor, true);
            mChecker = checker;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.user_row, parent, false);
            buildView(view, cursor);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            buildView(view, cursor);
        }

        private void buildView(View view, final Cursor cursor) {

            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final long id = cursor.getLong(cursor.getColumnIndex(User.F_GLOBAL_ID));
                    mChecker.checkHasChanged(id, isChecked);
                }
            });
            final CacheImageView memberAvi = (CacheImageView) view.findViewById(R.id.memberAvi);
            final TextView userName = (TextView) view.findViewById(R.id.userName);

            memberAvi.configure(Constants.BASE_URL + cursor.getString(cursor.getColumnIndex(User.F_IMAGE_URL)), 0,
                    true);
            userName.setText(cursor.getString(cursor.getColumnIndex(User.F_FIRST_NAME)) + " " + cursor.getString
                    (cursor.getColumnIndex(User.F_LAST_NAME)));
        }
    }

    public interface Checker {
        void checkHasChanged(long id, boolean isChecked);
    }

}
