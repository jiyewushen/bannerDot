package com.dotlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * update by cx on 2018/10/31.
 */

public class DotIndicator extends View {
    public static final boolean DEBUG=false;
    private static final String TAG = "DotIndicator";
    //选中画笔
    private Paint mCirclePaint;
    //背景画笔
    private Paint mCirclePaint2;
    //选中路径
    private Path mPath = new Path();
    //背景路径
    private Path mPath2 = new Path();
    //选中颜色
    private int mSelectedColor;
    //未选中颜色
    private int mUnSelectedColor;
    //圆点之间距离
    private float distance = 80;
    //起始圆初始半径
    private float mRadius = 30;
    //起始圆变化半径
    private float mChangeRadius;
    //背景圆初始半径
    private float mNomarlRadius = 20;
    //背景圆变化半径 背景圆都用这个
    private float mChangeBgRadius;
    //起始圆辅助圆变化半径
    private float mSupportChangeRadius;
    //将要到达位置的背景圆的辅助圆变化半径
    private float msupportNextChangeradius;
    //起始圆圆心坐标
    float mCenterPointX;
    float mCenterPointY;
    //起始圆辅助圆圆心坐标
    float mSupportCircleX;
    float mSupportCircleY;
    //当前背景圆圆心坐标
    float msupportNextCenterx;
    float msupportNextCentery;
    //将要到达位置的背景圆圆心坐标
    float mbgNextPointX;
    float mbgNextPointY;

    //是否进入自动移动状态
    private boolean autoMove = false;
    //第一阶段运动进度
    private float mProgress = 0;
    //第二阶段运动进度
    private float mProgress2 = 0;
    //整体运动进度 也是原始进度
    private float mOriginProgress;

    //当前选中的位置
    private int mSelectedIndex = 0;
    private int count;

    //第一阶段运动
    private int moveStepOne = 1;
    //第二阶段运动
    private int moveStepTwo = 2;
    //控制点坐标
    float controlPointX;
    float controlPointY;
    //起点坐标
    float mStartX;
    float mStartY;
    //终点坐标
    float endPointX;
    float endPointY;
    private int mDrection;
    //向右滑 向左滚动
    public static int DIRECTION_LEFT = 1;
    //向左滑 向右滚动
    public static int DIRECTION_RIGHT = 2;
    

    Interpolator accelerateinterpolator = new AccelerateDecelerateInterpolator();


    public DotIndicator(Context context) {
        this(context, null);
    }

