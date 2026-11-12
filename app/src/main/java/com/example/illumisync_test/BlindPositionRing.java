
package com.example.illumisync_test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class BlindPositionRing extends View {

    private final Paint trackPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint progressPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF arcBounds = new RectF();

    private int progress = 50;

    public BlindPositionRing(Context context) {
        super(context);
        init();
    }

    public BlindPositionRing(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlindPositionRing(
            Context context,
            AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(dp(5));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(0xFFE6E7E9);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(dp(5));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(0xFFFF4B19);

        setBackgroundColor(android.graphics.Color.TRANSPARENT);
        setWillNotDraw(false);
    }

    public void setProgress(int value) {
        progress = Math.max(0, Math.min(100, value));
        invalidate();
    }

    public int getProgress() {
        return progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float stroke = dp(5);
        float inset = stroke / 2f;

        arcBounds.set(
                inset,
                inset,
                getWidth() - inset,
                getHeight() - inset
        );

        if (arcBounds.width() <= 0 || arcBounds.height() <= 0) {
            return;
        }

        // Light-gray background ring
        canvas.drawArc(
                arcBounds, -90, 360, false, trackPaint
        );

        // Orange progress ring
        if (progress > 0) {
            canvas.drawArc(
                    arcBounds,
                    -90,
                    progress * 3.6f,
                    false,
                    progressPaint
            );
        }
    }

    private float dp(float value) {
        return value * getResources()
                .getDisplayMetrics().density;
    }
}
