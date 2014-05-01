package com.gjk;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gjk.database.objects.Message;
import com.gjk.views.CacheImageView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;


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

        Message item = getItem(position);
        TextView userName = (TextView) convertView.findViewById(R.id.userName);
        TextView message = (TextView) convertView.findViewById(R.id.message);
        TextView time = (TextView) convertView.findViewById(R.id.time);
        TextView headerDate = (TextView) convertView.findViewById(R.id.headerDate);
        TextView footerDate = (TextView) convertView.findViewById(R.id.footerDate);

        if (userName != null && message != null && time != null && footerDate != null && headerDate != null) {

            if (position == 0) { // first message
                headerDate.setVisibility(View.VISIBLE);
                headerDate.setText(convertDateToStr(item.getDate(), true));
                footerDate.setVisibility(View.GONE);
            } else if (position < getCount()) { // any message but the first
                Message prev = getItem(position - 1);
                String prevD = String.valueOf(DateFormat.format("yyyyMMdd", prev.getDate()));
                String thisD = String.valueOf(DateFormat.format("yyyyMMdd", item.getDate()));
                if (prevD.equals(thisD)) {
                    headerDate.setVisibility(View.GONE);
                } else {
                    headerDate.setVisibility(View.VISIBLE);
                    headerDate.setText(convertDateToStr(item.getDate(), true));
                }
                if (position < getCount() - 1) { // not the first or last message
                    Message next = getItem(position + 1);
                    String nextD = String.valueOf(DateFormat.format("yyyyMMdd", next.getDate()));
                    if (nextD.equals(thisD)) {
                        footerDate.setVisibility(View.GONE);
                    } else {
                        footerDate.setVisibility(View.VISIBLE);
                        footerDate.setText(convertDateToStr(item.getDate(), false));
                    }
                } else { // the last message
                    String currDate = String.valueOf(DateFormat.format("yyyyMMdd", Calendar.getInstance().get(Calendar
                            .MILLISECOND)));
                    if (currDate.equals(thisD)) {
                        footerDate.setVisibility(View.GONE);
                    } else {
                        footerDate.setVisibility(View.VISIBLE);
                        footerDate.setText(convertDateToStr(item.getDate(), false));
                    }
                }
            }

            String name = String.format(Locale.getDefault(), "%s %s", item.getSenderFirstName(), item.getSenderLastName());
            userName.setText(name);
            message.setText(item.getContent());
            time.setText(convertTimeToStr(item.getDate()));
            int color = getColor(item);
            userName.setTextColor(mContext.getResources().getColor(color));
            message.setTextColor(mContext.getResources().getColor(color));
            time.setTextColor(mContext.getResources().getColor(color));
            ((CacheImageView) convertView.findViewById(R.id.image)).configure(Constants.BASE_URL + item.getSenderImageUrl(), 0);
            CacheImageView attachment = (CacheImageView) convertView.findViewById(R.id.attachment);
            attachment.setVisibility(!TextUtils.isEmpty(item.getAttachments()) ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(item.getAttachments()))
                attachment.configure(Constants.BASE_URL + item.getAttachments(), 0);

        }
        return convertView;
    }

    private int getColor(Message m) {
        if (m.getMessageTypeId() == ThreadType.MAIN_CHAT.getValue()) { // if message doesn't have message type id, then it must be from main chat
            if (mType == ThreadType.MAIN_CHAT) {
                return R.color.black;
            }
            return R.color.lightgrey;
        } else {
            if (mThreadId == m.getTableId()) {
                return R.color.black;
            }
            return R.color.lightgrey;
        }
    }

    private CharSequence convertTimeToStr(long time) {
        return DateFormat.format("hh:mm:ss a", time);
    }

    private CharSequence convertDateToStr(long time, boolean header) {
        String str = String.valueOf(DateFormat.format("EEEE MMM d", time));
        String[] strSplit = str.split("\\s+");
        int day = Integer.valueOf(strSplit[2]);
        if (day >= 4 && day <= 19) {
            str += "th";
        } else if (day % 10 == 1) {
            str += "st";
        } else if (day % 10 == 2) {
            str += "nd";
        } else if (day % 10 == 3) {
            str += "rd";
        } else {
            str += "th";
        }

        if (header) {
            return String.format(Locale.getDefault(), "<%s>", str);
        }
        return String.format(Locale.getDefault(), "</%s>", str);
    }
}