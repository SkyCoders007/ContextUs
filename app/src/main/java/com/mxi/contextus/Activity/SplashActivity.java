package com.mxi.contextus.Activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.mxi.contextus.Adapter.NavigationListAdapter;
import com.mxi.contextus.Model.NavDrawerItem;
import com.mxi.contextus.R;
import com.mxi.contextus.Util.CommanClass;

import java.io.File;
import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    CommanClass cc;
    ProgressBar progressBar;
    public static boolean isFirstTimeEnters = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        cc = new CommanClass(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.KITKAT) {


        if (!cc.loadPrefBoolean("isProjectloadFirstTime")) {

            File file = new File(Environment.getExternalStorageDirectory()+ "/lessunknown/database/");

            if (file.exists()) {
                deleteDirectory(file);
            }else{
                progressBar.setVisibility(View.VISIBLE);
                Thread timerThread = new Thread() {
                    public void run() {
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                };
                timerThread.start();
            }


            cc.savePrefBoolean("isProjectloadFirstTime", true);
        }else{
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        }else{
            showAlertDialog();
        }
    }
    private void showAlertDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SplashActivity.this);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setCancelable(false);

        View dialogView = inflater.inflate(R.layout.dialog_alert, null);
        builder.setView(dialogView);

        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Button btn_yes = (Button) dialogView.findViewById(R.id.btn_alert_ok);


        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        alert.show();
    }


    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    private void deleteDirectory(final File file) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SplashActivity.this);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setCancelable(false);

        View dialogView = inflater.inflate(R.layout.dialog_delete_directory, null);
        builder.setView(dialogView);

        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_delete_directory_cancel);
        Button btn_yes = (Button) dialogView.findViewById(R.id.btn_delete_directory_yes);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                Thread timerThread = new Thread() {
                    public void run() {
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                };
                timerThread.start();
                alert.dismiss();
            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                Thread timerThread = new Thread() {
                    public void run() {
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                };
                timerThread.start();
                deleteRecursive(file);
                alert.dismiss();

            }
        });
        alert.show();
    }
}
