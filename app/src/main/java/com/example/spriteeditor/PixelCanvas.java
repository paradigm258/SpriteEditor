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

    public enum DRAWMODE{
        PEN,
        LINE,
        RECT,
        CUT,
        MOVE,
        FILL,
        CIRCLE
    }

    Bitmap bitmap;
    Bitmap lastBitmap;
    Bitmap[] bitmapHistory;
    int historyCounter;
    int historySize;
    int brushSize;
    Bitmap bg;
    Bitmap preview;

    private int roundedX;
    private int roundedY;

    private float left = 0;
    private float top = 0;
    private float scale = 1;
    private float minScale;

    private int shapeStartX;
    private int shapeStartY;
    private float pvTop;
    private float pvLeft;

    private boolean movable;

    Paint paint;
    float width;
    float height;
    int imgW;
    int imgH;
    int brushColor;
    boolean eraser;

    public DRAWMODE mode = DRAWMODE.PEN;
    DRAWMODE preMode;

    ScaleGestureDetector sgd;
    GestureDetector gestureDetector;
    GestureDetector shapeMove;

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
        shapeMove = new GestureDetector(this.getContext(),new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                mode = preMode;
                movable = false;
                drawShape();
                invalidate();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                boolean out = roundedX<pvLeft || roundedY<pvTop ||
                        roundedX>pvLeft+preview.getWidth() || roundedY>pvTop+preview.getHeight();
                if(!out){
                    pvLeft += -distanceX / scale;
                    pvTop += -distanceY / scale;
                }
                return true;
            }
        });
        //Init paint for canvas
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setDither(false);
        paint.setFilterBitmap(false);

        brushColor = 0xFF000000;
        historySize = 5;
        brushSize = 0;
        newHistory();
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        invalidate();
    }

    public void getRes() {
        //Adjust for image resolution
        width = getWidth();
        height = getHeight();
        imgW = bitmap.getWidth();
        imgH = bitmap.getHeight();
        float scaleX = (float) getWidth() / (float) imgW;
        float scaleY = (float) getHeight() / (float) imgH;
        minScale = Math.min(scaleX, scaleY);
        scale = minScale;
        left = 0;
        top = 0;
        movable = false;
        //Init background
        bg = Bitmap.createBitmap(imgW, imgH, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < bg.getWidth(); x++) {
            for (int y = 0; y < bg.getHeight(); y++) {
                bg.setPixel(x, y, x % 2 == y % 2 ? 0xFFF3F3F3 : 0xFFC2C2C2);
            }
        }
    }

    public void newHistory(){
        eraser = false;
        lastBitmap = null;
        bitmapHistory = new Bitmap[historySize];
        historyCounter = 0;
    }

    @Override
    public void draw(Canvas canvas) {
        if (bg != null && bitmap != null) {
            canvas.scale(scale, scale);
            canvas.drawBitmap(bg, left, top, paint);
            canvas.drawBitmap(bitmap, left, top, paint);
        }
        if (preview !=null && movable) {
            //Draw preview
            canvas.drawBitmap(preview, (int)(pvLeft) + left, (int)(pvTop)+ top, paint);
            //Draw outline
            Paint pvPaint = new Paint(paint);
            pvPaint.setColor(0xFF0000FF);
            pvPaint.setStyle(Paint.Style.STROKE);
            pvPaint.setStrokeWidth(1 / scale * 5);
            canvas.drawRect((int)(pvLeft) + left, (int)(pvTop) + top,
                    (int)(pvLeft) + left + preview.getWidth(), (int)(pvTop) + top + preview.getHeight(), pvPaint);
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

            float x = event.getX();
            float y = event.getY();
            roundedX = (int) Math.floor(x / scale - left);
            roundedY = (int) Math.floor(y / scale - top);
            if(mode == DRAWMODE.MOVE){
                shapeMove.onTouchEvent(event);
            }else {
                if(event.getActionMasked()==MotionEvent.ACTION_DOWN)updateBitmapHistory();
                switch (mode) {
                    case PEN:
                        drawPixel(roundedX, roundedY,brushColor);
                        break;
                    case FILL:
                        floodFill(roundedX, roundedY, bitmap.getPixel(roundedX, roundedY));
                        break;
                    case CUT:
                    case LINE:
                    case RECT:
                        makeShape(event);
                        break;
                }
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
                shapeStartX = roundedX;
                shapeStartY = roundedY;
                break;
            case MotionEvent.ACTION_MOVE:
                int preViewWidth = Math.abs(shapeStartX - roundedX) + 1;
                int preViewHeight = Math.abs(shapeStartY - roundedY) + 1;
                preview = Bitmap.createBitmap(preViewWidth, preViewHeight, Bitmap.Config.ARGB_8888);
                pvLeft = Math.min(shapeStartX, roundedX);
                pvTop = Math.min(shapeStartY, roundedY);
                switch (mode) {
                    case CUT:
                        drawCut();
                        break;
                    case LINE:
                        drawLine();
                        break;
                    case RECT:
                        drawRect();
                        break;
                    case CIRCLE:
                        drawCircle();
                        break;
                }
                movable = true;
                break;
            case MotionEvent.ACTION_UP:
                if(movable) {
                    if(mode == DRAWMODE.CUT){
                        for(int x=0;x<preview.getWidth();x++){
                            for(int y=0;y<preview.getHeight();y++){
                                bitmap.setPixel((int)pvLeft+x,(int)pvTop+y,0);
                            }
                        }
                    }
                    preMode = mode;
                    mode = DRAWMODE.MOVE;
                }
                break;
        }
    }

    private void drawShape(){
        for(int y=0;y<preview.getHeight();y++){
            for(int x=0;x<preview.getWidth();x++){
                int pixel = preview.getPixel(x,y);
                if(pixel!=0){
                    int bX = (int)(x+pvLeft);
                    int bY = (int)(y+pvTop);
                    drawPixel(bX,bY,pixel);
                }
            }
        }
    }

    private void drawCut(){
        for(int x=0;x<preview.getWidth();x++){
            for(int y=0;y<preview.getHeight();y++){
                preview.setPixel(x,y,bitmap.getPixel(x+(int)pvLeft,y+(int)pvTop));
            }
        }
    }

    private void drawRect(){
        for(int x=0;x<preview.getWidth();x++){
            preview.setPixel(x,0,brushColor);
            preview.setPixel(x,preview.getHeight()-1,brushColor);
        }
        for(int y=0;y<preview.getHeight();y++){
            preview.setPixel(0,y,brushColor);
            preview.setPixel(preview.getWidth()-1,y,brushColor);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void drawLine() {
        int x1=shapeStartX - (int)pvLeft;
        int y1=shapeStartY - (int)pvTop;
        int x2=roundedX - (int)pvLeft;
        int y2=roundedY - (int)pvTop;
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

        float error = 0;
        int yStep = (y1 < y2) ? 1 : -1;
        int y = y1;

        int maxX = x2;
        for (int x = x1; x <= maxX; x++) {
            if (steep) {
                preview.setPixel(y, x, brushColor);
            } else {
                preview.setPixel(x, y, brushColor);
            }
            error += (dy/dx);
            if (error > 0.5f) {
                y += yStep;
                error -= 1;
            }
        }
    }

    private void floodFill(int x, int y, int floodColor) {
        if (x < 0 || x >= imgW || y < 0 || y >= imgH) return;
        int color  = bitmap.getPixel(x, y);
        if( color == brushColor ) return;
        if( color != floodColor) return;
        bitmap.setPixel(x, y, brushColor);
        floodFill(x + 1, y, color);
        floodFill(x, y + 1, color);
        floodFill(x - 1, y, color);
        floodFill(x, y - 1, color);
    }

    private void drawCircle(){

    }
    private void drawPixel(int x, int y,int color){
        if (x >= 0 && x < imgW && y >= 0 && y < imgH)
            bitmap.setPixel(x, y, brushColor);
    }
}
