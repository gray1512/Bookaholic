package theboltentertainment.bookaholic;

import android.Manifest;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.support.constraint.ConstraintSet.BOTTOM;
import static android.support.constraint.ConstraintSet.LEFT;
import static android.support.constraint.ConstraintSet.RIGHT;
import static android.support.constraint.ConstraintSet.TOP;

public class MainActivity extends AppCompatActivity {

    private SQLDatabase db;

    public static final String ACTIVITY = "Name of Activity";
    public static final String MAIN_ACTIVITY = "Mark intent from main activity";

    private final int REQUEST_PERMISSION = 100;
    public static final int REQUEST_OCR_PICTURE = 101;
    public static final int REQUEST_OCR_RESULT = 102;

    public static final String QUOTE_EDIT = "Quote need to be editted";

    public static final String PICTURE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Bookaholic/";
    public static final String OCR_BITMAP = "OCR Bitmap";
    public static final String PATH_FOR_GALERY = "Path to start Galery Activity";

    private MenuItem searchItem;

    private ViewPager viewPager;
    private FloatingActionButton mBtn;
    private FloatingActionButton takePic;
    private FloatingActionButton galery;

    private String imagePath = "";

    public static int MAX_WIDTH;
    public static int MAX_HEIGHT;

    protected static ArrayList<QuoteClass> quoteList = new ArrayList<>();
    protected static ArrayList<BookClass> bookList = new ArrayList<>();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_scan:
                    setTitle("Recently");
                    mBtn.setVisibility(View.VISIBLE);
                    takePic.clearAnimation();
                    galery.clearAnimation();

                    takePic.setVisibility(View.INVISIBLE);
                    galery.setVisibility(View.INVISIBLE);

