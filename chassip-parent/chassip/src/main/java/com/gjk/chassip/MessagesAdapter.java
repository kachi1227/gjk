package com.gjk.chassip;

import java.util.List;
import java.util.Map;

import com.gjk.chassip.model.InstantMessageManager;
import com.google.common.collect.Maps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessagesAdapter extends ArrayAdapter<InstantMessage> {
	
	private final Context mContext;
	private final Map<Integer, Integer> mColorMap;
	private final long mThreadId;
	
	public MessagesAdapter(Context context, long threadId, List<InstantMessage> ims) {
		super(context, 0, ims);
		mContext = context;
		mThreadId = threadId;
		mColorMap = Maps.newHashMap();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_row, null);
		}
		TextView userName = (TextView) convertView.findViewById(R.id.userName);
		userName.setText(getItem(position).getUser().getName());
		TextView message = (TextView) convertView.findViewById(R.id.message);
		message.setText(getItem(position).getIm());

		Integer iPosition = Integer.valueOf(position);
		
		if (!mColorMap.containsKey(iPosition)) {
			int color = getItem(position).getThreadId() == mThreadId ? R.color.black : R.color.lightgrey;
			mColorMap.put(iPosition, color);
		}
		
		userName.setTextColor(mContext.getResources().getColor(mColorMap.get(iPosition)));
		message.setTextColor(mContext.getResources().getColor(mColorMap.get(iPosition)));
		
		return convertView;
	}
	
	@Override
	public void add(InstantMessage im) {
		InstantMessageManager.getInstance().trim();
		super.add(im);
	}
}