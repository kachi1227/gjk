package com.gjk.chassip;

import java.util.List;
import java.util.Locale;

import com.gjk.chassip.database.objects.Message;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class MessagesAdapter extends ArrayAdapter<Message> {
	private final String LOGTAG = getClass().getSimpleName();

	private final Context mContext;
	private final long mChatId;
	private final long mThreadId;
	private final ThreadType mType;

	public MessagesAdapter(Context context, long chatId, long threadId, ThreadType type, List<Message> ims) {
		super(context, 0, ims);
		mContext = context;
		mChatId = chatId;
		mThreadId = threadId;
		mType = type;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_row, null);
		}
		TextView userName = (TextView) convertView.findViewById(R.id.userName);
		String name = String.format(Locale.getDefault(), "%s %s", getItem(position).getSenderFirstName(), getItem(position).getSenderLastName());
		userName.setText(name);
		TextView message = (TextView) convertView.findViewById(R.id.message);
		message.setText(getItem(position).getContent());
		TextView time = (TextView) convertView.findViewById(R.id.time);
		time.setText(convertTimeToStr(getItem(position).getDate()));

		int color = getColor(position);
		userName.setTextColor(mContext.getResources().getColor(color));
		message.setTextColor(mContext.getResources().getColor(color));
		time.setTextColor(mContext.getResources().getColor(color));

		return convertView;
	}
	
	private int getColor(int position) {
		if (getItem(position).getMessageTypeId() == ThreadType.MAIN_CHAT.getValue()) { // if message doesn't have message type id, then it must be from main chat
			if (mType == ThreadType.MAIN_CHAT) {
				return R.color.black;
			}
			return R.color.lightgrey;
		}
		else {
			if (mThreadId == getItem(position).getTableId()) {
				return R.color.black;
			}
			return R.color.lightgrey;
		}
	}

	private CharSequence convertTimeToStr(long time) {
		return DateFormat.format("hh:mm:ss a", time);
	}
}