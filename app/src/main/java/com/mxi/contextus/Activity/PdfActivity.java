package com.mxi.contextus.Activity;


import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.print.PageRange;
import android.print.PrintDocumentAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mxi.contextus.Adapter.PdfAdapter;
import com.mxi.contextus.Database.SQLitehelper;
import com.mxi.contextus.Model.ComboImages;
import com.mxi.contextus.Model.ImageInfo;
import com.mxi.contextus.R;
import com.mxi.contextus.Util.CommanClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by android on 16/11/16.
 */
public class PdfActivity extends AppCompatActivity {

    int total_images;
    boolean isFromPdf=false;
    ArrayList<ImageInfo> imageInfos, temp_img_info;
    ArrayList<ComboImages> comboImagesArrayList;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    PdfAdapter pdfAdapter;
    CommanClass cc;
    SQLitehelper dbcon;
    Button btn_generate_pdf, btn_pdf_back;
    String project_id, project_name;
//    int execution_time=4500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        cc = new CommanClass(this);
        dbcon = new SQLitehelper(this);

        progressDialog = new ProgressDialog(PdfActivity.this);
        imageInfos = new ArrayList<>();
        temp_img_info = new ArrayList<>();
        comboImagesArrayList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.rv_pdf_main);


        btn_generate_pdf = (Button) findViewById(R.id.btn_generate_pdf);
        btn_pdf_back = (Button) findViewById(R.id.btn_pdf_back);

        project_name = cc.loadPrefString("lastUsedProject");
        Cursor cur = null;
        cur = dbcon.getProjectIdByName(project_name);
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            do {
                project_id = cur.getString(0);
            } while (cur.moveToNext());

        }

        Cursor pics_cur = null;
        pics_cur = dbcon.getPicsFromProjectId(project_id);

        int mCount = 0;
        if ((pics_cur.getCount() % 3) == 1) {
            mCount = mCount + 2;
        } else if ((pics_cur.getCount() % 3) == 2) {
            mCount = mCount + 1;
        } else {
            mCount = 0;
        }

        if (pics_cur != null && pics_cur.getCount() != 0) {
            int count = pics_cur.getCount() - 1;
            int i = 0;
            pics_cur.moveToFirst();
            do {
                ImageInfo imageInfo = new ImageInfo();
                imageInfo.setSequence_number(pics_cur.getString(2));
                imageInfo.setImage_path(pics_cur.getString(5));
                imageInfo.setDate_taken(pics_cur.getString(6));
                imageInfo.setPic_id(pics_cur.getString(0));
                imageInfo.setImg_byte_String_array(pics_cur.getString(7));
                imageInfo.setImage_name(pics_cur.getString(8));
                imageInfo.setOrientation(pics_cur.getString(9));
                imageInfo.setVisible(true);
                imageInfos.add(imageInfo);
            } while (pics_cur.moveToNext());
        }

        Collections.sort(imageInfos, new Comparator<ImageInfo>() {
            public int compare(ImageInfo obj1, ImageInfo obj2) {
                return (Integer.parseInt(obj1.getSequence_number()) < Integer.parseInt(obj2.getSequence_number())) ? -1
                        : (Integer.parseInt(obj1.getSequence_number()) > Integer.parseInt(obj2.getSequence_number())) ? 1 : 0;
            }
        });

        if (mCount > 0) {
            for (int j = 0; j < mCount; j++) {
                ImageInfo imageInfo_1 = new ImageInfo();
                imageInfo_1.setSequence_number(imageInfos.get(imageInfos.size() - 1).getSequence_number());
                imageInfo_1.setImage_path(imageInfos.get(imageInfos.size() - 1).getImage_path());
                imageInfo_1.setDate_taken(imageInfos.get(imageInfos.size() - 1).getDate_taken());
                imageInfo_1.setPic_id(imageInfos.get(imageInfos.size() - 1).getPic_id());
                imageInfo_1.setImg_byte_String_array(imageInfos.get(imageInfos.size() - 1).getImg_byte_String_array());
                imageInfo_1.setImage_name(imageInfos.get(imageInfos.size() - 1).getImage_name());
                imageInfo_1.setOrientation(imageInfos.get(imageInfos.size() - 1).getOrientation());
                imageInfo_1.setVisible(false);
                imageInfos.add(imageInfo_1);
            }

        }


        total_images = imageInfos.size();

        for (int i = 0; i < imageInfos.size(); i++) {
            ComboImages comboImages = new ComboImages();

            comboImages.setImg1(imageInfos.get(i));
            if (i + 1 < imageInfos.size()) {
                comboImages.setImg2(imageInfos.get(i + 1));
            } else {
                i = i + 1;
                comboImagesArrayList.add(comboImages);
                return;
            }

            if (i + 2 < imageInfos.size()) {
                comboImages.setImg3(imageInfos.get(i + 2));
            } else {
                i = i + 2;
                comboImagesArrayList.add(comboImages);
                return;
            }
            i = i + 2;
            comboImagesArrayList.add(comboImages);
        }

