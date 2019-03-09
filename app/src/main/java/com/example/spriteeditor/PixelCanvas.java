package com.example.spriteeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class PixelCanvas extends View {
    Bitmap bitmap;
    Bitmap bg;
    Bitmap preview;

    private float touchStartX;
    private float touchStartY;
    private int shapeStartX;
    private int shapeStartY;
    private int shapeEndX;
    private int shapeEndY;
    private int pvTop;
    private int pvLeft;
    private float left = 0;
    private float top = 0;
    private float scale = 1;
    private float minScale;
    private boolean pv = true;
    Paint paint;
    ScaleGestureDetector sgd;
    float width;
    float height;
    int imgW;
    int imgH;
    int brushColor;
    GestureDetector gestureDetector;

    public PixelCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Init touch detectors
        sgd = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scale = scale * detector.getScaleFactor();
                scale = Math.max(minScale, Math.min(scale, 64));
                return true;
            }
        });
        gestureDetector = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                left += -distanceX / scale;
                top += -distanceY / scale;
                return true;
            }
        });

        //Init paint for canvas
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setDither(false);
        paint.setFilterBitmap(false);

        brushColor = 0xFF000000;
    }

    public int getImgWidth() {
        return bitmap.getWidth();
    }

    public int getImgHeight() {
        return bitmap.getHeight();
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        invalidate();
    }

    public void getRes() {

        //Adjust for image resolution
        width = getWidth();
        height = getHeight();
        imgW = getImgWidth();
        imgH = getImgHeight();
        float scaleX = (float) getWidth() / (float) getImgWidth();
        float scaleY = (float) getHeight() / (float) getImgHeight();
        minScale = Math.min(scaleX, scaleY);
        scale = minScale;
        //Init background
        bg = Bitmap.createBitmap(imgW, imgH, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < bg.getWidth(); x++) {
            for (int y = 0; y < bg.getHeight(); y++) {
                bg.setPixel(x, y, x % 2 == y % 2 ? 0xFFF3F3F3 : 0xFFC2C2C2);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (bg != null && bitmap != null) {
            canvas.scale(scale, scale);
            canvas.drawBitmap(bg, left, top, paint);
            canvas.drawBitmap(bitmap, left, top, paint);

        }
        if (pv && preview != null) {
            canvas.drawBitmap(preview, pvLeft + left, pvTop + top, paint);
            Paint pvPaint = new Paint(paint);
            pvPaint.setColor(0xFF0000FF);
            pvPaint.setStyle(Paint.Style.STROKE);
            pvPaint.setStrokeWidth(1 / scale * 5);
            canvas.drawRect(pvLeft + left, pvTop + top,
                    pvLeft + left + preview.getWidth(), pvTop + top + preview.getHeight(), pvPaint);
        }
        super.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        if (pointerCount > 1) {
            dragAndScale(event);
        } else {
            boolean setPixel = true;
            event.getPointerId(0);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    if (touchStartY > height || touchStartY < top) {
                        return true;
                    }
                    break;
                default:
                    setPixel = false;
                    break;
            }

            if (setPixel) {
                int roundedX = (int) Math.floor(touchStartX / scale - left);
                int roundedY = (int) Math.floor(touchStartY / scale - top);
                System.out.println(scale);
                System.out.println(height + " " + width);
                System.out.println(touchStartX + " " + touchStartY);
                if (roundedX >= 0 && roundedX < imgW && roundedY >= 0 && roundedY < imgH)
                    bitmap.setPixel(roundedX, roundedY, brushColor);
            } else {
                makeShape(event);
            }
        }
        performClick();
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }


    private void dragAndScale(MotionEvent event) {
        sgd.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        left = Math.min(0, Math.max(left, width / scale - imgW));
        top = Math.min(0, Math.max(top, height / scale - imgW));
    }

    private void makeShape(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                shapeStartX = (int) Math.floor(touchStartX / scale - left);
                shapeStartY = (int) Math.floor(touchStartY / scale - top);
            case MotionEvent.ACTION_MOVE:
                shapeEndX = (int) Math.floor(touchStartX / scale - left);
                shapeEndY = (int) Math.floor(touchStartY / scale - top);
                int preViewHeight = Math.abs(shapeStartY - shapeEndY) + 1;
                int preViewWidth = Math.abs(shapeStartX - shapeEndX) + 1;
                preview = Bitmap.createBitmap(preViewWidth, preViewHeight, Bitmap.Config.ARGB_8888);
                pvLeft = Math.min(shapeStartX, shapeEndX);
                pvTop = Math.min(shapeStartY, shapeEndY);
                drawLine(shapeStartX - pvLeft, shapeStartY - pvTop, shapeEndX - pvLeft, shapeEndY - pvTop, preview);
                break;
            case MotionEvent.ACTION_UP:
                drawLine(shapeStartX,shapeStartY,shapeEndX,shapeEndY,bitmap);
                break;
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void drawLine(int x1, int y1, int x2, int y2, Bitmap bitmap) {
        boolean steep = Math.abs(y1 - y2) > Math.abs(x1 - x2);
        if (steep) {
            int temp;
            temp = x1;
            x1 = y1;
            y1 = temp;
            temp = x2;
            x2 = y2;
            y2 = temp;
        }
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
            temp = y1;
            y1 = y2;
            y2 = temp;
        }
        float dx = x2 - x1;
        float dy = Math.abs(y2 - y1);

        float error = dx / 2.0f;
        int ystep = (y1 < y2) ? 1 : -1;
        int y = y1;

        int maxX = x2;
        for (int x = x1; x <= maxX; x++) {
            if (steep) {
                bitmap.setPixel(y, x, brushColor);
            } else {
                bitmap.setPixel(x, y, brushColor);
            }
            error -= dy;
            if (error < 0) {
                y += ystep;
                error += dx;
            }
        }
    }

    public void floodFill(int x, int y, int floodColor) {
        if (x < 0 || x >= imgW || y < 0 || y >= imgH) return;
        int color = bitmap.getPixel(x, y);
        if (color != floodColor) return;
        bitmap.setPixel(x, y, brushColor);
        floodFill(x + 1, y, color);
        floodFill(x, y + 1, color);
        floodFill(x - 1, y, color);
        floodFill(x, y - 1, color);
    }
}
