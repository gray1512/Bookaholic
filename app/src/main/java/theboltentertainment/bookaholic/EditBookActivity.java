package theboltentertainment.bookaholic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static theboltentertainment.bookaholic.EditActivity.EDIT_ACTIVITY;
import static theboltentertainment.bookaholic.EditActivity.REQUEST_COVER_PICTURE;
import static theboltentertainment.bookaholic.EditActivity.REQUEST_CUSTOM_PICTURE;
import static theboltentertainment.bookaholic.MainActivity.PATH_FOR_GALERY;
import static theboltentertainment.bookaholic.MainActivity.PICTURE_PATH;

public class EditBookActivity extends AppCompatActivity {
    private BookClass book;
    private String bookCover = QuoteClass.DEFAULT_COVER;
    private String newBookCover = QuoteClass.DEFAULT_COVER;

    private ImageView editCover;
    private TextView displayTitle;
    private EditText editTitle;
    private EditText editAuthor;
    private EditText editContent;
    private EditText editType;
    private EditText editYear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        book = (BookClass) getIntent().getSerializableExtra(BookDetailActivity.BOOK);
        bookCover = book.get_cover();

        editCover = findViewById(R.id.edit_book_cover);
        displayTitle = findViewById(R.id.display_book_title);
        editTitle = findViewById(R.id.edit_book_title);
        editAuthor = findViewById(R.id.edit_book_author);
        editContent = findViewById(R.id.edit_book_content);
        editType = findViewById(R.id.edit_book_type);
        editYear = findViewById(R.id.edit_book_year);

        if (new File(bookCover).exists()) {
            displayTitle.setVisibility(View.INVISIBLE);
            Picasso.with(getBaseContext()).load(new File(bookCover)).resize(editCover.getLayoutParams().width,
                                                                            editCover.getLayoutParams().height)
                    .centerInside().into(editCover);
        }

        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                displayTitle.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editTitle.setText(book.get_title());
        editAuthor.setText((book.get_author().equals("Unknown")) ? "" : book.get_author());
        editContent.setText((book.get_content().equals("Unknown")) ? "" : book.get_content());
        editType.setText((book.get_type().equals("Unknown")) ? "" : book.get_type());
        editYear.setText((book.get_year().equals("Unknown")) ? "" : book.get_year());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);
        menu.findItem(R.id.choose_done).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_done:
                BookClass updateBook = getUpdateBook();
                SQLDatabase db = new SQLDatabase(getBaseContext());
                db.updateBookData(book, updateBook);
                db.close();

                Intent i = new Intent(this, BookDetailActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.putExtra(BookDetailActivity.BOOK, updateBook);
                startActivity(i);
                return true;
        }
        return false;
    }

    private BookClass getUpdateBook() {
        String title, content, cover, author, type, year;
        title = editTitle.getText().toString();
        content = editContent.getText().toString();
        cover = bookCover;
        author = editAuthor.getText().toString();
        type = editType.getText().toString();

        year = editYear.getText().toString();
        String[] data = new String[] {title, content, cover, author, type, year};
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null || data[i].equals("")) {
                data[i] = "Unknown";
            }
        }

        return new BookClass(data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_COVER_PICTURE && resultCode == RESULT_OK) {
            String path = (new File(newBookCover).exists()) ? newBookCover : data.getStringExtra(EditActivity.BOOK_COVER);
            newBookCover = QuoteClass.DEFAULT_COVER;
            Intent intent = new Intent(getBaseContext(), CustomImageActivity.class);
            intent.putExtra(CustomImageActivity.IMAGE_PATH, path);
            startActivityForResult(intent, REQUEST_CUSTOM_PICTURE);
        }

        if (requestCode == REQUEST_CUSTOM_PICTURE && resultCode == RESULT_OK) {
            bookCover = data.getStringExtra(CustomImageActivity.IMAGE_PATH);
            displayTitle.setVisibility(View.INVISIBLE);
            Picasso.with(getBaseContext()).load(new File(bookCover)).resize(editCover.getWidth(), editCover.getHeight())
                    .centerInside().into(editCover);
        }

    }

    public void takeNewCover(View v) {
        String fileUniqueName= new SimpleDateFormat("yyMMdd_HHss").format(new Date());
        String imageFileName = fileUniqueName + ".jpg";

        newBookCover = PICTURE_PATH + imageFileName;
        File file = new File(newBookCover);

        Uri outputUri;
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            outputUri = FileProvider.getUriForFile(getBaseContext(), "com.bookaholic.fileProvider", file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        } else {
            outputUri = Uri.fromFile(file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        }
        startActivityForResult(cameraIntent, REQUEST_COVER_PICTURE);
    }

    public void deleteCover (View v) {
        try {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup, null);

            final PopupWindow pw = new PopupWindow(layout, 300, 200, true);
            pw.setOutsideTouchable(true);
            pw.setFocusable(true);
            pw.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_frame));
            pw.showAtLocation(v.getRootView(), Gravity.CENTER, 0, 0);

            Button yesBtn = layout.findViewById(R.id.yes_btn);
            Button noBtn = layout.findViewById(R.id.no_btn);
            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bookCover = QuoteClass.DEFAULT_COVER;
                    editCover.setImageResource(R.drawable.book_cover);
                    displayTitle.setVisibility(View.VISIBLE);
                    pw.dismiss();
                }
            });
            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pw.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeCover (View v) {
        Intent intent = new Intent(this, GaleryActivity.class);
        intent.putExtra(PATH_FOR_GALERY, PICTURE_PATH);
        intent.putExtra(MainActivity.ACTIVITY, EDIT_ACTIVITY);
        startActivityForResult(intent, REQUEST_COVER_PICTURE);
    }



}
