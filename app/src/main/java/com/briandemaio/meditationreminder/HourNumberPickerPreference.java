package com.briandemaio.meditationreminder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class HourNumberPickerPreference extends NumberPickerPreference {

    public static final int MAX_VALUE = 59;
    public static final int MIN_VALUE = 1;

    public HourNumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        picker.setMinValue(MIN_VALUE);
        picker.setMaxValue(MAX_VALUE);
        picker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
        picker.setValue(getValue());
    }
}
