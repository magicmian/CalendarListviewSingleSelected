package com.magicmian.com.selectsinglecalendar;

/**
 * ClassName:
 * Function:
 * Reason: ADD REASON
 * Date: 2016/12/21
 *
 * @author neusoft 王冕
 * @since JDK 1.8
 */
public interface DatePickerController {
    public abstract int getMaxYear();

    public abstract void onDayOfMonthSelected(int year, int month, int day);

}
