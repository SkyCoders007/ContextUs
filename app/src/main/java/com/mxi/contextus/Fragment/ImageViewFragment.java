package com.mxi.contextus.Fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mxi.contextus.Activity.CameraActivity;
import com.mxi.contextus.Activity.MainActivity;
import com.mxi.contextus.Activity.SplashActivity;
import com.mxi.contextus.Custom.TouchImageView;
import com.mxi.contextus.Database.SQLitehelper;
import com.mxi.contextus.Model.ProjectImagesInfo;
import com.mxi.contextus.R;
import com.mxi.contextus.Util.CommanClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ImageViewFragment extends Fragment {

    public static boolean lastImageIsSet = false;
    public static boolean val;
    public static int deleted_sequence_number = 0;
    public static ViewPagerAdapter adapter;
    public boolean isMarked=false;

    ArrayList<ProjectImagesInfo> img_array;

    ViewPager vpager;
    TextView tv_no_img;
    LinearLayout ll_resequence;
    FloatingActionButton cameraButton;
    Button btn_ok,btn_cancel;

    Bitmap img_bitmap;
    Animation animation;

    boolean isPicDelete = false;
    public static boolean isDeleteDialog = false;
    boolean noProjectAvailable = false;

    String[] pickerValues;

    String project_id;
    String currentProject;
    int j = 1, totalPages = 0, currentPage = 0, currentItem = 0;

    SQLitehelper dbcon;
    CommanClass cc;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_image_view, container, false);
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide);

        dbcon = new SQLitehelper(getActivity());
        cc = new CommanClass(getActivity());

        cameraButton = (FloatingActionButton) view.findViewById(R.id.fab);

        currentProject = cc.loadPrefString("currentProject");
        project_id = getCurrentProject(currentProject);
        img_array = new ArrayList<ProjectImagesInfo>();
        img_array = getProjectImages(project_id);

        tv_no_img = (TextView) view.findViewById(R.id.tv_no_images);
        tv_no_img.setVisibility(View.GONE);

        ll_resequence = (LinearLayout) view.findViewById(R.id.ll_resequence);
        btn_ok = (Button) view.findViewById(R.id.button_resequence_ok);
        btn_cancel = (Button) view.findViewById(R.id.btn_cancel);

        vpager = (ViewPager) view.findViewById(R.id.view_pager);
        vpager.setOffscreenPageLimit(0);
        adapter = new ViewPagerAdapter(img_array, getActivity());
        vpager.setAdapter(adapter);


        if (!noProjectAvailable) {
            if (img_array.size() == 0) {
                tv_no_img.setVisibility(View.VISIBLE);
            } else {
                int saved_image_state = 0;
                if (SplashActivity.isFirstTimeEnters) {
                    SplashActivity.isFirstTimeEnters = false;
                    if (cc.loadPrefString("lastUsedProject_image_position").equals("")) {
                        saved_image_state = 0;
                    } else {
                        saved_image_state = Integer.parseInt(cc.loadPrefString("lastUsedProject_image_position"));
                    }
                } else if (MainActivity.fromNavButtonPress) {
                    saved_image_state = (img_array.size()) - 1;
                    MainActivity.fromNavButtonPress = false;
                } else {
                    if (cc.loadPrefString("lastUsedProject_image_position").equals("")) {
                        saved_image_state = 0;
                    } else {
                        saved_image_state = Integer.parseInt(cc.loadPrefString("lastUsedProject_image_position"));
                    }
                }

                vpager.setCurrentItem(saved_image_state);
                tv_no_img.setVisibility(View.GONE);
            }
        } else {
            tv_no_img.setVisibility(View.GONE);
        }

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MainActivity.navDrawerItems.isEmpty()) {
                    Intent intent = new Intent(getActivity(), CameraActivity.class);
                    String toolbar_title = cc.loadPrefString("lastUsedProject");
                    intent.putExtra("ProjectName", toolbar_title);

                    startActivity(intent);
                    getActivity().finish();

                } else {
                    cc.showSnackbar(getView(), "No project yet");
                }
            }
        });

        vpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (totalPages == 0) {
                    currentPage = 0;
                } else {
                    currentPage = position + 1;
                }

                cc.savePrefString("lastUsedProject", currentProject);
                cc.savePrefString("lastUsedProject_image_position", position + "");
                if (isPicDelete) {
                    img_array = getProjectImages(project_id);
                    isPicDelete = false;
                }
                MainActivity.tv_toolbar_project_name.setText(currentProject);
                MainActivity.tv_toolbar_page_index.setText(" (" + currentPage + "/" + img_array.size() + ")");
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    public String getCurrentProject(String currentProject) {
        String currentProjectID = "";
        Cursor cur;
        cur = dbcon.getProjectIdByName(currentProject);
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            currentProjectID = cur.getString(0);
            cc.savePrefString("CurrentProjectId", project_id);
        } else {
            noProjectAvailable = true;
        }

        return currentProjectID;
    }

    public ArrayList<ProjectImagesInfo> getProjectImages(String projectID) {

        ArrayList<ProjectImagesInfo> imagesPath = new ArrayList<>();
        Cursor cur1 = null;

        cur1 = dbcon.getPicsFromProjectId(projectID);

        if (cur1 != null && cur1.getCount() != 0) {

            cur1.moveToFirst();
            Log.e("Cursor", cur1.getCount() + "");
            cc.savePrefString("project_id_for_pic", projectID);
            do {
                ProjectImagesInfo pImagesInfo = new ProjectImagesInfo();
                Log.e("cursor pos 1", cur1.getString(0) + "");
                pImagesInfo.setPictureID(cur1.getString(0));

                pImagesInfo.setProjectID(cur1.getString(1));
                pImagesInfo.setSequenceNumber(Integer.parseInt(cur1.getString(2)));

                pImagesInfo.setPicSizeX(cur1.getString(3));
                pImagesInfo.setPicSizeY(cur1.getString(4));
                String image_path = cur1.getString(5);
                pImagesInfo.setPicturePath(cur1.getString(5));
                pImagesInfo.setPicInString(cur1.getString(7));
                pImagesInfo.setPictureName(cur1.getString(8));
                pImagesInfo.setOrientation(cur1.getString(9));

                imagesPath.add(pImagesInfo);

                totalPages++;
            } while (cur1.moveToNext());
        } else {
            cc.savePrefBoolean("noProjectCamera", true);
        }

        Collections.sort(imagesPath, new Comparator<ProjectImagesInfo>() {
            public int compare(ProjectImagesInfo obj1, ProjectImagesInfo obj2) {
                return (obj1.sequenceNumber < obj2.sequenceNumber) ? -1
                        : (obj1.sequenceNumber > obj2.sequenceNumber) ? 1 : 0;
            }
        });

        return imagesPath;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_resequence: {

                if (!noProjectAvailable) {
                    if (img_array.size() != 0) {
                        ll_resequence.setVisibility(View.VISIBLE);
                        btn_cancel.setVisibility(View.VISIBLE);
                        ll_resequence.requestFocus();
                        btn_ok.setClickable(true);
                        showResequencePopUp(getCurrentimage());
                    } else {
                        cc.showSnackbar(vpager, "No pictures yet");
                    }

                }
                return true;
            }
            case R.id.delete_image: {

                if (!noProjectAvailable) {

                    if (img_array.size() != 0) {

                        String image_id = img_array.get(getCurrentimage()-1).getPictureID();
                        int pic_sequence_number = img_array.get(getCurrentimage()-1).getSequenceNumber();
                        String img = img_array.get(getCurrentimage()-1).getPicturePath();
                        String tempImg="";
                        Cursor c = dbcon.getPicFromPicId(image_id);
                        if (c != null && c.getCount() != 0) {

                            c.moveToFirst();
                            do {
                                tempImg=c.getString(7);
                            } while (c.moveToNext());

                        }

                        if (TouchImageView.isDeleteDialogAvailable) {
                            if (!isDeleteDialog){}
                        }
                        showDeleteDialog(image_id, pic_sequence_number, img, getCurrentimage(),tempImg);

                    } else {
                        cc.showSnackbar(vpager, "No pictures yet");
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private int getCurrentimage() {

        String number = MainActivity.tv_toolbar_page_index.getText().toString();

        String parse[] = number.split("/");
        String parse1 = parse[0];

        parse1 = parse1.replace("(", "").trim();

        int currentImageNumber = Integer.parseInt(parse1);
        Log.e("currentImageNumber", currentImageNumber + "");
        return currentImageNumber;
    }

    private void showResequencePopUp(final int resequence_number) {

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_cancel.setVisibility(View.GONE);
                ll_resequence.setVisibility(View.GONE);
            }
        });


        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = getCurrentimage();
                Log.e("currentItem", resequence_number + "");
                Log.e("value", value + "");
                cc.showToast("" + value);
                val = true;
                btn_cancel.setVisibility(View.GONE);
                ll_resequence.setVisibility(View.GONE);

                currentItem = resequence_number - 1;

                for (int i = 0; i < img_array.size(); i++) {

                    if (currentItem == i) {
                        dbcon.updatePicSequenceNumber(img_array.get(i).getPictureID(), String.valueOf(value));
                    } else {
                        if (j != value) {
                            dbcon.updatePicSequenceNumber(img_array.get(i).getPictureID(), String.valueOf(j));
                            j++;
                        } else {
                            j++;
                            dbcon.updatePicSequenceNumber(img_array.get(i).getPictureID(), String.valueOf(j));
                            j++;
                        }
                    }
                }

                img_array = getProjectImages(project_id);

                adapter = null;
                adapter = new ViewPagerAdapter(img_array, getActivity());
                vpager.setAdapter(adapter);
                j = 1;
                vpager.setCurrentItem(value - 1);

            }
        });

    }

