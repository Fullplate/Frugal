package fullplate.frugal.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;

import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodicEntry;
import fullplate.frugal.domain.SingleEntry;

public class EntriesTableHandler {
    private SQLiteDatabase database;
    private DatabaseHelper helper;
    private String[] columns = {DatabaseHelper.COLUMN_TIMESTAMP,
                                DatabaseHelper.COLUMN_AMOUNT,
                                DatabaseHelper.COLUMN_DESCRIPTION};

    public EntriesTableHandler(Context context) {
        helper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    // todo: probably don't need the query part
    public Entry createEntry(Entry entry) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TIMESTAMP, entry.getTimestamp());
        values.put(DatabaseHelper.COLUMN_AMOUNT, entry.getAmount());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, entry.getDescription());

        long insertId = database.insert(DatabaseHelper.TABLE_ENTRIES, null, values);

        Cursor cursor = database.query(DatabaseHelper.TABLE_ENTRIES, columns,
                DatabaseHelper.COLUMN_TIMESTAMP + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();

        Entry newEntry = cursorToEntry(cursor);
        cursor.close();

        return newEntry;
    }

    public void deleteEntry(Entry entry) {
        long timestamp = entry.getTimestamp();

        System.out.println("Entry deleted with ts: "+timestamp);

        database.delete(DatabaseHelper.TABLE_ENTRIES, DatabaseHelper.COLUMN_TIMESTAMP + " = " + timestamp, null);
    }

    public void wipeTable() {
        database.execSQL("delete from "+helper.TABLE_ENTRIES);
    }

    public ArrayList<Entry> getAllEntries() {
        ArrayList<Entry> entries = new ArrayList<>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_ENTRIES, columns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Entry entry = cursorToEntry(cursor);
            entries.add(entry);
            cursor.moveToNext();
        }
        cursor.close();

        return entries;
    }

    private Entry cursorToEntry(Cursor cursor) {
        Long timestamp = cursor.getLong(0);

        if (timestamp == -1) {
            return new PeriodicEntry(cursor.getInt(1), cursor.getString(2));
        }
        else {
            return new SingleEntry(timestamp, cursor.getInt(1), cursor.getString(2));
        }
    }

}
