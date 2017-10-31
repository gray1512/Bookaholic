package theboltentertainment.bookaholic;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class BookClass  implements Serializable {
    // title, content, cover, author, type, year, quotes
    private String _title;
    private String _content;
    private String _cover;
    private String _author;
    private String _type;
    private String _year;

    public BookClass(String[] dataList) {
        this._title = dataList[0];
        this._content = dataList[1];
        this._cover = dataList[2];
        this._author = dataList[3];
        this._type = dataList[4];
        this._year = dataList[5];
    }


    public String get_title() {
        return this._title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public String get_cover() {
        return this._cover;
    }

    public void set_cover(String _cover) {
        this._cover = _cover;
    }

    public String get_author() {
        return this._author;
    }

    public String get_type() {
        return this._type;
    }

    public String get_year() {
        return this._year;
    }

    public String get_content() {
        return this._content;
    }

    public void set_content(String _content) {
        this._content = _content;
    }

    public void set_author(String _author) {
        this._author = _author;
    }

    public void set_type(String _type) {
        this._type = _type;
    }

    public void set_year(String _year) {
        this._year = _year;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        try {
            BookClass obj = (BookClass) o;
            return this._title.equals(obj.get_title()) && this._content.equals(obj.get_content()) && this._cover.equals(obj.get_cover()) &&
                    this._author.equals(obj.get_author()) && this._type.equals(obj.get_type()) && this._year.equals(obj.get_year());

        } catch (Exception e ) {
            Log.d("Exception while compare", e.toString());
        }
        return super.equals(o);
    }
}
