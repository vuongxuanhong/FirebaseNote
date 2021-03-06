package com.stevedao.note.model.SQLiteDAO;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;
import com.stevedao.note.control.Common;
import com.stevedao.note.model.EntityDAO;
import com.stevedao.note.model.Note;

/**
 * Created by thanh.dao on 07/04/2016.
 *
 */
public class SQLiteNoteDAOImpl implements EntityDAO<Note> {
    private final DatabaseOpenHelper dbHelper;

    @Override
    public Object addEntity(Note note) {
        synchronized (dbHelper) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseSpec.NoteDB.FIELD_FIREBASE_KEY, note.getFirebaseId());
            values.put(DatabaseSpec.NoteDB.FIELD_TITLE, note.getTitle());
            values.put(DatabaseSpec.NoteDB.FIELD_COLOR, note.getColor());
            values.put(DatabaseSpec.NoteDB.FIELD_IS_DONE, note.isDone() ? 1 : 0);
            values.put(DatabaseSpec.NoteDB.FIELD_STORAGE_MODE, note.getStorageMode());
            values.put(DatabaseSpec.NoteDB.FIELD_LAST_MODIFIED, note.getLastModified());
            values.put(DatabaseSpec.NoteDB.FIELD_DELETED_TIME, note.getDeletedTime());