//        execution_time=comboImagesArrayList.size()*execution_time;

/*
        btn_generate_pdf.setVisibility(View.GONE);
        btn_pdf_back.setVisibility(View.GONE);*/

        pdfAdapter = new PdfAdapter(PdfActivity.this, comboImagesArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(PdfActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(pdfAdapter);



        btn_generate_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                generatePDF();
            }
        });
        btn_pdf_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go_back = new Intent(PdfActivity.this, MainActivity.class);
                startActivity(go_back);
                finish();
            }
        });
    }

    private void generatePDF() {


        CreatePdf createPdf = new CreatePdf();
        createPdf.execute();
//======================================================================================================

    }

/*    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        doWhateverAfterScreenViewIsRendered();
        generatePDF();
    }*/

    //======================================================================================================
    public String getCurrentDate() {

        SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMMdd");

        return sdf_date.format(Calendar.getInstance().getTime());
    }

    public String getCurrentTime() {


        SimpleDateFormat sdf_time = new SimpleDateFormat("hhmm");

        return sdf_time.format(Calendar.getInstance().getTime());
    }


    public class CreatePdf extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog.setMessage("Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String[] parts = project_name.split("_");
                String part1 = parts[0];
                String project_initials = part1;


                String pdfName = project_initials + getCurrentDate() + "_" + getCurrentTime() + ".pdf";

                String path = Environment.getExternalStorageDirectory() + "/lessunknown/" + project_name + "/";

                File outputFile = new File(path, pdfName);
                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Error In", "Create New File");
                }

                PdfDocument document = PdfAdapter.document;
                FileOutputStream out = new FileOutputStream(outputFile);
                document.writeTo(out);

                out.close();


            } catch (IOException ioe) {
            } finally {

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            cc.showToast("PDF is generated");



            progressDialog.dismiss();

            String[] parts = project_name.split("_");
            String part1 = parts[0];
            String project_initials = part1;


            String pdfName = project_initials + getCurrentDate() + "_" + getCurrentTime() + ".pdf";

            String path = Environment.getExternalStorageDirectory() + "/lessunknown/" + project_name + "/";

            File outputFile = new File(path, pdfName);

            Uri u_path = Uri.fromFile(outputFile );
            Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
            pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfOpenintent.setDataAndType(u_path , "application/pdf");
            try {
                isFromPdf=true;
                startActivity(pdfOpenintent);
            }
            catch (ActivityNotFoundException e) {

            }

        }


    }



/*
    public class SetPdf extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            pdfAdapter = new PdfAdapter(PdfActivity.this, comboImagesArrayList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(PdfActivity.this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(pdfAdapter);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                generatePDF();
                }
            }, execution_time);
        }
    }
*/


    @Override
    protected void onResume() {
        super.onResume();
        if(isFromPdf){
            PdfAdapter.document.close();
            PdfAdapter.document=new PdfDocument();
            Intent go_back = new Intent(PdfActivity.this, MainActivity.class);
            go_back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            go_back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            go_back.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(go_back);
            finish();
        }
    }
}
