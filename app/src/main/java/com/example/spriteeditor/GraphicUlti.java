package com.example.spriteeditor;

import android.graphics.Bitmap;

final class GraphicUlti {
    static void drawCut(int top, int left, Bitmap src, Bitmap dst){
        for(int x=0;x<dst.getWidth();x++){
            for(int y=0;y<dst.getHeight();y++){
                dst.setPixel(x,y,src.getPixel(x+left,y+top));
            }
        }
    }

    static void drawShape(int top, int left, Bitmap src, Bitmap dst){
        for(int y=0;y<src.getHeight();y++){
            for(int x=0;x<src.getWidth();x++){
                int pixel = src.getPixel(x,y);
                if(pixel!=0){
                    drawPixel(x+left,y+top,pixel,dst);
                }
            }
        }
    }

    static void drawRect(Bitmap bitmap, int brushColor,int size){
        for(int x=0;x<bitmap.getWidth();x++){
            drawPoint(x,0,brushColor,bitmap,size);
            drawPoint(x,bitmap.getHeight()-1,brushColor,bitmap,size);
        }
        for(int y=0;y<bitmap.getHeight();y++){
            drawPoint(0,y,brushColor,bitmap,size);
            drawPoint(bitmap.getWidth()-1,y,brushColor,bitmap,size);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    static void drawLine(int x1, int y1, int x2, int y2, Bitmap preview, int brushColor,int size) {
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
                drawPoint(y, x, brushColor,preview,size);
            } else {
                drawPoint(x, y, brushColor,preview,size);
            }
            error += (dy/dx);
            if (error > 0.5f) {
                y += yStep;
                error -= 1;
            }
        }
    }

    public static void floodFill(int x, int y, int floodColor, Bitmap bitmap, int brushColor) {
        if (x < 0 || x >= bitmap.getWidth() || y < 0 || y >= bitmap.getHeight()) return;
        int color  = bitmap.getPixel(x, y);
        if( color == brushColor ) return;
        if( color != floodColor) return;
        bitmap.setPixel(x, y, brushColor);
        floodFill(x + 1, y, color,bitmap,brushColor);
        floodFill(x, y + 1, color,bitmap,brushColor);
        floodFill(x - 1, y, color,bitmap,brushColor);
        floodFill(x, y - 1, color,bitmap,brushColor);
    }

    static void drawCircle(){

    }

    static void drawPoint(int x, int y, int color,Bitmap bitmap,int size){
        if(size == 0){
            drawPixel(x,y,color,bitmap);
        }else
        for(int i=-1;i<size;i++){
            for (int j=-1;j<size;j++){
                drawPixel(x+i,y+j,color,bitmap);
            }
        }

    }
    private static void drawPixel(int x, int y, int color, Bitmap bitmap){
        if (x >= 0 && x < bitmap.getWidth() && y >= 0 && y < bitmap.getHeight())
            bitmap.setPixel(x, y, color);
    }
}
