package theboltentertainment.bookaholic;

import android.os.Parcelable;

import java.io.Serializable;

public class QuoteClass implements Serializable {
    private String[] _dataList;
    private String _quote;
    private String _time;
    private BookClass _book;

    public static final String DEFAULT_COVER = "Use default cover for book";

    QuoteClass (String[] data_list) {
        this._dataList = data_list;
        this._quote   = data_list[0];
        this._time    = data_list[1];
        this._book = new BookClass(new String[] {data_list[2], data_list[3], data_list[4], data_list[5], data_list[6], data_list[7]});
    }

    public String getShareText() {
        return this._quote + "\n - " + this._book.get_author() + " (" + this._book.get_title() + ")";
    }

    public BookClass get_book() {
        return this._book;
    }


    public String get_quote() {
        return _quote;
    }

    public void set_quote(String _quote) {
        this._quote = _quote;
    }

    public String get_time() {
        return _time;
    }

    public String get_date() {
        return _time.substring(0, 9);
    }

    public void set_time(String _time) {
        this._time = _time;
    }

    public String get_title() {
        return _book.get_title();
    }

    public void set_title(String _title) {
        this._book.set_title(_title);
    }

    public String get_content() {
        return _book.get_content();
    }

    public void set_content(String _content) {
        this._book.set_content(_content);
    }

    public String get_cover() {
        return _book.get_cover();
    }

    public void set_cover(String _cover) {
        this._book.set_cover(_cover);
    }

    public String get_author() {
        return _book.get_author();
    }

    public void set_author(String _author) {
        this._book.set_author(_author);
    }

    public String get_type() {
        return this._book.get_type();
    }

    public void set_type(String _type) {
        this._book.set_type(_type);
    }

    public String get_year() {
        return _book.get_year();
    }

    public void set_year(String _year) {
        this._book.set_year(_year);
    }

    public String[] get_dataList() {
        return _dataList;
    }
}
