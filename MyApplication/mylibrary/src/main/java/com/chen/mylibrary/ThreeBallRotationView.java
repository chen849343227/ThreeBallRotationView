package com.chen.mylibrary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by long on 17-6-5.
 */

public class ThreeBallRotationView extends View {

    /**
     * 默认球的最大半径和最小半径
     * 默认的距离
     */
    private final static int DEFAULT_MAX_RADIUS = 18;
    private final static int DEFAULT_MIN_RADIUS = 12;
    private final static int DEFAULT_DISTANCE = 70;

    /**
     * 当设置宽度和高度为wrap_content时,view的高(view宽度为默认为占满父容器)
     */
    private final static int DEFAULT_VIEW_HEIGHT = 50;

    /**
     * 三个小球默认的颜色
     */
    private final static int DEFAULT_FIRST_BALL_COLOR = Color
            .parseColor("#40df73");
    private final static int DEFAULT_SECOND_BALL_COLOR = Color
            .parseColor("#ffdf3e");
    private final static int DEFAULT_THIRD_BALL_COLOR = Color
            .parseColor("#ff733e");

    /**
     * 默认动画持续时间
     */
    private final static int DEFAULT_ANIMATION_DURATION = 1000;

    /**
     * 三个小球的画笔
     */
    private Paint mFirstPaint;
    private Paint mSecondPaint;
    private Paint mThirdPaint;

    private int firstBallColor = DEFAULT_FIRST_BALL_COLOR;
    private int secondBallColor = DEFAULT_SECOND_BALL_COLOR;
    private int thirdBallColor = DEFAULT_THIRD_BALL_COLOR;

    private float maxRadius = DEFAULT_MAX_RADIUS;
    private float minRadius = DEFAULT_MIN_RADIUS;
    private int distance = DEFAULT_DISTANCE;
    private long duration = DEFAULT_ANIMATION_DURATION;

    private Ball mFirstBall;
    private Ball mSecondBall;
    private Ball mThirdBall;

    private float mCenterX;
    private float mCenterY;

    private AnimatorSet animatorSet;

    public ThreeBallRotationView(Context context) {
        this(context, null);
    }