//---------------------------Adapter---------------------------------------------------------------------------------------------------------------------------------------

    public class ViewPagerAdapter extends PagerAdapter {
        public String path_image_to_load="";
        CommanClass cc;
        ArrayList<ProjectImagesInfo> img_array;

        Context context;
        SQLitehelper dbcon;
        RelativeLayout relativeLayout;

        public ViewPagerAdapter(ArrayList<ProjectImagesInfo> uri, Context mContext) {
            this.context = mContext;
            this.img_array = uri;
        }

        @Override
        public int getCount() {
            return img_array.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(final ViewGroup parent, final int position) {
            cc = new CommanClass(context);
            dbcon = new SQLitehelper(context);

            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager_item, parent, false);

            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.rl_view_pager);
            final TouchImageView img_view = (TouchImageView) itemView.findViewById(R.id.iv_viewpager_item);

            img_view.setPicId(img_array.get(position).getPictureID());

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            final String img = img_array.get(position).getPicturePath();
            Matrix matrix = new Matrix();


//      @akshay Logic: if images are marked than they are fetched from the database.
            if (img_array.get(position).getPicInString().equalsIgnoreCase("null")) {

                path_image_to_load=img;
                img_bitmap = BitmapFactory.decodeFile(img, options);
                isMarked=false;
                if (img_array.get(position).getOrientation().equals("0")) {
                    Log.e("Rotate", "90");
                    matrix.setRotate(90);
                }
                Log.e("Rotate", "0");

            } else {

                // @akshay new changes Jan-2017

//                img_bitmap = StringToBitMap(img_array.get(position).getPicInString());
                path_image_to_load=img_array.get(position).getPicInString();
                img_bitmap = BitmapFactory.decodeFile(img_array.get(position).getPicInString(), options);
                isMarked=true;


                /*if (img_array.get(position).getOrientation().equals("0")) {
                    Log.e("Rotate", "90");
                    matrix.setRotate(90);
                }*/

            }

            img_bitmap = getResizedImage(img_bitmap);

            img_view.setImageBitmap(Bitmap.createBitmap(img_bitmap, 0, 0, img_bitmap.getWidth(), img_bitmap.getHeight(), matrix, true));

//            Picasso.with(context).load(path_image_to_load).into(img_view);



            if (position == (img_array.size() - 1)) {
                lastImageIsSet = true;
            }
            img_view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    return true;

                }
            });

            parent.addView(itemView);
            itemView.invalidate();
            return itemView;
        }

        public void destroyItem(ViewGroup parent, int position, Object object) {
            parent.removeView((RelativeLayout) object);

        }

    }