                    mBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showBtnAnimation();
                        }
                    });
                    if (searchItem != null) searchItem.setVisible(true);
                    viewPager.setCurrentItem(0);
                    return true;

                case R.id.navigation_bookshelf:
                    setTitle("Bookshelf");
                    mBtn.setVisibility(View.VISIBLE);
                    takePic.clearAnimation();
                    galery.clearAnimation();

                    takePic.setVisibility(View.INVISIBLE);
                    galery.setVisibility(View.INVISIBLE);

                    mBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showBtnAnimation();
                        }
                    });
                    if (searchItem != null) searchItem.setVisible(true);
                    viewPager.setCurrentItem(1);
                    return true;

                case R.id.navigation_shop:
                    setTitle("Shop");
                    takePic.clearAnimation();
                    galery.clearAnimation();

                    mBtn.setVisibility(View.GONE);
                    takePic.setVisibility(View.GONE);
                    galery.setVisibility(View.GONE);
                    if (searchItem != null) searchItem.setVisible(false);
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // create OCR
            prepareTesseract();
        } else {
            final String[] permissions = new String[]{Manifest.permission.CAMERA,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.READ_EXTERNAL_STORAGE};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, REQUEST_PERMISSION);
            }
        }

        db = new SQLDatabase(getBaseContext());

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));

        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setTitle("Recently");
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0: navigation.setSelectedItemId(R.id.navigation_scan); break;
                    case 1: navigation.setSelectedItemId(R.id.navigation_bookshelf); break;
                    case 2: navigation.setSelectedItemId(R.id.navigation_shop); break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        mBtn = (FloatingActionButton) findViewById(R.id.scan_button);
        takePic = (FloatingActionButton) findViewById(R.id.take_pic_btn);
        galery = (FloatingActionButton) findViewById((R.id.galery_btn));

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBtnAnimation();
            }
        });

        if(getIntent().getStringExtra(ACTIVITY) != null &&
                getIntent().getStringExtra(ACTIVITY).equals(BookDetailActivity.BOOK_DETAIL_ACTIVITY)) {
            viewPager.setCurrentItem(1);
        }

        quoteList = getData();
        bookList = getBooks();
        Log.e("Book List", bookList.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                ViewPagerAdapter.getCurrentAdapter(viewPager.getCurrentItem()).getFilter().filter(query);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getBaseContext(), "Permissions granted!", Toast.LENGTH_LONG).show();
                    prepareTesseract();
                } else {
                    Toast.makeText(getBaseContext(), "Bookaholic need Camera Permission to work", Toast.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE},
                                            REQUEST_PERMISSION);
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OCR_PICTURE && resultCode == RESULT_OK) {
            checkForPictureSize();

            Intent intent = new Intent(getBaseContext(), DisplayOCRActivity.class);
            intent.putExtra(OCR_BITMAP, imagePath);
            startActivityForResult(intent, REQUEST_OCR_RESULT);

        } else if (requestCode == REQUEST_OCR_RESULT && resultCode == RESULT_OK) {
            Log.e("Get OCR result", "Success" + data.getSerializableExtra(DisplayOCRActivity.OCR_RESULT));
            QuoteClass newQuote = (QuoteClass) data.getSerializableExtra(DisplayOCRActivity.OCR_RESULT);
            quoteList.add(0, newQuote);

            if (!bookList.contains(newQuote.get_book())) bookList.add(0, newQuote.get_book());
            db.insertData(newQuote.get_dataList());

            ViewPagerAdapter.ScanFragment.notifyDataSetChanged();
            ViewPagerAdapter.BookshelfFragment.notifyDataSetChanged();
            // TODO: Display result use QuoteClass
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }



    private ArrayList<QuoteClass> getData() {
        return db.getAllBookData();
    }

    private ArrayList<BookClass> getBooks() {
        return db.getAllBook();
    }

    private void checkForPictureSize() {
        MAX_WIDTH = dpToPx(350);
        MAX_HEIGHT = dpToPx(400);
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

    private void prepareDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("Prepare Dir", "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.e("Prepare Dir", "Created directory " + path);
        }
    }

    private void prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + "tessdata/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        copyTessDataFiles("tessdata");
    }

    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {
                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                File langFile = new File(DATA_PATH + path + "/" + fileName);
                Log.e("Copy tessdata", "Check before copy to tessdata");

                if (!langFile.exists()) { // Check if lang be copied or not
                    Log.e("Copy tessdata", "Start copy to tessdata");
                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(langFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                        Log.e("Copy tessdata", "Copying to tessdata");
                    }
                    in.close();
                    out.close();

                    Log.e("Copy tessdata", "Copied " + fileName + "to tessdata");
                }
            }
        } catch (Exception e) {
            Log.e("Copy tessdata", "Unable to copy files to tessdata " + e.toString());
        }
    }

    private void showBtnAnimation() {
        Animation showTakePic = AnimationUtils.loadAnimation(this, R.anim.ping_take_pic_btn);
        Animation showGalery = AnimationUtils.loadAnimation(this, R.anim.ping_galery_btn);

        takePic.startAnimation(showTakePic);
        galery.startAnimation(showGalery);

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOCRPicture();
            }
        });
        galery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), GaleryActivity.class);
                intent.putExtra(PATH_FOR_GALERY, PICTURE_PATH);
                intent.putExtra(ACTIVITY, MAIN_ACTIVITY);
                startActivityForResult(intent, REQUEST_OCR_RESULT);
            }
        });

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBtnAnimation();
            }
        });
    }

    private void hideBtnAnimation() {
        Animation hideTakePic = AnimationUtils.loadAnimation(this, R.anim.hide_take_pic_btn);
        Animation hideGalery = AnimationUtils.loadAnimation(this, R.anim.hide_galery_btn);

        takePic.startAnimation(hideTakePic);
        galery.startAnimation(hideGalery);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBtnAnimation();
            }
        });

        takePic.setClickable(false);
        galery.setClickable(false);
    }

    private void getOCRPicture () {
        String fileUniqueName= new SimpleDateFormat("yymmdd_hhss").format(new Date());
        String imageFileName = fileUniqueName + ".jpg";

        String storageDir = PICTURE_PATH + "/Bookaholic Pics/";
        prepareDirectory(storageDir);

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

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void expandQuoteView (View view) {
        if (((TextView) view).getEllipsize() == null) {
            ((TextView) view).setEllipsize(TextUtils.TruncateAt.END);
            ((TextView) view).setMaxLines(5);
        } else {
            ((TextView) view).setEllipsize(null);
            ((TextView) view).setMaxLines(1000000000);
        }
    }
}