    public ThreeBallRotationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThreeBallRotationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ThreeBallRotationView);
        firstBallColor = ta.getColor(R.styleable.ThreeBallRotationView_first_ball_color, DEFAULT_FIRST_BALL_COLOR);
        secondBallColor = ta.getColor(R.styleable.ThreeBallRotationView_second_ball_color, DEFAULT_SECOND_BALL_COLOR);
        thirdBallColor = ta.getColor(R.styleable.ThreeBallRotationView_third_ball_color, DEFAULT_THIRD_BALL_COLOR);
        distance = (int) ta.getDimension(R.styleable.ThreeBallRotationView_balls_distance, DEFAULT_DISTANCE);
        maxRadius = ta.getDimension(R.styleable.ThreeBallRotationView_ball_max_radius, DEFAULT_MAX_RADIUS);
        minRadius = ta.getDimension(R.styleable.ThreeBallRotationView_ball_min_radius, DEFAULT_MIN_RADIUS);
        ta.recycle();
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mFirstBall = new Ball();
        mSecondBall = new Ball();
        mThirdBall = new Ball();

        mFirstBall.setColor(firstBallColor);
        mSecondBall.setColor(secondBallColor);
        mThirdBall.setColor(thirdBallColor);

        mFirstPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFirstPaint.setColor(firstBallColor);
        mSecondPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondPaint.setColor(secondBallColor);
        mThirdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThirdPaint.setColor(thirdBallColor);

        configAnimator();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        Log.e("onSizeChanged", "mCenterX:" + mCenterX + "," + "mCenterY:" + mCenterY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            //如果小球的直径大于当前View高度,则将View的高度变更为小球的直径（简单来说,就是小球可以将View撑大）
            if (maxRadius * 2 > DEFAULT_VIEW_HEIGHT) {
                setMeasuredDimension(widthSpecSize, (int) (maxRadius * 2));
                mCenterX = widthSpecSize / 2;
                mCenterY = maxRadius;
            } else {
                setMeasuredDimension(widthSpecSize, DEFAULT_VIEW_HEIGHT);
                mCenterX = widthSpecSize / 2;
                mCenterY = DEFAULT_VIEW_HEIGHT / 2;
            }
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, heightSpecSize);
            mCenterX = widthSpecSize / 2;
            mCenterY = heightSpecSize / 2;
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            if (maxRadius * 2 > DEFAULT_VIEW_HEIGHT) {
                setMeasuredDimension(widthSpecSize, (int) (maxRadius * 2));
                mCenterX = widthSpecSize / 2;
                mCenterY = maxRadius;
            } else {
                setMeasuredDimension(widthSpecSize, DEFAULT_VIEW_HEIGHT);
                mCenterX = widthSpecSize / 2;
                mCenterY = DEFAULT_VIEW_HEIGHT / 2;
            }
        }
        Log.e("onMeasure", "mCenterX:" + mCenterX + "," + "mCenterY:" + mCenterY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /**
         * 这里我还没有处理padding属性
         */
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();

      /*  int width = (int) (mCenterX - paddingLeft - paddingRight);
        int height = (int) (mCenterY - paddingTop - paddingBottom);*/
        if (mFirstBall.getRadius() >= mSecondBall.getRadius()) {
            if (mThirdBall.getRadius() >= mFirstBall.getRadius()) {
                canvas.drawCircle(mSecondBall.getCenterX(), mCenterY,
                        mSecondBall.getRadius(), mSecondPaint);
                canvas.drawCircle(mFirstBall.getCenterX(), mCenterY,
                        mFirstBall.getRadius(), mFirstPaint);
                canvas.drawCircle(mThirdBall.getCenterX(), mCenterY,
                        mThirdBall.getRadius(), mThirdPaint);
            } else {
                if (mSecondBall.getRadius() <= mThirdBall.getRadius()) {
                    canvas.drawCircle(mSecondBall.getCenterX(), mCenterY,
                            mSecondBall.getRadius(), mSecondPaint);
                    canvas.drawCircle(mThirdBall.getCenterX(), mCenterY,
                            mThirdBall.getRadius(), mThirdPaint);
                    canvas.drawCircle(mFirstBall.getCenterX(), mCenterY,
                            mFirstBall.getRadius(), mFirstPaint);
                } else {
                    canvas.drawCircle(mThirdBall.getCenterX(), mCenterY,
                            mThirdBall.getRadius(), mThirdPaint);
                    canvas.drawCircle(mSecondBall.getCenterX(), mCenterY,
                            mSecondBall.getRadius(), mSecondPaint);
                    canvas.drawCircle(mFirstBall.getCenterX(), mCenterY,
                            mFirstBall.getRadius(), mFirstPaint);
                }
            }
        } else {
            if (mThirdBall.getRadius() >= mSecondBall.getRadius()) {
                canvas.drawCircle(mFirstBall.getCenterX(), mCenterY,
                        mFirstBall.getRadius(), mFirstPaint);
                canvas.drawCircle(mSecondBall.getCenterX(), mCenterY,
                        mSecondBall.getRadius(), mSecondPaint);
                canvas.drawCircle(mThirdBall.getCenterX(), mCenterY,
                        mThirdBall.getRadius(), mThirdPaint);
            } else {
                if (mFirstBall.getRadius() <= mThirdBall.getRadius()) {
                    canvas.drawCircle(mFirstBall.getCenterX(), mCenterY,
                            mFirstBall.getRadius(), mFirstPaint);
                    canvas.drawCircle(mThirdBall.getCenterX(), mCenterY,
                            mThirdBall.getRadius(), mThirdPaint);
                    canvas.drawCircle(mSecondBall.getCenterX(), mCenterY,
                            mSecondBall.getRadius(), mSecondPaint);
                } else {
                    canvas.drawCircle(mThirdBall.getCenterX(), mCenterY,
                            mThirdBall.getRadius(), mThirdPaint);
                    canvas.drawCircle(mFirstBall.getCenterX(), mCenterY,
                            mFirstBall.getRadius(), mFirstPaint);
                    canvas.drawCircle(mSecondBall.getCenterX(), mCenterY,
                            mSecondBall.getRadius(), mSecondPaint);
                }
            }
        }
    }

    /**
     * 初始化动画
     */
    private void configAnimator() {
        final float centerRadius = (minRadius + maxRadius) * 0.5f;
        //第一个小球
        //缩放动画
        ObjectAnimator mFirstScaleAnimator = ObjectAnimator.ofFloat(mFirstBall, "radius", centerRadius, maxRadius, centerRadius, minRadius, centerRadius);
        mFirstScaleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        //平移动画
        ValueAnimator mFirstCenterAnimator = ValueAnimator.ofFloat(-1, 0, 1, -1);
        mFirstCenterAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mFirstCenterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                float x = mCenterX + (distance) * value;
                Log.e("xxx", String.valueOf(mCenterX));
                Log.e("xx", String.valueOf(x));
                mFirstBall.setCenterX(x);
                invalidate();
            }
        });
        //透明度动画
        ValueAnimator mFirstAlphaAnimator = ValueAnimator.ofFloat(0.8f, 1, 0.8f, 0, 0.8f);
        mFirstAlphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mFirstAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                int alpha = (int) (255 * value);
                mFirstPaint.setAlpha(alpha);
            }
        });

        //第二个小球
        //缩放
        ObjectAnimator mSecondScaleAnimator = ObjectAnimator.ofFloat(mSecondBall, "radius", maxRadius, centerRadius, minRadius, centerRadius, maxRadius);
        mSecondScaleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        //平移
        ValueAnimator mSecondCenterAnimator = ValueAnimator.ofFloat(0, 1, -1, 0);
        mSecondCenterAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mSecondCenterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                float x = mCenterX + (distance) * value;
                mSecondBall.setCenterX(x);
            }
        });
        //透明度
        ValueAnimator mSecondAlphaAnimator = ValueAnimator.ofFloat(1, 0.8f, 0, 0.8f, 1);
        mSecondAlphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mSecondAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                int alpha = (int) (255 * value);
                mSecondPaint.setAlpha(alpha);
            }
        });

        //第三个小球
        //缩放
        ObjectAnimator mThirdScaleAnimator = ObjectAnimator.ofFloat(mThirdBall, "radius", centerRadius, minRadius, centerRadius, maxRadius, centerRadius);
        mThirdScaleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        //平移
        ValueAnimator mThirdCenterAnimator = ValueAnimator.ofFloat(1, -1, 0, 1);
        mThirdCenterAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mThirdCenterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                float x = mCenterX + (distance) * value;
                mThirdBall.setCenterX(x);
            }
        });
        ValueAnimator mThirdAlphaAnimator = ValueAnimator.ofFloat(0.8f, 0, 0.8f, 1, 0.8f);
        mThirdAlphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mThirdAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                int alpha = (int) (value * 255);
                mThirdPaint.setAlpha(alpha);
            }
        });

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(mFirstScaleAnimator, mFirstCenterAnimator, mFirstAlphaAnimator,
                mSecondScaleAnimator, mSecondCenterAnimator, mSecondAlphaAnimator,
                mThirdScaleAnimator, mThirdCenterAnimator, mThirdAlphaAnimator);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new LinearInterpolator());
    }

    public class Ball {
        private float radius;
        private float centerX;
        private int color;

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public float getCenterX() {
            return centerX;
        }

        public void setCenterX(float centerX) {
            this.centerX = centerX;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (getVisibility() != visibility) {
            super.setVisibility(visibility);
            if (visibility == GONE || visibility == INVISIBLE) {
                stopAnimator();
            } else {
                startAnimator();
            }
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == GONE || visibility == INVISIBLE) {
            stopAnimator();
        } else {
            startAnimator();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimator();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimator();
    }

    /**
     * 设置第一个球的绘制颜色
     *
     * @param color
     */
    public void setOneBallColor(int color) {
        mFirstBall.setColor(color);
    }

    /**
     * 设置第二个球的绘制颜色
     *
     * @param color
     */
    public void setTwoBallColor(int color) {
        mSecondBall.setColor(color);
    }

    /**
     * 设置第三个球的绘制颜色
     *
     * @param color
     */
    public void setThreeBallColor(int color) {
        mThirdBall.setColor(color);
    }

    /**
     * 设置小球的最大半径
     *
     * @param maxRadius
     */
    public void setMaxRadius(float maxRadius) {
        this.maxRadius = maxRadius;
        configAnimator();
    }

    /**
     * 设置小球的小一点的半径
     *
     * @param minRadius
     */
    public void setMinRadius(float minRadius) {
        this.minRadius = minRadius;
        configAnimator();
    }

    /**
     * 设置小球之间的距离
     *
     * @param distance
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * 设置单个动画的持续时间
     *
     * @param duration
     */
    public void setDuration(long duration) {
        this.duration = duration;
        if (animatorSet != null) {
            animatorSet.setDuration(duration);
        }
    }

    /**
     * 开始动画
     */
    private void startAnimator() {
        if (getVisibility() != VISIBLE)
            return;

        if (animatorSet.isRunning())
            return;

        if (animatorSet != null) {
            animatorSet.start();
        }
    }

    /**
     * 停止动画
     */
    private void stopAnimator() {
        if (animatorSet != null) {
            animatorSet.end();
        }
    }
}
