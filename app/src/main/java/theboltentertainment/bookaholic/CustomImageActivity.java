package theboltentertainment.bookaholic;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static theboltentertainment.bookaholic.DisplayOCRActivity.perspectiveImage;
import static theboltentertainment.bookaholic.MainActivity.PICTURE_PATH;


public class CustomImageActivity extends AppCompatActivity{
    public static final String IMAGE_PATH = "Image path";
    private CustomPictureView customView;
    private ImageView imageView;
    private ImageButton checked;
    private ImageButton unchecked;

    private String path;
    private String customedImagePath;

    private Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_image);

        customView = findViewById(R.id.custom_picture);
        imageView = findViewById(R.id.customed_picture);
        checked = findViewById(R.id.custom_checked);
        unchecked = findViewById(R.id.custom_unchecked);

        path = getIntent().getStringExtra(IMAGE_PATH);
        Bitmap bm = BitmapFactory.decodeFile(path);
        customView.setImageBitmap(bm);

        checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedCustom();
            }
        });
        unchecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uncheckedCustom();
            }
        });
    }

    public void checkedCustom() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!OpenCVLoader.initDebug()) {
                    Log.e(getBaseContext().getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
                } else {
                    Log.d(getBaseContext().getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
                }

                Mat img = perspectiveImage(path, customView, Imgcodecs.CV_LOAD_IMAGE_COLOR);
                Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2RGB);
                Bitmap bitmap;

                try {
                    bitmap = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(img, bitmap);

                    saveBitmap(bitmap);
                    final Bitmap displayBm = bitmap;
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(displayBm);
                            customView.setVisibility(View.INVISIBLE);
                            imageView.setVisibility(View.VISIBLE);
                        }
                    });
                }catch(Exception ex){
                    System.out.println(ex.getMessage());
                }

                checked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkedImage();
                    }
                });
                unchecked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uncheckedImage();
                    }
                });
            }
        }).start();

    }

    private void saveBitmap(Bitmap bm) {
        customedImagePath = PICTURE_PATH + new SimpleDateFormat("yyMMdd_HHss").format(new Date()) + ".jpg";
        File pictureFile = new File(customedImagePath );

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Error", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Error", "Error accessing file: " + e.getMessage());
        }
    }

    public  void uncheckedCustom() {
        onBackPressed();
    }

    private void checkedImage() {
        setResult(RESULT_OK, getIntent().putExtra(IMAGE_PATH, customedImagePath));
        finish();
    }

    private void uncheckedImage() {
        imageView.setVisibility(View.GONE);
        customView.setVisibility(View.VISIBLE);

        checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedCustom();
            }
        });
        unchecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uncheckedCustom();
            }
        });
    }
}
