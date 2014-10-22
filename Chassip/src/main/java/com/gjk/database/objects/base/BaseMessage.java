package com.gjk.database.objects.base;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.gjk.ConvoType;
import com.gjk.database.PersistentObject;
import com.gjk.database.objects.Message;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseMessage extends PersistentObject {

    private static final String ERROR_MSG_CLOSED_CURSOR = "Tried to hyrdate PostItem from closed cursor.";

    private static final String ERROR_MSG_HYDRATE_NO_ID = "Error fetching column 'id' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_GLOBAL_ID = "Error fetching column 'global_id' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_GROUP_ID = "Error fetching column 'group_id' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_SENDER_ID = "Error fetching column 'sender_id' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_SENDER_FIRST_NAME = "Error fetching column 'sender_first_name' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_SENDER_LAST_NAME = "Error fetching column 'sender_last_name' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_SENDER_IMAGE_URL = "Error fetching column 'sender_image_url' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_RECIPIENT_ID = "Error fetching column 'recipient_id' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_RECIPIENT_FIRST_NAME = "Error fetching column 'recipient_first_name' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_RECIPIENT_LAST_NAME = "Error fetching column 'recipient_last_name' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_TOPIC_ID = "Error fetching column 'topic_id' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_TOPIC_NAME = "Error fetching column 'topic_name' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_CONTENT = "Error fetching column 'content' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_ATTACHMENT = "Error fetching column 'attachment' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_MESSAGE_TYPE_ID = "Error fetching column 'message_type_id' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_TABLE_ID = "Error fetching column 'table_id' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_SUCCESSFUL = "Error fetching column 'successful' from table 'message'";
    private static final String ERROR_MSG_HYDRATE_NO_DATE = "Error fetching column 'date' from table 'message'";

    public static final String TABLE_NAME = "message";


    public static final String F_ID = "_id";
    public static final String F_GLOBAL_ID = "global_id";
    public static final String F_GROUP_ID = "group_id";
    public static final String F_SENDER_ID = "sender_id";
    public static final String F_SENDER_FIRST_NAME = "sender_first_name";
    public static final String F_SENDER_LAST_NAME = "sender_last_name";
    public static final String F_SENDER_IMAGE_URL = "sender_image_url";
    public static final String F_RECIPIENT_ID = "recipient_id";
    public static final String F_RECIPIENT_FIRST_NAME = "recipient_first_name";
    public static final String F_RECIPIENT_LAST_NAME = "recipient_last_name";
    public static final String F_TOPIC_ID = "topic_id";
    public static final String F_TOPIC_NAME = "topic_name";
    public static final String F_CONTENT = "content";
    public static final String F_ATTACHMENT = "attachment";
    public static final String F_MESSAGE_TYPE_ID = "message_type_id";
    public static final String F_TABLE_ID = "table_id";
    public static final String F_DATE = "date";
    public static final String F_SUCCESSFUL = "successful";

    public static final String[] ALL_COLUMN_NAMES = new String[]{F_ID, F_GLOBAL_ID, F_GROUP_ID, F_SENDER_ID, F_SENDER_FIRST_NAME, F_SENDER_LAST_NAME, F_SENDER_IMAGE_URL, F_RECIPIENT_ID, F_RECIPIENT_FIRST_NAME, F_RECIPIENT_LAST_NAME, F_TOPIC_ID, F_TOPIC_NAME, F_CONTENT, F_ATTACHMENT, F_MESSAGE_TYPE_ID, F_TABLE_ID, F_DATE, F_SUCCESSFUL};

    public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE \"message\"(   \"_id\" INTEGER PRIMARY KEY NOT NULL,   \"global_id\" INTEGER NOT NULL,		\"group_id\" INTEGER NOT NULL,   \"sender_id\" INTEGER NOT NULL,	\"sender_first_name\" VARCHAR(256),		\"sender_last_name\" VARCHAR(256),   \"sender_image_url\" VARCHAR(2000),	\"recipient_id\" INTEGER,		\"recipient_first_name\" VARCHAR(256),		\"recipient_last_name\" VARCHAR(256),	\"topic_id\" INTEGER,   \"topic_name\" VARCHAR(2000),	\"content\" TEXT,   \"attachment\" VARCHAR(5000),	\"message_type_id\" INTEGER,	\"table_id\" INTEGER,	\"date\" DATETIME      , \"successful\" INTEGER)";
    public static final String DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS 'message';";

    private static final String COUNT_STATEMENT = "SELECT COUNT(" + F_ID + ") FROM message";

    private static final String EMPTY_STRING = "";


    private long mGlobalId;
    private long mGroupId;
    private long mSenderId;
    private String mSenderFirstName;
    private String mSenderLastName;
    private String mSenderImageUrl;
    private long mRecipientId;
    private String mRecipientFirstName;
    private String mRecipientLastName;
    private long mTopicId;
    private String mTopicName;
    private String mContent;
    private String mAttachment;
    private int mMessageTypeId;
    private long mTableId;
    private long mDate;
    private long mSuccessful;

    public BaseMessage(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
        super(dbm, c, skipOk);
    }

    public BaseMessage(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
        super(dbm, jsonString, skipOk);
    }

    public BaseMessage(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
        super(dbm, obj, skipOk);
    }

    public BaseMessage(SQLiteOpenHelper dbm, long id, boolean list) {
        super(dbm, id, list);
    }

    public BaseMessage(SQLiteOpenHelper dbm) {
        super(dbm);
    }

    @Override
    protected void initNewObject() {
        super.initNewObject();

        mGlobalId = 0;
        mGroupId = 0;
        mSenderId = 0;
        mSenderFirstName = "";
        mSenderLastName = "";
        mSenderImageUrl = "";
        mRecipientId = 0;
        mRecipientFirstName = "";
        mRecipientLastName = "";
        mTopicId = 0;
        mTopicName = "";
        mContent = "";
        mAttachment = "";
        mMessageTypeId = ConvoType.MAIN_CHAT.getValue();
        mTableId = 0;
        mDate = System.currentTimeMillis();
        mSuccessful = 0;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String[] getAllColumnNames() {
        return ALL_COLUMN_NAMES;
    }

    @Override
    public String getIdColumnName() {
        return F_ID;
    }

    @Override
    public String getCreateTableStatement() {
        return CREATE_TABLE_STATEMENT;
    }

    @Override
    public void hydrate(Cursor c, boolean skipOk) {
        if (c.isClosed()) {
            throw new PersistentObjectHydrateException(ERROR_MSG_CLOSED_CURSOR);
        }


        try {
            setId(c.getLong(c.getColumnIndexOrThrow(F_ID)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setGlobalId(c.getLong(c.getColumnIndexOrThrow(F_GLOBAL_ID)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_GLOBAL_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setGroupId(c.getLong(c.getColumnIndexOrThrow(F_GROUP_ID)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_GROUP_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setSenderId(c.getLong(c.getColumnIndexOrThrow(F_SENDER_ID)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SENDER_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setSenderFirstName(c.getString(c.getColumnIndexOrThrow(F_SENDER_FIRST_NAME)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SENDER_FIRST_NAME, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setSenderLastName(c.getString(c.getColumnIndexOrThrow(F_SENDER_LAST_NAME)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SENDER_LAST_NAME, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setSenderImageUrl(c.getString(c.getColumnIndexOrThrow(F_SENDER_IMAGE_URL)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SENDER_IMAGE_URL, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setRecipientId(c.getLong(c.getColumnIndexOrThrow(F_RECIPIENT_ID)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_RECIPIENT_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setRecipientFirstName(c.getString(c.getColumnIndexOrThrow(F_RECIPIENT_FIRST_NAME)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_RECIPIENT_FIRST_NAME, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setRecipientLastName(c.getString(c.getColumnIndexOrThrow(F_RECIPIENT_LAST_NAME)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_RECIPIENT_LAST_NAME, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setTopicId(c.getLong(c.getColumnIndexOrThrow(F_TOPIC_ID)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_TOPIC_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setTopicName(c.getString(c.getColumnIndexOrThrow(F_TOPIC_NAME)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_TOPIC_NAME, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setContent(c.getString(c.getColumnIndexOrThrow(F_CONTENT)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_CONTENT, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setAttachment(c.getString(c.getColumnIndexOrThrow(F_ATTACHMENT)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_ATTACHMENT, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setMessageTypeId(c.getInt(c.getColumnIndexOrThrow(F_MESSAGE_TYPE_ID)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_MESSAGE_TYPE_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setTableId(c.getLong(c.getColumnIndexOrThrow(F_TABLE_ID)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_TABLE_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setDate(c.getLong(c.getColumnIndexOrThrow(F_DATE)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_DATE, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            setSuccessful(c.getLong(c.getColumnIndexOrThrow(F_SUCCESSFUL)));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SUCCESSFUL, e);
            } else {
                setIsComplete(false);
            }
        }
        setIsDirty(false);
    }

    @Override
    public void hydrate(JSONObject obj, boolean skipOk) {

        try {
            setId(obj.getLong(F_ID));
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mGlobalId = obj.getLong(F_GLOBAL_ID);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_GLOBAL_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mGroupId = obj.getLong(F_GROUP_ID);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_GROUP_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mSenderId = obj.getLong(F_SENDER_ID);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SENDER_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mSenderFirstName = obj.getString(F_SENDER_FIRST_NAME);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SENDER_FIRST_NAME, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mSenderLastName = obj.getString(F_SENDER_LAST_NAME);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SENDER_LAST_NAME, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mSenderImageUrl = obj.getString(F_SENDER_IMAGE_URL);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SENDER_IMAGE_URL, e);
            } else {
                setIsComplete(false);
            }
        }

        try {
            mRecipientId = obj.getLong(F_RECIPIENT_ID);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_RECIPIENT_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mRecipientFirstName = obj.getString(F_RECIPIENT_FIRST_NAME);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_RECIPIENT_FIRST_NAME, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mRecipientLastName = obj.getString(F_RECIPIENT_LAST_NAME);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_RECIPIENT_LAST_NAME, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mTopicId = obj.getLong(F_TOPIC_ID);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_TOPIC_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mTopicName = obj.getString(F_TOPIC_NAME);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_TOPIC_NAME, e);
            } else {
                setIsComplete(false);
            }
        }

        try {
            mContent = obj.getString(F_CONTENT);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_CONTENT, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mAttachment = obj.getString(F_ATTACHMENT);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_ATTACHMENT, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mMessageTypeId = obj.getInt(F_MESSAGE_TYPE_ID);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_MESSAGE_TYPE_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mTableId = obj.getLong(F_TABLE_ID);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_TABLE_ID, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mDate = obj.getLong(F_DATE);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_DATE, e);
            } else {
                setIsComplete(false);
            }
        }
        try {
            mSuccessful = obj.getLong(F_SUCCESSFUL);
        } catch (Exception e) {
            if (!skipOk) {
                e.printStackTrace();
                throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SUCCESSFUL, e);
            } else {
                setIsComplete(false);
            }
        }

        setIsDirty(true);
        setIsNew(true);
    }

    @Override
    public JSONObject getJSONObject() {
        try {
            JSONObject obj = new JSONObject();

            obj.put(F_ID, getId());
            obj.put(F_GLOBAL_ID, mGlobalId);
            obj.put(F_GROUP_ID, mGroupId);
            obj.put(F_SENDER_ID, mSenderId);
            obj.put(F_SENDER_FIRST_NAME, mSenderFirstName);
            obj.put(F_SENDER_LAST_NAME, mSenderLastName);
            obj.put(F_SENDER_IMAGE_URL, mSenderImageUrl);
            obj.put(F_RECIPIENT_ID, mRecipientId);
            obj.put(F_RECIPIENT_FIRST_NAME, mRecipientFirstName);
            obj.put(F_RECIPIENT_LAST_NAME, mRecipientLastName);
            obj.put(F_TOPIC_ID, mTopicId);
            obj.put(F_TOPIC_NAME, mTopicName);
            obj.put(F_CONTENT, mContent);
            obj.put(F_ATTACHMENT, mAttachment);
            obj.put(F_MESSAGE_TYPE_ID, mMessageTypeId);
            obj.put(F_TABLE_ID, mTableId);
            obj.put(F_DATE, mDate);
            obj.put(F_SUCCESSFUL, mSuccessful);

            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues(14);

        cv.put(F_GLOBAL_ID, mGlobalId);
        cv.put(F_GROUP_ID, mGroupId);
        cv.put(F_SENDER_ID, mSenderId);
        cv.put(F_SENDER_FIRST_NAME, mSenderFirstName);
        cv.put(F_SENDER_LAST_NAME, mSenderLastName);
        cv.put(F_SENDER_IMAGE_URL, mSenderImageUrl);
        cv.put(F_RECIPIENT_ID, mRecipientId);
        cv.put(F_RECIPIENT_FIRST_NAME, mRecipientFirstName);
        cv.put(F_RECIPIENT_LAST_NAME, mRecipientLastName);
        cv.put(F_TOPIC_ID, mTopicId);
        cv.put(F_TOPIC_NAME, mTopicName);
        cv.put(F_CONTENT, mContent);
        cv.put(F_ATTACHMENT, mAttachment);
        cv.put(F_MESSAGE_TYPE_ID, mMessageTypeId);
        cv.put(F_TABLE_ID, mTableId);
        cv.put(F_DATE, mDate);
        cv.put(F_SUCCESSFUL, mSuccessful);

        return cv;
    }

    public static long getCount(SQLiteOpenHelper dbm) {
        return getCount(dbm, null);
    }

    public static long getCount(SQLiteOpenHelper dbm, String whereClause) {
        long count = 0;
        SQLiteStatement stmt = null;
        if (whereClause == null) {
            stmt = dbm.getReadableDatabase().compileStatement(COUNT_STATEMENT);
        } else {
            stmt = dbm.getReadableDatabase().compileStatement(COUNT_STATEMENT + " WHERE " + whereClause);
        }
        count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public static boolean isTableEmpty(SQLiteOpenHelper dbm) {
        return getCount(dbm, null) == 0;
    }

    public static int deleteById(SQLiteOpenHelper dbm, long id, boolean notEqual) {
        return dbm.getWritableDatabase().delete(TABLE_NAME, F_ID + (notEqual ? " <> " : " = ") + id, null);
    }

    public static int deleteByIds(SQLiteOpenHelper dbm, List<Long> idsToDelete, boolean notIn) {
        String idList = "";
        for (int i = 0; i < idsToDelete.size(); i++) {
            if (!idList.equals(EMPTY_STRING)) {
                idList += ", ";
            }
            idList += "" + idsToDelete.get(i);
        }
        return dbm.getWritableDatabase().delete(TABLE_NAME, F_ID + (notIn ? " NOT" : "") + " IN (" + idList + ")", null);
    }

    public static int deleteWhere(SQLiteOpenHelper dbm, String whereClause) {
        return deleteWhere(dbm, whereClause, null);
    }

    public static int deleteWhere(SQLiteOpenHelper dbm, String whereClause, String[] whereArgs) {
        return dbm.getWritableDatabase().delete(TABLE_NAME, whereClause, whereArgs);
    }

    public static List<Message> findAllObjects(SQLiteOpenHelper dbm, String orderBy) {
        ArrayList<Message> objList = new ArrayList<Message>();
        Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, null, null, null, null, orderBy);
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            return objList;
        }


        while (!c.isAfterLast()) {
            objList.add(new Message(dbm, c, false));
            c.moveToNext();
        }
        c.close();
        return objList;
    }

    public static Message findById(SQLiteOpenHelper dbm, long id) {
        Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, F_ID + " = " + id, null, null, null, null, "1");
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            return null;
        }
        Message obj = new Message(dbm, c, false);
        c.close();
        return obj;
    }

    public static Message findOneByGlobalId(SQLiteOpenHelper dbm, long val) {
        Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, F_GLOBAL_ID + " = " + val, null, null, null, null);
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            return null;
        }
        Message obj = new Message(dbm, c, false);
        c.close();
        return obj;
    }

    public static Message findOneBySuccessful(SQLiteOpenHelper dbm, long val) {
        Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, F_SUCCESSFUL + " = " + val, null, null, null,
                null);
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            return null;
        }
        Message obj = new Message(dbm, c, false);
        c.close();
        return obj;
    }

    public long getGlobalId() {
        return mGlobalId;
    }

    public void setGlobalId(long val) {
        this.mGlobalId = val;
        setIsDirty(true);
    }


    public static int deleteByGlobalId(SQLiteOpenHelper dbm, long val) {
        return dbm.getWritableDatabase().delete(TABLE_NAME, F_GLOBAL_ID + "=" + val, null);
    }

    public long getGroupId() {
        return mGroupId;
    }

    public void setGroupId(long val) {
        this.mGroupId = val;
        setIsDirty(true);
    }

    public long getSenderId() {
        return mSenderId;
    }

    public void setSenderId(long val) {
        this.mSenderId = val;
        setIsDirty(true);
    }

    public String getSenderFirstName() {
        return mSenderFirstName;
    }

    public void setSenderFirstName(String val) {
        this.mSenderFirstName = val;
        setIsDirty(true);
    }

    public String getSenderLastName() {
        return mSenderLastName;
    }

    public void setSenderLastName(String val) {
        this.mSenderLastName = val;
        setIsDirty(true);
    }

    public String getSenderImageUrl() {
        return mSenderImageUrl;
    }

    public void setSenderImageUrl(String val) {
        this.mSenderImageUrl = val;
        setIsDirty(true);
    }

    public long getRecipientId() {
        return mRecipientId;
    }

    public void setRecipientId(long val) {
        this.mRecipientId = val;
        setIsDirty(true);
    }

    public String getRecipientFirstName() {
        return mRecipientFirstName;
    }

    public void setRecipientFirstName(String val) {
        this.mRecipientFirstName = val;
        setIsDirty(true);
    }

    public String getRecipientLastName() {
        return mRecipientLastName;
    }

    public void setRecipientLastName(String val) {
        this.mRecipientLastName = val;
        setIsDirty(true);
    }

    public long getTopicId() {
        return mTopicId;
    }


    public void setTopicId(long val) {
        this.mTopicId = val;
        setIsDirty(true);
    }

    public String getTopicName() {
        return mTopicName;
    }


    public void setTopicName(String val) {
        this.mTopicName = val;
        setIsDirty(true);
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String val) {
        this.mContent = val;
        setIsDirty(true);
    }

    public String getAttachments() {
        return mAttachment;
    }

    public void setAttachment(String val) {
        this.mAttachment = val;
        setIsDirty(true);
    }

    public int getMessageTypeId() {
        return mMessageTypeId;
    }

    public void setMessageTypeId(int val) {
        this.mMessageTypeId = val;
        setIsDirty(true);
    }

    public Long getTableId() {
        return mTableId;
    }

    public void setTableId(long val) {
        this.mTableId = val;
        setIsDirty(true);
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long val) {
        this.mDate = val;
        setIsDirty(true);
    }

    public long getSuccessful() {
        return mSuccessful;
    }

    public void setSuccessful(long val) {
        this.mSuccessful = val;
        setIsDirty(true);
    }
}