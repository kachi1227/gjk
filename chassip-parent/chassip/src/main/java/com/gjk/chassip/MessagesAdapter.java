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

	public MessagesAdapter(Context context, long chatId, long threadId, List<Message> ims) {
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
		String name = String.format(Locale.getDefault(), "%s %s", getItem(position).getSenderFirstName(), getItem(position).getSenderLastName());
		userName.setText(name);
		TextView message = (TextView) convertView.findViewById(R.id.message);
		message.setText(getItem(position).getContent());
		TextView time = (TextView) convertView.findViewById(R.id.time);
		time.setText(convertTimeToStr(getItem(position).getDate()));

		Integer color = R.color.black;
		if (color != null) {
			userName.setTextColor(mContext.getResources().getColor(color));
			message.setTextColor(mContext.getResources().getColor(color));
			time.setTextColor(mContext.getResources().getColor(color));
		}

		return convertView;
	}

	private CharSequence convertTimeToStr(long time) {
		return DateFormat.format("hh:mm:ss a", time);
	}
}