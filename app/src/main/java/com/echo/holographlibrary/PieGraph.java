/*
 * 	   Created by Daniel Nadeau
 * 	   daniel.nadeau01@gmail.com
 * 	   danielnadeau.blogspot.com
 * 
 * 	   Licensed to the Apache Software Foundation (ASF) under one
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.namelessrom.devicecontrol.R;

import java.util.ArrayList;

public class PieGraph extends View {

    private int mPadding;
    private int mInnerCircleRatio;
    private ArrayList<PieSlice> mSlices        = new ArrayList<PieSlice>();
    private Paint               mPaint         = new Paint();
    private int                 mSelectedIndex = -1;
    private OnSliceClickedListener mListener;
    private boolean mDrawCompleted = false;
    private RectF   mRectF         = new RectF();

    public PieGraph(Context context) {
        this(context, null);
    }

    public PieGraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        final TypedArray a =
                context.getTheme().obtainStyledAttributes(attrs, R.styleable.PieGraph, 0, 0);
        try {
            mInnerCircleRatio = a.getInt(R.styleable.PieGraph_pieInnerCircleRatio, 0);
            mPadding = a.getDimensionPixelSize(R.styleable.PieGraph_pieSlicePadding, 0);
        } finally {
            if (a != null) a.recycle();
        }
    }

    public void onDraw(Canvas canvas) {
        float midX, midY, radius, innerRadius;

        canvas.drawColor(Color.TRANSPARENT);
        mPaint.reset();
        mPaint.setAntiAlias(true);

        float currentAngle = 270;
        float currentSweep = 0;
        int totalValue = 0;

        midX = getWidth() / 2;
        midY = getHeight() / 2;
        if (midX < midY) {
            radius = midX;
        } else {
            radius = midY;
        }
        radius -= mPadding;
        innerRadius = radius * mInnerCircleRatio / 255;

        for (PieSlice slice : mSlices) {
            totalValue += slice.getValue();
        }

        int count = 0;
        for (PieSlice slice : mSlices) {
            Path p = slice.getPath();
            p.reset();

            if (mSelectedIndex == count && mListener != null) {
                mPaint.setColor(slice.getSelectedColor());
            } else {
                mPaint.setColor(slice.getColor());
            }
            currentSweep = (slice.getValue() / totalValue) * (360);
            mRectF.set(midX - radius, midY - radius, midX + radius, midY + radius);
            p.arcTo(mRectF,
                    currentAngle + mPadding, currentSweep - mPadding);
            mRectF.set(midX - innerRadius, midY - innerRadius,
                    midX + innerRadius, midY + innerRadius);
            p.arcTo(mRectF,
                    (currentAngle + mPadding) + (currentSweep - mPadding),
                    -(currentSweep - mPadding));
            p.close();

            // Create selection region
            Region r = slice.getRegion();
            r.set((int) (midX - radius),
                    (int) (midY - radius),
                    (int) (midX + radius),
                    (int) (midY + radius));
            canvas.drawPath(p, mPaint);
            currentAngle = currentAngle + currentSweep;

            count++;
        }
        mDrawCompleted = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDrawCompleted) {
            Point point = new Point();
            point.x = (int) event.getX();
            point.y = (int) event.getY();

            int count = 0;
            Region r = new Region();
            for (PieSlice slice : mSlices) {
                r.setPath(slice.getPath(), slice.getRegion());
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
                            mListener.onClick(mSelectedIndex);
                        }
                        break;
                }
                count++;
            }
        }
        // Reset selection
        if (MotionEvent.ACTION_UP == event.getAction()
                || MotionEvent.ACTION_CANCEL == event.getAction()) {
            mSelectedIndex = -1;
            postInvalidate();
        }
        return true;
    }

    public void setPadding(int padding) {
        mPadding = padding;
        postInvalidate();
    }

    public void setInnerCircleRatio(int innerCircleRatio) {
        mInnerCircleRatio = innerCircleRatio;
        postInvalidate();
    }

    public ArrayList<PieSlice> getSlices() {
        return mSlices;
    }

    public void setSlices(ArrayList<PieSlice> slices) {
        this.mSlices = slices;
        postInvalidate();
    }

    public PieSlice getSlice(int index) {
        return mSlices.get(index);
    }

    public void addSlice(PieSlice slice) {
        this.mSlices.add(slice);
        postInvalidate();
    }

    public void setOnSliceClickedListener(OnSliceClickedListener listener) {
        this.mListener = listener;
    }

    public void removeSlices() {
        this.mSlices.clear();
        postInvalidate();
    }

    public interface OnSliceClickedListener {
        public abstract void onClick(int index);
    }
}
