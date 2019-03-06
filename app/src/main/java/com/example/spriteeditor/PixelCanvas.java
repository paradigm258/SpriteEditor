package com.example.spriteeditor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class PixelCanvas extends View {
    Bitmap bitmap;
    private float touchStartX;
    private float touchStartY;
    private float left =0 ;
    private float top =0 ;
    private float scale = 1;
    private float minScale ;
    Paint paint;
    ScaleGestureDetector sgd;
    float width;
    float height;
    float imgW;
    float imgH;
    public PixelCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        sgd = new ScaleGestureDetector(context,new ScaleListener());
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setDither(false);
        paint.setFilterBitmap(false);
    }
    public int getImgWidth(){
        return bitmap.getWidth();
    }
    public int getImgHeight(){
        return bitmap.getHeight();
    }
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        width = getWidth();
        height = getHeight();
        imgW = getImgWidth();
        imgH = getImgHeight();
        float scaleX = (float)getWidth()/(float)getImgWidth();
        float scaleY = (float)getHeight()/(float)getImgHeight();
        minScale = Math.min(scaleX,scaleY);
        scale = minScale;
        performClick();
        invalidate();
    }
    @Override
    public void draw(Canvas canvas) {
        canvas.scale(scale,scale);
        canvas.drawBitmap(bitmap,left,top,paint);
        super.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        sgd.onTouchEvent(event);
//        event.getPointerId(0);
//        switch (event.getActionMasked()){
//            case MotionEvent.ACTION_DOWN:
//                touchStartX=event.getX();
//                touchStartY=event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float x = event.getX();
//                float y = event.getY();
//                float deltaX = x-touchStartX;
//                float deltaY = y-touchStartY;
//                deltaX/=scale;
//                deltaY/=scale;
//                hPan(deltaX);
//                vPan(deltaY);
//                touchStartX=x;
//                touchStartY=y;
//                break;
//            default:
//                break;
//        }
//        performClick();
//        invalidate();

        int pointerCount = event.getPointerCount();
        if(pointerCount>1){
            return true;
        }
        boolean setPixel = true;
        event.getPointerId(0);
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                touchStartX=event.getX();
                touchStartY=event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                touchStartX=event.getX();
                touchStartY=event.getY();
                break;
            default:
                setPixel = false;
                break;
        }

        if(setPixel){
            int roundedX = Math.round(touchStartX/scale);
            int roundedY = Math.round(touchStartY/scale);
            System.out.println(scale);
            System.out.println(event.getX() + " " + event.getY());
            System.out.println(touchStartX + " " + touchStartY);
            Bitmap newBitmap = bitmap.copy( Bitmap.Config.ARGB_8888 , true);
            newBitmap.setPixel(roundedX, roundedY, Color.argb(255,0,0,0));
            this.setBitmap(newBitmap);
        }

        performClick();
        invalidate();
        return true;
    }
    private void vPan(float delta){
        float newTop = top+delta;
        float edge = (height/scale)-imgH;
        if(delta>0&&newTop>0){
            top = 0;
        }else if(delta<0&&newTop<edge){
            if(edge<0)
            top = edge;
        }else{
            top = newTop;
        }
    }
    private void hPan(float delta){
        float newLeft = left+delta;
        float edge = (width/scale)-imgH;
        if(delta>0&&newLeft>0){
            left = 0;
        }else if(delta<0&&newLeft<edge){
            if(edge<0)
            left = edge;
        }else{
            left = newLeft;
        }
    }
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale = scale * detector.getScaleFactor();
            scale = Math.max(minScale,Math.min(scale,65));
            return true;
        }
    }
}
