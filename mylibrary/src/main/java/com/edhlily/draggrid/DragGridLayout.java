package com.edhlily.draggrid;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DragGridLayout extends ViewGroup {
    private static final String TAG = "DragGridLayout";
    /**
     * 滑动多少距离后触发拖动操作
     */
    private static final int DRAG_SLOP = 25;

    /**
     * 按住不动多长时间后触发拖动操作
     */
    private static final int CLICK_TIME_OUT = 200;

    /**
     * 允许所有方向拖动
     */
    public static final int ORIENTATION_ALL = 0;

    /**
     * 只允许纵向拖动
     */
    public static final int ORIENTATION_VERTICAL = 1;

    /**
     * 只允许横向拖动
     */
    public static final int ORIENTATION_HORIZONTAL = 2;

    /**
     * grid多少行
     */
    private static final int ROW_COUNT = R.styleable.DragGridLayout_rowCount;

    /**
     * grid多少列
     */
    private static final int COLUMN_COUNT = R.styleable.DragGridLayout_colCount;

    /**
     * row间隔
     */
    private static final int ROW_SPACING = R.styleable.DragGridLayout_rowSpacing;

    /**
     * column间隔
     */
    private static final int COL_SPACING = R.styleable.DragGridLayout_colSpacing;

    /**
     * 行
     */
    private int rowCount = 1;

    /**
     * 列
     */
    private int colCount = 1;

    /**
     * 行间隔
     */
    private int rowSpacing = 0;
    /**
     * 列间隔
     */
    private int colSpacing = 0;

    /**
     * 当Item被拖动时的放大系数
     */
    private float dragScale = 1.1f;

    /**
     * Item移动动画时间
     */
    private long animationDuration = -1;

    /**
     * Item是否可以拖动
     */
    private boolean draggable = true;
    /**
     * 约束可拖动的方向
     */
    private int dragOrientation = ORIENTATION_ALL;

    /**
     * 是否允许Item拖动的时候超出边界
     */
    private boolean allowItemOutside = false;

    private OnDragStatusChangedListener onDragStatusChangedListener;

    private Paint paint;


    public DragGridLayout(Context context) {
        this(context, null);
    }

    public DragGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);

    }

    public DragGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.DragGridLayout, defStyleAttr, defStyleRes);
        try {
            setRowCount(a.getInt(ROW_COUNT, 1));
            setColCount(a.getInt(COLUMN_COUNT, 1));
            setRowSpacing(a.getDimensionPixelSize(ROW_SPACING, 0));
            setColSpacing(a.getDimensionPixelSize(COL_SPACING, 0));
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        setClipChildren(false);
        setClipToPadding(false);

        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
    }

    public interface OnDragStatusChangedListener {
        /**
         * 开始拖动
         *
         * @param v
         */
        void onDragStart(android.view.View v);

        /**
         * 结束拖动
         *
         * @param v
         */
        void onDragEnd(android.view.View v);

        /**
         * Item位置发生了改变
         *
         * @param item   发生改变的Item
         * @param target 是否是被拖动的Item
         */
        void onItemPositionChanged(android.view.View item, boolean target);
    }

    public OnDragStatusChangedListener getOnDragStatusChangedListener() {
        return onDragStatusChangedListener;
    }

    public void setOnDragStatusChangedListener(OnDragStatusChangedListener onDragStatusChangedListener) {
        this.onDragStatusChangedListener = onDragStatusChangedListener;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {
        private static final int COLUMN = R.styleable.DragGridLayout_Layout_layout_column;
        private static final int COLUMN_SPAN = R.styleable.DragGridLayout_Layout_layout_columnSpan;
        private static final int ROW = R.styleable.DragGridLayout_Layout_layout_row;
        private static final int ROW_SPAN = R.styleable.DragGridLayout_Layout_layout_rowSpan;

        private int row = 0;
        private int col = 0;
        private int rowSpan = 1;
        private int colSpan = 1;

        public LayoutParams() {
            super(MATCH_PARENT, MATCH_PARENT);
        }


        public LayoutParams(ViewGroup.LayoutParams params) {
            super(params);
            if (params instanceof LayoutParams) {
                this.row = ((LayoutParams) params).row;
                this.col = ((LayoutParams) params).col;
                this.rowSpan = ((LayoutParams) params).rowSpan;
                this.colSpan = ((LayoutParams) params).colSpan;
            }
        }

        public LayoutParams(int row, int col, int rowSpan, int colSpan) {
            super(MATCH_PARENT, MATCH_PARENT);
            this.row = row;
            this.col = col;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            init(c, attrs);
        }

        private void init(Context context, AttributeSet attrs) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DragGridLayout_Layout);
            try {
                col = a.getInt(COLUMN, 1);
                colSpan = a.getInt(COLUMN_SPAN, 1);
                row = a.getInt(ROW, 1);
                rowSpan = a.getInt(ROW_SPAN, 1);
            } finally {
                a.recycle();
            }
        }


        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }

        public int getRowSpan() {
            return rowSpan;
        }

        public void setRowSpan(int rowSpan) {
            this.rowSpan = rowSpan;
        }

        public int getColSpan() {
            return colSpan;
        }

        public void setColSpan(int colSpan) {
            this.colSpan = colSpan;
        }
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public void setColCount(int colCount) {
        this.colCount = colCount;
    }

    public int getRowSpacing() {
        return rowSpacing;
    }

    public void setRowSpacing(int rowSpacing) {
        this.rowSpacing = rowSpacing;
    }

    public int getColSpacing() {
        return colSpacing;
    }

    public void setColSpacing(int colSpacing) {
        this.colSpacing = colSpacing;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    public int getDragOrientation() {
        return dragOrientation;
    }

    public void setDragOrientation(int dragOrientation) {
        this.dragOrientation = dragOrientation;
    }

    public boolean isAllowItemOutside() {
        return allowItemOutside;
    }

    public void setAllowItemOutside(boolean allowItemOutside) {
        this.allowItemOutside = allowItemOutside;
    }

    public float getDragScale() {
        return dragScale;
    }

    public void setDragScale(float dragScale) {
        this.dragScale = dragScale;
    }

    public long getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(long animationDuration) {
        this.animationDuration = animationDuration;
    }

    int cellWidth;
    int cellHeight;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        cellWidth = getCellWidth();
        cellHeight = getCellHeight();

        for (int i = 0; i < getChildCount(); i++) {
            android.view.View c = getChildAt(i);
            if (c.getVisibility() == android.view.View.GONE) {
                continue;
            }
            LayoutParams layoutParams = getLayoutParams(c);
            int cl = getCellLeft(layoutParams.col);
            int ct = getCellTop(layoutParams.row);
            int cr =
                    cl + layoutParams.colSpan * cellWidth + (layoutParams.colSpan - 1) * colSpacing;
            int cb =
                    ct + layoutParams.rowSpan * cellHeight + (layoutParams.rowSpan - 1) * rowSpacing;

            int cWidth = cr - cl;
            int cHeight = cb - ct;
            if (cWidth != c.getMeasuredWidth() || cHeight != c.getMeasuredHeight()) {
                c.measure(
                        MeasureSpec.makeMeasureSpec(cWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(cHeight, MeasureSpec.EXACTLY)
                );
            }

            c.layout(cl, ct, cr, cb);

        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (cellWidth != 0 && cellHeight != 0) {
            for (int i = 0; i < getChildCount(); i++) {
                android.view.View c = getChildAt(i);
                if (c.getVisibility() == android.view.View.GONE) {
                    continue;
                }
                LayoutParams layoutParams = getLayoutParams(c);
                int cl = getCellLeft(layoutParams.col);
                int ct = getCellTop(layoutParams.row);
                int cr =
                        cl + layoutParams.colSpan * cellWidth + (layoutParams.colSpan - 1) * colSpacing;
                int cb =
                        ct + layoutParams.rowSpan * cellHeight + (layoutParams.rowSpan - 1) * rowSpacing;

                int cWidth = cr - cl;
                int cHeight = cb - ct;
                c.measure(
                        MeasureSpec.makeMeasureSpec(cWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(cHeight, MeasureSpec.EXACTLY)
                );
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int getSumPaddingX() {
        return getPaddingLeft() + getPaddingRight();
    }

    private int getSumPaddingY() {
        return getPaddingTop() + getPaddingBottom();
    }

    private int getSumMarginX() {
        return (colCount - 1) * colSpacing;
    }

    private int getSumMarginY() {
        return (rowCount - 1) * rowSpacing;
    }

    int getCellWidth() {
        return (getWidth() - getSumPaddingX() - getSumMarginX()) / colCount;
    }

    int getCellHeight() {
        return (getHeight() - getSumPaddingY() - getSumMarginY()) / rowCount;
    }

    private int getCellLeft(int col) {
        return getPaddingLeft() + col * colSpacing + col * getCellWidth();
    }

    private int getCellTop(int row) {
        return getPaddingTop() + row * rowSpacing + row * getCellHeight();
    }

    private LayoutParams getLayoutParams(android.view.View c) {
        return (LayoutParams) c.getLayoutParams();
    }

    private float downX = 0f;
    private float downY = 0f;
    private float currentX = 0f;
    private float currentY = 0f;

    private final Runnable determineClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (draggingChild != null && !draggingChild.dragging && !draggingChild.dragged) {
                Log.i(TAG, "startDragChild determineClickRunnable");
                startDragChild(draggingChild);
            }
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = draggingChild == null ? super.onInterceptTouchEvent(ev) : draggingChild.dragging ? true : super.onInterceptTouchEvent(ev);
        android.util.Log.i(TAG, "onInterceptTouchEvent : " + result);
        return result;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                currentX = ev.getX();
                currentY = ev.getY();
                if (draggable && draggingChild == null) {
                    View child = findTopChildUnder((int) ev.getX(), (int) ev.getY());
                    if (child != null) {
                        //找到拖拽目标
                        prepareDrag(child);
                        postDelayed(determineClickRunnable, CLICK_TIME_OUT);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                currentX = ev.getX();
                currentY = ev.getY();
                if (draggable && draggingChild != null && !draggingChild.dragging && !draggingChild.dragged) {
                    if (Math.abs(currentX - downX) > DRAG_SLOP || Math.abs(currentY - downY) > DRAG_SLOP) {
                        Log.i(TAG, "startDragChild ACTION_MOVE");
                        startDragChild(draggingChild);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.i(TAG, "ACTION_CANCEL");
                getParent().requestDisallowInterceptTouchEvent(false);
                removeCallbacks(determineClickRunnable);
                if (draggable && draggingChild != null && draggingChild.dragging && !draggingChild.dragged) {
                    draggingChild.setDragging(false);
                    animateTarget(draggingChild);
                } else {
                    draggingChild = null;
                }
                break;

        }


        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (draggingChild != null) {
            Rect rect = new Rect();
            rect.left = getCellLeft(draggingChild.targetCol) - 10;
            rect.top = getCellTop(draggingChild.targetRow) - 10;
            rect.right = getCellLeft(draggingChild.targetCol + draggingChild.colSpan - 1) + cellWidth + 10;
            rect.bottom = getCellTop(draggingChild.targetRow + draggingChild.rowSpan - 1) + cellHeight + 10;
            canvas.drawRect(rect, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "onTouchEvent : ACTION_MOVE");
                if (draggingChild != null && draggingChild.dragging && !draggingChild.dragged) {
                    if (dragOrientation == ORIENTATION_ALL || dragOrientation == ORIENTATION_HORIZONTAL) {
                        float x = (currentX - draggingFingerStartX);
                        Log.i(TAG, "onTouchEvent : ACTION_MOVE : draggingChildStartX :" + draggingChildStartX + ",currentX:" + currentX + ",draggingFingerStartX:" + draggingFingerStartX);
                        if (!allowItemOutside) {
                            if (x < 0 && Math.abs(x) > draggingChild.view.getLeft()) {
                                x = -draggingChild.view.getLeft();
                            } else if (x > 0 && x > (getWidth() - draggingChild.view.getRight())) {
                                x = getWidth() - draggingChild.view.getRight();
                            }
                        }
                        draggingChild.view.setTranslationX(x);
                        targetRect.left = draggingChild.view.getLeft() + (int) x;
                        targetRect.right = draggingChild.view.getRight() + (int) x;
                    }
                    if (dragOrientation == ORIENTATION_ALL || dragOrientation == ORIENTATION_VERTICAL) {
                        float y = currentY - draggingFingerStartY;
                        if (!allowItemOutside) {
                            if (y < 0 && Math.abs(y) > draggingChild.view.getTop()) {
                                y = -draggingChild.view.getTop();
                            } else if (y > 0 && y > getHeight() - draggingChild.view.getBottom()) {
                                y = getHeight() - draggingChild.view.getBottom();
                            }
                        }
                        draggingChild.view.setTranslationY(y);
                        targetRect.top = draggingChild.view.getTop() + (int) y;
                        targetRect.bottom = draggingChild.view.getBottom() + (int) y;
                    }

                    Target dragging = new Target(draggingChild);

                    List<Target> underView = findAcceptChildUnder(dragging);
                    for (Target t : underView) {
                        if (!acceptView.contains(t.view)) {
                            acceptView.add(t.view);
                            draggingChild = dragging;
                            invalidate();
                            animateAccept(t);
                        }
                    }

                }

        }
        return true;
    }

    private android.view.View findTopChildUnder(float x, float y) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            android.view.View child = getChildAt(i);
            if (acceptView.contains(child)) {
                continue;
            }
            if (x >= child.getLeft() && x < child.getRight() && y >= child.getTop() && y < child.getBottom()
            ) {
                return child;
            }
        }
        return null;
    }

    class Target {
        private final android.view.View view;
        private int row;
        private int col;

        private int rowSpan;
        private int colSpan;

        private int targetRow;
        private int targetCol;

        private boolean dragging;
        private boolean dragged;

        public Target(Target target) {
            this.view = target.view;
            this.row = target.row;
            this.col = target.col;
            this.rowSpan = target.rowSpan;
            this.colSpan = target.colSpan;
            this.targetRow = target.targetRow;
            this.targetCol = target.targetCol;
            this.dragging = target.dragging;
            Log.i(TAG, "new Target");
        }

        public Target(android.view.View view, int row, int col, int rowSpan, int colSpan) {
            this(view, row, col, rowSpan, colSpan, row, col);
        }

        public Target(android.view.View view, int row, int col, int rowSpan, int colSpan, int targetRow, int targetCol) {
            this.view = view;
            this.row = row;
            this.col = col;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
            this.targetRow = targetRow;
            this.targetCol = targetCol;
            Log.i(TAG, "new Target");
        }

        public void setDragging(boolean dragging) {
            Log.i(TAG, "set dragging : " + dragging);
            this.dragging = dragging;
            if (!dragging) {
                dragged = true;
            }
        }

        @Override
        public String toString() {
            return "Target{" +
                    "view=" + view +
                    ", row=" + row +
                    ", col=" + col +
                    '}';
        }
    }

    private List<Target> findAcceptChildUnder(Target draggingTarget) {
        Log.i(TAG, "findAcceptChildUnder x:" + draggingTarget.view.getX() + ",y:" + draggingTarget.view.getY() + ",left:" + draggingTarget.view.getLeft() + ",right:" + draggingTarget.view.getRight());
        Rect childRect = null;
        List<Target> result = new ArrayList<>();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            android.view.View child = getChildAt(i);
            if (acceptView.contains(child)) {
                continue;
            }
            childRect = new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            if (child != draggingTarget.view && childRect.contains(targetRect.centerX(), targetRect.centerY())) {
                LayoutParams layoutParams = getLayoutParams(child);
                LayoutParams targetParams = getLayoutParams(draggingTarget.view);

                if (layoutParams.rowSpan == targetParams.rowSpan && layoutParams.colSpan == targetParams.colSpan) {
                    Target target = new Target(child, draggingTarget.row, draggingTarget.col, draggingTarget.rowSpan, draggingTarget.colSpan);
                    draggingTarget.row = layoutParams.row;
                    draggingTarget.col = layoutParams.col;
                    draggingTarget.targetRow = layoutParams.row;
                    draggingTarget.targetCol = layoutParams.col;
                    result.add(target);
                    return result;
                } else if (layoutParams.rowSpan == targetParams.rowSpan && layoutParams.row == draggingTarget.row) {//同一行且行高一样
                    if (layoutParams.col > draggingTarget.col && draggingTarget.col + targetParams.colSpan == layoutParams.col) { //在移动item的右边
                        Target target = new Target(child, draggingTarget.row, draggingTarget.col, draggingTarget.rowSpan, draggingTarget.colSpan);
                        draggingTarget.targetCol = target.col + layoutParams.colSpan;
                        draggingTarget.col = target.col + layoutParams.colSpan;
                        result.add(target);
                        return result;
                    } else if (layoutParams.col + layoutParams.colSpan == draggingTarget.col) { //在移动item的左边
                        Target target = new Target(
                                child,
                                layoutParams.row,
                                layoutParams.col + draggingTarget.colSpan, draggingTarget.rowSpan, draggingTarget.colSpan
                        );
                        draggingTarget.targetCol = target.col - draggingTarget.colSpan;
                        draggingTarget.col = target.col - draggingTarget.colSpan;
                        result.add(target);
                        return result;
                    }
                } else if (layoutParams.colSpan == targetParams.colSpan && layoutParams.col == draggingTarget.col) {//同一列且宽一样
                    if (layoutParams.row > draggingTarget.row && draggingTarget.row + targetParams.rowSpan == layoutParams.row) {//在移动item的下面
                        Target target = new Target(child, draggingTarget.row, draggingTarget.col, draggingTarget.rowSpan, draggingTarget.colSpan);
                        draggingTarget.targetRow = target.row + layoutParams.rowSpan;
                        draggingTarget.row = target.row + layoutParams.rowSpan;
                        result.add(target);
                        return result;
                    } else if (layoutParams.row + layoutParams.rowSpan == draggingTarget.row) {//在移动item的上面
                        Target target = new Target(
                                child,
                                layoutParams.row + draggingTarget.rowSpan,
                                layoutParams.col, draggingTarget.rowSpan, draggingTarget.colSpan
                        );
                        draggingTarget.targetRow = target.row - draggingTarget.rowSpan;
                        draggingTarget.row = target.row - draggingTarget.rowSpan;
                        result.add(target);
                        return result;
                    }
                }
            }
        }
        return result;
    }

    Target draggingChild = null;
    final Rect targetRect = new Rect();
    final Set<View> acceptView = new HashSet<>();

    float draggingChildStartX = 0f;
    float draggingChildStartY = 0f;
    float draggingFingerStartX = 0f;
    float draggingFingerStartY = 0f;

    private void prepareDrag(View view) {
        LayoutParams layoutParams = getLayoutParams(view);
        draggingChild = new Target(view, layoutParams.row, layoutParams.col, layoutParams.rowSpan, layoutParams.colSpan);
        targetRect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    private void startDragChild(Target draggingTarget) {
        invalidate();
        Log.i(TAG, "startDragChild");
        removeCallbacks(determineClickRunnable);
        getParent().requestDisallowInterceptTouchEvent(true);
        draggingTarget.setDragging(true);
        this.draggingChildStartX = draggingChild.view.getX();
        this.draggingChildStartY = draggingChild.view.getY();
        draggingFingerStartX = currentX;
        draggingFingerStartY = currentY;
        draggingChild.view.setElevation(10f);
        draggingChild.view.animate()
                .scaleX(dragScale)
                .scaleY(dragScale)
                .start();
        updateActiveStatus(draggingChild);

        if (onDragStatusChangedListener != null) {
            onDragStatusChangedListener.onDragStart(draggingChild.view);
        }
    }

    private void updateActiveStatus(Target target) {
        for (int i = 0; i < getChildCount(); i++) {
            android.view.View c = getChildAt(i);
            if (target == null) {
                c.setSelected(false);
                c.setActivated(false);
            } else if (target.view == c) {
                c.setSelected(true);
                c.setActivated(true);
            } else {
                c.setActivated(true);
                c.setSelected(false);
            }
        }
    }

    private void animateTarget(final Target draggingTarget) {
        final boolean changed = draggingTarget.row != draggingTarget.targetRow || draggingTarget.col != draggingTarget.targetCol;
        ViewPropertyAnimator viewPropertyAnimator = draggingTarget.view.animate();
        viewPropertyAnimator.scaleX(1f);
        viewPropertyAnimator.scaleY(1f);
        LayoutParams layoutParams = getLayoutParams(draggingTarget.view);
        layoutParams.row = draggingTarget.targetRow;
        layoutParams.col = draggingTarget.targetCol;
        viewPropertyAnimator.x(getCellLeft(draggingTarget.targetCol));
        viewPropertyAnimator.y(getCellTop(draggingTarget.targetRow));
        viewPropertyAnimator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            private void resetTarget() {
                draggingChild = null;
                draggingTarget.view.animate().setListener(null);
                int width = draggingTarget.view.getWidth();
                int height = draggingTarget.view.getHeight();
                draggingTarget.view.setLeft((int) draggingTarget.view.getX());
                draggingTarget.view.setRight(draggingTarget.view.getLeft() + width);
                draggingTarget.view.setTop((int) draggingTarget.view.getY());
                draggingTarget.view.setBottom(draggingTarget.view.getTop() + height);
                draggingTarget.view.setTranslationX(0f);
                draggingTarget.view.setTranslationY(0f);
                draggingTarget.view.setElevation(0f);
                invalidate();

                if (onDragStatusChangedListener != null) {
                    if (changed) {
                        onDragStatusChangedListener.onItemPositionChanged(draggingTarget.view, true);
                    }
                    onDragStatusChangedListener.onDragEnd(draggingTarget.view);
                }

                updateActiveStatus(null);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                resetTarget();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                resetTarget();
            }
        });
        if (animationDuration > -1) {
            viewPropertyAnimator.setDuration(animationDuration);
        }
        viewPropertyAnimator.start();
    }

    private void animateAccept(final Target acceptTarget) {
        acceptTarget.view.setElevation(9f);
        LayoutParams layoutParams = getLayoutParams(acceptTarget.view);
        layoutParams.row = acceptTarget.targetRow;
        layoutParams.col = acceptTarget.targetCol;
        ViewPropertyAnimator viewPropertyAnimator = acceptTarget.view.animate();
        viewPropertyAnimator.x(getCellLeft(acceptTarget.targetCol));
        viewPropertyAnimator.y(getCellTop(acceptTarget.targetRow));
        viewPropertyAnimator.setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            private void resetAcceptView() {
                acceptTarget.view.animate().setListener(null);
                acceptView.remove(acceptTarget.view);
                int width = acceptTarget.view.getWidth();
                int height = acceptTarget.view.getHeight();
                acceptTarget.view.setLeft((int) acceptTarget.view.getX());
                acceptTarget.view.setRight(acceptTarget.view.getLeft() + width);
                acceptTarget.view.setTop((int) acceptTarget.view.getY());
                acceptTarget.view.setBottom(acceptTarget.view.getTop() + height);
                acceptTarget.view.setTranslationX(0f);
                acceptTarget.view.setTranslationY(0f);
                acceptTarget.view.setElevation(0f);

                if (onDragStatusChangedListener != null) {
                    onDragStatusChangedListener.onItemPositionChanged(acceptTarget.view, false);
                }

                android.util.Log.i(
                        TAG,
                        String.format(
                                "acceptView width: %d height: %d,left: %d top: %d right : %d bottom %d",
                                width,
                                height,
                                acceptTarget.view.getLeft(),
                                acceptTarget.view.getTop(),
                                acceptTarget.view.getRight(),
                                acceptTarget.view.getBottom()
                        )
                );
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                resetAcceptView();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                resetAcceptView();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        if (animationDuration > -1) {
            viewPropertyAnimator.setDuration(animationDuration);
        }
        viewPropertyAnimator.start();
    }


}