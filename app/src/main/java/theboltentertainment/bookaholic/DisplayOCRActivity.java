package theboltentertainment.bookaholic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


public class DisplayOCRActivity extends AppCompatActivity {
    private final String language = "eng";
    public static final String OCR_RESULT = "OCR Result";
    public static final String DISPLAY_ACTIVITY = "Mark intent from display activity";
    private String imgPath;

    // Customize points and loading view
    private ProgressBar displayProBar;
    private Bitmap orgBitmap;
    private CustomPictureView ocrPicture;
    private ImageButton checkedBtn;

    // Display ocr result, edit and cancel view
    private ImageView ocrImg;
    private ImageButton textChecked;
    private ImageButton textUnchecked;
    private ScrollView scrollView;
    private TextView textView;

    private String text;

    private Handler h = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_ocr);

        displayProBar = (ProgressBar) findViewById(R.id.display_progress_bar);

        imgPath = getIntent().getStringExtra(MainActivity.OCR_BITMAP);
        orgBitmap = BitmapFactory.decodeFile(imgPath);
        ocrPicture = (CustomPictureView) findViewById(R.id.ocr_picture);

        ocrPicture.setImageBitmap(orgBitmap);

        ocrImg = (ImageView) findViewById(R.id.ocr_img);
        checkedBtn = (ImageButton) findViewById(R.id.checked);

        textChecked = (ImageButton) findViewById(R.id.text_checked);
        textUnchecked = (ImageButton) findViewById(R.id.text_unchecked);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        textView = (TextView) findViewById(R.id.display_ocr_text);

        checkedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getResult();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.REQUEST_OCR_RESULT && resultCode == RESULT_OK) {
            setResult(RESULT_OK, getIntent().putExtra(OCR_RESULT, data.getSerializableExtra(OCR_RESULT)));
            finish();
        }
    }

    private void getResult() {
        checkedBtn.setVisibility(View.INVISIBLE);
        displayProBar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final OCRClass ocrTool = new OCRClass(getBaseContext(), language);

                if (!OpenCVLoader.initDebug()) {
                    Log.e(getBaseContext().getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
                } else {
                    Log.d(getBaseContext().getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
                }

                orgBitmap = convertMatToBitmap(useOpenCV());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        ocrPicture.setVisibility(View.GONE);

                        ocrImg.setImageBitmap(orgBitmap);
                        ocrImg.setVisibility(View.VISIBLE);
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        text = '"' + ocrTool.getOCRResult(orgBitmap) + '"';
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                displayProBar.setVisibility(View.INVISIBLE);
                                checkedBtn.setVisibility(View.INVISIBLE);

                                textChecked.setVisibility(View.VISIBLE);
                                textUnchecked.setVisibility(View.VISIBLE);

                                textView.setText(text);

                                scrollView.setVisibility(View.VISIBLE);
                            }
                        });

                        ocrTool.onDestroy();
                    }
                }).start();
            }
        }).start();
    }

    private Mat useOpenCV() {
        Mat crop = perspectiveImage(imgPath, ocrPicture, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        Mat res = new Mat(crop.size(), CvType.CV_32FC1);
        Imgproc.threshold(crop, res, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.adaptiveThreshold(res, res, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 40);

        return res;

    }

    public static Mat perspectiveImage(String imgPath, CustomPictureView ocrPicture, int _scale) {
        Mat im = Imgcodecs.imread(imgPath, _scale);

        // get rect points
        float[][] ocrPoints = ocrPicture.getOCRPoints();
        Mat rect = new Mat(4, 1, CvType.CV_32FC2);
        for (int i = 0; i < ocrPoints.length ; i ++) { //0 1 2 3
            rect.put( i, 0, ocrPoints[i]);
        }

        /* Compute the width of the new image, which will be the
        maximum distance between bottom-right and bottom-left
        x-coordiates or the top-right and top-left x-coordinates */
        float widthA = getMaxDistance(ocrPoints[0][0], ocrPoints[1][0]);
        float widthB = getMaxDistance(ocrPoints[2][0], ocrPoints[3][0]);
        float maxWidth;
        if (widthA > widthB) maxWidth = widthA;
        else maxWidth = widthB;

        /*compute the height of the new image, which will be the
        maximum distance between the top-right and bottom-right
        y-coordinates or the top-left and bottom-left y-coordinates */
        float heightA = getMaxDistance(ocrPoints[0][1], ocrPoints[3][1]);
        float heightB = getMaxDistance(ocrPoints[1][1], ocrPoints[2][1]);
        float maxHeight;
        if (heightA > heightB) maxHeight = heightA;
        else maxHeight = heightB;

        /*now that we have the dimensions of the new image, construct
        the set of destination points to obtain a "birds eye view",
        (i.e. top-down view) of the image, again specifying points
        in the top-left, top-right, bottom-right, and bottom-left order */
        float[][] dest = {{0, 0}, {maxWidth, 0}, {maxWidth, maxHeight}, {0, maxHeight}};
        Mat des = new Mat(4, 1, CvType.CV_32FC2);
        for (int i = 0; i < dest.length ; i ++) { // 0 1 2 3
            des.put(i, 0, dest[i]);
        }

        Mat trans = Imgproc.getPerspectiveTransform(rect, des);
        Mat crop = new Mat(des.size(), CvType.CV_32FC2);
        Imgproc.warpPerspective(im, crop, trans, new Size(maxWidth, maxHeight));
        return crop;
    }

    private static float getMaxDistance(float n1, float n2) {
        float distance = n1 - n2;
        if (distance > 0) return distance;
        else return - distance;
    }

    private synchronized Bitmap convertMatToBitmap(Mat mat) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);
            return bitmap;

        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public void textChecked(View v) {
        text = textView.getText().toString();

        Intent intent = new Intent(getBaseContext(), EditActivity.class);
        intent.putExtra(MainActivity.ACTIVITY, DisplayOCRActivity.DISPLAY_ACTIVITY);
        intent.putExtra(OCR_RESULT, text);

        BookClass book = (BookClass) getIntent().getSerializableExtra(BookDetailActivity.BOOK);
        if ( book != null) {
            intent.putExtra(BookDetailActivity.BOOK, book);
        }
        startActivityForResult(intent, MainActivity.REQUEST_OCR_RESULT);
    }

    public void textUnchecked(View v) {
        scrollView.setVisibility(View.GONE);
        textChecked.setVisibility(View.GONE);
        textUnchecked.setVisibility(View.GONE);
        ocrImg.setVisibility(View.GONE);

        ocrPicture.setVisibility(View.VISIBLE);
        checkedBtn.setVisibility(View.VISIBLE);
    }

}
