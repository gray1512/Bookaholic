package theboltentertainment.bookaholic;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class GaleryActivity extends AppCompatActivity {
    private String activity;

    private RecyclerView recyclerView;

    protected static TextView actionBarTitle;
    protected static ImageButton actionBarBack;
    protected static ImageButton actionBarDone;

    private File parentFile;

    private Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_galery);

        activity = getIntent().getStringExtra(MainActivity.ACTIVITY);

        recyclerView = (RecyclerView) findViewById(R.id.galery_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, calculateDisplayColumn());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(25);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        final String path = getIntent().getStringExtra(MainActivity.PATH_FOR_GALERY);
        // TODO: put custom view for support action bar, get id of done btn and back btn, handle back btn inside recyclerviewadapter
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.galery_actionbar, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(v);

        actionBarTitle = (TextView) v.findViewById(R.id.actionbar_title);
        actionBarBack = (ImageButton) v.findViewById(R.id.actionbar_back);
        actionBarDone = (ImageButton) v.findViewById(R.id.actionbar_done);

        actionBarTitle.setText(new File(path).getName());
        actionBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFile = ((RecyclerViewAdapter) recyclerView.getAdapter()).getParentFile();
                if (parentFile.getAbsolutePath().equals(MainActivity.PICTURE_PATH)) {
                    onBackPressed();
                } else {
                    RecyclerViewAdapter galeryAdap = new RecyclerViewAdapter(getBaseContext(), parentFile.getParentFile());
                    recyclerView.setAdapter(galeryAdap);
                    GaleryActivity.actionBarTitle.setText(parentFile.getParentFile().getName());
                    GaleryActivity.actionBarDone.setVisibility(View.GONE);
                }
            }
        });
        actionBarDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.equals(MainActivity.MAIN_ACTIVITY) || activity.equals(BookDetailActivity.BOOK_DETAIL_ACTIVITY)) {
                    Intent i = new Intent(getBaseContext(), DisplayOCRActivity.class);
                    i.putExtra(MainActivity.OCR_BITMAP, RecyclerViewAdapter.getImgPath());

                    BookClass book = (BookClass) getIntent().getSerializableExtra(BookDetailActivity.BOOK);
                    if (book != null) i.putExtra(BookDetailActivity.BOOK, book);
                    startActivityForResult(i, MainActivity.REQUEST_OCR_RESULT);

                } else if (activity.equals(EditActivity.EDIT_ACTIVITY)) {
                    setResult(RESULT_OK, getIntent().putExtra(EditActivity.BOOK_COVER, RecyclerViewAdapter.getImgPath()));
                    finish();
                }

            }
        });

        scanAndDisplayGalery(path);
    }

    @Override
    public void onBackPressed() {
        parentFile = ((RecyclerViewAdapter) recyclerView.getAdapter()).getParentFile();
        if (parentFile.getAbsolutePath().equals(MainActivity.PICTURE_PATH)) {
            super.onBackPressed();
        } else {
            RecyclerViewAdapter galeryAdap = new RecyclerViewAdapter(getBaseContext(), parentFile.getParentFile());
            recyclerView.setAdapter(galeryAdap);
            GaleryActivity.actionBarTitle.setText(parentFile.getParentFile().getName());
            GaleryActivity.actionBarDone.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.REQUEST_OCR_RESULT && resultCode == RESULT_OK) {
            setResult(RESULT_OK, getIntent().putExtra(DisplayOCRActivity.OCR_RESULT, data.getSerializableExtra(DisplayOCRActivity.OCR_RESULT)));
            finish();
        }
    }

    private void scanAndDisplayGalery(String path) {
        RecyclerViewAdapter galeryAdap = new RecyclerViewAdapter(getBaseContext(), new File(path));
        galeryAdap.setHasStableIds(true);
        recyclerView.setAdapter(galeryAdap);
    }

    private int calculateDisplayColumn() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.pic, null);
        int layoutWidth = v.findViewById(R.id.pic).getLayoutParams().width;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Log.e("Galery cols", ":" + displayMetrics.widthPixels / layoutWidth);
        return displayMetrics.widthPixels / layoutWidth;

    }
}
