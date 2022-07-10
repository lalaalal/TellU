package com.letmeinform.tellu;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 3;
    public static final String DB_NAME = "products.db";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE Product (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, deadline DATETIME)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Product");
        onCreate(sqLiteDatabase);
    }

    public void addProduct(String name, Date date) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL(String.format(Locale.ENGLISH, "INSERT INTO Product (name, deadline) VALUES ('%s', '%tF %tT')", name, date, date));
        sqLiteDatabase.close();
    }

    public ArrayList<Product> getProducts() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM Product", null);

        ArrayList<Product> products = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = getID(cursor);
            String name = getName(cursor);
            Date date = getDeadline(cursor);
            products.add(new Product(id, name, date));
        }

        cursor.close();
        sqLiteDatabase.close();

        return products;
    }

    public void deleteProduct(int id) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM Product WHERE id=" + id);
        sqLiteDatabase.close();
    }

    public void clearProducts() {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM Product");
        sqLiteDatabase.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='Product'");
        sqLiteDatabase.close();
    }

    private int getID(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex("id");

        return cursor.getInt(columnIndex);
    }

    private String getName(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex("name");

        return cursor.getString(columnIndex);
    }

    private Date getDeadline(Cursor cursor) {
        try {
            int columnIndex = cursor.getColumnIndex("deadline");
            String dateString = cursor.getString(columnIndex);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

            return format.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