            int insertedId = (int) db.insert(DatabaseSpec.NoteDB.TABLE_NAME, null, values);
            if (insertedId == -1) {
                Log.e(Common.APPTAG, "SQLiteNoteDAOImpl - addEntity: Add note error: insertedId = -1");
            } else {
                note.setId(insertedId);

            }
            return insertedId;
        }
    }

    public SQLiteNoteDAOImpl(Context context) {
        dbHelper = DatabaseOpenHelper.getInstance(context);
    }

    @Override
    public int addEntities(ArrayList<Note> entities) {
        synchronized (dbHelper) {
            int count = 0;

            for (Note note : entities) {
                if (((Integer)addEntity(note)) >= 0) {
                    count++;
                }
            }

            Log.e(Common.APPTAG, "SQLiteNoteDAOImpl - addEntities: added " + count + " notes");
            return count;
        }
    }

    @Override
    public Note getEntity(Object id) {
        synchronized (dbHelper) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Note note = null;

            String[] projection = {
                    DatabaseSpec.NoteDB.FIELD_PKEY,
                    DatabaseSpec.NoteDB.FIELD_FIREBASE_KEY,
                    DatabaseSpec.NoteDB.FIELD_TITLE,
                    DatabaseSpec.NoteDB.FIELD_COLOR,
                    DatabaseSpec.NoteDB.FIELD_IS_DONE,
                    DatabaseSpec.NoteDB.FIELD_STORAGE_MODE,
                    DatabaseSpec.NoteDB.FIELD_LAST_MODIFIED,
                    DatabaseSpec.NoteDB.FIELD_DELETED_TIME
            };
            String selection = DatabaseSpec.NoteDB.FIELD_PKEY + " = ?";
            String[] selectionArgs = {
                    String.valueOf(id)
            };

            Cursor cursor = db.query(DatabaseSpec.NoteDB.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                note = new Note(cursor.getInt(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_PKEY)),
                                cursor.getString(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_FIREBASE_KEY)),
                                cursor.getString(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_TITLE)),
                                cursor.getInt(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_COLOR)),
                                cursor.getInt(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_IS_DONE)) > 0,
                                cursor.getInt(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_STORAGE_MODE)),
                                cursor.getLong(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_LAST_MODIFIED)),
                                cursor.getLong(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_DELETED_TIME)));

                cursor.close();
            }
            return note;
        }
    }

    @Override
    public ArrayList<Note> getAllEntities(@Nullable String column, Object value) {
        synchronized (dbHelper) {
            ArrayList<Note> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String[] projection = {
                    DatabaseSpec.NoteDB.FIELD_PKEY,
                    DatabaseSpec.NoteDB.FIELD_FIREBASE_KEY,
                    DatabaseSpec.NoteDB.FIELD_TITLE,
                    DatabaseSpec.NoteDB.FIELD_COLOR,
                    DatabaseSpec.NoteDB.FIELD_IS_DONE,
                    DatabaseSpec.NoteDB.FIELD_STORAGE_MODE,
                    DatabaseSpec.NoteDB.FIELD_LAST_MODIFIED,
                    DatabaseSpec.NoteDB.FIELD_DELETED_TIME
            };

            String selection = null;
            String[] selectionArgs = null;
            String orderBy = DatabaseSpec.NoteDB.FIELD_LAST_MODIFIED + " DESC";

            if (column != null) {
                if (column.equals(DatabaseSpec.NoteDB.FIELD_STORAGE_MODE)) {
                    selection = DatabaseSpec.NoteDB.FIELD_STORAGE_MODE + " = ?";
                } else if (column.equals(DatabaseSpec.NoteDB.FIELD_COLOR)) {
                    selection = DatabaseSpec.NoteDB.FIELD_COLOR + " = ?";
                } else if (column.equals(DatabaseSpec.NoteDB.FIELD_IS_DONE)) {
                    selection = DatabaseSpec.NoteDB.FIELD_IS_DONE + " = ?";
                }

                selectionArgs = new String[]{String.valueOf(value)};
            }


            Cursor cursor = db.query(DatabaseSpec.NoteDB.TABLE_NAME, projection, selection, selectionArgs, null, null, orderBy);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_PKEY));
                    String firebaseId = cursor.getString(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_FIREBASE_KEY));
                    String title = cursor.getString(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_TITLE));
                    int color = cursor.getInt(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_COLOR));
                    boolean isDone = cursor.getInt(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_IS_DONE)) > 0;
                    int storageMode = cursor.getInt(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_STORAGE_MODE));
                    long lastModified = cursor.getLong(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_LAST_MODIFIED));
                    long deletedTime = cursor.getLong(cursor.getColumnIndex(DatabaseSpec.NoteDB.FIELD_DELETED_TIME));

                    list.add(new Note(id, firebaseId, title, color, isDone, storageMode, lastModified, deletedTime));
                } while (cursor.moveToNext());

                cursor.close();
            }

            return list;
        }
    }

    @Override
    public void updateEntity(Note note) {
        synchronized (dbHelper) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseSpec.NoteDB.FIELD_FIREBASE_KEY, note.getFirebaseId());
            values.put(DatabaseSpec.NoteDB.FIELD_TITLE, note.getTitle());
            values.put(DatabaseSpec.NoteDB.FIELD_COLOR, note.getColor());
            values.put(DatabaseSpec.NoteDB.FIELD_IS_DONE, note.isDone() ? 1 : 0);
            values.put(DatabaseSpec.NoteDB.FIELD_STORAGE_MODE, note.getStorageMode());
            values.put(DatabaseSpec.NoteDB.FIELD_LAST_MODIFIED, note.getLastModified());
            values.put(DatabaseSpec.NoteDB.FIELD_DELETED_TIME, note.getDeletedTime());

            String selection;
            if (note.getFirebaseId().equals("")) {
                selection = DatabaseSpec.NoteDB.FIELD_PKEY + " = ?";
            } else {
                selection = DatabaseSpec.NoteDB.FIELD_FIREBASE_KEY + " = ?";
            }

            String[] selectionArgs =
                    { String.valueOf(note.getFirebaseId().equals("") ? note.getId() : note.getFirebaseId()) };

            db.update(DatabaseSpec.NoteDB.TABLE_NAME, values, selection, selectionArgs);
        }
    }

    @Override
    public void deleteEntity(Note note) {
        synchronized (dbHelper) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String selection;
            if (note.getFirebaseId().equals("")) {
                selection = DatabaseSpec.NoteDB.FIELD_PKEY + " = ?";
            } else {
                selection = DatabaseSpec.NoteDB.FIELD_FIREBASE_KEY + " = ?";
            }
            String[] selectionArgs =
                    { String.valueOf(note.getFirebaseId().equals("") ? note.getId() : note.getFirebaseId()) };

            db.delete(DatabaseSpec.NoteDB.TABLE_NAME, selection, selectionArgs);
        }
    }

    @SuppressWarnings("unused")
    public ArrayList<Note> getAllNotesByColor(int color) {
        return getAllEntities(DatabaseSpec.NoteDB.FIELD_COLOR, color);
    }

    @SuppressWarnings("unused")
    public ArrayList<Note> getAllNotesByIsDone(boolean isDone) {
        return getAllEntities(DatabaseSpec.NoteDB.FIELD_IS_DONE, isDone ? 1 : 0);
    }

    public void deleteAllNoteData() {
        synchronized (dbHelper) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            db.delete(DatabaseSpec.NoteDB.TABLE_NAME, null, null);
        }
    }
}