    public DotIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        initPaint();
    }


    private void initPaint() {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(mSelectedColor);
        p.setStyle(Paint.Style.FILL);
        p.setAntiAlias(true);
        p.setDither(true);
        mCirclePaint = p;

        Paint p1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p1.setColor(mUnSelectedColor);
        p1.setStyle(Paint.Style.FILL);
        p1.setAntiAlias(true);
        p1.setDither(true);
        mCirclePaint2 = p1;

    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BezierBannerDot);
        mSelectedColor = typedArray.getColor(R.styleable.BezierBannerDot_selectedColor, 0xFFFFFFFF);
        mUnSelectedColor = typedArray.getColor(R.styleable.BezierBannerDot_unSelectedColor, 0xFFAAAAAA);
        mRadius = typedArray.getDimension(R.styleable.BezierBannerDot_selectedRadius, mRadius);
        mNomarlRadius = typedArray.getDimension(R.styleable.BezierBannerDot_unSelectedRadius, mNomarlRadius);
        distance = typedArray.getDimension(R.styleable.BezierBannerDot_spacing, distance);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //宽度等于所有圆点宽度+之间的间隔+padding;要留出当左右两个是大圆点的左右边距
        int width = (int) (mNomarlRadius * 2 * count + (mRadius - mNomarlRadius) * 2 + (count - 1) * distance + getPaddingLeft() + getPaddingRight());
        int height = (int) (2 * mRadius + getPaddingTop() + getPaddingBottom());

        int mHeight, mWidth;

        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            mWidth = Math.min(widthSize, width);
        } else {
            mWidth = widthSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            mHeight = Math.min(heightSize, height);
        } else {
            mHeight = heightSize;
        }

        setMeasuredDimension(mWidth, mHeight);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());

        //画暂不活动的背景圆
        for (int i = 0; i < count; i++) {
            if (mDrection == DIRECTION_RIGHT) {
                if (i == mSelectedIndex || i == mSelectedIndex + 1) {
                    //活动的就不用画了
                } else {
                    canvas.drawCircle(getCenterPointAt(i), mRadius, mNomarlRadius, mCirclePaint2);
                }

            } else if (mDrection == DIRECTION_LEFT) {
                if (i == mSelectedIndex || i == mSelectedIndex - 1) {
                    //活动的就不用画了
                } else {
                    canvas.drawCircle(getCenterPointAt(i), mRadius, mNomarlRadius, mCirclePaint2);
                }
            }
        }

        //画活动背景圆
        canvas.drawCircle(msupportNextCenterx, msupportNextCentery, msupportNextChangeradius, mCirclePaint2);
        canvas.drawCircle(mbgNextPointX, mbgNextPointY, mChangeBgRadius, mCirclePaint2);
        canvas.drawPath(mPath2, mCirclePaint2);
        //画选中圆
        canvas.drawCircle(mSupportCircleX, mSupportCircleY, mSupportChangeRadius, mCirclePaint);
        canvas.drawCircle(mCenterPointX, mCenterPointY, mChangeRadius, mCirclePaint);
        canvas.drawPath(mPath, mCirclePaint);

        canvas.restore();

    }

    /**
     * 转化整体进度值使两个阶段的运动进度都是0-1
     *
     * @param progress 当前整体进度
     */
    public void setProgress(float progress) {
        //viewpager滑动完毕返回的0不需要，拦截掉
        if (progress == 0) {
            if (DEBUG) {
                Log.d(TAG, "拦截");
            }
            resetProgress();
            if (mDrection == DIRECTION_RIGHT) {
                moveToNext();
            } else {
                moveToPrivous();
            }
            invalidate();
            return;
        }
        mOriginProgress = progress;
        if (progress <= 0.5f) {
            mProgress = progress / 0.5f;
            mProgress2 = 0;
        } else {
            mProgress2 = (progress - 0.5f) / 0.5f;
            mProgress = 1;
        }
        if (mDrection == DIRECTION_RIGHT) {
            moveToNext();
        } else {
            moveToPrivous();
        }
        invalidate();
       if (DEBUG) {
           Log.d(TAG, "刷新");
       }
    }

    /**
     * 向右移动
     */
    private void moveToNext() {
        //重置路径
        mPath.reset();
        mPath2.reset();
        //使用一个插值器使圆的大小变化两边慢中间快
        float mRadiusProgress = accelerateinterpolator.getInterpolation(mOriginProgress);
        //----------------------选中圆--------------------------------
        //起始圆圆心
        mCenterPointX = getValue(getCenterPointAt(mSelectedIndex), getCenterPointAt(mSelectedIndex + 1) - mRadius, moveStepTwo);
        mCenterPointY = mRadius;
        //起始圆半径
        mChangeRadius = getValue(mRadius, 0, mRadiusProgress);

        //起点与起始圆圆心间的角度
        double radian = Math.toRadians(getValue(45, 0, moveStepOne));
        //X轴距离圆心距离
        float mX = (float) (Math.sin(radian) * mChangeRadius);
        //Y轴距离圆心距离
        float mY = (float) (Math.cos(radian) * mChangeRadius);

        //辅助圆
        mSupportCircleX = getValue(getCenterPointAt(mSelectedIndex) + mRadius, getCenterPointAt(mSelectedIndex + 1), moveStepOne);
        mSupportCircleY = mRadius;
        mSupportChangeRadius = getValue(0, mRadius, mRadiusProgress);

        //终点与辅助圆圆心间的角度
        double supportradian = Math.toRadians(getValue(0, 45, moveStepTwo));
        //X轴距离圆心距离
        float msupportradianX = (float) (Math.sin(supportradian) * mSupportChangeRadius);
        //Y轴距离圆心距离
        float msupportradianY = (float) (Math.cos(supportradian) * mSupportChangeRadius);

        //起点
        mStartX = mCenterPointX + mX;
        mStartY = mCenterPointY - mY;

        //终点
        endPointX = mSupportCircleX - msupportradianX;
        endPointY = mRadius - msupportradianY;

        //控制点
        controlPointX = getValueForAll(getCenterPointAt(mSelectedIndex) + mRadius, getCenterPointAt(mSelectedIndex + 1) - mRadius);
        controlPointY = mRadius;

        //移动到起点
        mPath.moveTo(mStartX, mStartY);

        //形成闭合区域
        mPath.quadTo(controlPointX, controlPointY, endPointX, endPointY);
        mPath.lineTo(endPointX, mRadius + msupportradianY);
        mPath.quadTo(controlPointX, mRadius, mStartX, mStartY + 2 * mY);
        mPath.lineTo(mStartX, mStartY);

        //----------------------背景圆反方向移动--------------------------------
        //起始圆圆心
        mbgNextPointX = getValue(getCenterPointAt(mSelectedIndex + 1), getCenterPointAt(mSelectedIndex) + mNomarlRadius, moveStepTwo);
        mbgNextPointY = mRadius;
        //起始圆半径
        mChangeBgRadius = getValue(mNomarlRadius, 0, mRadiusProgress);

        //起点与起始圆圆心间的角度
        double mNextRadian = Math.toRadians(getValue(45, 0, moveStepOne));
        float mxNext = (float) (Math.sin(mNextRadian) * mChangeBgRadius);
        float myNext = (float) (Math.cos(mNextRadian) * mChangeBgRadius);
        //辅助圆圆心
        msupportNextCenterx = getValue(getCenterPointAt(mSelectedIndex + 1) - mNomarlRadius, getCenterPointAt(mSelectedIndex), moveStepOne);
        msupportNextCentery = mRadius;
        //辅助圆半径
        msupportNextChangeradius = getValue(0, mNomarlRadius, mRadiusProgress);

        //终点与辅助圆圆心间的角度
        double msupportNextRadian = Math.toRadians(getValue(0, 45, moveStepTwo));
        float msupportNextRadianx = (float) (Math.sin(msupportNextRadian) * msupportNextChangeradius);
        float msupportNextRadiany = (float) (Math.cos(msupportNextRadian) * msupportNextChangeradius);

        //起点
        float startpointSupportNextx = mbgNextPointX - mxNext;
        float startpointSupportNexty = mbgNextPointY - myNext;

        //终点
        float endpointSupportNextx = msupportNextCenterx + msupportNextRadianx;
        float endpointSupportNexty = msupportNextCentery - msupportNextRadiany;

        //控制点
        float controlpointxNext = getValueForAll(getCenterPointAt(mSelectedIndex + 1) - mNomarlRadius, getCenterPointAt(mSelectedIndex) + mNomarlRadius);
        float controlpointyNext = mRadius;

        //移动到起点
        mPath2.moveTo(startpointSupportNextx, startpointSupportNexty);
        //形成闭合区域
        mPath2.quadTo(controlpointxNext, controlpointyNext, endpointSupportNextx, endpointSupportNexty);
        mPath2.lineTo(endpointSupportNextx, mRadius + msupportNextRadiany);
        mPath2.quadTo(controlpointxNext, controlpointyNext, startpointSupportNextx, startpointSupportNexty + 2 * myNext);
        mPath2.lineTo(startpointSupportNextx, startpointSupportNexty);

    }


    /**
     * 向左移动(与向右过程大致相同)
     */
    private void moveToPrivous() {
        mPath.reset();
        mPath2.reset();

        float mRadiusProgress = accelerateinterpolator.getInterpolation(mOriginProgress);

        //----------------------选中圆--------------------------------
        mCenterPointX = getValue(getCenterPointAt(mSelectedIndex), getCenterPointAt(mSelectedIndex - 1) + mRadius, moveStepTwo);
        mCenterPointY = mRadius;
        mChangeRadius = getValue(mRadius, 0, mRadiusProgress);
        //起点与起始圆圆心间的角度
        double radian = Math.toRadians(getValue(45, 0, moveStepOne));
        //X轴距离圆心距离
        float mX = (float) (Math.sin(radian) * mChangeRadius);
        //Y轴距离圆心距离
        float mY = (float) (Math.cos(radian) * mChangeRadius);

        //辅助圆
        mSupportCircleX = getValue(getCenterPointAt(mSelectedIndex) - mRadius, getCenterPointAt(mSelectedIndex - 1), moveStepOne);
        mSupportCircleY = mRadius;
        mSupportChangeRadius = getValue(0, mRadius, mRadiusProgress);


        //终点与辅助圆圆心间的角度
        double supportradian = Math.toRadians(getValue(0, 45, moveStepTwo));
        //X轴距离圆心距离
        float msupportradianX = (float) (Math.sin(supportradian) * mSupportChangeRadius);
        //Y轴距离圆心距离
        float msupportradianY = (float) (Math.cos(supportradian) * mSupportChangeRadius);

        mStartX = mCenterPointX - mX;
        mStartY = mCenterPointY - mY;

        endPointX = mSupportCircleX + msupportradianX;
        endPointY = mRadius - msupportradianY;

        controlPointX = getValueForAll(getCenterPointAt(mSelectedIndex) - mRadius, getCenterPointAt(mSelectedIndex - 1) + mRadius);
        controlPointY = mRadius;

        mPath.moveTo(mStartX, mStartY);
        mPath.quadTo(controlPointX, controlPointY, endPointX, endPointY);
        mPath.lineTo(endPointX, mRadius + msupportradianY);
        mPath.quadTo(controlPointX, mRadius, mStartX, mStartY + 2 * mY);
        mPath.lineTo(mStartX, mStartY);


        //----------------------背景圆反方向移动--------------------------------
        mbgNextPointX = getValue(getCenterPointAt(mSelectedIndex - 1), getCenterPointAt(mSelectedIndex) - mNomarlRadius, moveStepTwo);
        mbgNextPointY = mRadius;
        mChangeBgRadius = getValue(mNomarlRadius, 0, mRadiusProgress);
        //起点与起始圆圆心间的角度
        double mNextRadian = Math.toRadians(getValue(45, 0, moveStepOne));
        //X轴距离圆心距离
        float mxNext = (float) (Math.sin(mNextRadian) * mChangeBgRadius);
        //Y轴距离圆心距离
        float myNext = (float) (Math.cos(mNextRadian) * mChangeBgRadius);

        msupportNextCenterx = getValue(getCenterPointAt(mSelectedIndex - 1) + mNomarlRadius, getCenterPointAt(mSelectedIndex), moveStepOne);
        msupportNextCentery = mRadius;
        msupportNextChangeradius = getValue(0, mNomarlRadius, mRadiusProgress);

        //终点与辅助圆圆心间的角度
        double msupportNextRadian = Math.toRadians(getValue(0, 45, moveStepTwo));
        //X轴距离圆心距离
        float msupportNextRadianx = (float) (Math.sin(msupportNextRadian) * msupportNextChangeradius);
        //Y轴距离圆心距离
        float msupportNextRadiany = (float) (Math.cos(msupportNextRadian) * msupportNextChangeradius);

        float startpointSupportNextx = mbgNextPointX + mxNext;
        float startpointSupportNexty = mbgNextPointY - myNext;

        float endpointSupportNextx = msupportNextCenterx - msupportNextRadianx;
        float endpointSupportNexty = msupportNextCentery - msupportNextRadiany;

        float controlpointxNext = getValueForAll(getCenterPointAt(mSelectedIndex - 1) + mNomarlRadius, getCenterPointAt(mSelectedIndex) - mNomarlRadius);
        float controlpointyNext = mRadius;

        mPath2.moveTo(startpointSupportNextx, startpointSupportNexty);
        mPath2.quadTo(controlpointxNext, controlpointyNext, endpointSupportNextx, endpointSupportNexty);
        mPath2.lineTo(endpointSupportNextx, mRadius + msupportNextRadiany);
        mPath2.quadTo(controlpointxNext, controlpointyNext, startpointSupportNextx, startpointSupportNexty + 2 * myNext);
        mPath2.lineTo(startpointSupportNextx, startpointSupportNexty);

    }


    /**
     * 获取当前值(适用分阶段变化的值)
     *
     * @param start 初始值
     * @param end   终值
     * @param step  第几活动阶段
     * @return
     */
    public float getValue(float start, float end, int step) {
        if (step == moveStepOne) {
            return start + (end - start) * mProgress;
        } else {
            return start + (end - start) * mProgress2;
        }
    }

    /**
     * 获取当前值（适用全过程变化的值）
     *
     * @param start 初始值
     * @param end   终值
     * @return
     */
    public float getValueForAll(float start, float end) {
        return start + (end - start) * mOriginProgress;
    }

    /**
     * 通过进度获取当前值
     *
     * @param start    初始值
     * @param end      终值
     * @param progress 当前进度
     * @return
     */
    public float getValue(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    /**
     * 获取圆心X坐标
     *
     * @param index 第几个圆
     * @return
     */
    private float getCenterPointAt(int index) {
        if (index == 0) {
            return mRadius;
        }
        return index * (distance + 2 * mNomarlRadius) + mNomarlRadius + (mRadius - mNomarlRadius);
    }


    public void setDirection(int direction) {
        mDrection = direction;
    }

    /**
     * 重置进度
     */
    public void resetProgress() {
        mProgress = 0;
        mProgress2 = 0;
        mOriginProgress = 0;
    }

    /**
     * 绑定viewpager
     */
    public void attachToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                DotIndicator.this.onScrollStateChanged(recyclerView,newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                DotIndicator.this.onScrolled(recyclerView, dx, dy);
            }
        });
        count = recyclerView.getAdapter().getItemCount();
        moveToNext();
        mDrection = DIRECTION_RIGHT;
        invalidate();
    }

    public void setSelectedIndex(int selectedIndex) {
        mSelectedIndex = selectedIndex;
        resetProgress();
        if (mDrection == DIRECTION_RIGHT) {
            moveToNext();
        } else {
            moveToPrivous();
        }
        invalidate();
    }

    private void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        switch (newState){
            case RecyclerView.SCROLL_STATE_IDLE:
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                break;
            default:
                break;
        }
    }


    private void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
            if (manager.canScrollHorizontally()) {
                int fp = manager.findFirstVisibleItemPosition();
                /**
                 * fp==lp出现情况：在每一个Item的宽度恰好等于RecyclerView的长度时，当总Item数目为1,则二者恒等
                 * 如果大于1,当第一个Item或最后一个Item完全显示时出现。
                 */
                View firstView = manager.findViewByPosition(fp);
                int width = manager.getChildAt(0).getWidth();
                if (width != 0) {
                    if (firstView!=null) {
                        float right = recyclerView.getWidth() -firstView.getRight();
                        float ratio = right / width;
                        onPageScrolled(fp, ratio);
                    }
                }

            }
        }
    }

    /**
     * @param position
     * @param positionOffset 表示偏移量，当positionOffset为0时表示运动停止
     */
    public void onPageScrolled(int position, float positionOffset) {
        //偏移量为0 说明运动停止
        if (positionOffset == 0) {
            mSelectedIndex = position;
            resetProgress();
            if (mDrection == DIRECTION_RIGHT) {
                moveToNext();
            } else {
                moveToPrivous();
            }
            invalidate();
            return;
        }
        //向左滑，指示器向右移动
        if (position + positionOffset - mSelectedIndex > 0) {
            mDrection = DIRECTION_RIGHT;
            //向左快速滑动 偏移量不归0 但是position发生了改变 需要更新当前索引
            if (mDrection == DIRECTION_RIGHT && position + positionOffset > mSelectedIndex + 1) {
                mSelectedIndex = position;
            } else {
                setProgress(positionOffset);
            }
        } else if (position + positionOffset - mSelectedIndex < 0) { //向右滑，指示器向左移动
            mDrection = DIRECTION_LEFT;
            //向右快速滑动
            if (mDrection == DIRECTION_LEFT && position + positionOffset < mSelectedIndex - 1) {
                mSelectedIndex = position;
            } else {
                setProgress(1 - positionOffset);
            }
        }
    }


}
