/*
 *     Created by Daniel Nadeau
 *     daniel.nadeau01@gmail.com
 *     danielnadeau.blogspot.com
 *
 *     Licensed to the Apache Software Foundation (ASF) under one
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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import org.namelessrom.devicecontrol.resources.R;

import java.util.ArrayList;

public class BarGraph extends View {

    private static final int   VALUE_FONT_SIZE          = 30;
    private static final int   AXIS_LABEL_FONT_SIZE     = 15;
    // How much space to leave between labels when shrunken. Increase for less space.
    private static final float LABEL_PADDING_MULTIPLIER = 1.6f;
    private static final int   ORIENTATION_HORIZONTAL   = 0;
    private static final int   ORIENTATION_VERTICAL     = 1;

    private final int mOrientation;
    private ArrayList<Bar> mBars       = new ArrayList<Bar>();
    private Paint          mPaint      = new Paint();
    private Rect           mBoundsRect = new Rect();
    private Rect           mTextRect   = new Rect();
    private boolean mShowAxis;
    private boolean mShowAxisLabel;
    private boolean mShowBarText;
    private boolean mShowPopup;
    private int mSelectedIndex = -1;
    private OnBarClickedListener mListener;
    private int                  mAxisColor;

    public BarGraph(Context context) {
        this(context, null);
    }

    public BarGraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BarGraph);
        try {
            mOrientation = a.getInt(R.styleable.BarGraph_orientation, ORIENTATION_VERTICAL);
            mAxisColor = a.getColor(R.styleable.BarGraph_barAxisColor, Color.LTGRAY);
            mShowAxis = a.getBoolean(R.styleable.BarGraph_barShowAxis, true);
            mShowAxisLabel = a.getBoolean(R.styleable.BarGraph_barShowAxisLabel, true);
            mShowBarText = a.getBoolean(R.styleable.BarGraph_barShowText, true);
            mShowPopup = a.getBoolean(R.styleable.BarGraph_barShowPopup, true);
        } finally {
            if (a != null) a.recycle();
        }
    }

    public void setShowAxis(boolean show) {
        mShowAxis = show;
    }

    public void setShowAxisLabel(boolean show) {
        mShowAxisLabel = show;
    }

    public void setShowBarText(boolean show) {
        mShowBarText = show;
    }

    public void setShowPopup(boolean show) {
        mShowPopup = show;
    }

    public void setBars(ArrayList<Bar> points) {
        this.mBars = points;
        postInvalidate();
    }

    public ArrayList<Bar> getBars() {
        return this.mBars;
    }

    public void setAxisColor(int axisColor) {
        mAxisColor = axisColor;
    }

    public void onDraw(Canvas canvas) {
        final Resources resources = getContext().getResources();

        canvas.drawColor(Color.TRANSPARENT);
        NinePatchDrawable popup = (NinePatchDrawable) resources.getDrawable(R.drawable.popup_black);

        float maxValue = 0;
        float padding = 7 * resources.getDisplayMetrics().density;
        float bottomPadding = 30 * resources.getDisplayMetrics().density;

        float usableHeight;
        if (mShowBarText) {
            this.mPaint.setTextSize(VALUE_FONT_SIZE * resources.getDisplayMetrics().scaledDensity);
            this.mPaint.getTextBounds("$", 0, 1, mTextRect);
            if (mShowPopup) {
                usableHeight = getHeight() - bottomPadding
                        - Math.abs(mTextRect.top - mTextRect.bottom)
                        - 24 * resources.getDisplayMetrics().density;
            } else {
                usableHeight = getHeight() - bottomPadding
                        - Math.abs(mTextRect.top - mTextRect.bottom)
                        - 18 * resources.getDisplayMetrics().density;
            }
        } else {
            usableHeight = getHeight() - bottomPadding;
        }

        // Draw x-axis line
        if (mShowAxis) {
            mPaint.setColor(mAxisColor);
            mPaint.setStrokeWidth(2 * resources.getDisplayMetrics().density);
            mPaint.setAntiAlias(true);
            canvas.drawLine(0,
                    getHeight() - bottomPadding + 10 * resources.getDisplayMetrics().density,
                    getWidth(),
                    getHeight() - bottomPadding + 10 * resources.getDisplayMetrics().density,
                    mPaint);
        }
        float barWidth = (getWidth() - (padding * 2) * mBars.size()) / mBars.size();

        // Maximum y value = sum of all values.
        for (final Bar bar : mBars) {
            if (bar.getValue() > maxValue) {
                maxValue = bar.getValue();
            }
        }
        if (maxValue == 0) {
            maxValue = 1;
        }

        int count = 0;

        //Calculate the maximum text size for all the axis labels
        this.mPaint.setTextSize(AXIS_LABEL_FONT_SIZE
                * resources.getDisplayMetrics().scaledDensity);
        for (final Bar bar : mBars) {
            int left = (int) ((padding * 2) * count + padding + barWidth * count);
            int right = (int) ((padding * 2) * count + padding + barWidth * (count + 1));
            float textWidth = this.mPaint.measureText(bar.getName());
            // Decrease text size to fit and not overlap with other labels.
            while (right - left + (padding * LABEL_PADDING_MULTIPLIER) < textWidth) {
                this.mPaint.setTextSize(this.mPaint.getTextSize() - 1);
                textWidth = this.mPaint.measureText(bar.getName());
            }
            count++;
        }
        //Save it to use later
        float labelTextSize = mPaint.getTextSize();

        count = 0;
        SparseArray<Float> valueTextSizes = new SparseArray<Float>();
        for (final Bar bar : mBars) {
            // Set bar bounds
            int left = (int) ((padding * 2) * count + padding + barWidth * count);
            int top = (int) (getHeight() - bottomPadding
                    - (usableHeight * (bar.getValue() / maxValue)));
            int right = (int) ((padding * 2) * count + padding + barWidth * (count + 1));
            int bottom = (int) (getHeight() - bottomPadding);
            mBoundsRect.set(left, top, right, bottom);

            // Draw bar
            if (count == mSelectedIndex && null != mListener) {
                this.mPaint.setColor(bar.getSelectedColor());
            } else {
                this.mPaint.setColor(bar.getColor());
            }
            canvas.drawRect(mBoundsRect, this.mPaint);

            // Create selection region
            Path p = bar.getPath();
            p.reset();
            p.addRect(mBoundsRect.left,
                    mBoundsRect.top,
                    mBoundsRect.right,
                    mBoundsRect.bottom,
                    Path.Direction.CW);
            bar.getRegion().set(mBoundsRect.left,
                    mBoundsRect.top,
                    mBoundsRect.right,
                    mBoundsRect.bottom);

            // Draw x-axis label text
            if (mShowAxisLabel) {
                this.mPaint.setColor(bar.getLabelColor());
                mPaint.setTextSize(labelTextSize);
                float textWidth = this.mPaint.measureText(bar.getName());
                int x = (int) (((mBoundsRect.left + mBoundsRect.right) / 2) - (textWidth / 2));
                int y = (int) (getHeight() - 3 * resources.getDisplayMetrics().scaledDensity);
                canvas.drawText(bar.getName(), x, y, this.mPaint);
            }

            // Draw value text
            if (mShowBarText) {
                this.mPaint.setTextSize(VALUE_FONT_SIZE
                        * resources.getDisplayMetrics().scaledDensity);
                this.mPaint.setColor(bar.getValueColor());
                this.mPaint.getTextBounds(bar.getValueString(), 0, 1, mTextRect);

                int boundLeft = (int) (((mBoundsRect.left + mBoundsRect.right) / 2)
                        - (this.mPaint.measureText(bar.getValueString()) / 2)
                        - 10 * resources.getDisplayMetrics().density);
                int boundTop = (int) (mBoundsRect.top + (mTextRect.top - mTextRect.bottom)
                        - 18 * resources.getDisplayMetrics().density);
                int boundRight = (int) (((mBoundsRect.left + mBoundsRect.right) / 2)
                        + (this.mPaint.measureText(bar.getValueString()) / 2)
                        + 10 * resources.getDisplayMetrics().density);

                // Limit popup width to bar width
                if (boundLeft < mBoundsRect.left) {
                    boundLeft = mBoundsRect.left - ((int) padding / 2);
                }
                if (boundRight > mBoundsRect.right) {
                    boundRight = mBoundsRect.right + ((int) padding / 2);
                }

                if (mShowPopup) {
                    popup.setBounds(boundLeft, boundTop, boundRight, mBoundsRect.top);
                    popup.draw(canvas);
                }

                // Check cache to see if we've done this calculation before
                if (0 > valueTextSizes.indexOfKey(bar.getValueString().length())) {
                    while (this.mPaint.measureText(bar.getValueString()) > boundRight - boundLeft) {
                        this.mPaint.setTextSize(this.mPaint.getTextSize() - (float) 1);
                    }
                    valueTextSizes.put(bar.getValueString().length(), mPaint.getTextSize());
                } else {
                    this.mPaint.setTextSize(valueTextSizes.get(bar.getValueString().length()));
                }
                canvas.drawText(bar.getValueString(),
                        (int) (((mBoundsRect.left + mBoundsRect.right) / 2)
                                - (this.mPaint.measureText(bar.getValueString())) / 2),
                        mBoundsRect.top - (mBoundsRect.top - boundTop) / 2f
                                + (float) Math.abs(mTextRect.top - mTextRect.bottom) / 2f * 0.7f,
                        this.mPaint
                );
            }
            count++;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        int count = 0;
        Region r = new Region();
        for (Bar bar : mBars) {
            r.setPath(bar.getPath(), bar.getRegion());
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
        // Reset selection
        if (MotionEvent.ACTION_UP == event.getAction()
                || MotionEvent.ACTION_CANCEL == event.getAction()) {
            mSelectedIndex = -1;
            postInvalidate();
        }
        return true;
    }

    public void setOnBarClickedListener(OnBarClickedListener listener) {
        this.mListener = listener;
    }

    public interface OnBarClickedListener {
        abstract void onClick(int index);
    }
}
