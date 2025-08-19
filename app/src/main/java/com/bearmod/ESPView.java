package com.bearmod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.SystemClock;
import android.view.View;
import android.graphics.Matrix;
import android.graphics.RectF;

import androidx.annotation.NonNull;

public class ESPView extends View implements Runnable {

    private int mFPS = 0;
    private int mFPSCounter = 0;
    private long mFPSTime = 0;
  
    
    Paint p;
    Bitmap bitmap;
    Bitmap out;
    Bitmap out2;
    Paint mStrokePaint;
    Paint mTextPaint;
    Paint mFilledPaint;
    //
   Thread mThread;
    
	int screenWidth, screenHeight;
    Bitmap[] OTHER = new Bitmap[4];
    private int mTargetFps = 60; // Default, can be set dynamically
    public ESPView(Context context) {
        super(context, null, 0);
        
        InitializePaints();
        
        
        // LAG FIX / full smooth
        setFocusableInTouchMode(false);
        setEnabled(false);
        setFitsSystemWindows(false);
        setHapticFeedbackEnabled(false);
        setFocusable(false);
        setFocusedByDefault(false);
        setFocusable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setForceDarkAllowed(false);
        }
        setHovered(false);
        setKeepScreenOn(false);
      //  setAutoHandwritingEnabled(false);//Crash
        setActivated(false);
       setHovered(false);

        setBackgroundColor(Color.TRANSPARENT);
        
       mThread = new Thread(this);
        mThread.start();

    }

    public void InitializePaints() {
        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setColor(Color.rgb(0, 0, 0));
        
        mFilledPaint = new Paint();
        mFilledPaint.setStyle(Paint.Style.FILL);
        mFilledPaint.setAntiAlias(true);
        mFilledPaint.setColor(Color.rgb(0, 0, 0));
        
        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.rgb(0, 0, 0));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStrokeWidth(1.1f);

        }
    
        @SuppressLint("SuspiciousIndentation")
        @Override
        protected void onDraw(@NonNull Canvas canvas) {
                  if (this.getVisibility() == View.VISIBLE) {
                            this.ClearCanvas(canvas);
							Floating.DrawOn(this, canvas);
                  
                       }
             }
             
             
    public void DrawText5(Canvas canvas, int n, int n2, int n3, int n4, float f, String string2, float f2, float f3, float f4) {
        this.mTextPaint.setARGB(n, n2, n3, n4);
        this.mTextPaint.setTextSize(f4);
        if (SystemClock.uptimeMillis() - this.mFPSTime > (long) 1000) {
            this.mFPSTime = SystemClock.uptimeMillis();
            this.mFPS = this.mFPSCounter;
            this.mFPSCounter = 0;
        } else {
            this.mFPSCounter = 1 + this.mFPSCounter;
        }
        String s = "" + this.mFPS;
        this.mTextPaint.setColor(Color.rgb((int)n2, (int)n3, (int)n4));
        this.mTextPaint.setAlpha(n);
        this.mTextPaint.setStrokeWidth(f);
        if (this.getRight() > 1950 || this.getBottom() > 1920) {
            this.mTextPaint.setTextSize(f4 + (float)4);
        } else if (this.getRight() == 1950 || this.getBottom() == 1920) {
            this.mTextPaint.setTextSize(f4 + (float)2);
        } else {
            this.mTextPaint.setTextSize(f4);
        }
        canvas.drawText(string2, f2, f3+20, this.mTextPaint);
    }
    
             
    public void NRG_DrawText(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size) {
        mTextPaint.setShadowLayer(3, 0,0, Color.BLACK);
        mTextPaint.setColor(Color.rgb(r, g, b));

            mTextPaint.setTextSize(size);
            
        cvs.drawText(txt, posX, posY, mTextPaint);
    }
	
    public void NRG_DrawRect(Canvas cvs, int a, int r, int g, int b, float stroke, float x, float y, float width, float height) {
        mStrokePaint.setStrokeWidth(stroke);
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        cvs.drawRoundRect(new RectF(x, y, width, height), 5, 5, mStrokePaint);

    }

    public void NRG_DrawCircle(Canvas cvs, int a, int r, int g, int b, float posX, float posY, float radius, float stroke) {
        mStrokePaint.setARGB(a, r, g, b);
        mStrokePaint.setStrokeWidth(stroke);
        cvs.drawCircle(posX, posY, radius, mStrokePaint);
    }
    
    public void NRG_DrawLine(Canvas cvs, int a, int r, int g, int b, float lineWidth, float fromX, float fromY, float toX, float toY) {
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        mStrokePaint.setStrokeWidth(lineWidth);
        cvs.drawLine(fromX, fromY, toX, toY, mStrokePaint);
    }
    public void NRG_DrawFilledRect(Canvas cvs, int a, int r, int g, int b, float x, float y, float width, float height) {
        mFilledPaint.setColor(Color.rgb(r, g, b));
        mFilledPaint.setAlpha(a);
        cvs.drawRoundRect(new RectF(x, y, width, height), 5, 5, mFilledPaint);

    }
    public void DrawRect(Canvas cvs, int a, int r, int g, int b, float stroke, float x, float y, float width, float height) {
        mStrokePaint.setStrokeWidth(stroke);
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        cvs.drawRoundRect(new RectF(x, y, width, height), 5, 5, mStrokePaint);

    }
    private int getWeaponIcon(int id) {

        return 0;
    }
     public void ClearCanvas(Canvas cvs) {
         cvs.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SCREEN);
    }

    public static Bitmap scale(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width;
        int height;
        float widthRatio = (float) bitmap.getWidth() / maxWidth;
        float heightRatio = (float) bitmap.getHeight() / maxHeight;

        if (widthRatio >= heightRatio) {
            width = maxWidth;
            height = (int) (((float) width / bitmap.getWidth()) * bitmap.getHeight());
        } else {
            height = maxHeight;
            width = (int) (((float) height / bitmap.getHeight()) * bitmap.getWidth());
        }

        Bitmap scaledBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        float ratioX = (float) width / bitmap.getWidth();
        float ratioY = (float) height / bitmap.getHeight();
        float middleX = width / 2.0f;
        float middleY = height / 2.0f;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - (float) bitmap.getWidth() / 2, middleY - (float) bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }

    public void setTargetFps(int fps) {
        mTargetFps = fps;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
        while (mThread.isAlive() && !mThread.isInterrupted()) {
            try {
                long sleepTime = 1000 / mTargetFps;
                long t1 = System.currentTimeMillis();
                postInvalidate();
                long td = System.currentTimeMillis() - t1;
                long sleepDuration = Math.max(0, sleepTime - td);
                Thread.sleep(sleepDuration);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    
    }
    
    
