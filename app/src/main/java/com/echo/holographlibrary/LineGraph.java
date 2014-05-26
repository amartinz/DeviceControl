/*
 *        Created by Daniel Nadeau
 *        daniel.nadeau01@gmail.com
 *        danielnadeau.blogspot.com
 *
 *        Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.echo.holographlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import org.namelessrom.devicecontrol.R;

import java.util.ArrayList;

public class LineGraph extends View {

    private static final int DEFAULT_PADDING = 10;
    private final int   mDipPadding;
    private final int   mFillColor;
    private final int   mAxisColor;
    private final float mStrokeWidth;
    private final int   mStrokeSpacing;
    private ArrayList<Line> mLines = new ArrayList<Line>();
    private Paint           mPaint = new Paint();
    private float           mMinY  = 0, mMinX = 0;
    private float mMaxY = 0, mMaxX = 0;
    private double  mRangeYRatio   = 0;
    private double  mRangeXRatio   = 0;
    private boolean mUserSetMaxX   = false;
    private int     mLineToFill    = -1;
    private int     mSelectedIndex = -1;
    private OnPointClickedListener mListener;
    private Bitmap                 mFullImage;
    // Since this is a new addition, it has to default to false to be backwards compatible
    private boolean                mUseDips;
    private Path               mPath     = new Path();
    private PorterDuffXfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private Canvas mCanvas;

    public LineGraph(Context context) {
        this(context, null);
    }

    public LineGraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDipPadding = getPixelForDip(DEFAULT_PADDING);

        final TypedArray a =
                context.getTheme().obtainStyledAttributes(attrs, R.styleable.LineGraph, 0, 0);
        try {
            mFillColor = a.getColor(R.styleable.LineGraph_lineStrokeColor, Color.BLACK);
            mAxisColor = a.getColor(R.styleable.LineGraph_lineAxisColor, Color.LTGRAY);
            mStrokeWidth = a.getDimension(R.styleable.LineGraph_lineStrokeWidth, 2);
            mStrokeSpacing = a.getDimensionPixelSize(R.styleable.LineGraph_lineStrokeSpacing, 10);
            mUseDips = a.getBoolean(R.styleable.LineGraph_lineUseDip, false);
        } finally {
            if (a != null) a.recycle();
        }
    }

    public boolean isUsingDips() {
        return mUseDips;
    }

    public void setUsingDips(boolean treatSizesAsDips) {
        this.mUseDips = treatSizesAsDips;
    }

    public void removeAllLines() {
        while (mLines.size() > 0) {
            mLines.remove(0);
        }
        postInvalidate();
    }

    public void addLine(Line line) {
        mLines.add(line);
        postInvalidate();
    }

    public void addPointToLine(int lineIndex, double x, double y) {
        addPointToLine(lineIndex, (float) x, (float) y);
    }

    public void addPointToLine(int lineIndex, float x, float y) {
        LinePoint p = new LinePoint(x, y);

        addPointToLine(lineIndex, p);
    }

    public double getRangeYRatio() {
        return mRangeYRatio;
    }

    public void setRangeYRatio(double rr) {
        this.mRangeYRatio = rr;
    }

    public double getRangeXRatio() {
        return mRangeXRatio;
    }

    public void setRangeXRatio(double rr) {
        this.mRangeXRatio = rr;
    }

    public void addPointToLine(int lineIndex, LinePoint point) {
        Line line = getLine(lineIndex);
        line.addPoint(point);
        mLines.set(lineIndex, line);
        resetLimits();
        postInvalidate();
    }

    public void addPointsToLine(int lineIndex, LinePoint[] points) {
        Line line = getLine(lineIndex);
        for (LinePoint point : points) {
            line.addPoint(point);
        }
        mLines.set(lineIndex, line);
        resetLimits();
        postInvalidate();
    }

    public void removeAllPointsAfter(int lineIndex, double x) {
        removeAllPointsBetween(lineIndex, x, getMaxX());
    }

    public void removeAllPointsBefore(int lineIndex, double x) {
        removeAllPointsBetween(lineIndex, getMinX(), x);
    }

    public void removeAllPointsBetween(int lineIndex, double startX, double finishX) {
        Line line = getLine(lineIndex);
        LinePoint[] pts = new LinePoint[line.getPoints().size()];
        pts = line.getPoints().toArray(pts);
        for (LinePoint point : pts) {
            if (point.getX() >= startX && point.getX() <= finishX) {
                line.removePoint(point);
            }
        }
        mLines.set(lineIndex, line);
        resetLimits();
        postInvalidate();
    }

    public void removePointsFromLine(int lineIndex, LinePoint[] points) {
        Line line = getLine(lineIndex);
        for (LinePoint point : points) {
            line.removePoint(point);
        }
        mLines.set(lineIndex, line);
        resetLimits();
        postInvalidate();
    }

    public void removePointFromLine(int lineIndex, float x, float y) {
        LinePoint p = null;
        Line line = getLine(lineIndex);
        p = line.getPoint(x, y);
        removePointFromLine(lineIndex, p);
    }

    public void removePointFromLine(int lineIndex, LinePoint point) {
        Line line = getLine(lineIndex);
        line.removePoint(point);
        mLines.set(lineIndex, line);
        resetLimits();
        postInvalidate();
    }

    public void resetYLimits() {
        float range = getMaxY() - getMinY();
        setRangeY(getMinY() - range * getRangeYRatio(), getMaxY() + range * getRangeYRatio());
    }

    public void resetXLimits() {
        float range = getMaxX() - getMinX();
        setRangeX(getMinX() - range * getRangeXRatio(), getMaxX() + range * getRangeXRatio());
    }

    public void resetLimits() {
        resetYLimits();
        resetXLimits();
    }

    public ArrayList<Line> getLines() {
        return mLines;
    }

    public void setLineToFill(int indexOfLine) {
        this.mLineToFill = indexOfLine;
        postInvalidate();
    }

    public int getLineToFill() {
        return mLineToFill;
    }

    public void setLines(ArrayList<Line> lines) {
        this.mLines = lines;
    }

    public Line getLine(int index) {
        return mLines.get(index);
    }

    public int getSize() {
        return mLines.size();
    }

    public void setRangeY(float min, float max) {
        mMinY = min;
        mMaxY = max;
    }

    private void setRangeY(double min, double max) {
        mMinY = (float) min;
        mMaxY = (float) max;
    }

    public void setRangeX(float min, float max) {
        mMinX = min;
        mMaxX = max;
        mUserSetMaxX = true;
    }

    private void setRangeX(double min, double max) {
        mMinX = (float) min;
        mMaxX = (float) max;
    }

    public float getMaxY() {
        float max = mLines.get(0).getPoint(0).getY();
        for (Line line : mLines) {
            for (LinePoint point : line.getPoints()) {
                max = point.getY() > max ? point.getY() : max;
            }
        }
        mMaxY = max;
        return mMaxY;
    }

    public float getMinY() {
        float min = mLines.get(0).getPoint(0).getY();
        for (Line line : mLines) {
            for (LinePoint point : line.getPoints()) {
                min = point.getY() < min ? point.getY() : min;
            }
        }
        mMinY = min;
        return mMinY;
    }

    public float getMinLimY() {
        return mMinY;
    }

    public float getMaxLimY() {
        return mMaxY;
    }

    public float getMinLimX() {
        return mMinX;
    }

    public float getMaxLimX() {
        if (mUserSetMaxX) {
            return mMaxX;
        } else {
            return getMaxX();
        }
    }

    public float getMaxX() {
        float max = mLines.size() > 0 ? mLines.get(0).getPoint(0).getX() : 0;
        for (Line line : mLines) {
            for (LinePoint point : line.getPoints()) {
                max = point.getX() > max ? point.getX() : max;
            }
        }
        mMaxX = max;
        return mMaxX;

    }

    public float getMinX() {
        float min = mLines.size() > 0 ? mLines.get(0).getPoint(0).getX() : 0;
        for (Line line : mLines) {
            for (LinePoint point : line.getPoints()) {
                min = point.getX() < min ? point.getX() : min;
            }
        }
        mMinX = min;
        return mMinX;
    }

    public void onDraw(Canvas canvas) {
        if (null == mFullImage) {
            mFullImage = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mFullImage);
        }

        mCanvas.drawColor(Color.WHITE);
        mPaint.reset();
        float bottomPadding = 10, topPadding = 10;
        float sidePadding = 10;
        if (mUseDips) {
            bottomPadding = mDipPadding;
            topPadding = mDipPadding;
            sidePadding = mDipPadding;
        }
        float usableHeight = getHeight() - bottomPadding - topPadding;
        float usableWidth = getWidth() - 2 * sidePadding;

        float maxY = getMaxLimY();
        float minY = getMinLimY();
        float maxX = getMaxLimX();
        float minX = getMinLimX();


        int lineCount = 0;
        for (Line line : mLines) {
            int count = 0;
            float lastXPixels = 0, newYPixels = 0;
            float lastYPixels = 0, newXPixels = 0;

            if (lineCount == mLineToFill) {
                // Draw lines
                mPaint.setColor(mFillColor);
                mPaint.setStrokeWidth(mStrokeWidth);
                for (int i = 10; i - getWidth() < getHeight(); i = i + mStrokeSpacing) {
                    mCanvas.drawLine(
                            i, getHeight() - bottomPadding,
                            0, getHeight() - bottomPadding - i, mPaint);
                }

                // Erase lines above the line
                mPaint.reset();
                mPaint.setXfermode(mXfermode);
                for (LinePoint p : line.getPoints()) {
                    float yPercent = (p.getY() - minY) / (maxY - minY);
                    float xPercent = (p.getX() - minX) / (maxX - minX);
                    if (count == 0) {
                        lastXPixels = sidePadding + (xPercent * usableWidth);
                        lastYPixels = getHeight() - bottomPadding - (usableHeight * yPercent);
                        mPath.moveTo(lastXPixels, lastYPixels);
                    } else {
                        newXPixels = sidePadding + (xPercent * usableWidth);
                        newYPixels = getHeight() - bottomPadding - (usableHeight * yPercent);
                        mPath.lineTo(newXPixels, newYPixels);
                        mPath.moveTo(lastXPixels, lastYPixels);
                        mPath.lineTo(newXPixels, newYPixels);
                        mPath.lineTo(newXPixels, 0);
                        mPath.lineTo(lastXPixels, 0);
                        mPath.close();
                        mCanvas.drawPath(mPath, mPaint);
                        lastXPixels = newXPixels;
                        lastYPixels = newYPixels;
                    }
                    count++;
                }

                mPath.reset();
                mPath.moveTo(0, getHeight() - bottomPadding);
                mPath.lineTo(sidePadding, getHeight() - bottomPadding);
                mPath.lineTo(sidePadding, 0);
                mPath.lineTo(0, 0);
                mPath.close();
                mCanvas.drawPath(mPath, mPaint);

                mPath.reset();
                mPath.moveTo(getWidth(), getHeight() - bottomPadding);
                mPath.lineTo(getWidth() - sidePadding, getHeight() - bottomPadding);
                mPath.lineTo(getWidth() - sidePadding, 0);
                mPath.lineTo(getWidth(), 0);
                mPath.close();

                mCanvas.drawPath(mPath, mPaint);
            }
            lineCount++;
        }

        // Draw x-axis line
        mPaint.reset();
        mPaint.setColor(mAxisColor);
        mPaint.setStrokeWidth(2 * getResources().getDisplayMetrics().density);
        mPaint.setAntiAlias(true);
        mCanvas.drawLine(
                sidePadding, getHeight() - bottomPadding,
                getWidth() - sidePadding, getHeight() - bottomPadding, mPaint);
        mPaint.reset();

        // Draw lines
        for (Line line : mLines) {
            int count = 0;
            float lastXPixels = 0, newYPixels = 0;
            float lastYPixels = 0, newXPixels = 0;

            mPaint.setColor(line.getColor());
            mPaint.setStrokeWidth(getStrokeWidth(line));

            for (LinePoint p : line.getPoints()) {
                float yPercent = (p.getY() - minY) / (maxY - minY);
                float xPercent = (p.getX() - minX) / (maxX - minX);
                if (count == 0) {
                    lastXPixels = sidePadding + (xPercent * usableWidth);
                    lastYPixels = getHeight() - bottomPadding - (usableHeight * yPercent);
                } else {
                    newXPixels = sidePadding + (xPercent * usableWidth);
                    newYPixels = getHeight() - bottomPadding - (usableHeight * yPercent);
                    mCanvas.drawLine(lastXPixels, lastYPixels, newXPixels, newYPixels, mPaint);
                    lastXPixels = newXPixels;
                    lastYPixels = newYPixels;
                }
                count++;
            }
        }

        // Draw points
        int pointCount = 0;
        for (Line line : mLines) {
            mPaint.setColor(line.getColor());
            mPaint.setStrokeWidth(getStrokeWidth(line));
            mPaint.setStrokeCap(Paint.Cap.ROUND);

            if (line.isShowingPoints()) {
                for (LinePoint p : line.getPoints()) {
                    float yPercent = (p.getY() - minY) / (maxY - minY);
                    float xPercent = (p.getX() - minX) / (maxX - minX);
                    float xPixels = sidePadding + (xPercent * usableWidth);
                    float yPixels = getHeight() - bottomPadding - (usableHeight * yPercent);

                    int outerRadius;
                    if (line.isUsingDips()) {
                        outerRadius = getPixelForDip(line.getStrokeWidth() + 4);
                    } else {
                        outerRadius = line.getStrokeWidth() + 4;
                    }
                    int innerRadius = outerRadius / 2;

                    mPaint.setColor(p.getColor());
                    mCanvas.drawCircle(xPixels, yPixels, outerRadius, mPaint);
                    mPaint.setColor(Color.WHITE);
                    mCanvas.drawCircle(xPixels, yPixels, innerRadius, mPaint);

                    // Create selection region
                    Path path = p.getPath();
                    path.reset();
                    outerRadius *= 2;
                    path.addCircle(xPixels, yPixels, outerRadius, Direction.CW);
                    p.getRegion().set((int) (xPixels - outerRadius),
                            (int) (yPixels - outerRadius),
                            (int) (xPixels + outerRadius),
                            (int) (yPixels + outerRadius));

                    // Draw selection
                    if (mSelectedIndex == pointCount && mListener != null) {
                        mPaint.setColor(p.getSelectedColor());
                        mCanvas.drawPath(p.getPath(), mPaint);
                        mPaint.setAlpha(255);
                    }

                    pointCount++;
                }
            }
        }
        canvas.drawBitmap(mFullImage, 0, 0, null);
    }

    private int getStrokeWidth(Line line) {
        int strokeWidth;
        if (line.isUsingDips()) {
            strokeWidth = getPixelForDip(line.getStrokeWidth());
        } else {
            strokeWidth = line.getStrokeWidth();
        }
        return strokeWidth;
    }

    private int getPixelForDip(int dipValue) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dipValue,
                getResources().getDisplayMetrics());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        int count = 0;
        int lineCount = 0;
        int pointCount;

        Region r = new Region();
        for (Line line : mLines) {
            pointCount = 0;
            for (LinePoint p : line.getPoints()) {
                r.setPath(p.getPath(), p.getRegion());

                switch (event.getAction()) {
                    default:
                        break;
                    case MotionEvent.ACTION_DOWN:
                        if (r.contains(point.x, point.y)) {
                            mSelectedIndex = count;
                            postInvalidate();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (count == mSelectedIndex
                                && mListener != null
                                && r.contains(point.x, point.y)) {
                            mListener.onClick(lineCount, pointCount);
                        }
                        break;
                }
                pointCount++;
                count++;
            }
            lineCount++;
        }
        // Reset selection
        if (MotionEvent.ACTION_UP == event.getAction()
                || MotionEvent.ACTION_CANCEL == event.getAction()) {
            mSelectedIndex = -1;
            postInvalidate();
        }
        return true;
    }

    public void setOnPointClickedListener(OnPointClickedListener listener) {
        this.mListener = listener;
    }

    public interface OnPointClickedListener {
        abstract void onClick(int lineIndex, int pointIndex);
    }
}
