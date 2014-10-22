package com.gjk;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gjk.database.objects.Message;
import com.gjk.helper.DatabaseHelper;
import com.gjk.helper.GeneralHelper;
import com.gjk.utils.media2.ImageManager;
import com.gjk.views.CacheImageView;
import com.gjk.views.RecyclingImageView;

import java.util.Locale;


public class MessagesAdapter extends CursorAdapter {
    private static final String LOGTAG = "MessagesAdapter";

    private final FragmentActivity mA;
    private final long mConvoId;
    private final ConvoType mType;
    private final FragmentManager mFm;


    public MessagesAdapter(FragmentActivity a, Cursor cursor, long convoId, ConvoType type) {
        super(a, cursor, true);
        mA = a;
        mConvoId = convoId;
        mType = type;
        mFm = a.getSupportFragmentManager();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_row, parent, false);
        buildView(view, cursor);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        buildView(view, cursor);
    }

    private void buildView(View view, Cursor cursor) {

        final RelativeLayout row = (RelativeLayout) view.findViewById(R.id.messageLayout);
        final TextView userName = (TextView) view.findViewById(R.id.userName);
        final TextView message = (TextView) view.findViewById(R.id.message);
        final TextView time = (TextView) view.findViewById(R.id.time);
        final TextView headerDate = (TextView) view.findViewById(R.id.headerDate);
        final TextView footerDate = (TextView) view.findViewById(R.id.footerDate);

        final String thisD = String.valueOf(DateFormat.format("yyyyMMdd", cursor.getLong(cursor.getColumnIndex
                (Message.F_DATE))));
        if (cursor.isFirst()) { // first message
            headerDate.setVisibility(View.VISIBLE);
            headerDate.setText(convertDateToStr(cursor.getLong(cursor.getColumnIndex(Message.F_DATE)), true));
            footerDate.setVisibility(View.GONE);
            if (cursor.getCount() > 1) {
                cursor.moveToNext();
                String nextD = String.valueOf(DateFormat.format("yyyyMMdd", cursor.getLong(cursor.getColumnIndex
                        (Message.F_DATE))));
                cursor.moveToPrevious();
                if (nextD.equals(thisD)) {
                    footerDate.setVisibility(View.GONE);
                } else {
                    footerDate.setVisibility(View.VISIBLE);
                    footerDate.setText(convertDateToStr(cursor.getLong(cursor.getColumnIndex(Message.F_DATE)), false));
                }
            }
        } else if (cursor.getPosition() < getCount()) { // any message but the first
            cursor.moveToPrevious();
            String prevD = String.valueOf(DateFormat.format("yyyyMMdd", cursor.getLong(cursor.getColumnIndex(Message
                    .F_DATE))));
            cursor.moveToNext();
            if (prevD.equals(thisD)) {
                headerDate.setVisibility(View.GONE);
            } else {
                headerDate.setVisibility(View.VISIBLE);
                headerDate.setText(convertDateToStr(cursor.getLong(cursor.getColumnIndex(Message.F_DATE)), true));
            }
            if (!cursor.isLast()) { // not the first or last message
                cursor.moveToNext();
                String nextD = String.valueOf(DateFormat.format("yyyyMMdd", cursor.getLong(cursor.getColumnIndex
                        (Message.F_DATE))));
                cursor.moveToPrevious();
                if (nextD.equals(thisD)) {
                    footerDate.setVisibility(View.GONE);
                } else {
                    footerDate.setVisibility(View.VISIBLE);
                    footerDate.setText(convertDateToStr(cursor.getLong(cursor.getColumnIndex(Message.F_DATE)), false));
                }
            } else { // the last message
                String currDate = String.valueOf(DateFormat.format("yyyyMMdd", System.currentTimeMillis()));
                if (currDate.equals(thisD)) {
                    footerDate.setVisibility(View.GONE);
                } else {
                    footerDate.setVisibility(View.VISIBLE);
                    footerDate.setText(convertDateToStr(cursor.getLong(cursor.getColumnIndex(Message.F_DATE)), false));
                }
            }
        }

        if (cursor.getLong(cursor.getColumnIndex(Message.F_SENDER_ID)) == DatabaseHelper.getAccountUserId()) {
            row.setBackgroundColor(mA.getResources().getColor(R.color.ivory));
        } else {
            row.setBackgroundColor(mA.getResources().getColor(R.color.ghostwhite));
        }
        String name = String.format(Locale.getDefault(), "%s %s", cursor.getString(cursor.getColumnIndex(Message
                .F_SENDER_FIRST_NAME)), cursor.getString(cursor.getColumnIndex(Message
                .F_SENDER_LAST_NAME)));
        userName.setText(name);
        message.setText(cursor.getString(cursor.getColumnIndex(Message.F_CONTENT)));
        Linkify.addLinks(message, Linkify.ALL);
        time.setText(cursor.getLong(cursor.getColumnIndex(Message.F_SUCCESSFUL)) == 0 ? convertTimeToStr(cursor.getLong
                (cursor.getColumnIndex(Message.F_DATE))) : "Sending...");
        int color = getColor(cursor.getInt(cursor.getColumnIndex(Message.F_MESSAGE_TYPE_ID)),
                cursor.getLong(cursor.getColumnIndex(Message.F_TABLE_ID)));
        userName.setTextColor(mA.getResources().getColor(color));
        message.setTextColor(mA.getResources().getColor(color));
        time.setTextColor(mA.getResources().getColor(color));

        CacheImageView avi = (CacheImageView) view.findViewById(R.id.memberAvi);
        RecyclingImageView avi2 = (RecyclingImageView) view.findViewById(R.id.memberAvi2);
        CacheImageView attachment = (CacheImageView) view.findViewById(R.id.attachment);
        RecyclingImageView attachment2 = (RecyclingImageView) view.findViewById(R.id.attachment2);

        final String senderImageUrl = cursor.getString(cursor.getColumnIndex(Message.F_SENDER_IMAGE_URL));
        final String attachmentUrl = cursor.getString(cursor.getColumnIndex(Message.F_ATTACHMENT));
        if (GeneralHelper.getKachisCachePref()) {
            avi2.setVisibility(View.INVISIBLE);
            avi.setVisibility(View.VISIBLE);
            avi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(mA, "IMAGETOVIEW", Toast.LENGTH_LONG).show();
                    Log.d(LOGTAG, "IMAGETOVIEW");
                    Bundle args = new Bundle();
                    args.putString("imgUrl", senderImageUrl);
                    ImageViewerFragment imgFrag = new ImageViewerFragment();
                    imgFrag.setArguments(args);
                    FragmentTransaction transaction = mFm.beginTransaction();
                    transaction.add(R.id.drawer_layout,imgFrag, "imgFrag");
                    transaction.addToBackStack("imgFrag");
                    transaction.commit();
                }
            });
            attachment2.setVisibility(View.GONE);
            if (GeneralHelper.getCirclizeMemberAvisPref()) {
                avi.configure(Constants.BASE_URL + senderImageUrl, 0, true);
            } else {
                avi.configure(Constants.BASE_URL + senderImageUrl, 0, false);
            }
            attachment.setVisibility(!TextUtils.isEmpty(attachmentUrl) ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(attachmentUrl)) {
                attachment.configure(Constants.BASE_URL + attachmentUrl, 0, false);
                attachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(mA, "IMAGETOVIEW", Toast.LENGTH_LONG).show();
                        Log.d(LOGTAG, "IMAGETOVIEW");
                        Bundle args = new Bundle();
                        args.putString("imgUrl",attachmentUrl);
                        ImageViewerFragment imgFrag = new ImageViewerFragment();
                        imgFrag.setArguments(args);
                        FragmentTransaction transaction = mFm.beginTransaction();
                        transaction.add(R.id.drawer_layout,imgFrag, "imgFrag");
                        transaction.addToBackStack("imgFrag");
                        transaction.commit();
                     }
                 });


            }
        } else {
            avi.setVisibility(View.INVISIBLE);
            avi2.setVisibility(View.VISIBLE);
            avi2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(mA, "IMAGETOVIEW", Toast.LENGTH_LONG).show();
                    Log.d(LOGTAG, "IMAGETOVIEW");
                    Bundle args = new Bundle();
                    args.putString("imgUrl", senderImageUrl);
                    ImageViewerFragment imgFrag = new ImageViewerFragment();
                    imgFrag.setArguments(args);
                    FragmentTransaction transaction = mFm.beginTransaction();
                    transaction.add(R.id.drawer_layout,imgFrag, "imgFrag");
                    transaction.addToBackStack("imgFrag");
                    transaction.commit();
                }
            });
            attachment.setVisibility(View.GONE);
            if (GeneralHelper.getCirclizeMemberAvisPref()) {
                ImageManager.getInstance(mFm).loadCirclizedImage(senderImageUrl, avi2);
            } else {
                ImageManager.getInstance(mFm).loadUncirclizedImage(senderImageUrl, avi2);
            }
            attachment2.setVisibility(!TextUtils.isEmpty(attachmentUrl) ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(attachmentUrl)) {
                ImageManager.getInstance(mFm).loadUncirclizedImage(attachmentUrl, attachment2);
                attachment2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(mA, "IMAGETOVIEW", Toast.LENGTH_LONG).show();
                        Log.d(LOGTAG, "IMAGETOVIEW");
                        Bundle args = new Bundle();
                        args.putString("imgUrl",attachmentUrl);
                        ImageViewerFragment imgFrag = new ImageViewerFragment();
                        imgFrag.setArguments(args);
                        FragmentTransaction transaction = mFm.beginTransaction();
                        transaction.add(R.id.drawer_layout,imgFrag, "imgFrag");
                        transaction.addToBackStack("imgFrag");
                        transaction.commit();
                    }
                });
            }
        }

    }

    private int getColor(int type, long id) {
        if (type == ConvoType.MAIN_CHAT.getValue()) { // if message doesn't have message type id, then it must be from main chat
            if (mType == ConvoType.MAIN_CHAT) {
                return R.color.black;
            }
            return R.color.lightgrey;
        } else {
            if (mConvoId == id) {
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