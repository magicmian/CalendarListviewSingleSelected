package com.magicmian.com.selectsinglecalendar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * ClassName:
 * Function:
 * Reason: ADD REASON
 * Date: 2016/12/21
 *
 * @author neusoft 王冕
 * @since JDK 1.8
 */
public class SimpleMonthView extends View {
    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_DAY = "selected_begin_day";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_MONTH = "selected_begin_month";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_YEAR = "selected_begin_year";
    public static final String VIEW_PARAMS_WEEK_START = "week_start";

    private static final int SELECTED_CIRCLE_ALPHA = 128;
    protected static int DEFAULT_HEIGHT = 32;
    protected static final int DEFAULT_NUM_ROWS = 6;
    protected static int DAY_SELECTED_CIRCLE_SIZE;
    protected static int DAY_SEPARATOR_WIDTH = 1;
    protected static int MINI_DAY_NUMBER_TEXT_SIZE;
    protected static int MIN_HEIGHT = 10;
    protected static int MONTH_DAY_LABEL_TEXT_SIZE;
    protected static int MONTH_HEADER_SIZE;
    protected static int MONTH_LABEL_TEXT_SIZE;

    protected int mPadding = 0;

    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;

    protected Paint mMonthDayLabelPaint;
    protected Paint mMonthNumPaint;
    protected Paint mMonthTitleBGPaint;
    protected Paint mMonthTitlePaint;
    protected Paint mMonthTitlePaintLeft;
    protected Paint mMonthTitlePaintRight;
    protected Paint mSelectedCirclePaint;
    protected Paint mMonthNumPrePaint;


    protected int mCurrentDayTextColor;
    protected int mMonthTextColor;
    protected int mDayTextColor;
    protected int mDayNumColor;
    protected int mMonthTitleBGColor;
    protected int mPreviousDayColor;
    protected int mSelectedDaysColor;

    private final StringBuilder mStringBuilder;

    protected boolean mIsPrev = false;
    protected boolean mNeedWeekTitle = false;
    protected int mSelectedBeginDay = -1;
    protected int mSelectedBeginYear = -1;
    protected int mSelectedBeginMonth = -1;
    protected int mToday = -1;
    protected int mWeekStart = 1;
    protected int mNumDays = 7;
    protected int mNumCells = mNumDays;
    private int mDayOfWeekStart = 0;
    protected int mMonth;
    protected Boolean mDrawRect;
    protected int mRowHeight = DEFAULT_HEIGHT;
    protected int mWidth;
    protected int mYear;
    final Time today;
    private int mMonthTitleLocation;
    private final Calendar mCalendar;
    private final Calendar mDayLabelCalendar;
    private final Boolean isPrevDayEnabled;

    private int mNumRows = DEFAULT_NUM_ROWS;

    private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();

    private OnDayClickListener mOnDayClickListener;


