package com.example.spriteeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class PixelCanvas extends View {
    Bitmap bitmap;
    Bitmap lastBitmap;
    Bitmap[] bitmapHistory;
    int historyCounter;
    int historySize;
    Bitmap bg;
    private float touchStartX;
    private float touchStartY;
    private float left = 0;
    private float top = 0;
    private float scale = 1;
    private float minScale;
    Paint paint;
    ScaleGestureDetector sgd;
    float width;
    float height;
    float imgW;
    float imgH;
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
        gestureDetector = new GestureDetector(this.getContext(),new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                left+=-distanceX/scale;
                top+=-distanceY/scale;
                return true;
            }
        });
        //Init paint for canvas
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setDither(false);
        paint.setFilterBitmap(false);

        historySize = 5;
        newHistory();
    }

    public void newHistory(){
        brushColor = 0xFF000000;
        lastBitmap = null;
        bitmapHistory = new Bitmap[historySize];
        historyCounter = 0;
    }

    public int getImgWidth() {
        return bitmap.getWidth();
    }

    public int getImgHeight() {
        return bitmap.getHeight();
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);

        invalidate();
    }

    public void getRes() {
        //Init background
        bg = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (x % 2 == y % 2) {
                    bg.setPixel(x, y, 0xFFF3F3F3);
                } else {
                    bg.setPixel(x, y, 0xFFC2C2C2);
                }
            }
        }
        //Adjust for image resolution
        width = getWidth();
        height = getHeight();
        imgW = getImgWidth();
        imgH = getImgHeight();
        float scaleX = (float) getWidth() / (float) getImgWidth();
        float scaleY = (float) getHeight() / (float) getImgHeight();
        minScale = Math.min(scaleX, scaleY);
        scale = minScale;
    }

    @Override
    public void draw(Canvas canvas) {
        if(bg!=null && bitmap!=null) {
            canvas.scale(scale, scale);
            canvas.drawBitmap(bg, left, top, paint);
            canvas.drawBitmap(bitmap, left, top, paint);
        }
        super.draw(canvas);
    }

    public void updateBitmapHistory(){
        setNull();
        if(historyCounter<=historySize-1){
            bitmapHistory[historyCounter] = bitmap.copy(Bitmap.Config.ARGB_8888,true);
            historyCounter++;
        }else{
            for(int i=0; i<historySize-1;i++){
                if(bitmapHistory[i+1]!=null){
                    bitmapHistory[i] = bitmapHistory[i+1].copy(Bitmap.Config.ARGB_8888,true);
                }
            }
            bitmapHistory[historySize-1] = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        }
    }

    public void setNull(){
        System.out.println(historyCounter);
        for(int i=historyCounter; i<5; i++){
            bitmapHistory[i] = null;
        }
        lastBitmap = null;
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
                    updateBitmapHistory();

                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    if(touchStartY>height||touchStartY<top){
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
                if(roundedX>=0&&roundedX<imgW && roundedY>=0&&roundedY<imgH)
                bitmap.setPixel(roundedX, roundedY, brushColor);
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
        left = Math.min(0,Math.max(left,width/scale-imgW));
        top = Math.min(0,Math.max(top,height/scale-imgW));
    }
    
    public void floodFill(int x, int y, int floodColor){
        if (x < 0 || x >= imgW || y < 0 || y >= imgH) return;
        int color = bitmap.getPixel(x,y);
        if(color!=floodColor)return;
        bitmap.setPixel(x,y,brushColor);
        floodFill(x+1,y,color);
        floodFill(x,y+1,color);
        floodFill(x-1,y,color);
        floodFill(x,y-1,color);
    }
}
