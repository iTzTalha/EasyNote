package com.icode.easynote.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.icode.easynote.models.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteDatabase {
    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    public NoteDatabase(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public long insertNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_TITLE, note.getTitle());
        values.put(DatabaseHelper.KEY_SUBTITLE, note.getSubtitle());
        values.put(DatabaseHelper.KEY_DATE, note.getDate());
        values.put(DatabaseHelper.KEY_NOTE, note.getNote());
        values.put(DatabaseHelper.KEY_COLOR, note.getColor());
        values.put(DatabaseHelper.KEY_PHOTO, note.getPhotoPath());
        values.put(DatabaseHelper.KEY_LINK, note.getLink());

        return db.insert(DatabaseHelper.DATABASE_TABLE_NAME, null, values);
    }

    public long updateNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_TITLE, note.getTitle());
        values.put(DatabaseHelper.KEY_SUBTITLE, note.getSubtitle());
        values.put(DatabaseHelper.KEY_DATE, note.getDate());
        values.put(DatabaseHelper.KEY_NOTE, note.getNote());
        values.put(DatabaseHelper.KEY_COLOR, note.getColor());
        values.put(DatabaseHelper.KEY_PHOTO, note.getPhotoPath());
        values.put(DatabaseHelper.KEY_LINK, note.getLink());

        return db.update(DatabaseHelper.DATABASE_TABLE_NAME, values, DatabaseHelper.KEY_ID + "=?", new String[]{String.valueOf(note.getId())});
    }

    public void deleteNote(long id) {
        db.delete(DatabaseHelper.DATABASE_TABLE_NAME, DatabaseHelper.KEY_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }


    public Note getNote(long id) {
        String[] columns = {DatabaseHelper.KEY_ID, DatabaseHelper.KEY_TITLE, DatabaseHelper.KEY_SUBTITLE, DatabaseHelper.KEY_DATE, DatabaseHelper.KEY_NOTE, DatabaseHelper.KEY_COLOR, DatabaseHelper.KEY_PHOTO, DatabaseHelper.KEY_LINK};
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(DatabaseHelper.DATABASE_TABLE_NAME, columns, DatabaseHelper.KEY_ID + "=?", selectionArgs, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        return new Note(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7));
    }

    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(DatabaseHelper.QUERY_GET_TABLE, null);
        } catch (SQLException e) {
            Log.e("GET_TABLE", e.getMessage());
            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Note note = new Note(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7));
                notes.add(note);
            }
        }

        return notes;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        //DB Data
        private final static String DATABASE_NAME = "easyNote";
        private final static String DATABASE_TABLE_NAME = "notes";
        private final static int DATABASE_VERSION = 1;

        //Columns for DB
        private final static String KEY_ID = "id";
        private final static String KEY_TITLE = "title";
        private final static String KEY_SUBTITLE = "subtitle";
        private final static String KEY_DATE = "date";
        private final static String KEY_NOTE = "note";
        private final static String KEY_COLOR = "color";
        private final static String KEY_PHOTO = "photo";
        private final static String KEY_LINK = "link";

        //QUERIES:
        //Create Table
        private final static String QUERY_CREATE_TABLE = "CREATE TABLE " + DATABASE_TABLE_NAME +
                " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_TITLE + " TEXT, " + KEY_SUBTITLE + " TEXT, " + KEY_DATE + " TEXT, " + KEY_NOTE + " TEXT, " + KEY_COLOR + " TEXT, " + KEY_PHOTO + " TEXT, " + KEY_LINK + " TEXT)";
        //Delete Table
        private final static String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + DATABASE_TABLE_NAME;
        //select all from db
        private final static String QUERY_GET_TABLE = "SELECT * FROM " + DATABASE_TABLE_NAME;

        private Context context;

        public DatabaseHelper(@Nullable Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(QUERY_CREATE_TABLE);
            } catch (SQLException e) {
                //Log.d("SQL_CREATION_ERROR", e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL(QUERY_DROP_TABLE);
                onCreate(db);
            } catch (SQLException e) {
                //Log.d("SQL_CREATION_ERROR", e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
