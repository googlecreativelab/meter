package com.androidexperiments.meter.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.androidexperiments.meter.R;

/**
 * Render the ToggleButton as my drawable checkbox graphics
 */
public class CustomToggleButton extends ToggleButton
{

    public CustomToggleButton(Context context) {
        super(context);
    }


    public CustomToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public CustomToggleButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onFinishInflate(){
        super.onFinishInflate();
        setText("");
        updateBackground();
    }

    private void updateBackground(){
        int drawable = isChecked() ? R.drawable.menu_checkbox_selected : R.drawable.menu_checkbox_unselected;
        setBackground(getContext().getResources().getDrawable(drawable, null));
    }


    @Override
    public void setChecked(boolean checked)
    {
        super.setChecked(checked);
        setText("");
        updateBackground();
    }

}
