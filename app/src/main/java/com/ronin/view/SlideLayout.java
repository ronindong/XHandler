package com.ronin.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.ronin.example.R;

/**
 * Created by Administrator on 2017/6/27.
 */

public class SlideLayout extends ViewGroup {

    public static final int SLIDE_LEFT = 0;
    public static final int SLIDE_RIGHT = 1;
    public static final int SLIDE_TOP = 2;
    public static final int SLIDE_BOTTOM = 3;


    private int mSlideDirection;
    private int mSlideDelta = 0;
    private Scroller mScroller;

    private View mContentView;
    private View mSlideView;

    private int lastX = 0, lastY = 0;


    public SlideLayout(Context context) {
        this(context, null);
    }

    public SlideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideLayout);
        mSlideDirection = a.getInt(R.styleable.SlideLayout_slideDirection, SLIDE_RIGHT);
        mSlideDelta = a.getInt(R.styleable.SlideLayout_slideCriticalValue, 0);
        a.recycle();

        mScroller = new Scroller(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2) {
            throw new IllegalStateException("SlideLayout only need contains two child (content and slide).");
        }
        mContentView = getChildAt(0);
        mSlideView = getChildAt(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mContentView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        switch (mSlideDirection) {
            case SLIDE_LEFT:
                mSlideView.layout(-mSlideView.getMeasuredWidth(), 0, 0, getMeasuredHeight());
                break;
            case SLIDE_RIGHT:
                mSlideView.layout(getMeasuredWidth(), 0,
                        getMeasuredWidth() + mSlideView.getMeasuredWidth(), getMeasuredHeight());
                break;
            case SLIDE_TOP:
                mSlideView.layout(0, -mSlideView.getMeasuredHeight(),
                        getMeasuredWidth(), 0);
                break;
            case SLIDE_BOTTOM:
                mSlideView.layout(0, getMeasuredHeight(),
                        getMeasuredWidth(), mSlideView.getMeasuredHeight() + getMeasuredHeight());
                break;
        }

    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        int eventX = (int) ev.getX();
        int eventY = (int) ev.getY();
        int scrollX = getScrollX();
        int scrollY = getScrollY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) ev.getX();
                lastY = (int) ev.getY();

                super.dispatchTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                //X轴移动的距离
                int offsetX = eventX - lastX;
                //Y轴移动的距离
                int offsetY = eventY - lastY;

                int newScrollX = 0;
                int newScrollY = 0;

                switch (mSlideDirection) {
                    case SLIDE_LEFT:
                        newScrollX = scrollX - offsetX;
                        if (newScrollX < -mSlideView.getMeasuredWidth()) {
                            newScrollX = -mSlideView.getMeasuredWidth();
                        } else if (newScrollX > 0) {
                            newScrollX = 0;
                        }
                        break;
                    case SLIDE_RIGHT:
                        newScrollX = scrollX - offsetX;
                        if (newScrollX > mSlideView.getMeasuredWidth()) {
                            newScrollX = mSlideView.getMeasuredWidth();
                        } else if (newScrollX < 0) {
                            newScrollX = 0;
                        }
                        break;
                    case SLIDE_TOP:
                        newScrollY = scrollY - offsetY;

                        if (newScrollY < -mSlideView.getMeasuredHeight()) {
                            newScrollY = -mSlideView.getMeasuredHeight();
                        } else if (newScrollY > 0) {
                            newScrollY = 0;
                        }

                        break;
                    case SLIDE_BOTTOM:
                        newScrollY = scrollY - offsetY;
                        if (newScrollY > mSlideView.getMeasuredHeight()) {
                            newScrollY = mSlideView.getMeasuredHeight();
                        } else if (newScrollY < 0) {
                            newScrollY = 0;
                        }
                        break;
                }

                scrollTo(newScrollX, newScrollY);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int finalScrollX = 0;
                int finalScrollY = 0;

                switch (mSlideDirection) {
                    case SLIDE_LEFT:
                        if (scrollX < -getFixSlideDelta()) {
                            finalScrollX = -mSlideView.getMeasuredWidth();
                        }
                        break;
                    case SLIDE_RIGHT:
                        if (scrollX > getFixSlideDelta()) {
                            finalScrollX = mSlideView.getMeasuredWidth();
                        }
                        break;
                    case SLIDE_TOP:
                        if (scrollY < -getFixSlideDelta()) {
                            finalScrollY = -mSlideView.getMeasuredHeight();
                        }
                        break;
                    case SLIDE_BOTTOM:
                        if (scrollY > getFixSlideDelta()) {
                            finalScrollY = mSlideView.getMeasuredHeight();
                        }
                        break;
                }
                smoothScrollTo(finalScrollX, finalScrollY);
                break;
        }

        lastX = eventX;
        lastY = eventY;
        return super.dispatchTouchEvent(ev);
    }

    /**
     * @return
     */
    private int getFixSlideDelta() {
        if (mSlideDirection == SLIDE_LEFT
                || mSlideDirection == SLIDE_RIGHT) {
            if (mSlideDelta == 0) {
                mSlideDelta = mSlideView.getMeasuredWidth() / 2;
            }
        } else {
            if (mSlideDelta == 0) {
                mSlideDelta = mSlideView.getMeasuredHeight() / 2;
            }
        }

        return mSlideDelta;
    }


    private void smoothScrollTo(int destX, int destY) {
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        int deltaX = destX - scrollX;
        int deltaY = destY - scrollY;

        if (mScroller != null) {
            mScroller.startScroll(scrollX, scrollY, deltaX, deltaY,
                    (int) (Math.abs(Math.sqrt(deltaX * deltaX + deltaY * deltaY) * 3)));
        }
        postInvalidate();
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }


}