    public SimpleMonthView(Context context, TypedArray typedArray)
    {
        super(context);

        Resources resources = context.getResources();
        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();
        today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        mDayOfWeekTypeface = resources.getString(R.string.sans_serif);
        mMonthTitleTypeface = resources.getString(R.string.sans_serif);
        mCurrentDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorCurrentDay, resources.getColor(R.color.normal_day));
        mMonthTextColor = typedArray.getColor(R.styleable.DayPickerView_colorMonthName, resources.getColor(R.color.normal_day));
        mDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorDayName, resources.getColor(R.color.normal_day));
        mDayNumColor = typedArray.getColor(R.styleable.DayPickerView_colorNormalDay, resources.getColor(R.color.normal_day));
        mPreviousDayColor = typedArray.getColor(R.styleable.DayPickerView_colorPreviousDay, resources.getColor(R.color.older_day));
        mSelectedDaysColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayBackground, resources.getColor(R.color.selected_day_background));
        mMonthTitleBGColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayText, resources.getColor(R.color.selected_day_text));
        mDrawRect = typedArray.getBoolean(R.styleable.DayPickerView_drawRoundRect, false);

        mNeedWeekTitle = typedArray.getBoolean(R.styleable.DayPickerView_needWeedTitle, false);
        mMonthTitleLocation = typedArray.getInt(R.styleable.DayPickerView_MonthTitleLocation,0);

        mStringBuilder = new StringBuilder(50);

        MINI_DAY_NUMBER_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeDay, resources.getDimensionPixelSize(R.dimen.text_size_day));
        MONTH_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeMonth, resources.getDimensionPixelSize(R.dimen.text_size_month));
        MONTH_DAY_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeDayName, resources.getDimensionPixelSize(R.dimen.text_size_day_name));
        MONTH_HEADER_SIZE = typedArray.getDimensionPixelOffset(R.styleable.DayPickerView_headerMonthHeight, resources.getDimensionPixelOffset(R.dimen.header_month_height));
        DAY_SELECTED_CIRCLE_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_selectedDayRadius, resources.getDimensionPixelOffset(R.dimen.selected_day_radius));

        mRowHeight = ((typedArray.getDimensionPixelSize(R.styleable.DayPickerView_calendarHeight, resources.getDimensionPixelOffset(R.dimen.calendar_height)) - MONTH_HEADER_SIZE) / 6);

        isPrevDayEnabled = typedArray.getBoolean(R.styleable.DayPickerView_enablePreviousDay, true);

        initView();

    }

    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }
    //title of the week list
    private void drawMonthDayLabels(Canvas canvas) {
        if(mNeedWeekTitle){
            int y = MONTH_HEADER_SIZE - (MONTH_DAY_LABEL_TEXT_SIZE / 2);
            int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);

            for (int i = 0; i < mNumDays; i++) {
                int calendarDay = (i + mWeekStart) % mNumDays;
                int x = (2 * i + 1) * dayWidthHalf + mPadding;
                mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
                canvas.drawText(mDateFormatSymbols.getShortWeekdays()[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()), x, y, mMonthDayLabelPaint);
            }
        }
    }

    private void drawMonthTitle(Canvas canvas) {
        switch (mMonthTitleLocation){
            case 0:
                //center
                int x = (mWidth + 2 * mPadding) / 2;
                int y = (MONTH_HEADER_SIZE - MONTH_DAY_LABEL_TEXT_SIZE) / 2 + (MONTH_LABEL_TEXT_SIZE / 3);
                StringBuilder stringBuilder = new StringBuilder(getMonthAndYearString().toLowerCase());
                stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
                canvas.drawText(stringBuilder.toString(), x, y, mMonthTitlePaint);
                break;
            case 1:
                //left
                int x1 = (mWidth + 2 * mPadding) / 28;
                int y1 = (MONTH_HEADER_SIZE - MONTH_DAY_LABEL_TEXT_SIZE) / 2 + (MONTH_LABEL_TEXT_SIZE / 3);
                StringBuilder stringBuilder1 = new StringBuilder(getMonthAndYearString().toLowerCase());
                stringBuilder1.setCharAt(0, Character.toUpperCase(stringBuilder1.charAt(0)));
                canvas.drawText(stringBuilder1.toString(), x1, y1, mMonthTitlePaintLeft);
                break;
            case 2:
                //right
                int x2 = ((mWidth + 2 * mPadding) / 28)*27 ;
                int y2 = (MONTH_HEADER_SIZE - MONTH_DAY_LABEL_TEXT_SIZE) / 2 + (MONTH_LABEL_TEXT_SIZE / 3);
                StringBuilder stringBuilder2 = new StringBuilder(getMonthAndYearString().toLowerCase());
                stringBuilder2.setCharAt(0, Character.toUpperCase(stringBuilder2.charAt(0)));
                canvas.drawText(stringBuilder2.toString(), x2, y2, mMonthTitlePaintRight);
                break;
        }

    }

    private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }

    private String getMonthAndYearString() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        mStringBuilder.setLength(0);
        long millis = mCalendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), millis, millis, flags);
    }

    private void onDayClick(SimpleMonthAdapter.CalendarDay calendarDay) {
        if (mOnDayClickListener != null && (isPrevDayEnabled || !((calendarDay.month == today.month) && (calendarDay.year == today.year) && calendarDay.day < today.monthDay))) {
            mOnDayClickListener.onDayClick(this, calendarDay);
        }
    }

    private boolean sameDay(int monthDay, Time time) {
        return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
    }

    private boolean prevDay(int monthDay, Time time) {
        return ((mYear < time.year)) || (mYear == time.year && mMonth < time.month) || ( mMonth == time.month && monthDay < time.monthDay);
    }

    //画具体每一页的时候
    protected void drawMonthNums(Canvas canvas) {
        int y = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH + MONTH_HEADER_SIZE;
        int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
        int dayOffset = findDayOffset();
        int day = 1;
        while (day <= mNumCells) {
            int x = paddingDay * (1 + dayOffset * 2) + mPadding;
            //当前日期
            if ((mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear)) {
                if (mDrawRect) {
                    mMonthNumPaint.setColor(mSelectedDaysColor);
                    RectF rectF = new RectF(x - DAY_SELECTED_CIRCLE_SIZE, (y  - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x + DAY_SELECTED_CIRCLE_SIZE, (y  - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                    canvas.drawRoundRect(rectF, 10.0f, 10.0f,mSelectedCirclePaint);
                } else {
                    mMonthNumPaint.setColor(mSelectedDaysColor);
                    canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
                }

            }else if(prevDay(day, today)) {
                mMonthNumPaint.setColor(mPreviousDayColor);
                mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            } else {
                mMonthNumPaint.setColor(mDayNumColor);
                mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
//            if (!isPrevDayEnabled && prevDay(day, today) && today.month == mMonth && today.year == mYear)
//            {
//                mMonthNumPaint.setColor(mPreviousDayColor);
//                mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
//            }
            if(sameDay(day, today)){
                //如果是今天的话，输出今天
                canvas.drawText(getResources().getString(R.string.today), x, y, mMonthNumPaint);
            }else{
                canvas.drawText(String.format("%d", day), x, y, mMonthNumPaint);
            }

            dayOffset++;
            if (dayOffset == mNumDays) {
                dayOffset = 0;
                y += mRowHeight;
            }
            day++;
        }
    }

    public SimpleMonthAdapter.CalendarDay getDayFromLocation(float x, float y) {
        int padding = mPadding;
        if ((x < padding) || (x > mWidth - mPadding)) {
            return null;
        }

        int yDay = (int) (y - MONTH_HEADER_SIZE) / mRowHeight;
        int day = 1 + ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset()) + yDay * mNumDays;

        if (mMonth > 11 || mMonth < 0 || CalendarUtils.getDaysInMonth(mMonth, mYear) < day || day < 1)
            return null;

        return new SimpleMonthAdapter.CalendarDay(mYear, mMonth, day);
    }

    protected void initView() {
        mMonthTitlePaint = new Paint();
        mMonthTitlePaint.setFakeBoldText(true);
        mMonthTitlePaint.setAntiAlias(true);
        mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaint.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.BOLD));
        mMonthTitlePaint.setColor(mMonthTextColor);
        mMonthTitlePaint.setTextAlign(Paint.Align.CENTER);
        mMonthTitlePaint.setStyle(Paint.Style.FILL);

        mMonthTitlePaintLeft = new Paint();
        mMonthTitlePaintLeft.setFakeBoldText(true);
        mMonthTitlePaintLeft.setAntiAlias(true);
        mMonthTitlePaintLeft.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaintLeft.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.BOLD));
        mMonthTitlePaintLeft.setColor(mMonthTextColor);
        mMonthTitlePaintLeft.setTextAlign(Paint.Align.LEFT);
        mMonthTitlePaintLeft.setStyle(Paint.Style.FILL);

        mMonthTitlePaintRight = new Paint();
        mMonthTitlePaintRight.setFakeBoldText(true);
        mMonthTitlePaintRight.setAntiAlias(true);
        mMonthTitlePaintRight.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaintRight.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.BOLD));
        mMonthTitlePaintRight.setColor(mMonthTextColor);
        mMonthTitlePaintRight.setTextAlign(Paint.Align.RIGHT);
        mMonthTitlePaintRight.setStyle(Paint.Style.FILL);

        mMonthTitleBGPaint = new Paint();
        mMonthTitleBGPaint.setFakeBoldText(true);
        mMonthTitleBGPaint.setAntiAlias(true);
        mMonthTitleBGPaint.setColor(mMonthTitleBGColor);
        mMonthTitleBGPaint.setTextAlign(Paint.Align.CENTER);
        mMonthTitleBGPaint.setStyle(Paint.Style.FILL);

        mSelectedCirclePaint = new Paint();
        mSelectedCirclePaint.setFakeBoldText(true);
        mSelectedCirclePaint.setAntiAlias(true);
        mSelectedCirclePaint.setColor(mSelectedDaysColor);
        mSelectedCirclePaint.setTextAlign(Paint.Align.CENTER);
        mSelectedCirclePaint.setStyle(Paint.Style.FILL);
        mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        mMonthDayLabelPaint = new Paint();
        mMonthDayLabelPaint.setAntiAlias(true);
        mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
        mMonthDayLabelPaint.setColor(mDayTextColor);
        mMonthDayLabelPaint.setTypeface(Typeface.create(mDayOfWeekTypeface, Typeface.NORMAL));
        mMonthDayLabelPaint.setStyle(Paint.Style.FILL);
        mMonthDayLabelPaint.setTextAlign(Paint.Align.CENTER);
        mMonthDayLabelPaint.setFakeBoldText(true);

        mMonthNumPaint = new Paint();
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthNumPaint.setStyle(Paint.Style.FILL);
        mMonthNumPaint.setTextAlign(Paint.Align.CENTER);
        mMonthNumPaint.setFakeBoldText(false);

        mMonthNumPrePaint = new Paint();
        mMonthNumPrePaint.setAntiAlias(true);
        mMonthNumPrePaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthNumPrePaint.setStyle(Paint.Style.FILL);
        mMonthNumPrePaint.setTextAlign(Paint.Align.CENTER);
        mMonthNumPrePaint.setFakeBoldText(false);
        mMonthNumPrePaint.setColor(mPreviousDayColor);
    }

    protected void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);
        drawMonthDayLabels(canvas);
        drawMonthNums(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows + MONTH_HEADER_SIZE);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            SimpleMonthAdapter.CalendarDay calendarDay = getDayFromLocation(event.getX(), event.getY());
            if (calendarDay != null) {
                onDayClick(calendarDay);
            }
        }
        return true;
    }

    public void reuse() {
        mNumRows = DEFAULT_NUM_ROWS;
        requestLayout();
    }

    public void setMonthParams(HashMap<String, Integer> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(params);

        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mRowHeight = params.get(VIEW_PARAMS_HEIGHT);
            if (mRowHeight < MIN_HEIGHT) {
                mRowHeight = MIN_HEIGHT;
            }
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_DAY)) {
            mSelectedBeginDay = params.get(VIEW_PARAMS_SELECTED_BEGIN_DAY);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_MONTH)) {
            mSelectedBeginMonth = params.get(VIEW_PARAMS_SELECTED_BEGIN_MONTH);
        }

        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_YEAR)) {
            mSelectedBeginYear = params.get(VIEW_PARAMS_SELECTED_BEGIN_YEAR);
        }

        mMonth = params.get(VIEW_PARAMS_MONTH);
        mYear = params.get(VIEW_PARAMS_YEAR);

        mToday = -1;

        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        mNumCells = CalendarUtils.getDaysInMonth(mMonth, mYear);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (prevDay(day, today)) {
            }
        }

        mNumRows = calculateNumRows();
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    public static abstract interface OnDayClickListener {
        public abstract void onDayClick(SimpleMonthView simpleMonthView, SimpleMonthAdapter.CalendarDay calendarDay);
    }
}
