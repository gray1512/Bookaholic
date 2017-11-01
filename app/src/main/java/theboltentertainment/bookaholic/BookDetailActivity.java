package theboltentertainment.bookaholic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static theboltentertainment.bookaholic.MainActivity.ACTIVITY;
import static theboltentertainment.bookaholic.MainActivity.OCR_BITMAP;
import static theboltentertainment.bookaholic.MainActivity.PATH_FOR_GALERY;
import static theboltentertainment.bookaholic.MainActivity.PICTURE_PATH;
import static theboltentertainment.bookaholic.MainActivity.REQUEST_OCR_PICTURE;
import static theboltentertainment.bookaholic.MainActivity.REQUEST_OCR_RESULT;

public class BookDetailActivity extends AppCompatActivity {
    private SQLDatabase db;

    public static final String BOOK_DETAIL_ACTIVITY = "Book detail activity";
    public static final String BOOK = "Book Item";

    private BookClass book;
    private ArrayList<QuoteClass> bookQuotesList = new ArrayList<>();
    private String imagePath;

    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout toolbarLayout;
    private MenuItem edit;
    private MenuItem delete;

    private FloatingActionButton fab;
    private FloatingActionButton takepicFab;
    private FloatingActionButton galeryFab;

    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBarLayout = findViewById(R.id.app_bar);
        toolbarLayout = findViewById(R.id.toolbar_layout);

        db = new SQLDatabase(getBaseContext());

        book = (BookClass) getIntent().getSerializableExtra(BOOK);
        String title = book.get_title();
        setTitle(title);

        String cover = book.get_cover();
        if (new File(cover).exists()) {
            Drawable coverDrawable = new BitmapDrawable(getResources(), cover);
            toolbarLayout.setBackground(coverDrawable);
        }

        fab = (FloatingActionButton) findViewById(R.id.book_scan_btn);
        takepicFab = (FloatingActionButton) findViewById(R.id.book_takepic_btn);
        galeryFab = (FloatingActionButton) findViewById(R.id.book_galery_btn);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBtnAnimation();
            }
        });
        takepicFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTakepicFabClicked();
            }
        });
        galeryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), GaleryActivity.class);
                intent.putExtra(PATH_FOR_GALERY, PICTURE_PATH);
                intent.putExtra(BOOK, book);
                startActivityForResult(intent, REQUEST_OCR_RESULT);
            }
        });

        bookQuotesList = db.getBookQuotes(book);
        Log.e("Book Quotes", bookQuotesList.toString());

        recyclerView = (RecyclerView) findViewById(R.id.book_quotes_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        adapter = new RecyclerViewAdapter(bookQuotesList, getBaseContext());
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_book_detail, menu);
        edit = menu.findItem(R.id.action_edit);
        delete = menu.findItem(R.id.action_del);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (appBarLayout.getTotalScrollRange() + verticalOffset == 0) {
                    edit.setVisible(true);
                    delete.setVisible(true);
                } else {
                    edit.setVisible(false);
                    delete.setVisible(false);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_del: {
                areYouSure();
                return true;
            }

            case R.id.action_edit: {
                Intent intent = new Intent(getBaseContext(), EditBookActivity.class);
                intent.putExtra(BOOK, book);
                startActivity(intent);
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getBaseContext(), MainActivity.class).putExtra(MainActivity.ACTIVITY, BOOK_DETAIL_ACTIVITY));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OCR_PICTURE && resultCode == RESULT_OK) {
            checkForPictureSize();

            Intent intent = new Intent(getBaseContext(), DisplayOCRActivity.class);
            intent.putExtra(OCR_BITMAP, imagePath);
            intent.putExtra(BOOK, book);
            startActivityForResult(intent, REQUEST_OCR_RESULT);

        } else if (requestCode == REQUEST_OCR_RESULT && resultCode == RESULT_OK) {
            Log.e("Get OCR res BookDetail", "Success" + data.getSerializableExtra(DisplayOCRActivity.OCR_RESULT));
            QuoteClass newQuote = (QuoteClass) data.getSerializableExtra(DisplayOCRActivity.OCR_RESULT);

            if (newQuote.equals(book)) bookQuotesList.add(0, newQuote);
            db.insertData(newQuote.get_dataList());

            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void areYouSure() {
        try {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup, null);

            final PopupWindow pw = new PopupWindow(layout, 400, 300, true);
            pw.setOutsideTouchable(true);
            pw.setFocusable(true);
            pw.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_frame));
            pw.showAtLocation((View) recyclerView.getParent(), Gravity.CENTER, 0, 0);

            Button yesBtn = layout.findViewById(R.id.yes_btn);
            Button noBtn = layout.findViewById(R.id.no_btn);
            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SQLDatabase db = new SQLDatabase(getBaseContext());

                    for (QuoteClass quote : bookQuotesList) {
                        db.deleteQuote(quote);
                    }
                    db.close();
                    startActivity(new Intent(getBaseContext(), MainActivity.class).putExtra(MainActivity.ACTIVITY, BOOK_DETAIL_ACTIVITY));
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

    private void checkForPictureSize() {
        int MAX_WIDTH = dpToPx(350);
        int MAX_HEIGHT = dpToPx(400);
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        int width = bm.getWidth();
        int height = bm.getHeight();

        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            float ratio = (float) width / height;
            int newWidth, newHeight;
            if (ratio > 1) {
                newWidth = MAX_WIDTH;
                newHeight = (int) (newWidth / ratio);
            } else {
                newHeight = MAX_HEIGHT;
                newWidth = (int) (newHeight * ratio);
            }
            bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, false);
        }

        File imgFile = new File(imagePath);
        try {
            FileOutputStream fos = new FileOutputStream(imgFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Save Resized Bitmap", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Save Resized Bitmap", "Error accessing file: " + e.getMessage());
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void setTakepicFabClicked() {
        String fileUniqueName= new SimpleDateFormat("yymmdd_hhss").format(new Date());
        String imageFileName = fileUniqueName + ".jpg";

        String storageDir = PICTURE_PATH + "/Bookaholic Pics/";
        File dir = new File(storageDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("Prepare Dir", "ERROR: Creation of directory " + storageDir + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.e("Prepare Dir", "Created directory " + storageDir);
        }

        imagePath = storageDir + imageFileName;
        File file = new File(imagePath);
        Log.e("Create Image File", imagePath);

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
        startActivityForResult(cameraIntent, REQUEST_OCR_PICTURE);
    }

    private void showBtnAnimation() {
        takepicFab.setVisibility(View.VISIBLE);
        galeryFab.setVisibility(View.VISIBLE);

        takepicFab.animate().translationXBy(-100).translationYBy(100)
                .setDuration(500);
        galeryFab.animate().translationYBy(125)
                .setDuration(500);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBtnAnimation();
            }
        });
    }

    private void hideBtnAnimation() {
        takepicFab.animate().translationXBy(100).translationYBy(-100)
                .setDuration(500);
        galeryFab.animate().translationYBy(-125)
                .setDuration(500);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBtnAnimation();
            }
        });
    }
}
