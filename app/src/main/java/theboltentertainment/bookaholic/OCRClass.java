package theboltentertainment.bookaholic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;

/**
 * OCR Class
 */

public class OCRClass {
    private Context context;
    private TessBaseAPI mTess;

    OCRClass(Context c, String language) {
        context = c;

        mTess = new TessBaseAPI();
        String datapath = Environment.getExternalStorageDirectory().toString() + "/Bookaholic/";
        Log.e("datapath", datapath);
        mTess.init(datapath, language);
    }

    String getOCRResult(Bitmap bitmap) {
        mTess.setImage(bitmap);
        return mTess.getUTF8Text();
    }

    void onDestroy() {
        if (mTess != null)
            mTess.end();
    }
}
