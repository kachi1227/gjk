package com.gjk.chassip;

import java.util.List;
import com.gjk.chassip.model.ImManagerFactory;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessagesAdapter extends ArrayAdapter<InstantMessage> {
	
	private final String LOGTAG = getClass().getSimpleName();
	
	private final Context mContext;
	private final long mChatId;
	private final long mThreadId;
	
	public MessagesAdapter(Context context, long chatId, long threadId, List<InstantMessage> ims) {
		super(context, 0, ims);
		mContext = context;
		mChatId = chatId;
		mThreadId = threadId;
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
		
		userName.setTextColor(mContext.getResources().getColor(ImManagerFactory.getImManger(mChatId).get(getItem(position),  mThreadId)));
		message.setTextColor(mContext.getResources().getColor(ImManagerFactory.getImManger(mChatId).get(getItem(position),  mThreadId)));

		return convertView;
	}
	
	@Override
	public void add(InstantMessage im) {
//		if (InstantMessageManager.getInstance().trim()) {
//			mColors.remove(0);
//			remove(getItem(0));
//		}
		Log.d(LOGTAG, "mThreadId="+mThreadId+" imThreadId="+im.getThreadId());
		super.add(im);
		ImManagerFactory.getImManger(mChatId).add(im);
	}
}