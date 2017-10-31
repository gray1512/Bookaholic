package theboltentertainment.bookaholic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static theboltentertainment.bookaholic.MainActivity.PATH_FOR_GALERY;
import static theboltentertainment.bookaholic.MainActivity.PICTURE_PATH;

public class EditActivity extends AppCompatActivity {
    private  String activity;
    public static String EDIT_ACTIVITY = "Edit Activity";

    private ConstraintLayout detailsBtn;
    private TextView stt;
    private ImageButton arrow;

    private ConstraintLayout detailsLayout;
    private TextView tip;

    private EditText editQuote;
    private EditText editAuthor;
    private EditText editTitle;
    private EditText editContent;
    private EditText editType;
    private EditText editTime;

    private ImageView editCover;
    private TextView displayTitle;

    private String bookCover = QuoteClass.DEFAULT_COVER;
    private final int REQUEST_COVER_PICTURE = 115;
    private final int REQUEST_CUSTOM_PICTURE = 116;

    public static final String BOOK_COVER = "New book cover from galery activity";

    private QuoteClass edittedQuote;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        setTitle("Edit your quote");

        detailsBtn = (ConstraintLayout) findViewById(R.id.advance_content);
        stt = (TextView) detailsBtn.findViewById(R.id.detail_stt);
        arrow = (ImageButton) detailsBtn.findViewById(R.id.arrow_stt);

        detailsLayout = (ConstraintLayout) findViewById(R.id.more_detail_layout);
        tip = (TextView) findViewById(R.id.edit_tip);

        detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdvanceDetails();
            }
        });

        editQuote = (EditText) findViewById(R.id.edit_quote);
        editAuthor = (EditText) findViewById(R.id.edit_author);
        editTitle = (EditText) findViewById(R.id.edit_title);
        editContent = (EditText) findViewById(R.id.edit_content);
        editType = (EditText) findViewById(R.id.edit_type);
        editTime = (EditText) findViewById(R.id.edit_time);

        editCover = (ImageView) findViewById(R.id.book_cover);
        displayTitle = (TextView) findViewById(R.id.book_cover_title);

        activity = getIntent().getStringExtra(MainActivity.ACTIVITY);
        if (activity.equals(MainActivity.MAIN_ACTIVITY) || activity.equals(BookDetailActivity.BOOK_DETAIL_ACTIVITY)) {
            edittedQuote = (QuoteClass) getIntent().getSerializableExtra(MainActivity.QUOTE_EDIT);

            editQuote.setText(edittedQuote.get_quote());
            editAuthor.setText(edittedQuote.get_author().equals("Unknown") ? "" : edittedQuote.get_author());
            editTitle.setText(edittedQuote.get_title().equals("Unknown") ? "" : edittedQuote.get_title());
            editContent.setText(edittedQuote.get_content().equals("Unknown") ? "" : edittedQuote.get_content());
            editType.setText(edittedQuote.get_type().equals("Unknown") ? "" : edittedQuote.get_type());
            editTime.setText(edittedQuote.get_year().equals("Unknown") ? "" : edittedQuote.get_year());
            if (new File(edittedQuote.get_cover()).exists()) {
                Picasso.with(getBaseContext()).load(new File(edittedQuote.get_cover())).resize(editCover.getLayoutParams().width,
                                                    editCover.getLayoutParams().height).centerInside().into(editCover);
                displayTitle.setVisibility(View.GONE);
            }

        } else if (activity.equals(DisplayOCRActivity.DISPLAY_ACTIVITY)) {
            String quote = getIntent().getStringExtra(DisplayOCRActivity.OCR_RESULT);
            editQuote.setText(quote, TextView.BufferType.EDITABLE);

            BookClass book = (BookClass) getIntent().getSerializableExtra(BookDetailActivity.BOOK);
            if (book != null) {
                editAuthor.setText(book.get_author());
                editTitle.setText(book.get_title());
                editContent.setText(book.get_content());
                editType.setText(book.get_type());
                editTime.setText(book.get_year());
            }
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
                if (activity.equals(DisplayOCRActivity.DISPLAY_ACTIVITY)) {
                    QuoteClass newQuote = getNewQuote();
                    setResult(RESULT_OK, getIntent().putExtra(DisplayOCRActivity.OCR_RESULT, newQuote));
                    finish();
                }

                if (activity.equals(MainActivity.MAIN_ACTIVITY)) {
                    SQLDatabase db = new SQLDatabase(getBaseContext());
                    db.updateData(getUpdateQuote());
                    db.close();

                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                }

                if (activity.equals(BookDetailActivity.BOOK_DETAIL_ACTIVITY)) {
                    QuoteClass quote = getUpdateQuote();
                    SQLDatabase db = new SQLDatabase(getBaseContext());
                    db.updateData(quote);
                    db.close();

                    Intent i = new Intent(this, BookDetailActivity.class);
                    i.putExtra(BookDetailActivity.BOOK, quote.get_book());
                    i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);
                }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_COVER_PICTURE && resultCode == RESULT_OK) {
            String path = (new File(bookCover).exists()) ? bookCover : data.getStringExtra(EditActivity.BOOK_COVER);
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
        // create a collision-resistant file name

        String fileUniqueName= new SimpleDateFormat("yyMMdd_HHss").format(new Date());
        String imageFileName = fileUniqueName + ".jpg";

        bookCover = PICTURE_PATH + imageFileName;
        File file = new File(bookCover);

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

    public void changeCover (View v) {
        Intent intent = new Intent(this, GaleryActivity.class);
        intent.putExtra(PATH_FOR_GALERY, PICTURE_PATH);
        intent.putExtra(MainActivity.ACTIVITY, EDIT_ACTIVITY);
        startActivityForResult(intent, REQUEST_COVER_PICTURE);
    }

    private QuoteClass getUpdateQuote() {
        String quote    = editQuote.getText().toString();
        String time     = edittedQuote.get_time();
        String title    = editTitle.getText().toString();
        String content  = editContent.getText().toString();
        String cover    = bookCover;
        if (cover.equals(QuoteClass.DEFAULT_COVER)) {
            cover = title;
        }
        String author   = editAuthor.getText().toString();
        String type     = editType.getText().toString();
        String year     = editTime.getText().toString();

        // Create new Quote:                   quote  time  title  content  cover  author  type  year
        String[] newQuoteArgs =  new String[] {quote, time, title, content, cover, author, type, year};
        for (int i = 0; i < newQuoteArgs.length; i++) {
            if (newQuoteArgs[i] == null || newQuoteArgs[i].equals("")) {
                newQuoteArgs[i] = "Unknown";
            }
        }


        return new QuoteClass(newQuoteArgs);
    }

    private QuoteClass getNewQuote() {
        String quote    = editQuote.getText().toString();
        String time     = new SimpleDateFormat("yyyy/MM/dd HH:ss").format(new Date());
        String title    = editTitle.getText().toString();
        String content  = editContent.getText().toString();
        String cover    = bookCover;
        if (cover.equals(QuoteClass.DEFAULT_COVER)) {
            cover = title;
        }
        String author   = editAuthor.getText().toString();
        String type     = editType.getText().toString();
        String year     = editTime.getText().toString();

        // Create new Quote:                   quote  time  title  content  cover  author  type  year
        String[] newQuoteArgs =  new String[] {quote, time, title, content, cover, author, type, year};
        for (int i = 0; i < newQuoteArgs.length; i++) {
            if (newQuoteArgs[i] == null || newQuoteArgs[i].equals("")) {
                newQuoteArgs[i] = "Unknown";
            }
        }
        return new QuoteClass(newQuoteArgs);
    }

    private void showAdvanceDetails() {
        stt.setText("Fewer Details");
        arrow.animate().rotation(arrow.getRotation() + 180)
                        .setDuration(500);

        tip.setVisibility(View.INVISIBLE);
        detailsLayout.setVisibility(View.VISIBLE);

        handler.post(new Runnable() {
            @Override
            public void run() {
                detailsLayout.animate()
                        .alpha(1.0f)
                        .setDuration(500);
            }
        });

        detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAdvanceDetails();
            }
        });
    }

    private void hideAdvanceDetails() {
        stt.setText("More Details");

        arrow.animate().rotation(arrow.getRotation() + 180)
                        .setDuration(500);
        tip.setVisibility(View.VISIBLE);

        handler.post(new Runnable() {
            @Override
            public void run() {
                detailsLayout.animate()
                        .alpha(0.0f)
                        .setDuration(500);
            }
        });

        detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdvanceDetails();
            }
        });
    }
}
