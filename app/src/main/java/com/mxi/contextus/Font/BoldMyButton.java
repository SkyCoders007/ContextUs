package com.mxi.contextus.Font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class BoldMyButton extends Button {

    public BoldMyButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BoldMyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoldMyButton(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/CALIBRI.TTF");
        setTypeface(tf);
    }
}
