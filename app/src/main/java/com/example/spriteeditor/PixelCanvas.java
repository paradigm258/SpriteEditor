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

    public enum DRAWMODE{
        PEN,
        LINE,
        RECT,
        CUT,
        MOVE,
        FILL,
        CIRCLE,
        PICK
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

    public DRAWMODE mode = DRAWMODE.CUT;
    DRAWMODE preMode;

    ScaleGestureDetector sgd;
    GestureDetector gestureDetector;
    GestureDetector shapeMove;
    OnTouchListener onTouchListener;
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
                GraphicUlti.drawShape((int)pvTop,(int)pvLeft,preview,bitmap);
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
        onTouchListener = new OnTouchListener() {
            private int shapeStartX;
            private int shapeStartY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        shapeStartX = roundedX;
                        shapeStartY = roundedY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int preViewWidth = Math.abs(shapeStartX - roundedX) + 1 ;
                        int preViewHeight = Math.abs(shapeStartY - roundedY) + 1 ;
                        preview = Bitmap.createBitmap(preViewWidth, preViewHeight, Bitmap.Config.ARGB_8888);
                        pvLeft = Math.min(shapeStartX, roundedX);
                        pvTop = Math.min(shapeStartY, roundedY);
                        switch (mode) {
                            case CUT:
                                GraphicUlti.drawCut((int)pvTop,(int)pvLeft,bitmap,preview);
                                break;
                            case LINE:
                                int x1=shapeStartX - (int)pvLeft;
                                int y1=shapeStartY - (int)pvTop;
                                int x2=roundedX - (int)pvLeft;
                                int y2=roundedY - (int)pvTop;
                                GraphicUlti.drawLine(x1,y1,x2,y2,preview,brushColor,brushSize);
                                break;
                            case RECT:
                                GraphicUlti.drawRect(preview,brushColor,brushSize);
                                break;
                            case CIRCLE:
                                GraphicUlti.drawCircle();
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
                return false;
            }
        };
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

    public void setMode(DRAWMODE mode) {
        switch (mode){
            case PICK:
            case MOVE:
                preMode = mode;
            default:
                this.mode = mode;

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
            roundedX = (int) Math.floor(event.getX() / scale - left);
            roundedY = (int) Math.floor(event.getY() / scale - top);
            if(mode == DRAWMODE.MOVE){
                shapeMove.onTouchEvent(event);
            }else {
                if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
                    updateBitmapHistory();
                }
                switch (mode) {
                    case PEN:
                        System.out.println(scale);
                        System.out.println(event.getX() + " " + event.getY());
                        System.out.println(roundedX + " " + roundedY);
                        GraphicUlti.drawPoint(roundedX,roundedY,brushColor,bitmap,brushSize);
                        break;
                    case FILL:
                        GraphicUlti.floodFill(roundedX, roundedY, bitmap.getPixel(roundedX, roundedY),bitmap,brushColor);
                        break;
                    case PICK:
                        pickColor();
                        setMode(preMode==null?DRAWMODE.PEN:preMode);
                        break;
                    case CUT:
                    case LINE:
                    case RECT:
                    case CIRCLE:
                        onTouchListener.onTouch(this,event);
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

    private void pickColor(){
        brushColor = bitmap.getPixel(roundedX,roundedY);
    }

}
