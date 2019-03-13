package com.example.spriteeditor;

import android.graphics.Bitmap;
import android.graphics.Point;
import java.util.ArrayDeque;
import java.util.Queue;

final class GraphicUlti {
    static void drawCut(int top, int left, Bitmap src, Bitmap dst) {
        for (int x = 0; x < dst.getWidth(); x++) {
            for (int y = 0; y < dst.getHeight(); y++) {
                dst.setPixel(x, y, src.getPixel(x + left, y + top));
            }
        }
    }

    static void drawShape(int top, int left, Bitmap src, Bitmap dst) {
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int pixel = src.getPixel(x, y);
                if (pixel != 0) {
                    drawPixel(x + left, y + top, pixel, dst);
                }
            }
        }
    }

    static void drawRect(Bitmap bitmap, int brushColor, int size) {
        for (int x = 0; x < bitmap.getWidth(); x++) {
            drawPoint(x, 0, brushColor, bitmap, size);
            drawPoint(x, bitmap.getHeight() - 1, brushColor, bitmap, size);
        }
        for (int y = 0; y < bitmap.getHeight(); y++) {
            drawPoint(0, y, brushColor, bitmap, size);
            drawPoint(bitmap.getWidth() - 1, y, brushColor, bitmap, size);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    static void drawLine(int x1, int y1, int x2, int y2, Bitmap preview, int brushColor, int size) {
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

        float error = dx / 2;
        int yStep = (y1 < y2) ? 1 : -1;
        int y = y1;

        int maxX = x2;
        for (int x = x1; x <= maxX; x++) {
            if (steep) {
                drawPoint(y, x, brushColor, preview, size);
            } else {
                drawPoint(x, y, brushColor, preview, size);
            }
            error -= dy;
            if (error < 0) {
                y += yStep;
                error += dx;
            }
        }
    }

    public static void floodFill(int x, int y, int floodColor, Bitmap bitmap, int brushColor) {
        if (floodColor == brushColor) return;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        boolean[][] visit = new boolean[width][height];
        Queue<Point> queue = new ArrayDeque<>();
        queue.add(new Point(x, y));
        while (!queue.isEmpty()) {
            Point n = queue.remove();
            boolean outOfBound = n.x < 0 || n.x >= width || n.y < 0 || n.y >= height;
            if (!outOfBound && !visit[n.x][n.y] && bitmap.getPixel(n.x, n.y) == floodColor) {
                visit[n.x][n.y] = true;
                bitmap.setPixel(n.x, n.y, brushColor);
                queue.add(new Point(n.x - 1, n.y));
                queue.add(new Point(n.x, n.y + 1));
                queue.add(new Point(n.x + 1, n.y));
                queue.add(new Point(n.x, n.y - 1));
            }
        }
    }

    static void drawCircle(int brushColor, Bitmap bitmap, int size) {
        int radius = bitmap.getWidth()/2+1;
        int x0 = radius-1;
        int y0 = radius-1;

        int x = radius - 1;
        int y = 0;
        int dx = 1;
        int dy = 1;
        int err = dx - (radius*2);
        drawPoint(x0,y0,brushColor,bitmap,size);
        while (x >= y) {
            drawPoint(x0 + x, y0 + y, brushColor, bitmap, size);
            drawPoint(x0 + y, y0 + x, brushColor, bitmap, size);
            drawPoint(x0 - y, y0 + x, brushColor, bitmap, size);
            drawPoint(x0 - x, y0 + y, brushColor, bitmap, size);
            drawPoint(x0 - x, y0 - y, brushColor, bitmap, size);
            drawPoint(x0 - y, y0 - x, brushColor, bitmap, size);
            drawPoint(x0 + y, y0 - x, brushColor, bitmap, size);
            drawPoint(x0 + x, y0 - y, brushColor, bitmap, size);

            if (err <= 0) {
                y++;
                err += dy;
                dy += 2;
            }

            if (err > 0) {
                x--;
                dx += 2;
                err += dx - (radius*2);
            }
        }
    }


    static void drawPoint(int x, int y, int color, Bitmap bitmap, int size) {
        if (size == 0) {
            drawPixel(x, y, color, bitmap);
        } else
            for (int i = -1; i < size; i++) {
                for (int j = -1; j < size; j++) {
                    drawPixel(x + i, y + j, color, bitmap);
                }
            }

    }

    private static void drawPixel(int x, int y, int color, Bitmap bitmap) {
        if (x >= 0 && x < bitmap.getWidth() && y >= 0 && y < bitmap.getHeight())
            bitmap.setPixel(x, y, color);
    }
}
