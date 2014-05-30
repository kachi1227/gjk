package com.gjk;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gjk.database.objects.Message;
import com.gjk.utils.media2.ImageCache;
import com.gjk.utils.media2.ImageFetcher;
import com.gjk.views.CacheImageView;
import com.gjk.views.RecyclingImageView;

import java.util.List;
import java.util.Locale;


public class MessagesAdapter extends ArrayAdapter<Message> {
    private final String LOGTAG = getClass().getSimpleName();

    private final Context mCtx;
    private final long mChatId;
    private final long mThreadId;
    private final ThreadType mType;
    private final ImageFetcher mImageFetcher;

    public MessagesAdapter(Context ctx, FragmentManager fm, long chatId, long threadId, ThreadType type,
                           List<Message> ims) {
        super(ctx, 0, ims);
        mCtx = ctx;
        mChatId = chatId;
        mThreadId = threadId;
        mType = type;
        mImageFetcher = new ImageFetcher(mCtx, 1000);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo, true);
        ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams(mCtx, "image_cache");
        params.setMemCacheSizePercent(0.8f);
        mImageFetcher.addImageCache(fm, params);
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
                    String currDate = String.valueOf(DateFormat.format("yyyyMMdd", System.currentTimeMillis()));
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
            Linkify.addLinks(message, Linkify.ALL);
            time.setText(convertTimeToStr(item.getDate()));
            int color = getColor(item);
            userName.setTextColor(mCtx.getResources().getColor(color));
            message.setTextColor(mCtx.getResources().getColor(color));
            time.setTextColor(mCtx.getResources().getColor(color));

            CacheImageView avi = (CacheImageView) convertView.findViewById(R.id.memberAvi);
            RecyclingImageView avi2 = (RecyclingImageView) convertView.findViewById(R.id.memberAvi2);
            CacheImageView attachment = (CacheImageView) convertView.findViewById(R.id.attachment);
            RecyclingImageView attachment2 = (RecyclingImageView) convertView.findViewById(R.id.attachment2);

            if (Application.get().getPreferences().getBoolean(Constants.PROPERTY_SETTING_USE_KACHIS_CACHE,
                    Constants.PROPERTY_SETTING_USE_KACHIS_CACHE_DEFAULT)) {
                avi2.setVisibility(View.INVISIBLE);
                avi.setVisibility(View.VISIBLE);
                attachment2.setVisibility(View.GONE);
                avi.configure(Constants.BASE_URL + item.getSenderImageUrl(), 0);
                attachment.setVisibility(!TextUtils.isEmpty(item.getAttachments()) ? View.VISIBLE : View.GONE);
                if (!TextUtils.isEmpty(item.getAttachments()))
                    attachment.configure(Constants.BASE_URL + item.getAttachments(), 0);
            } else {
                avi.setVisibility(View.INVISIBLE);
                avi2.setVisibility(View.VISIBLE);
                attachment.setVisibility(View.GONE);
                mImageFetcher.loadImage(Constants.BASE_URL + item.getSenderImageUrl(), avi2, true);
                attachment2.setVisibility(!TextUtils.isEmpty(item.getAttachments()) ? View.VISIBLE : View.GONE);
                if (!TextUtils.isEmpty(item.getAttachments()))
                    mImageFetcher.loadImage(Constants.BASE_URL + item.getAttachments(), attachment2, false);
            }
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