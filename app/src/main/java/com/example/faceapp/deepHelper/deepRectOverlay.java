package com.example.faceapp.deepHelper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class deepRectOverlay extends  deepGraphicOverlay.Graphic {
    private int mRectColor = Color.GREEN;
    private float mStrokeWidth = 4.0f;
    private Paint mRectPaint;
    private deepGraphicOverlay deepgraphicoverlay;
    private Rect rect;
    public deepRectOverlay(deepGraphicOverlay deepGraphicOverlay, Rect rect) {
        super(deepGraphicOverlay);
        mRectPaint = new Paint();
        mRectPaint.setColor(mRectColor);
        mRectPaint.setStrokeWidth(mStrokeWidth);

        this.deepgraphicoverlay = deepgraphicoverlay;
        this.rect = rect;

        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {

        RectF rectF = new RectF(rect);
        rectF.left = translateX(rectF.left);
        rectF.right = translateX(rectF.right);
        rectF.top = translateY(rectF.top);
        rectF.bottom = translateY(rectF.bottom);
        canvas.drawRect(rectF, mRectPaint);

    }
}