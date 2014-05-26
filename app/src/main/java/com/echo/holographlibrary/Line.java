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

import java.util.ArrayList;

public class Line {
    private ArrayList<LinePoint> mPoints = new ArrayList<LinePoint>();
    private int mColor;
    private boolean mShowPoints  = true;
    // 6 has been the default prior to the addition of custom stroke widths
    private int     mStrokeWidth = 6;
    // Since this is a new addition, it has to default to false to be backwards compatible
    private boolean mUseDips     = false;


    public boolean isUsingDips() {
        return mUseDips;
    }

    public void setUsingDips(boolean treatSizesAsDips) {
        this.mUseDips = treatSizesAsDips;
    }

    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        if (strokeWidth < 0) {
            throw new IllegalArgumentException("strokeWidth must not be less than zero");
        }
        this.mStrokeWidth = strokeWidth;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public ArrayList<LinePoint> getPoints() {
        return mPoints;
    }

    public void setPoints(ArrayList<LinePoint> points) {
        this.mPoints = points;
    }

    public void addPoint(LinePoint point) {
        LinePoint p;
        for (int i = 0; i < mPoints.size(); i++) {
            p = mPoints.get(i);
            if (point.getX() < p.getX()) {
                mPoints.add(i, point);
                return;
            }
        }
        mPoints.add(point);
    }

    public void removePoint(LinePoint point) {
        mPoints.remove(point);
    }

    public LinePoint getPoint(int index) {
        return mPoints.get(index);
    }

    public LinePoint getPoint(float x, float y) {
        LinePoint p;
        for (int i = 0; i < mPoints.size(); i++) {
            p = mPoints.get(i);
            if (p.getX() == x && p.getY() == y) {
                return p;
            }
        }
        return null;
    }

    public int getSize() {
        return mPoints.size();
    }

    public boolean isShowingPoints() {
        return mShowPoints;
    }

    public void setShowingPoints(boolean showPoints) {
        this.mShowPoints = showPoints;
    }

}