/*    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }*/

    private Bitmap getResizedImage(Bitmap mImg_bitmap) {
        Bitmap myBitmap = mImg_bitmap;
        if(!isMarked){
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
            myBitmap = Bitmap.createScaledBitmap(myBitmap, outWidth, outHeight, false);
        }

        return myBitmap;
    }

    public void showDeleteDialog(final String image_id, final int sequence_number, final String img_path, final int position, final String tempImgPath) {

        isDeleteDialog = true;

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View dialogView = inflater.inflate(R.layout.delete_image, null);

        builder.setView(dialogView);
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);

        Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        Button btn_yes = (Button) dialogView.findViewById(R.id.btn_yes);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommanClass.isReadyToDelete = false;
                isDeleteDialog = false;
                alert.dismiss();


            }
        });
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleted_sequence_number = sequence_number;
                isDeleteDialog = false;
                isPicDelete = true;
                dbcon.deletePic(image_id);
                File file = new File(img_path);
                File tempFile = new File(tempImgPath);

                deleteExternalImage(file);

                if(!tempImgPath.equals("") || !tempImgPath.equals(null) ){
                    deleteExternalImage(tempFile);
                }

                Snackbar snackbar = Snackbar
                        .make(vpager, "Picture deleted successfully", Snackbar.LENGTH_LONG);

                Cursor cur = null;
                cur = dbcon.getPicsFromProjectId(project_id);
                if (cur != null && cur.getCount() != 0) {
                    cur.moveToFirst();
                    do {
                        String pic_id = cur.getString(0);
                        int seq_no = Integer.parseInt(cur.getString(2));
                        if (seq_no > deleted_sequence_number) {
                            dbcon.updatePicSequenceNumber(pic_id, String.valueOf(seq_no - 1));
                        }
                    } while (cur.moveToNext());
                }

                snackbar.show();

                img_array.clear();
                img_array = getProjectImages(project_id);

                adapter = new ViewPagerAdapter(img_array, getActivity());
                vpager.setAdapter(adapter);
                adapter.notifyDataSetChanged();


                if (img_array.isEmpty()) {
                    tv_no_img.setVisibility(View.VISIBLE);
                    MainActivity.tv_toolbar_project_name.setText(currentProject);
                    MainActivity.tv_toolbar_page_index.setText(" (" + 0 + "/" + img_array.size() + ")");
                } else {
                    int mPosition = position;
                    if (mPosition != 0) {
                        mPosition = position - 1;
                    }
                    vpager.setCurrentItem(mPosition);
                }

                CommanClass.isReadyToDelete = false;
                alert.dismiss();

            }
        });
        alert.show();

    }

    public void deleteExternalImage(File file){
        file.delete();

        if (file.exists()) {
            try {
                file.getCanonicalFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (file.exists()) {
                getContext().deleteFile(file.getName());
            }
        }

    }

}
