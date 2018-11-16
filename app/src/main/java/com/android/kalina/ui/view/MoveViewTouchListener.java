package com.android.kalina.ui.view;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Dmitriy on 07.02.2018.
 */

public class MoveViewTouchListener implements View.OnTouchListener {
    private GestureDetector mGestureDetector;
    private View mView;
    private boolean enableX = true;
    private boolean enableY = true;
    private float maxTranslationX = 0;
    private float alertBound = 1f;
    private boolean alert = false;
    private ActionVoiceViewListener listener;
    private float scaleFactor = 1f;
    private float mTranslationX = 0f;
    private boolean destroyed = false;

    public MoveViewTouchListener(View view) {
        mGestureDetector = new GestureDetector(view.getContext(), mGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);
        mView = view;
    }

    public void destroy() {
        destroyed = true;
        listener = null;
    }

    public void setAlertBound(float alertBound) {
        this.alertBound = alertBound;
    }

    public void setMaxTranslationX(float max) {
        maxTranslationX = max;
    }

    public void setListener(ActionVoiceViewListener listener) {
        this.listener = listener;
    }

    public void reset() {
        alert = false;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setEnableX(boolean enable) {
        enableX = enable;
    }

    public void setEnableY(boolean enable) {
        enableY = enable;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (destroyed) return false;

        Log.d("alert_trace", "onTouch = " + event.getAction());

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            listener.onStart();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            listener.onFinish();
        }

        return mGestureDetector.onTouchEvent(event);
    }

    private void checkAlertBound() {
        float translationX = mTranslationX;

        if (translationX > 0) {
            return;
        }

        Log.d("alert_trace", "trans = " + translationX);
        Log.d("alert_trace", "ava = " + maxTranslationX * alertBound);

        float translate = Math.abs(translationX * scaleFactor);
        float maxTranslate = maxTranslationX * alertBound;
        listener.onTranslateOffsetChanged((maxTranslate -  translate) / maxTranslate, translate);

        if (maxTranslate < translate && !alert) {
            notifyAlertBoundChanged();
        }
    }

    private void notifyAlertBoundChanged() {
        listener.onOutBound();
        alert = !alert;
    }

    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        private float mMotionDownX, mMotionDownY;

        @Override
        public boolean onDown(MotionEvent e) {
            if (destroyed) return false;

            Log.d("alert_trace", "onDown = " + e.getAction());

            if (enableX) {
                mMotionDownX = e.getRawX() - mView.getTranslationX();
                Log.d("alert_trace", "motion x = " + mMotionDownX);
            }
            if (enableY) {
                mMotionDownY = e.getRawY() - mView.getTranslationY();
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (destroyed || e1 == null) return false;

            Log.d("alert_trace", "onScroll = " + e1.getAction());

            if (enableX) {
                mTranslationX = (e2.getRawX() - mMotionDownX) / scaleFactor;
                mView.setTranslationX(mTranslationX);
            }
            if (enableY) {
                float translation = (e2.getRawY() - mMotionDownY) / scaleFactor;
                mView.setTranslationY(translation);
            }

            checkAlertBound();
            return true;
        }
    };

    public interface ActionVoiceViewListener {
        void onStart();

        void onFinish();

        void onOutBound();

        void onTranslateOffsetChanged(float offset, float translate);
    }
}
