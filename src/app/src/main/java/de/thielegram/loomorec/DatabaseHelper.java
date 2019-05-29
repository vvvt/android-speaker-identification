/*
    Based on the SQLite database example, provided by Mitch Tabian {@link https://github.com/mitchtabian/SaveReadWriteDeleteSQLite}
 */

package de.thielegram.loomorec;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "speakers";
    private static final String COL1 = "id";
    private static final String COL2 = "name";
    private static final String COL3 = "display";

    DatabaseHelper(Context ctx) {
        super(ctx, TABLE_NAME + ".db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL2 + " TEXT, "
                + COL3 + " TEXT)";
        db.execSQL(createTable);
        Log.d(TAG, "Database " + TABLE_NAME + " created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void addData(String displayName, String fileName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, fileName);
        contentValues.put(COL3, displayName);

        Log.d(TAG, "Adding " + displayName + " (" + fileName + ") to the database");

        long result = db.insert(TABLE_NAME, null, contentValues);

    }

    HashMap<String, String> getSpeakers() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        HashMap<String, String> output = new HashMap<>();

        while(data.moveToNext()){
            output.put(data.getString(1), data.getString(2));
            Log.d(TAG, "Read " + data.getString(1) + " and " + data.getString(2) + " from the database");
        }

        data.close();

        return output;
    }

    void removeData(String displayName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL3 + " = '" + displayName + "'";
        db.execSQL(query);

        Log.d(TAG, "Removed " + displayName + " from the database");
    }
}
