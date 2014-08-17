/*
 * Copyright (C) 2012 The Android Open Source Project 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.parabay.cinema.facebook;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class to interact with the database that stores the Picasa contacts
 */
public class UserDatabase {
	
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_MAIL = "mail";
    public static final String KEY_LASTUPDATE = "lastupdate";
    public static final String KEY_NOTIFY = "notify";

    public static final String KEY_PID = "pid";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_USER = "user";
    public static final String KEY_LINK = "link";
    public static final String KEY_PREVIEW = "preview";
    public static final String KEY_DETAILS = "details";
    public static final String KEY_TYPEOF = "typeof";
    public static final String KEY_MODIFIED = "modified";

    
    private static final String TAG = "UserDatabase";

    private static final String DATABASE_NAME = "parabay";
    private static final String DATABASE_TABLE_USERS = "users";
    private static final String DATABASE_TABLE_MEDIA = "media";

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_USERS_CREATE = "create table users"
            + " (_id integer primary key autoincrement, " + "name text not null, "
            + "mail text not null," + "notify integer not null," + "lastupdate integer);";

    private static final String DATABASE_MEDIA_CREATE = " create table media"
            + " (_id integer primary key autoincrement, " + "pid text not null," 
            + "source integer not null, " + "user text not null, "+ "link integer, " + "preview text, " 
            + "details text not null," + "typeof integer not null," + "modified integer);";

    
    private final Context mContext;

    private DatabaseHelper mDbHelper;

    private SQLiteDatabase mDatabase;

    public UserDatabase(Context ctx) {
        this.mContext = ctx;
        mDbHelper = new DatabaseHelper(mContext);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) throws SQLException {
        	
            db.execSQL(DATABASE_USERS_CREATE);
            db.execSQL(DATABASE_MEDIA_CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            onCreate(db);
        }
    }

    /**
     * Opens the database
     * 
     * @return Returns the database 
     * @throws SQLException
     */
    public UserDatabase open() throws SQLException {
    	
        mDatabase = mDbHelper.getWritableDatabase();
        
        //TODO: uncomment to drop tables
        //mDatabase.execSQL("DROP TABLE users;");
        //mDatabase.execSQL("DROP TABLE media;");
        //mDbHelper.onCreate(mDatabase);
        
        return this;
    }

    /**
     * Closes the database
     */
    public void close() {
        mDbHelper.close();
    }

