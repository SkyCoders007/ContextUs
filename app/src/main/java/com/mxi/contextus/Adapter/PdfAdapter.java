package com.mxi.contextus.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.pdf.PdfDocument;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.mxi.contextus.Database.SQLitehelper;
import com.mxi.contextus.Model.ComboImages;
import com.mxi.contextus.Model.CommentItem;
import com.mxi.contextus.Model.ImageInfo;
import com.mxi.contextus.R;
import com.mxi.contextus.Util.CommanClass;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by android on 16/11/16.
 */
public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.MyViewHolder_2> {


    public static PdfDocument document = new PdfDocument();
    public boolean isMarked = false;
    SQLitehelper dbcon;
    CommanClass cc;
    ComboImages comboImage_current;
    LinearLayoutManager llm, llm2, llm3;

    int i = 0;
    String first_pic_taken = "";
    String last_pic_taken = "";
    List<ComboImages> data = Collections.emptyList();
    private Context context;
    BitmapFactory.Options options = new BitmapFactory.Options();


    public PdfAdapter(Context context, List<ComboImages> data) {

        cc = new CommanClass(context);
        dbcon = new SQLitehelper(context);
        this.context = context;
        this.data = data;
    }


    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }


    @Override
    public MyViewHolder_2 onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_item_list, parent, false);

        return new MyViewHolder_2(view);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder_2 holder, final int position) {
        comboImage_current = data.get(position);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        getFirstAndLastDate();
        String project_header = "";
        String projectName_full = cc.loadPrefString("lastUsedProject");

        String[] parts = projectName_full.split("_");
        String part1 = parts[0];
        String projectName = part1;

        project_header = dbcon.getProjectHeaderByName(projectName_full);
        Log.e("project_name", projectName_full + "");
        Log.e("project_header", project_header + "");

        if (project_header == null) {
            project_header = "";
        }

        final int number = position + 1;
        holder.header.setText(projectName + " " + first_pic_taken + " - " + last_pic_taken + " " + project_header);
        holder.pagecount.setText("PAGE " + number + "/" + data.size());

        ImageInfo img1 = comboImage_current.getImg1();

        Bitmap img_bitmap;
        //
        Matrix matrix = new Matrix();

//      @akshay Logic: if images are marked than they are fetched from the database.
        if (img1.getImg_byte_String_array().equalsIgnoreCase("null")) {

            String img = img1.getImage_path();
            img_bitmap = BitmapFactory.decodeFile(img, options);
            isMarked = false;
            if (img1.getOrientation().equals("0")) {
                matrix.setRotate(180);
            }

        } else {

            // @akshay new changes Jan-2017

/*            if (img1.getOrientation().equals("0")) {
                matrix.setRotate(90);
            }
            img_bitmap = StringToBitMap(img1.getImg_byte_String_array());*/

            img_bitmap = BitmapFactory.decodeFile(img1.getImg_byte_String_array(), options);
            isMarked = true;
            if (img1.getOrientation().equals("0")) {
                matrix.setRotate(90);
            }
        }

        holder.image_name.setText(img1.getImage_name());
        img_bitmap = getResizedImage(img_bitmap);
        holder.iv1.setImageBitmap(Bitmap.createBitmap(img_bitmap, 0, 0, img_bitmap.getWidth(), img_bitmap.getHeight(), matrix, true));
        String pic_id = img1.getPic_id();
        Cursor cur = null;
        cur = dbcon.getMarkupsFromPicId(pic_id);
        ArrayList<CommentItem> commentList = new ArrayList<>();
        i++;
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();

            do {
                CommentItem commentItem = new CommentItem();
                commentItem.setSeq_no(i + "");
                commentItem.setComment_tag(cur.getString(2));
                commentItem.setComment_text(cur.getString(5));
                Log.e("Commenttext1", commentItem.getComment_text());
                commentList.add(commentItem);

            } while (cur.moveToNext());
        }

        PDFCommentAdapter pdfCommentAdapter = new PDFCommentAdapter(context, commentList);
        pdfCommentAdapter.setHasStableIds(true);
        llm = new LinearLayoutManager(context);
        holder.rv1.setLayoutManager(llm);
        holder.rv1.setAdapter(pdfCommentAdapter);


        ImageInfo img2 = comboImage_current.getImg2();

        if (!img2.isVisible()) {
            holder.tbl_row_2.setVisibility(View.INVISIBLE);
        } else {
            holder.tbl_row_2.setVisibility(View.VISIBLE);
        }
        Bitmap img_bitmap2;

        Matrix matrix2 = new Matrix();
        if (img2.getImg_byte_String_array().equalsIgnoreCase("null")) {

            String img = img2.getImage_path();
            img_bitmap2 = BitmapFactory.decodeFile(img, options);
            isMarked = false;
            if (img2.getOrientation().equals("0")) {
                matrix2.setRotate(180);
            }

        } else {

/*
            img_bitmap = BitmapFactory.decodeFile(img1.getImg_byte_String_array());

            if (img1.getOrientation().equals("0")) {
                matrix.setRotate(180);
            }*/
            // @akshay new changes Jan-2017
            isMarked = true;
            if (img2.getOrientation().equals("0")) {
                matrix2.setRotate(90);
            }
            img_bitmap2 = BitmapFactory.decodeFile(img2.getImg_byte_String_array(), options);


        }
        holder.image_name2.setText(img2.getImage_name());

        img_bitmap2 = getResizedImage(img_bitmap2);
        holder.iv2.setImageBitmap(Bitmap.createBitmap(img_bitmap2, 0, 0, img_bitmap2.getWidth(), img_bitmap2.getHeight(), matrix2, true));

        String pic_id2 = img2.getPic_id();
        Cursor cur2 = null;
        cur2 = dbcon.getMarkupsFromPicId(pic_id2);
        ArrayList<CommentItem> commentList2 = new ArrayList<>();
        i++;
        if (cur2 != null && cur2.getCount() != 0) {
            cur2.moveToFirst();
            do {
                CommentItem commentItem = new CommentItem();
                commentItem.setSeq_no(i + "");
                commentItem.setComment_tag(cur2.getString(2));
                commentItem.setComment_text(cur2.getString(5));
                Log.e("Commenttext1", commentItem.getComment_text());
                commentList2.add(commentItem);

            } while (cur2.moveToNext());
        }

        PDFCommentAdapter pdfCommentAdapter2 = new PDFCommentAdapter(context, commentList2);
        pdfCommentAdapter2.setHasStableIds(true);
        llm2 = new LinearLayoutManager(context);
        holder.rv2.setLayoutManager(llm2);
        holder.rv2.setAdapter(pdfCommentAdapter2);


        ImageInfo img3 = comboImage_current.getImg3();
        if (!img3.isVisible()) {
            holder.tbl_row_3.setVisibility(View.INVISIBLE);
        } else {
            holder.tbl_row_3.setVisibility(View.VISIBLE);
        }
        Bitmap img_bitmap3;
        Matrix matrix3 = new Matrix();
        if (img3.getImg_byte_String_array().equalsIgnoreCase("null")) {

            String img = img3.getImage_path();
            img_bitmap3 = BitmapFactory.decodeFile(img, options);
            isMarked = false;
            if (img3.getOrientation().equals("0")) {
                matrix3.setRotate(180);
            }

        } else {
            // @akshay new changes Jan-2017
            isMarked = true;
            if (img3.getOrientation().equals("0")) {
                matrix3.setRotate(90);
            }
            img_bitmap3 = BitmapFactory.decodeFile(img3.getImg_byte_String_array(), options);
        }
        holder.image_name3.setText(img3.getImage_name());

        img_bitmap3 = getResizedImage(img_bitmap3);
        holder.iv3.setImageBitmap(Bitmap.createBitmap(img_bitmap3, 0, 0, img_bitmap3.getWidth(), img_bitmap3.getHeight(), matrix3, true));

        String pic_id3 = img3.getPic_id();
        Cursor cur3 = null;
        cur3 = dbcon.getMarkupsFromPicId(pic_id3);
        ArrayList<CommentItem> commentList3 = new ArrayList<>();
        i++;
        if (cur3 != null && cur3.getCount() != 0) {
            cur3.moveToFirst();

            do {
                CommentItem commentItem = new CommentItem();
                commentItem.setSeq_no(i + "");
                commentItem.setComment_tag(cur3.getString(2));
                commentItem.setComment_text(cur3.getString(5));
                Log.e("Commenttext1", commentItem.getComment_text());
                commentList3.add(commentItem);

            } while (cur3.moveToNext());
        }

        PDFCommentAdapter pdfCommentAdapter3 = new PDFCommentAdapter(context, commentList3);
        pdfCommentAdapter3.setHasStableIds(true);
        llm3 = new LinearLayoutManager(context);
        holder.rv3.setLayoutManager(llm3);
        holder.rv3.setAdapter(pdfCommentAdapter3);


        holder.itemView.post(new Runnable() {
            @Override
            public void run() {
                View content = holder.itemView;
                Log.e("content", content + "");
                Log.e("content_height", content.getHeight() + "");
                Log.e("content_width", content.getWidth() + "");

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(content.getWidth(),
                        content.getHeight(), number).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                content.draw(page.getCanvas());
                document.finishPage(page);

            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    class MyViewHolder_2 extends RecyclerView.ViewHolder {


        ImageView iv1, iv2, iv3;
        RecyclerView rv1, rv2, rv3;
        TextView header, text, pagecount, image_name, image_name2, image_name3;
        TableRow tbl_row_1, tbl_row_2, tbl_row_3;

        public MyViewHolder_2(View itemView) {
            super(itemView);

            iv1 = (ImageView) itemView.findViewById(R.id.iv_first_2);
            iv2 = (ImageView) itemView.findViewById(R.id.iv_second_2);
            iv3 = (ImageView) itemView.findViewById(R.id.iv_third_2);
            rv1 = (RecyclerView) itemView.findViewById(R.id.rv_sub_pdf1_2);
            rv2 = (RecyclerView) itemView.findViewById(R.id.rv_sub_pdf2_2);
            rv3 = (RecyclerView) itemView.findViewById(R.id.rv_sub_pdf3_2);
            header = (TextView) itemView.findViewById(R.id.tv_header_2);
            text = (TextView) itemView.findViewById(R.id.tv_less_unknown);
            pagecount = (TextView) itemView.findViewById(R.id.tv_Page_count);//
            image_name = (TextView) itemView.findViewById(R.id.tv_pdf_image_name1);
            image_name2 = (TextView) itemView.findViewById(R.id.tv_pdf_image_name2);
            image_name3 = (TextView) itemView.findViewById(R.id.tv_pdf_image_name3);
            tbl_row_1 = (TableRow) itemView.findViewById(R.id.tbl_row_1);
            tbl_row_2 = (TableRow) itemView.findViewById(R.id.tbl_row_2);
            tbl_row_3 = (TableRow) itemView.findViewById(R.id.tbl_row_3);

        }
    }

    public void getFirstAndLastDate() {
        first_pic_taken = data.get(0).getImg1().getDate_taken();
        last_pic_taken = data.get(data.size() - 1).getImg1().getDate_taken();
    }

/*
    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
*/

    private Bitmap getResizedImage(Bitmap mImg_bitmap) {
        Bitmap myBitmap = mImg_bitmap;
        if (!isMarked) {
            final int maxSize = 1750;//960
            int outWidth;
            int outHeight;
            int inWidth = myBitmap.getWidth();
            int inHeight = myBitmap.getHeight();
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
            myBitmap = Bitmap.createScaledBitmap(myBitmap, outWidth, outHeight, false);
//        myBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }

        return myBitmap;
    }

}


