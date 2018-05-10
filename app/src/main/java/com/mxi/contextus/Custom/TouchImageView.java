package com.mxi.contextus.Custom;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.OverScroller;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.mxi.contextus.Activity.MainActivity;
import com.mxi.contextus.Database.SQLitehelper;
import com.mxi.contextus.Fragment.ImageViewFragment;
import com.mxi.contextus.Model.Coordinate;
import com.mxi.contextus.Model.MarkupItem;
import com.mxi.contextus.Model.ProjectImagesInfo;
import com.mxi.contextus.R;
import com.mxi.contextus.Util.CommanClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class TouchImageView extends ImageView {

    public static ArrayList<MarkupItem> markupList = new ArrayList<>();
    public static boolean isActivityRestarted = false;
    public static boolean isDeleteDialogAvailable = false;

    private static final String DEBUG = "DEBUG";

    private static final float SUPER_MIN_MULTIPLIER = .75f;
    private static final float SUPER_MAX_MULTIPLIER = 1.25f;

    public Bitmap currentBitmap;
    private float normalizedScale;

    private Matrix matrix, prevMatrix;

    private static enum State {NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM}

    ;
    private State state;

    private float minScale;
    private float maxScale;
    private float superMinScale;
    private float superMaxScale;
    private float[] m;

    private Context context;
    private Fling fling;
    PointF point;

    SQLitehelper dbcon;
    CommanClass cc;
    private ScaleType mScaleType;

    Toast toast;
    Snackbar snackbar;
    CountDownTimer toastCountDown;

    private ZoomVariables delayedZoomVariables;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private GestureDetector.OnDoubleTapListener doubleTapListener = null;
    private OnTouchListener userTouchListener = null;
    private OnTouchImageViewListener touchImageViewListener = null;

    private boolean imageRenderedAtLeastOnce;
    private boolean onDrawReady;

    boolean imageIsDeleted = false;
    boolean imageIsCommented = false;
    boolean onTapIsPerformed = false;


    ArrayList<ProjectImagesInfo> img_array;
    public ArrayList<Coordinate> pointsList = new ArrayList<>();

    String pic_id, markup_text;
    int markup_count;
    private int viewWidth, viewHeight, prevViewWidth, prevViewHeight;
    private float matchViewWidth, matchViewHeight, prevMatchViewWidth, prevMatchViewHeight;

    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);

    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    public TouchImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {
        setWillNotDraw(false);
        super.setClickable(true);
        this.context = context;

        cc = new CommanClass(context);
        dbcon = new SQLitehelper(context);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(context, new GestureListener());

        matrix = new Matrix();
        prevMatrix = new Matrix();
        m = new float[9];
        normalizedScale = 1;

        if (mScaleType == null) {
            mScaleType = ScaleType.FIT_CENTER;
        }

        minScale = 1;
        maxScale = 5;
        superMinScale = SUPER_MIN_MULTIPLIER * minScale;
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale;

        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
        setState(State.NONE);
        onDrawReady = false;
        super.setOnTouchListener(new PrivateOnTouchListener());
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        userTouchListener = l;
    }


    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        savePreviousImageValues();
        fitImageToView();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        currentBitmap = bm;
        super.setImageBitmap(currentBitmap);
        img_array = new ArrayList<ProjectImagesInfo>();
        savePreviousImageValues();
        fitImageToView();
        setUpMarkupSequence();
        invalidate();
    }

    private void setUpMarkupSequence() {
        pointsList.clear();

        Cursor mark_cur = null;
        mark_cur = dbcon.getMarkupsFromPicId(pic_id);
        if (mark_cur != null && mark_cur.getCount() != 0) {
            mark_cur.moveToFirst();
            do {
                float c_x = Float.parseFloat(mark_cur.getString(3));
                float c_y = Float.parseFloat(mark_cur.getString(4));
                int c_j = Integer.parseInt((mark_cur.getString(2)));
                pointsList.add(new Coordinate(c_x, c_y, c_j));
                markup_count = Integer.parseInt(mark_cur.getString(2));
            } while (mark_cur.moveToNext());
        } else {
            markup_count = 0;
        }
    }

    public void setPicId(String current_pic_id) {
        pic_id = current_pic_id;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        savePreviousImageValues();
        fitImageToView();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        savePreviousImageValues();
        fitImageToView();
    }

    @Override
    public void setScaleType(ScaleType type) {
        if (type == ScaleType.FIT_START || type == ScaleType.FIT_END) {
            throw new UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END");
        }
        if (type == ScaleType.MATRIX) {
            super.setScaleType(ScaleType.MATRIX);

        } else {
            mScaleType = type;
            if (onDrawReady) {
                setZoom(this);
            }
        }
    }

    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }


    public boolean isZoomed() {
        return normalizedScale != 1;
    }

    public RectF getZoomedRect() {
        if (mScaleType == ScaleType.FIT_XY) {
            throw new UnsupportedOperationException("getZoomedRect() not supported with FIT_XY");
        }
        PointF topLeft = transformCoordTouchToBitmap(0, 0, true);
        PointF bottomRight = transformCoordTouchToBitmap(viewWidth, viewHeight, true);

        float w = getDrawable().getIntrinsicWidth();
        float h = getDrawable().getIntrinsicHeight();
        return new RectF(topLeft.x / w, topLeft.y / h, bottomRight.x / w, bottomRight.y / h);
    }

    private void savePreviousImageValues() {

        if (matrix != null && viewHeight != 0 && viewWidth != 0) {
            matrix.getValues(m);
            prevMatrix.setValues(m);
            prevMatchViewHeight = matchViewHeight;
            prevMatchViewWidth = matchViewWidth;
            prevViewHeight = viewHeight;
            prevViewWidth = viewWidth;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putFloat("saveScale", normalizedScale);
        bundle.putFloat("matchViewHeight", matchViewHeight);
        bundle.putFloat("matchViewWidth", matchViewWidth);
        bundle.putInt("viewWidth", viewWidth);
        bundle.putInt("viewHeight", viewHeight);
        matrix.getValues(m);
        bundle.putFloatArray("matrix", m);
        bundle.putBoolean("imageRendered", imageRenderedAtLeastOnce);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            normalizedScale = bundle.getFloat("saveScale");
            m = bundle.getFloatArray("matrix");
            prevMatrix.setValues(m);
            prevMatchViewHeight = bundle.getFloat("matchViewHeight");
            prevMatchViewWidth = bundle.getFloat("matchViewWidth");
            prevViewHeight = bundle.getInt("viewHeight");
            prevViewWidth = bundle.getInt("viewWidth");
            imageRenderedAtLeastOnce = bundle.getBoolean("imageRendered");
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onDrawReady = true;
        imageRenderedAtLeastOnce = true;
        if (delayedZoomVariables != null) {
            setZoom(delayedZoomVariables.scale, delayedZoomVariables.focusX, delayedZoomVariables.focusY, delayedZoomVariables.scaleType);
            delayedZoomVariables = null;
        }

        CommanClass.isReadyToDelete = false;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        savePreviousImageValues();
        invalidate();
    }

    public float getMaxZoom() {
        return maxScale;
    }

    public void setMaxZoom(float max) {
        maxScale = max;
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale;
    }

    public float getMinZoom() {
        return minScale;
    }

    public float getCurrentZoom() {
        return normalizedScale;
    }

    public void setMinZoom(float min) {
        minScale = min;
        superMinScale = SUPER_MIN_MULTIPLIER * minScale;
    }

    public void resetZoom() {
        normalizedScale = 1;
        fitImageToView();
    }

    public void setZoom(float scale) {
        setZoom(scale, 0.5f, 0.5f);
    }

    public void setZoom(float scale, float focusX, float focusY) {
        setZoom(scale, focusX, focusY, mScaleType);
    }

    public void setZoom(float scale, float focusX, float focusY, ScaleType scaleType) {

        if (!onDrawReady) {
            delayedZoomVariables = new ZoomVariables(scale, focusX, focusY, scaleType);
            return;
        }

        if (scaleType != mScaleType) {
            setScaleType(scaleType);
        }
        resetZoom();
        scaleImage(scale, viewWidth / 2, viewHeight / 2, true);

        matrix.getValues(m);
        m[Matrix.MTRANS_X] = -((focusX * getImageWidth()) - (viewWidth * 0.5f));
        m[Matrix.MTRANS_Y] = -((focusY * getImageHeight()) - (viewHeight * 0.5f));
        matrix.setValues(m);

        fixTrans();
        setImageMatrix(matrix);
    }

    public void setZoom(TouchImageView img) {
        PointF center = img.getScrollPosition();
        setZoom(img.getCurrentZoom(), center.x, center.y, img.getScaleType());
    }

    public PointF getScrollPosition() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return null;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        PointF point = transformCoordTouchToBitmap(viewWidth / 2, viewHeight / 2, true);
        point.x /= drawableWidth;
        point.y /= drawableHeight;
        return point;
    }

    public void setScrollPosition(float focusX, float focusY) {
        setZoom(normalizedScale, focusX, focusY);
    }

    private void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, getImageWidth());
        float fixTransY = getFixTrans(transY, viewHeight, getImageHeight());

        if (fixTransX != 0 || fixTransY != 0) {
            matrix.postTranslate(fixTransX, fixTransY);
        }
    }

    private void fixScaleTrans() {
        fixTrans();
        matrix.getValues(m);
        if (getImageWidth() < viewWidth) {
            m[Matrix.MTRANS_X] = (viewWidth - getImageWidth()) / 2;
        }

        if (getImageHeight() < viewHeight) {
            m[Matrix.MTRANS_Y] = (viewHeight - getImageHeight()) / 2;
        }
        matrix.setValues(m);
    }

    private float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;

        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    private float getImageWidth() {
        return matchViewWidth * normalizedScale;
    }

    private float getImageHeight() {
        return matchViewHeight * normalizedScale;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
            setMeasuredDimension(0, 0);
            return;
        }

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        viewWidth = setViewSize(widthMode, widthSize, drawableWidth);
        viewHeight = setViewSize(heightMode, heightSize, drawableHeight);

        setMeasuredDimension(viewWidth, viewHeight);

        fitImageToView();
    }

    private void fitImageToView() {
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
            return;
        }
        if (matrix == null || prevMatrix == null) {
            return;
        }

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        float scaleX = (float) viewWidth / drawableWidth;
        float scaleY = (float) viewHeight / drawableHeight;

        switch (mScaleType) {
            case CENTER:
                scaleX = scaleY = 1;
                break;

            case CENTER_CROP:
                scaleX = scaleY = Math.max(scaleX, scaleY);
                break;

            case CENTER_INSIDE:
                scaleX = scaleY = Math.min(1, Math.min(scaleX, scaleY));

            case FIT_CENTER:
                scaleX = scaleY = Math.min(scaleX, scaleY);
                break;

            case FIT_XY:
                break;

            default:

                throw new UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END");

        }

        float redundantXSpace = viewWidth - (scaleX * drawableWidth);
        float redundantYSpace = viewHeight - (scaleY * drawableHeight);
        matchViewWidth = viewWidth - redundantXSpace;
        matchViewHeight = viewHeight - redundantYSpace;
        if (!isZoomed() && !imageRenderedAtLeastOnce) {

            matrix.setScale(scaleX, scaleY);
            matrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2);
            normalizedScale = 1;

        } else {

            if (prevMatchViewWidth == 0 || prevMatchViewHeight == 0) {
                savePreviousImageValues();
            }

            prevMatrix.getValues(m);

            m[Matrix.MSCALE_X] = matchViewWidth / drawableWidth * normalizedScale;
            m[Matrix.MSCALE_Y] = matchViewHeight / drawableHeight * normalizedScale;

            float transX = m[Matrix.MTRANS_X];
            float transY = m[Matrix.MTRANS_Y];

            float prevActualWidth = prevMatchViewWidth * normalizedScale;
            float actualWidth = getImageWidth();
            translateMatrixAfterRotate(Matrix.MTRANS_X, transX, prevActualWidth, actualWidth, prevViewWidth, viewWidth, drawableWidth);

            float prevActualHeight = prevMatchViewHeight * normalizedScale;
            float actualHeight = getImageHeight();
            translateMatrixAfterRotate(Matrix.MTRANS_Y, transY, prevActualHeight, actualHeight, prevViewHeight, viewHeight, drawableHeight);

            matrix.setValues(m);
        }
        fixTrans();
        setImageMatrix(matrix);
    }

    private int setViewSize(int mode, int size, int drawableWidth) {
        int viewSize;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                viewSize = size;
                break;

            case MeasureSpec.AT_MOST:
                viewSize = Math.min(drawableWidth, size);
                break;

            case MeasureSpec.UNSPECIFIED:
                viewSize = drawableWidth;
                break;

            default:
                viewSize = size;
                break;
        }
        return viewSize;
    }

    private void translateMatrixAfterRotate(int axis, float trans, float prevImageSize, float imageSize, int prevViewSize, int viewSize, int drawableSize) {
        if (imageSize < viewSize) {

            m[axis] = (viewSize - (drawableSize * m[Matrix.MSCALE_X])) * 0.5f;

        } else if (trans > 0) {

            m[axis] = -((imageSize - viewSize) * 0.5f);

        } else {

            float percentage = (Math.abs(trans) + (0.5f * prevViewSize)) / prevImageSize;
            m[axis] = -((percentage * imageSize) - (viewSize * 0.5f));
        }
    }

    private void setState(State state) {
        this.state = state;
    }

    public boolean canScrollHorizontallyFroyo(int direction) {
        return canScrollHorizontally(direction);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        matrix.getValues(m);
        float x = m[Matrix.MTRANS_X];

        if (getImageWidth() < viewWidth) {
            return false;

        } else if (x >= -1 && direction < 0) {
            return false;

        } else if (Math.abs(x) + viewWidth + 1 >= getImageWidth() && direction > 0) {
            return false;
        }

        return true;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (doubleTapListener != null) {
                return doubleTapListener.onSingleTapConfirmed(e);
            }

            if (!onTapIsPerformed) {
                Log.e("Main x,y", e.getX() + "," + e.getY());
                PointF points = convertCoordinatesAsPerBitmap(e.getX(), e.getY());
                Log.e("Single x,y", points.x + "," + points.y);
                Log.e("++++++++", "Go For Check Comments");

                checkForAvailableComments(points.x, points.y, e.getX(), e.getY());
            }


            return performClick();
        }

        @Override
        public void onLongPress(MotionEvent event) {
            performLongClick();
            PointF points = convertCoordinatesAsPerBitmap(event.getX(), event.getY());
            showMarkupDialog(points.x, points.y);

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            final int SWIPE_MIN_DISTANCE = 120;
            final int SWIPE_THRESHOLD_VELOCITY = 200;

            CommanClass.isReadyToDelete = false;

            if (fling != null) {

                fling.cancelFling();
            }
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                CommanClass.isReadyToDelete = false;
                return false;
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                CommanClass.isReadyToDelete = false;
                return false;
            }

            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                CommanClass.isReadyToDelete = true;
                return false;
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                CommanClass.isReadyToDelete = false;
                return false;
            }

            fling = new Fling((int) velocityX, (int) velocityY);
            compatPostOnAnimation(fling);
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            boolean consumed = false;
            if (doubleTapListener != null) {
                consumed = doubleTapListener.onDoubleTap(e);
            }
            if (state == State.NONE) {
                float targetZoom = (normalizedScale == minScale) ? maxScale : minScale;
                DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, e.getX(), e.getY(), false);
                compatPostOnAnimation(doubleTap);
                consumed = true;
            }
            return consumed;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (doubleTapListener != null) {
                return doubleTapListener.onDoubleTapEvent(e);
            }
            return false;
        }
    }

    private PointF convertCoordinatesAsPerBitmap(float x, float y) {
        float[] values = new float[9];
        matrix.getValues(values);

        float relativeX = (x - values[2]) / values[0];
        float relativeY = (y - values[5]) / values[4];

        return new PointF(relativeX, relativeY);
    }

    private void checkForAvailableComments(float x, float y, float original_x, float original_y) {
        onTapIsPerformed = true;
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    onTapIsPerformed = false;
                }
            }
        };
        timerThread.start();
        String commentText = "";
        String markUpId = "";
        String markupName = "";
        float coordinate_x;
        float coordinate_y;

        Cursor cur = null;

        cur = dbcon.getMarkupsFromPicId(pic_id);

        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            int i = 1;
            do {
                coordinate_x = Float.parseFloat(cur.getString(3));
                coordinate_y = Float.parseFloat(cur.getString(4));
                if ((coordinate_x > x - 40) && (coordinate_x < x + 40) && (coordinate_y < y + 40) && (coordinate_y > y - 40)) {
                    markupName = cur.getString(2);
                    markUpId = cur.getString(0);
                    commentText = cur.getString(5);
                    i++;
                }

            } while (cur.moveToNext());

        }

        if (!commentText.equals("")) {

            final String finalMarkUpId = markUpId;
            final int finalMarkupName = Integer.parseInt(markupName);


            snackbar = Snackbar
                    .make(getRootView().findViewById(android.support.design.R.id.design_navigation_view), "UPDATE", Snackbar.LENGTH_LONG)
                    .setAction("DELETE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            toastCountDown.cancel();
                            onTapIsPerformed = false;
                            snackbar.dismiss();
                            toast.cancel();
                            deleteCurrentMarkup(finalMarkUpId, finalMarkupName);

                        }
                    });

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);

            final String finalMarkUpId1 = markUpId;
            final String finalCommentText = commentText;

            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toastCountDown.cancel();
                    onTapIsPerformed = false;
                    snackbar.dismiss();
                    toast.cancel();
                    updateMarkUp(finalMarkUpId1, finalCommentText);

                }
            });

            snackbar.setActionTextColor(Color.RED);

            toast = Toast.makeText(context, commentText, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.LEFT | Gravity.TOP, Integer.parseInt(Math.round(original_x) + ""), Integer.parseInt(Math.round(original_y) + ""));

            toastCountDown = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    toast.show();
                    snackbar.show();

                }

                public void onFinish() {
                    onTapIsPerformed = false;
                    snackbar.dismiss();
                    toast.cancel();
                }
            };


            snackbar.show();
            toast.show();
            toastCountDown.start();
        }
    }

    private void deleteCurrentMarkup(final String finalMarkUpId, final int finalMarkupName) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());

        LayoutInflater inflater = (LayoutInflater) getRootView().getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setCancelable(false);

        View dialogView = inflater.inflate(R.layout.dialog_delete_markup, null);
        builder.setView(dialogView);

        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);

        Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_delete_project_cancel);
        Button btn_yes = (Button) dialogView.findViewById(R.id.btn_delete_project_yes);


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();


            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dbcon.deleteMarkUpFromId(finalMarkUpId);
                Cursor cur = null;
                cur = dbcon.getMarkupsFromPicId(pic_id);
                if (cur != null && cur.getCount() != 0) {
                    cur.moveToFirst();
                    do {
                        String update_seq_markupId = cur.getString(0);
                        int update_seq_markupName = Integer.parseInt(cur.getString(2));
                        if (update_seq_markupName > finalMarkupName) {
                            dbcon.updateMarkupSequenceNumber(update_seq_markupId, String.valueOf(update_seq_markupName - 1));
                        }

                    } while (cur.moveToNext());
                }
                alert.dismiss();
                setUpMarkupSequence();
