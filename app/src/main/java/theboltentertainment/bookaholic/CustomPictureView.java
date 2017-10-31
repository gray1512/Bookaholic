package theboltentertainment.bookaholic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.AttrRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class CustomPictureView extends android.support.v7.widget.AppCompatImageView {
    Context c;

    Paint paintW;
    Paint paintB;
    Bitmap picture;

    int coor_x, coor_y;
    boolean firstTime;
    float displayedWidth;
    float displayedHeight;

    float[] points;
    int px = -1, py = -1;

    float distance;
    float newDistance;
    boolean firstSize = true;

    String mode;
    String MODE_POINTER_MOVE = "Zoom In/ Out";
    String MODE_MOVE = "Control 4 points";
    int TOLER = 30;


    public CustomPictureView(Context context) {
        super(context);
        c = context;
        init();
    }

    public CustomPictureView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        c = context;
        init();
    }
    public CustomPictureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        c = context;
        init();
    }

    private void init() {
        points = new float[4 * 2];
        firstTime = true;

        paintW = new Paint();
        paintW.setColor(Color.WHITE);
        paintW.setAntiAlias(true);
        paintW.setStrokeWidth(8f);

        paintB = new Paint();
        paintB.setColor(Color.BLACK);
        paintB.setAntiAlias(true);
        paintB.setStrokeWidth(5f);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        picture = bm;
        super.setImageBitmap(bm);
    }

    public float[][] getOCRPoints() {
        float[] matrixValues = new float[9];
        getImageMatrix().getValues(matrixValues);
        float scaleX = matrixValues[Matrix.MSCALE_X];
        float scaleY = matrixValues[Matrix.MSCALE_Y];
        Log.e("Scale:", ": " + scaleX +" " + scaleY);

        float imageW = picture.getWidth() * scaleX;
        float imageH = picture.getHeight() * scaleY;
        Log.e("Image:", ": " + imageW +" " + imageH);

        coor_x = (int) ((this.getMeasuredWidth() - imageW) / 2);
        coor_y = (int) ((this.getMeasuredHeight() - imageH) / 2);
        Log.e("Coord:", ": " + coor_x +" " + coor_y);

        return new float[][]{{(points[0] - coor_x) / scaleX, (points[1] - coor_y) / scaleY},
                              {(points[2] - coor_x) / scaleX, (points[3] - coor_y) / scaleY},
                              {(points[4] - coor_x) / scaleX, (points[5] - coor_y) / scaleY},
                              {(points[6] - coor_x) / scaleX, (points[7] - coor_y) / scaleY}};
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            // Handle single touch in 4 corner points
            case  MotionEvent.ACTION_DOWN: {
                mode = MODE_POINTER_MOVE;

                float x = event.getX();
                float y =  event.getY();

                for (int i = 0; i < 8; i += 2) {
                    if ((points[i] - TOLER < x && x <= points[i] + TOLER)
                            && (points[i + 1] - TOLER < y && y <= points[i + 1] + TOLER)) {
                        px = i;
                        py = i + 1;
                        mode = MODE_MOVE;
                    }
                }
                invalidate();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mode.equals(MODE_MOVE) && px != -1 && py != -1) {
                    points[px] = event.getX();
                    points[py] = event.getY();
                    invalidate();

                } else if (mode.equals(MODE_POINTER_MOVE) && event.getPointerCount() == 2) {
                    float x = event.getX(0) - event.getX(1);
                    float y = event.getY(0) - event.getY(1);
                    newDistance = (float) Math.sqrt(x * x + y * y);

                    if (!firstSize) {
                        float change =  (newDistance - distance) / 2;
                        Log.e("Spacing", ":" + change);

                        points[0] -= change; points[1] -= change;
                        points[2] += change; points[3] -= change;
                        points[4] += change; points[5] += change;
                        points[6] -= change; points[7] += change;
                    } else {
                        firstSize = false;
                    }

                    distance = newDistance;
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                px = py = -1;
                mode = "";
                firstSize = true;
                invalidate();
                break;
            }

            // Handle multi-touch inside rect to re-size and rotate rect
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (event.getPointerCount() == 2) {
                    float x = event.getX(0) - event.getX(1);
                    float y = event.getY(0) - event.getY(1);

                    distance = (float) Math.sqrt(x * x + y * y);
                    mode = MODE_POINTER_MOVE;
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        if (firstTime) {
            displayedWidth = c.getWidth();
            displayedHeight = c.getHeight();

            // Top-right
            points[0] = displayedWidth / 4;     points[1] = displayedHeight / 4;
            // Top-left
            points[2] = displayedWidth * 3/4;     points[3] = displayedHeight /4;
            // Bot-left
            points[4] = displayedWidth * 3/4;   points[5] = displayedHeight * 3/4;
            // Bot-right
            points[6] = displayedWidth / 4;   points[7] = displayedHeight * 3/4;

            /*coor_x = (int) ((c.getWidth() - displayedWidth) / 2);
            coor_y = (int) ((c.getHeight() - displayedHeight) / 2);
            for (int i = 0; i < 8; i ++) {
                if ((i%2) == 0) {
                    points[i] += coor_x;
                } else {
                    points[i] += coor_y;
                }
            }*/
            firstTime = false;
        }

        float[] lines_points = {points[0], points[1],  points[2], points[3],
                                points[2], points[3],  points[4], points[5],
                                points[4], points[5],  points[6], points[7],
                                points[6], points[7],  points[0], points[1]};
        c.drawLines(lines_points, paintW);
        c.drawLines(lines_points, paintB);

        for (int i = 0; i < 8; i += 2) {
            c.drawCircle(points[i], points[i + 1], 15, paintW);
            c.drawCircle(points[i], points[i + 1], 10, paintB);
        }
    }
}
