package theboltentertainment.bookaholic;

/*
  SQL Database for data
       ID  |   Quote   |    Time create  |    Title   |   Content  |    Cover   |   Author   |   Type   |
*/

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;


class SQLDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME       = "Bookaholic.db";

    private static final String BOOK_TABLE_NAME     = "QuoteData";
    private static final String BOOK_COLUMN_ID      = "id";
    private static final String BOOK_COLUMN_QUOTE   = "quote";
    private static final String BOOK_COLUMN_TIME    = "time";
    private static final String BOOK_COLUMN_TITLE   = "title";
    private static final String BOOK_COLUMN_CONTENT = "content";
    private static final String BOOK_COLUMN_COVER   = "cover";
    private static final String BOOK_COLUMN_AUTHOR  = "author";
    private static final String BOOK_COLUMN_TYPE    = "type";
    private static final String BOOK_COLUMN_YEAR    = "year";


    private final String[] DATALIST = { BOOK_COLUMN_QUOTE, BOOK_COLUMN_TIME,
                                        BOOK_COLUMN_TITLE, BOOK_COLUMN_CONTENT, BOOK_COLUMN_COVER,
                                        BOOK_COLUMN_AUTHOR, BOOK_COLUMN_TYPE, BOOK_COLUMN_YEAR };


    SQLDatabase(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "CREATE TABLE " + BOOK_TABLE_NAME + "(" +
                        BOOK_COLUMN_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        BOOK_COLUMN_QUOTE   + " TEXT," +
                        BOOK_COLUMN_TIME    + " TEXT," +
                        BOOK_COLUMN_TITLE   + " TEXT," +
                        BOOK_COLUMN_CONTENT + " TEXT," +
                        BOOK_COLUMN_COVER   + " TEXT," +
                        BOOK_COLUMN_AUTHOR  + " TEXT," +
                        BOOK_COLUMN_TYPE    + " TEXT," +
                        BOOK_COLUMN_YEAR    + " TEXT)" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BOOK_TABLE_NAME);
        this.onCreate(db);
    }

    void insertData(String[] dataList) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        for (int i = 0; i < dataList.length; i++) {
            contentValues.put(DATALIST[i], dataList[i]);
        }
        db.insert(BOOK_TABLE_NAME, null, contentValues);
    }

    void updateData(QuoteClass quote) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        Cursor res =  db.rawQuery( "SELECT * FROM " + BOOK_TABLE_NAME, null );

        int id = -1;
        res.moveToFirst();
        while(!res.isAfterLast()){
            if (quote.get_time().equals(res.getString(res.getColumnIndex(BOOK_COLUMN_TIME)))) {
                id  = res.getInt(res.getColumnIndex(BOOK_COLUMN_ID));
                break;
            }
            res.moveToNext();
        }
        res.close();

        String[] dataList = quote.get_dataList();
        for (int i = 0; i < dataList.length; i++) {
            contentValues.put(DATALIST[i], dataList[i]);
        }

        db = this.getWritableDatabase();
        db.update(BOOK_TABLE_NAME, contentValues, "id = ?", new String[] { String.valueOf(id) } );
        Log.e("SQLDatabase Update", "Success");
    }

    void updateBookData(BookClass oldBook, BookClass newBook) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        Cursor res =  db.rawQuery( "SELECT * FROM " + BOOK_TABLE_NAME, null );

        int id = -1;
        String quote, time, title, content, cover, author, type, year;
        res.moveToFirst();
        while(!res.isAfterLast()){
            if (oldBook.get_title().equals(res.getString(res.getColumnIndex(BOOK_COLUMN_TITLE))) &&
                    oldBook.get_content().equals(res.getString(res.getColumnIndex(BOOK_COLUMN_CONTENT))) &&
                    oldBook.get_cover().equals(res.getString(res.getColumnIndex(BOOK_COLUMN_COVER))) &&
                    oldBook.get_author().equals(res.getString(res.getColumnIndex(BOOK_COLUMN_AUTHOR))) &&
                    oldBook.get_type().equals(res.getString(res.getColumnIndex(BOOK_COLUMN_TYPE))) &&
                    oldBook.get_year().equals(res.getString(res.getColumnIndex(BOOK_COLUMN_YEAR)))) {
                id = res.getInt(res.getColumnIndex(BOOK_COLUMN_ID));
                quote   = res.getString(res.getColumnIndex(BOOK_COLUMN_QUOTE));
                time    = res.getString(res.getColumnIndex(BOOK_COLUMN_TIME));
                title   = newBook.get_title();
                content = newBook.get_content();
                cover   = newBook.get_cover();
                author  = newBook.get_author();
                type    = newBook.get_type();
                year    = newBook.get_year();

                String[] dataList = new String[]{quote, time, title, content, cover, author, type, year};
                for (int i = 0; i < dataList.length; i++) {
                    contentValues.put(DATALIST[i], dataList[i]);
                }

                db = this.getWritableDatabase();
                db.update(BOOK_TABLE_NAME, contentValues, "id = ?", new String[] { String.valueOf(id) } );
            }
            res.moveToNext();
        }
        res.close();
        Log.e("SQLDatabase Update", "Success");
    }

    void deleteQuote(QuoteClass delQuote) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + BOOK_TABLE_NAME, null );

        res.moveToFirst();
        int id = -1;
        String time;

        while(!res.isAfterLast()){
            time = res.getString(res.getColumnIndex(BOOK_COLUMN_TIME));

            if (delQuote.get_time().equals(time)) {
                id = res.getInt(res.getColumnIndex(BOOK_COLUMN_ID));
                Log.e("Delete", ":" + id);
                break;
            }
            res.moveToNext();
        }
        res.close();
        db.delete(BOOK_TABLE_NAME, "id = ? ", new String[] { Integer.toString(id)});
    }

    ArrayList<QuoteClass> getAllBookData () {
        String quote, time, title, content, cover, author, type, year;
        ArrayList<QuoteClass> quote_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + BOOK_TABLE_NAME, null );

        res.moveToFirst();

        while(!res.isAfterLast()){
            quote   = res.getString(res.getColumnIndex(BOOK_COLUMN_QUOTE));
            time    = res.getString(res.getColumnIndex(BOOK_COLUMN_TIME));
            title   = res.getString(res.getColumnIndex(BOOK_COLUMN_TITLE));
            content = res.getString(res.getColumnIndex(BOOK_COLUMN_CONTENT));
            cover   = res.getString(res.getColumnIndex(BOOK_COLUMN_COVER));
            author  = res.getString(res.getColumnIndex(BOOK_COLUMN_AUTHOR));
            type    = res.getString(res.getColumnIndex(BOOK_COLUMN_TYPE));
            year    = res.getString(res.getColumnIndex(BOOK_COLUMN_YEAR));

            quote_list.add(new QuoteClass(new String[]{quote, time, title, content, cover, author, type, year}));
            res.moveToNext();
        }

        res.close();
        Collections.reverse(quote_list);
        return quote_list;
    }

    ArrayList<BookClass> getAllBook () {
        String title, content, cover, author, type, year;
        ArrayList<BookClass> allBooks = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + BOOK_TABLE_NAME, null );

        res.moveToFirst();

        BookClass newBook;
        while(!res.isAfterLast()){
            title   = res.getString(res.getColumnIndex(BOOK_COLUMN_TITLE));
            content = res.getString(res.getColumnIndex(BOOK_COLUMN_CONTENT));
            cover   = res.getString(res.getColumnIndex(BOOK_COLUMN_COVER));
            author  = res.getString(res.getColumnIndex(BOOK_COLUMN_AUTHOR));
            type    = res.getString(res.getColumnIndex(BOOK_COLUMN_TYPE));
            year    = res.getString(res.getColumnIndex(BOOK_COLUMN_YEAR));

            newBook = new BookClass(new String[]{title, content, cover, author, type, year});
            if (!allBooks.contains(newBook)) allBooks.add(newBook);

            res.moveToNext();
        }

        res.close();
        Collections.reverse(allBooks);
        return allBooks;
    }

    ArrayList<QuoteClass> getBookQuotes (BookClass book) {
        String quote, time, title, content, cover, author, type, year;
        ArrayList<QuoteClass> quote_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + BOOK_TABLE_NAME, null );

        res.moveToFirst();

        while(!res.isAfterLast()){
            quote   = res.getString(res.getColumnIndex(BOOK_COLUMN_QUOTE));
            time    = res.getString(res.getColumnIndex(BOOK_COLUMN_TIME));
            title   = res.getString(res.getColumnIndex(BOOK_COLUMN_TITLE));
            content = res.getString(res.getColumnIndex(BOOK_COLUMN_CONTENT));
            cover   = res.getString(res.getColumnIndex(BOOK_COLUMN_COVER));
            author  = res.getString(res.getColumnIndex(BOOK_COLUMN_AUTHOR));
            type    = res.getString(res.getColumnIndex(BOOK_COLUMN_TYPE));
            year    = res.getString(res.getColumnIndex(BOOK_COLUMN_YEAR));

            if (book.get_title().equals(title) && book.get_content().equals(content) && book.get_cover().equals(cover)
                    && book.get_author().equals(author) && book.get_type().equals(type) && book.get_year().equals(year)) {
                quote_list.add(new QuoteClass(new String[]{quote, time, title, content, cover, author, type, year}));
            }
            res.moveToNext();
        }

        res.close();
        Collections.reverse(quote_list);
        return quote_list;
    }
}