//                invalidate();

//          @akshay    After delete markup Image convert into string and add into database here.

                getOriginalImage(pic_id);

                Bitmap bitmap=markBitmapImage();
               // @akshay new changes Jan-2017
                String temp_image_path = saveImageToTempAndGetImagePath(getImageName(pic_id),getProjectId(pic_id),bitmap);

//                String image_string = BitMapToString(bitmap);
                dbcon.updateMarkedImage(pic_id, temp_image_path);

                ImageViewFragment.adapter.notifyDataSetChanged();
/*                Intent restart = new Intent(getContext(), MainActivity.class);
                restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                restart.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                getContext().startActivity(restart);*/


            }
        });
        alert.show();
    }

    public String saveImageToTempAndGetImagePath(String image_name,String projectId ,Bitmap bitmap){
        String imagePath="";
        String project_name=dbcon.getProjectNameById(projectId);
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/localTemp/lessunknown/" + project_name + "/" + "DCIM/");

        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        imagePath = mediaStorageDir + "/" + image_name;

        File file = new File (imagePath);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }





        return imagePath;
    }

    public String getImageName(String picId){
        String imageName="";
        Cursor cursor = dbcon.getPicFromPicId(picId);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                imageName = cursor.getString(8);
            } while (cursor.moveToNext());
        }
        return imageName;
    }

    public String getProjectId(String picId){
        String projectName="";
        Cursor cursor = dbcon.getPicFromPicId(picId);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                projectName = cursor.getString(1);
            } while (cursor.moveToNext());
        }
        return projectName;
    }

    private void getOriginalImage(String pic_Id) {
        String image_path = "";
        Bitmap img_bitmap;
        String orientation = "";
        Cursor cursor = dbcon.getPicFromPicId(pic_Id);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                orientation = cursor.getString(9);
                image_path = cursor.getString(5);
            } while (cursor.moveToNext());
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        img_bitmap = BitmapFactory.decodeFile(image_path, options);
        img_bitmap = getResizedImage(img_bitmap);

        Matrix matrix = new Matrix();

        if (orientation.equals("0")) {
            matrix.setRotate(90);
        }

        currentBitmap = img_bitmap.createBitmap(img_bitmap, 0, 0, img_bitmap.getWidth(), img_bitmap.getHeight(), matrix, true);

    }


    private void updateMarkUp(final String markUpId, String commentText) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setCancelable(false);

        final View dialogView = inflater.inflate(R.layout.add_comment, null);
        builder.setView(dialogView);

        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);

        final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);
        Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        Button btn_create = (Button) dialogView.findViewById(R.id.btn_create);

        et_name.setText(commentText);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                alert.dismiss();
            }
        });

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                markup_text = et_name.getText().toString();
                if (markup_text.length() == 0) {


                } else {
                    markup_text = markup_text.replace("'", "`");
                    dbcon.updateMarkupDetail(markUpId, et_name.getText().toString());
                    setUpMarkupSequence();
                    invalidate();
                }
                alert.dismiss();
            }
        });

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        alert.show();
    }

    private void showMarkupDialog(final float x, final float y) {
        point = new PointF(x, y);
        final String c_x = String.valueOf(x);
        final String c_y = String.valueOf(y);

        Log.e("x,y", c_x + "," + c_y);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setCancelable(false);

        final View dialogView = inflater.inflate(R.layout.add_comment, null);
        builder.setView(dialogView);

        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);
        Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        Button btn_create = (Button) dialogView.findViewById(R.id.btn_create);
        dialogView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                alert.dismiss();
            }
        });

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);

                markup_text = et_name.getText().toString();

                alert.dismiss();

                if (markup_text.length() == 0) {


                } else {

                    markup_count++;
                    markup_text = markup_text.replace("'", "`");
                    pointsList.add(new Coordinate(x, y, markup_count));
                    point.set(x, y);

                    MarkupItem markupItem = new MarkupItem(pic_id, String.valueOf(markup_count), c_x, c_y, markup_text);
                    markupList.add(markupItem);
                    dbcon.insertMarkups(pic_id, String.valueOf(markup_count), c_x, c_y, markup_text);

//                   @akshay After insert the markup Image convert into string and add into database here.
//                            getOriginalImage(pic_id);
                    Bitmap bitmap=markBitmapImage();

                    // @akshay new changes Jan-2017

                    String temp_image_path = saveImageToTempAndGetImagePath(getImageName(pic_id),getProjectId(pic_id),bitmap);

//                    String image_string = BitMapToString(bitmap);
                        dbcon.updateMarkedImage(pic_id, temp_image_path);

                    ImageViewFragment.adapter.notifyDataSetChanged();
/*                    Intent restart = new Intent(getContext(), MainActivity.class);
                    restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    restart.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    getContext().startActivity(restart);*/

                }

            }
        });
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        alert.show();
    }

    private Bitmap markBitmapImage() {
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        myOptions.inPurgeable = true;

        Bitmap workingBitmap = Bitmap.createBitmap(currentBitmap);

        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

//        mutableBitmap = getResizedImage(mutableBitmap);

        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint();
        Paint paint1 = new Paint();
        for (Coordinate coor : pointsList) {

            paint.setTextSize(40);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.YELLOW);

            int myColor = context.getResources().getColor(R.color.Purple);
            paint1.setColor(myColor);
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(3);

            canvas.drawCircle(coor.x, coor.y, 50, paint1);

            canvas.drawText(String.valueOf(coor.j), coor.x - 10, coor.y + 5, paint);

        }
        setImageBitmap(mutableBitmap);

        return mutableBitmap;
    }


    public ArrayList<ProjectImagesInfo> getProjectImages(String projectID) {
        ArrayList<ProjectImagesInfo> imagesPath = new ArrayList<>();
        Cursor cur1 = dbcon.getPicsFromProjectId(projectID);
        if (cur1 != null && cur1.getCount() != 0) {

            cur1.moveToFirst();
            cc.savePrefString("project_id_for_pic", projectID);
            do {
                ProjectImagesInfo pImagesInfo = new ProjectImagesInfo();
                pImagesInfo.setPictureID(cur1.getString(0));

                pImagesInfo.setProjectID(cur1.getString(1));
                pImagesInfo.setSequenceNumber(Integer.parseInt(cur1.getString(2)));

                pImagesInfo.setPicSizeX(cur1.getString(3));
                pImagesInfo.setPicSizeY(cur1.getString(4));
                String image_path = cur1.getString(5);
                pImagesInfo.setPicturePath(cur1.getString(5));
                pImagesInfo.setPicInString(cur1.getString(7));
                pImagesInfo.setOrientation(cur1.getString(9));

                imagesPath.add(pImagesInfo);

            } while (cur1.moveToNext());
        } else {
            cc.savePrefBoolean("noProjectCamera", true);
        }

        Collections.sort(imagesPath, new Comparator<ProjectImagesInfo>() {
            public int compare(ProjectImagesInfo obj1, ProjectImagesInfo obj2) {
                return (obj1.sequenceNumber < obj2.sequenceNumber) ? -1
                        : (obj1.sequenceNumber > obj2.sequenceNumber) ? 1 : 0;
            }
        });

        return imagesPath;
    }


    private Bitmap getResizedImage(Bitmap mImg_bitmap) {
        Bitmap myBitmap = mImg_bitmap;

        final int maxSize = 1750;//960
        int outWidth;
        int outHeight;
        int inWidth = myBitmap.getWidth();
        int inHeight = myBitmap.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        myBitmap = Bitmap.createScaledBitmap(myBitmap, outWidth, outHeight, false);
        myBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return myBitmap;
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public interface OnTouchImageViewListener {
        public void onMove();

    }


    private class PrivateOnTouchListener implements OnTouchListener {

        private PointF last = new PointF();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mScaleDetector.onTouchEvent(event);
            mGestureDetector.onTouchEvent(event);
            PointF curr = new PointF(event.getX(), event.getY());
            CommanClass.currentX = event.getX();

            CommanClass.currentY = event.getY();

            Log.e("TouchCounts_ot", event.getPointerCount() + "");

            if (event.getPointerCount() == 1) {
                isDeleteDialogAvailable = true;
            }

            if (state == State.NONE || state == State.DRAG || state == State.FLING) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        last.set(curr);
                        if (fling != null)
                            fling.cancelFling();
                        setState(State.DRAG);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (state == State.DRAG) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;
                            float fixTransX = getFixDragTrans(deltaX, viewWidth, getImageWidth());
                            float fixTransY = getFixDragTrans(deltaY, viewHeight, getImageHeight());
                            matrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();
                            last.set(curr.x, curr.y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        setState(State.NONE);
                        break;
                }
            }

            setImageMatrix(matrix);

            if (userTouchListener != null) {
                userTouchListener.onTouch(v, event);
            }

            if (touchImageViewListener != null) {
                touchImageViewListener.onMove();
            }

            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            setState(State.ZOOM);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleImage(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY(), true);

            if (touchImageViewListener != null) {
                touchImageViewListener.onMove();
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            setState(State.NONE);
            boolean animateToZoomBoundary = false;
            float targetZoom = normalizedScale;
            if (normalizedScale > maxScale) {
                targetZoom = maxScale;
                animateToZoomBoundary = true;

            } else if (normalizedScale < minScale) {
                targetZoom = minScale;
                animateToZoomBoundary = true;
            }

            if (animateToZoomBoundary) {
                DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, viewWidth / 2, viewHeight / 2, true);
                compatPostOnAnimation(doubleTap);
            }
        }
    }

    private void scaleImage(double deltaScale, float focusX, float focusY, boolean stretchImageToSuper) {

        float lowerScale, upperScale;
        if (stretchImageToSuper) {
            lowerScale = superMinScale;
            upperScale = superMaxScale;

        } else {
            lowerScale = minScale;
            upperScale = maxScale;
        }

        float origScale = normalizedScale;
        normalizedScale *= deltaScale;
        if (normalizedScale > upperScale) {
            normalizedScale = upperScale;
            deltaScale = upperScale / origScale;
        } else if (normalizedScale < lowerScale) {
            normalizedScale = lowerScale;
            deltaScale = lowerScale / origScale;
        }

        matrix.postScale((float) deltaScale, (float) deltaScale, focusX, focusY);
        fixScaleTrans();
    }

    private class DoubleTapZoom implements Runnable {

        private long startTime;
        private static final float ZOOM_TIME = 500;
        private float startZoom, targetZoom;
        private float bitmapX, bitmapY;
        private boolean stretchImageToSuper;
        private AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        private PointF startTouch;
        private PointF endTouch;

        DoubleTapZoom(float targetZoom, float focusX, float focusY, boolean stretchImageToSuper) {
            setState(State.ANIMATE_ZOOM);
            startTime = System.currentTimeMillis();
            this.startZoom = normalizedScale;
            this.targetZoom = targetZoom;
            this.stretchImageToSuper = stretchImageToSuper;
            PointF bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false);
            this.bitmapX = bitmapPoint.x;
            this.bitmapY = bitmapPoint.y;


            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY);
            endTouch = new PointF(viewWidth / 2, viewHeight / 2);
        }

        @Override
        public void run() {
            float t = interpolate();
            double deltaScale = calculateDeltaScale(t);
            scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper);
            translateImageToCenterTouchPosition(t);
            fixScaleTrans();
            setImageMatrix(matrix);

            if (touchImageViewListener != null) {
                touchImageViewListener.onMove();
            }

            if (t < 1f) {

                compatPostOnAnimation(this);

            } else {

                setState(State.NONE);
            }
        }

        private void translateImageToCenterTouchPosition(float t) {
            float targetX = startTouch.x + t * (endTouch.x - startTouch.x);
            float targetY = startTouch.y + t * (endTouch.y - startTouch.y);
            PointF curr = transformCoordBitmapToTouch(bitmapX, bitmapY);
            matrix.postTranslate(targetX - curr.x, targetY - curr.y);
        }


        private float interpolate() {
            long currTime = System.currentTimeMillis();
            float elapsed = (currTime - startTime) / ZOOM_TIME;
            elapsed = Math.min(1f, elapsed);
            return interpolator.getInterpolation(elapsed);
        }

        private double calculateDeltaScale(float t) {
            double zoom = startZoom + t * (targetZoom - startZoom);
            return zoom / normalizedScale;
        }
    }


    private PointF transformCoordTouchToBitmap(float x, float y, boolean clipToBitmap) {
        matrix.getValues(m);
        float origW = getDrawable().getIntrinsicWidth();
        float origH = getDrawable().getIntrinsicHeight();
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
        float finalX = ((x - transX) * origW) / getImageWidth();
        float finalY = ((y - transY) * origH) / getImageHeight();

        if (clipToBitmap) {
            finalX = Math.min(Math.max(finalX, 0), origW);
            finalY = Math.min(Math.max(finalY, 0), origH);
        }

        return new PointF(finalX, finalY);
    }

    private PointF transformCoordBitmapToTouch(float bx, float by) {
        matrix.getValues(m);
        float origW = getDrawable().getIntrinsicWidth();
        float origH = getDrawable().getIntrinsicHeight();
        float px = bx / origW;
        float py = by / origH;
        float finalX = m[Matrix.MTRANS_X] + getImageWidth() * px;
        float finalY = m[Matrix.MTRANS_Y] + getImageHeight() * py;
        return new PointF(finalX, finalY);
    }

    private class Fling implements Runnable {

        CompatScroller scroller;
        int currX, currY;

        Fling(int velocityX, int velocityY) {
            setState(State.FLING);
            scroller = new CompatScroller(context);
            matrix.getValues(m);

            int startX = (int) m[Matrix.MTRANS_X];
            int startY = (int) m[Matrix.MTRANS_Y];
            int minX, maxX, minY, maxY;

            if (getImageWidth() > viewWidth) {
                minX = viewWidth - (int) getImageWidth();
                maxX = 0;

            } else {
                minX = maxX = startX;
            }

            if (getImageHeight() > viewHeight) {
                minY = viewHeight - (int) getImageHeight();
                maxY = 0;

            } else {
                minY = maxY = startY;
            }

            scroller.fling(startX, startY, (int) velocityX, (int) velocityY, minX,
                    maxX, minY, maxY);
            currX = startX;
            currY = startY;
        }

        public void cancelFling() {
            if (scroller != null) {
                setState(State.NONE);
                scroller.forceFinished(true);
            }
        }

        @Override
        public void run() {

            if (touchImageViewListener != null) {
                touchImageViewListener.onMove();
            }

            if (scroller.isFinished()) {
                scroller = null;
                return;
            }

            if (scroller.computeScrollOffset()) {
                int newX = scroller.getCurrX();
                int newY = scroller.getCurrY();
                int transX = newX - currX;
                int transY = newY - currY;
                currX = newX;
                currY = newY;
                matrix.postTranslate(transX, transY);
                fixTrans();
                setImageMatrix(matrix);
                compatPostOnAnimation(this);
            }
        }
    }

    @TargetApi(VERSION_CODES.GINGERBREAD)
    private class CompatScroller {
        Scroller scroller;
        OverScroller overScroller;
        boolean isPreGingerbread;

        public CompatScroller(Context context) {
            if (VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
                isPreGingerbread = true;
                scroller = new Scroller(context);

            } else {
                isPreGingerbread = false;
                overScroller = new OverScroller(context);
            }
        }

        public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
            if (isPreGingerbread) {
                scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            } else {
                overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            }
        }

        public void forceFinished(boolean finished) {
            if (isPreGingerbread) {
                scroller.forceFinished(finished);
            } else {
                overScroller.forceFinished(finished);
            }
        }

        public boolean isFinished() {
            if (isPreGingerbread) {
                return scroller.isFinished();
            } else {
                return overScroller.isFinished();
            }
        }

        public boolean computeScrollOffset() {
            if (isPreGingerbread) {
                return scroller.computeScrollOffset();
            } else {
                overScroller.computeScrollOffset();
                return overScroller.computeScrollOffset();
            }
        }

        public int getCurrX() {
            if (isPreGingerbread) {
                return scroller.getCurrX();
            } else {
                return overScroller.getCurrX();
            }
        }

        public int getCurrY() {
            if (isPreGingerbread) {
                return scroller.getCurrY();
            } else {
                return overScroller.getCurrY();
            }
        }
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    private void compatPostOnAnimation(Runnable runnable) {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            postOnAnimation(runnable);

        } else {
            postDelayed(runnable, 1000 / 60);
        }
    }

    private class ZoomVariables {
        public float scale;
        public float focusX;
        public float focusY;
        public ScaleType scaleType;

        public ZoomVariables(float scale, float focusX, float focusY, ScaleType scaleType) {
            this.scale = scale;
            this.focusX = focusX;
            this.focusY = focusY;
            this.scaleType = scaleType;
        }
    }
}
