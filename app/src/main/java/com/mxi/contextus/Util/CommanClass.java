package com.mxi.contextus.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

public class CommanClass {
    public boolean isDebug = false;
    SharedPreferences pref;
    Context context;
    public static float currentX, currentY;
    public static boolean isReadyToDelete = false;

    public CommanClass(Context mContext) {
        this.context = mContext;
        pref = context.getSharedPreferences("ContextUs",
                context.MODE_PRIVATE);
        isDebug = false;
    }


    public void showToast(String text) {
        // TODO Auto-generated method stub
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public void showSnackbar(View coordinatorLayout, String text) {

        Snackbar.make(coordinatorLayout, text, Snackbar.LENGTH_LONG).show();
    }

    public void savePrefString(String key, String value) {
        // TODO Auto-generated method stub
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void savePrefBoolean(String key, Boolean value) {
        // TODO Auto-generated method stub
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public String loadPrefString(String key) {
        // TODO Auto-generated method stub
        String strSaved = pref.getString(key, "");
        return strSaved;
    }

    public Boolean loadPrefBoolean(String key) {
        // TODO Auto-generated method stub
        boolean isbool = pref.getBoolean(key, false);
        return isbool;
    }

}