    /**
     * Insert a name into the database
     * 
     * @param name Name of the user
     * @param mail Mail of the user
     * @param notify Notification set or not
     * @param lastupdate The last updated value 
     * @return Returns new row 
     */
    public long insertName(String name, String mail, int notify, int lastupdate) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_MAIL, mail);
        initialValues.put(KEY_NOTIFY, notify);
        initialValues.put(KEY_LASTUPDATE, lastupdate);
        return mDatabase.insert(DATABASE_TABLE_USERS, null, initialValues);
    }

    /**
     * Deletes a particular title
     * 
     * @param string The title to delete
     * @return Return true if row is deleted ,false otherwise
     */
    public boolean deleteName(String string) {
        return mDatabase.delete(DATABASE_TABLE_USERS, KEY_NAME + "=?", new String[] {
            string
        }) > 0;
    }

    /**
     * Retrieves all the titles
     * 
     * @return Returns all the rows in the table
     */
    public Cursor getAllNames() {
        return mDatabase.query(DATABASE_TABLE_USERS, new String[] {
                KEY_ROWID, KEY_NAME, KEY_MAIL, KEY_NOTIFY, KEY_LASTUPDATE
        }, null, null, null, null, null);
    }

    /**
     * Retrieves a particular Name
     * 
     * @param rowId The row id of the table
     * @return Return the particular row
     * @throws SQLException
     */
    public Cursor getName(long rowId) throws SQLException {
        Cursor mCursor = mDatabase.query(true, DATABASE_TABLE_USERS, new String[] {
                KEY_ROWID, KEY_NAME, KEY_MAIL, KEY_NOTIFY, KEY_LASTUPDATE
        }, KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Updates a Name
     * 
     * @param rowId The row id of the table
     * @param name The name of the user
     * @param mail The mail of the user
     * @param notify Notification flag is set or not
     * @param lastupdate The last updated value
     * @return Returns the updated row
     */
    public boolean updateName(long rowId, String name, String mail, int notify, int lastupdate) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_MAIL, mail);
        args.put(KEY_NOTIFY, notify);
        args.put(KEY_LASTUPDATE, lastupdate);
        return mDatabase.update(DATABASE_TABLE_USERS, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    
    /**
     * Insert a name into the database
     * 
     * @param name Media of the user
     * @param mail Mail of the user
     * @param notify Notification set or not
     * @param lastupdate The last updated value 
     * @return Returns new row 
     */
    public void insertMedia(String pid, int source, String user, String preview, String details, int typeof, int modified) {
        
    	Cursor cursor = this.getMedia(pid);
    	
    	if ( (cursor != null) && (cursor.getCount() > 0) ) {
    		
    		this.updateMedia(pid, source, user, preview, details, typeof, modified);
    	}
    	else {
    		
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_PID, pid);
	        initialValues.put(KEY_SOURCE, source);
	        initialValues.put(KEY_USER, user);
	        initialValues.put(KEY_PREVIEW, preview);
	        initialValues.put(KEY_DETAILS, details);
	        initialValues.put(KEY_TYPEOF, typeof);
	        initialValues.put(KEY_MODIFIED, modified);
	        mDatabase.insert(DATABASE_TABLE_MEDIA, null, initialValues);
    	}
    }

    /**
     * Deletes a particular title
     * 
     * @param string The title to delete
     * @return Return true if row is deleted ,false otherwise
     */
    public boolean deleteMedia(String string) {
        return mDatabase.delete(DATABASE_TABLE_MEDIA, KEY_PID + "=?", new String[] {
            string
        }) > 0;
    }

    /**
     * Retrieves all the titles
     * 
     * @return Returns all the rows in the table
     */
    public Cursor getAllMedias() {
    	
        return mDatabase.query(DATABASE_TABLE_MEDIA, new String[] {
                KEY_ROWID, KEY_PID, KEY_SOURCE, KEY_USER, KEY_PREVIEW, KEY_DETAILS, KEY_TYPEOF, KEY_MODIFIED
        }, null, null, null, null, null);
    }

    /**
     * Retrieves a particular Media
     * 
     * @param rowId The row id of the table
     * @return Return the particular row
     * @throws SQLException
     */
    public Cursor getMedia(String pid) throws SQLException {
    	
        Cursor mCursor = mDatabase.query(true, DATABASE_TABLE_MEDIA, new String[] {
        		KEY_ROWID, KEY_PID, KEY_SOURCE, KEY_USER, KEY_PREVIEW, KEY_DETAILS, KEY_TYPEOF, KEY_MODIFIED
        }, KEY_PID + "=?", new String[] { pid }, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Updates a Media
     * 
     * @param rowId The row id of the table
     * @param name The name of the user
     * @param mail The mail of the user
     * @param notify Notification flag is set or not
     * @param lastupdate The last updated value
     * @return Returns the updated row
     */
    public boolean updateMedia(String pid, int source, String user, String preview, String details, int typeof, int modified) {
    	
        ContentValues args = new ContentValues();        
        args.put(KEY_SOURCE, source);
        args.put(KEY_USER, user);
        args.put(KEY_PREVIEW, preview);
        args.put(KEY_DETAILS, details);
        args.put(KEY_TYPEOF, typeof);
        args.put(KEY_MODIFIED, modified);

        return mDatabase.update(DATABASE_TABLE_MEDIA, args, KEY_PID + "=?", new String[] { pid }) > 0;
    }
}
