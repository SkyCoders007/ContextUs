package com.mxi.contextus.Activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mxi.contextus.Adapter.NavigationListAdapter;
import com.mxi.contextus.Database.SQLitehelper;

import com.mxi.contextus.Fragment.ImageViewFragment;
import com.mxi.contextus.Util.CommanClass;
import com.mxi.contextus.Model.NavDrawerItem;
import com.mxi.contextus.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static TextView tv_toolbar_project_name, tv_toolbar_page_index;
    public static boolean fromNavButtonPress = false;
    public static ArrayList<NavDrawerItem> navDrawerItems;


    public Toolbar toolbar;
    DrawerLayout drawer;

    Fragment fragment;
    FragmentManager fragmentManager;
    LinearLayout ll_lastUsed_main, ll_first_tab;

    RecyclerView recyclerView;
    Button btn_start_project;
    TextView tv_text, tv_lastused;
    ImageView iv_no_project;


    CommanClass cc;
    SQLitehelper dbcon;

    NavDrawerItem nav;
    NavigationListAdapter navigationListAdapter;

    String project_title, project_id, delete_this_project;
    String mProject_name;

    int i = 1;
    int curImgNumber = 0, totalImgNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cc = new CommanClass(this);
        dbcon = new SQLitehelper(MainActivity.this);

        fragmentManager = getSupportFragmentManager();
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        ll_lastUsed_main = (LinearLayout) findViewById(R.id.ll_last_opend_projects);
        ll_first_tab = (LinearLayout) findViewById(R.id.ll_first_LastUsed);


        btn_start_project = (Button) findViewById(R.id.button_start_new_project);
        iv_no_project = (ImageView) findViewById(R.id.iv_no_project);

        tv_lastused = (TextView) findViewById(R.id.tv_last_used);
        tv_text = (TextView) findViewById(R.id.text);

        tv_toolbar_project_name = (TextView) toolbar.findViewById(R.id.tv_toolbar_project_name);
        tv_toolbar_page_index = (TextView) toolbar.findViewById(R.id.tv_toolbar_page_index);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = (RecyclerView) findViewById(R.id.nav_items_list);
        recyclerView.setLayoutManager(llm);


        navDrawerItems = new ArrayList<NavDrawerItem>();
        navDrawerItems.clear();

        Cursor c = dbcon.getProjects();
        if (c != null && c.getCount() != 0) {

            c.moveToFirst();
            do {
                nav = new NavDrawerItem();
                nav.setTitle(c.getString(1));
                navDrawerItems.add(nav);
                cc.savePrefString("lastProject", nav.getTitle());
            } while (c.moveToNext());

        } else {
            cc.savePrefBoolean("noProject", true);
            cc.savePrefBoolean("noProjectMenu", true);
            invalidateOptionsMenu();
            cc.savePrefBoolean("noProjectCamera", true);
            cc.savePrefString("currentProjectImageCount", 0 + "");
            tv_text.setVisibility(View.VISIBLE);
            btn_start_project.setVisibility(View.VISIBLE);
            iv_no_project.setVisibility(View.VISIBLE);
        }

        project_title = cc.loadPrefString("lastUsedProject");

        if (project_title.length() == 0 && cc.loadPrefString("lastProject").length() != 0) {

            project_title = cc.loadPrefString("lastProject");
        }


        Cursor cur = null;
        cur = dbcon.getProjectDetailsByName(project_title);
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            String id = cur.getString(1);
        } else {
            project_title = cc.loadPrefString("lastProject");
        }
        cc.savePrefString("currentProject", project_title);
        setToolbarTitle(project_title);

        navigationListAdapter = new NavigationListAdapter(MainActivity.this, navDrawerItems);
        recyclerView.setAdapter(navigationListAdapter);

        callFrgmentViewImage();

        getLastUsedProjects();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new MainActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                nav = navDrawerItems.get(position);
                project_title = nav.getTitle();
                fromNavButtonPress = true;

                ndProjectClickEvent(project_title);
                getLastUsedProjects();

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        btn_start_project.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialog();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerLayout = navigationView.getHeaderView(0);
        View panel = headerLayout.findViewById(R.id.nav_header);
        panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.lessunknown.com/"); //
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_resequence:
                if (navDrawerItems.isEmpty()) {
                    cc.showSnackbar(recyclerView, "No project yet for re-sequence");
                }
                break;
            case R.id.action_assemble_pdf:
                if (navDrawerItems.isEmpty()) {
                    cc.showSnackbar(recyclerView, "No project yet for create PDF");
                } else {
                    if (totalImgNumber != 0) {
                        Intent intent = new Intent(MainActivity.this, PdfActivity.class);
                        startActivity(intent);
                    } else {
                        cc.showSnackbar(recyclerView, "No pictures yet");
                    }
                }
                break;
            case R.id.action_add_header_data:
                if (navDrawerItems.isEmpty()) {
                    cc.showSnackbar(recyclerView, "No project yet for add header data");
                } else {
                    updateHeader();
                }

                break;
            case R.id.action_delete_project:
                if (navDrawerItems.isEmpty()) {
                    cc.showSnackbar(recyclerView, "No project yet for delete");
                } else {
                    deleteCurrentProject();
                }

                break;
            case R.id.delete_image:
                if (navDrawerItems.isEmpty()) {
                    cc.showSnackbar(recyclerView, "No project available yet ");
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void updateHeader() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setCancelable(false);

        final View dialogView = inflater.inflate(R.layout.dialog_add_project, null);
        builder.setView(dialogView);

        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);

        final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);
        TextView btn_cancell = (Button) dialogView.findViewById(R.id.btn_cancel);
        TextView btn_create = (Button) dialogView.findViewById(R.id.btn_create);
        TextView dialog_header = (TextView) dialogView.findViewById(R.id.tv_dialog_header);

        et_name.requestFocus();
        et_name.setFocusable(true);

        dialog_header.setText(" ADD HEADER");

        btn_create.setText("SAVE");

        String _project_name = cc.loadPrefString("lastUsedProject");
        String _project_id = null;
        Cursor cur = dbcon.getProjectIdByName(_project_name);
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            do {
                _project_id = cur.getString(0);
            } while (cur.moveToNext());

        }
        String project_header = dbcon.getProjectHeaderByName(_project_name);

        if (!project_header.equals("null") && !project_header.isEmpty() && project_header != null) {
            et_name.setText(project_header);
        } else {
            et_name.setHint("Add Header");
        }


        btn_cancell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);

                alert.dismiss();
            }
        });

        final String final_project_id = _project_id;
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                String project_header;
                project_header = et_name.getText().toString().trim();
                if (project_header.length() == 0) {

                    cc.showToast("Project header can not be empty");

                } else {
                    project_header = project_header.replace("'", "`");
                    dbcon.updateProjectHeader(final_project_id, project_header);
                }
                alert.dismiss();
            }
        });

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        alert.show();
    }

    private void deleteCurrentProject() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setCancelable(false);

        View dialogView = inflater.inflate(R.layout.dialog_delete_project, null);
        builder.setView(dialogView);

        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_delete_project_cancel);
        Button btn_yes = (Button) dialogView.findViewById(R.id.btn_delete_project_yes);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommanClass.isReadyToDelete = false;
                alert.dismiss();


            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (project_id != null) {
                    try {
                        String delete_project_Name = dbcon.getProjectNameById(project_id);
                        dbcon.deleteProject(project_id);

                        navDrawerItems = new ArrayList<NavDrawerItem>();
                        Cursor c = dbcon.getProjects();
                        if (c != null && c.getCount() != 0) {

                            c.moveToFirst();
                            do {
                                nav = new NavDrawerItem();
                                nav.setTitle(c.getString(1));
                                navDrawerItems.add(nav);
                            } while (c.moveToNext());

                        } else {
                            cc.savePrefBoolean("noProject", true);
                            cc.savePrefBoolean("noProjectMenu", true);
                            invalidateOptionsMenu();
                            cc.savePrefBoolean("noProjectCamera", true);
                            cc.savePrefString("currentProjectImageCount", 0 + "");
                            tv_text.setVisibility(View.VISIBLE);
                            btn_start_project.setVisibility(View.VISIBLE);
                            iv_no_project.setVisibility(View.VISIBLE);
                        }

                        File file = new File(Environment.getExternalStorageDirectory() + "/lessunknown/" + delete_project_Name + "/");

                        deleteRecursive(file);

                        File tempFile = new File(Environment.getExternalStorageDirectory() + "/localTemp/lessunknown/" + delete_project_Name + "/");

                        deleteRecursive(tempFile);

                        cc.savePrefString("currentProject", nav.getTitle());
                        setToolbarTitle(nav.getTitle());

                        navigationListAdapter = new NavigationListAdapter(MainActivity.this, navDrawerItems);
                        recyclerView.setAdapter(navigationListAdapter);

                        getLastUsedProjects();
                        callFrgmentViewImage();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                alert.dismiss();

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.add_new_project:
                showDialog();
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                break;

            default:

                break;
        }

        return true;
    }

    private void getLastUsedProjects() {

        ArrayList<String> lastusedProjectList = new ArrayList<>();
        Cursor cursor = null;

        cursor = dbcon.getProjectsOnLastUsed();
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                String ProjectList = cursor.getString(1);
                lastusedProjectList.add(ProjectList);

            } while (cursor.moveToNext());


        }
        int projectCount = lastusedProjectList.size();

        if (projectCount >= 2) {

            ll_lastUsed_main.setVisibility(View.VISIBLE);
            tv_lastused.setText(lastusedProjectList.get(1));
        } else if (projectCount == 1) {

            tv_lastused.setVisibility(View.VISIBLE);
            tv_lastused.setText(lastusedProjectList.get(0));
        } else if (projectCount == 0) {

            ll_lastUsed_main.setVisibility(View.GONE);
        }

        tv_lastused.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromNavButtonPress = true;
                ndProjectClickEvent(tv_lastused.getText().toString());
            }
        });


    }


    public String getCurrentDateandtime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yymmddhhmmss");
        String last_update = sdf.format(Calendar.getInstance().getTime());
        return last_update;
    }

    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyyyy");
        String last_update = sdf.format(Calendar.getInstance().getTime());
        last_update = last_update.toUpperCase();

        return last_update;
    }


    private void showDialog() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//getLayoutInflater();
        builder.setCancelable(false);

        final View dialogView = inflater.inflate(R.layout.dialog_add_project, null);
        builder.setView(dialogView);
        final android.support.v7.app.AlertDialog alert = builder.create();

        alert.setCanceledOnTouchOutside(true);

        final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);
        TextView btn_cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        TextView btn_create = (Button) dialogView.findViewById(R.id.btn_create);

        dialogView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                alert.dismiss();
            }
        });
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                i = 1;
                project_title = et_name.getText().toString().trim();
                if (project_title.length() == 0) {

                    cc.showToast("Project title can not be empty");

                } else {
                    project_title = project_title.replace("'", "`");
                    btn_start_project.setVisibility(View.GONE);
                    tv_text.setVisibility(View.GONE);
                    iv_no_project.setVisibility(View.GONE);

                    String last_update = getCurrentDateandtime();
                    String appandDate = getCurrentDate();
                    String project_initial_name = project_title;
                    project_title = project_title + "_" + appandDate;

                    final File direct = new File(Environment.getExternalStorageDirectory()
                            + "/lessunknown/" + project_title + "/");
                    if (!direct.exists()) {
                        direct.mkdirs();
                    }

                    Cursor cur = null;

                    cur = dbcon.getProjectDetailsByName(project_title);

                    if (cur != null && cur.getCount() != 0) {
                        cur.moveToFirst();
                        String id = cur.getString(1);
                        showConfirmationPopUp(project_initial_name, last_update, project_title);
                        alert.dismiss();
                    } else {
                        dbcon.insertProject(project_title, last_update, null);

                        alert.dismiss();

                        navDrawerItems.clear();
                        Cursor c = dbcon.getProjects();
                        if (c != null && c.getCount() != 0) {

                            c.moveToFirst();
                            do {
                                nav = new NavDrawerItem();
                                nav.setTitle(c.getString(1));

                                navDrawerItems.add(nav);
                            } while (c.moveToNext());

                        } else {
                            tv_text.setVisibility(View.VISIBLE);
                            btn_start_project.setVisibility(View.VISIBLE);
                            iv_no_project.setVisibility(View.VISIBLE);
                        }

                        navigationListAdapter = new NavigationListAdapter(MainActivity.this, navDrawerItems);
                        recyclerView.setAdapter(navigationListAdapter);
                        getLastUsedProjects();

                        cc.savePrefBoolean("noProjectMenu", false);
                        cc.savePrefString("currentProject", project_title);

                        tv_toolbar_page_index.setVisibility(View.VISIBLE);
                        invalidateOptionsMenu();
                        setToolbarTitle(project_title);
                        callFrgmentViewImage();
                    }
                }
            }
        });

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        alert.show();
    }

    private void showConfirmationPopUp(final String project_initial_name, final String last_update, final String project_name) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setCancelable(false);

        mProject_name = project_name;
        View dialogView = inflater.inflate(R.layout.dialog_confirm_project_name, null);

        builder.setView(dialogView);
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_delete_project_cancel);
        Button btn_yes = (Button) dialogView.findViewById(R.id.btn_delete_project_yes);


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alert.dismiss();


            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 1;
                checkProjectExistance(project_initial_name);

                alert.dismiss();

                navDrawerItems.clear();
                Cursor c = dbcon.getProjects();
                if (c != null && c.getCount() != 0) {

                    c.moveToFirst();
                    do {
                        nav = new NavDrawerItem();
                        nav.setTitle(c.getString(1));

                        navDrawerItems.add(nav);
                    } while (c.moveToNext());

                } else {
                    tv_text.setVisibility(View.VISIBLE);
                    btn_start_project.setVisibility(View.VISIBLE);
                    iv_no_project.setVisibility(View.VISIBLE);
                }

                navigationListAdapter = new NavigationListAdapter(MainActivity.this, navDrawerItems);
                recyclerView.setAdapter(navigationListAdapter);
                getLastUsedProjects();

                cc.savePrefBoolean("noProjectMenu", false);
                cc.savePrefString("currentProject", project_title);
                tv_toolbar_page_index.setVisibility(View.VISIBLE);

                invalidateOptionsMenu();
                setToolbarTitle(project_title);
                callFrgmentViewImage();

                alert.dismiss();

            }

            private void checkProjectExistance(String nProject_name) {
                String init_name = nProject_name;
                nProject_name = init_name + "_" + i + "_" + getCurrentDate();

                Cursor cur = null;
                cur = dbcon.getProjectDetailsByName(nProject_name);
                if (cur != null && cur.getCount() != 0) {
                    cur.moveToFirst();
                    i++;
                    checkProjectExistance(init_name);
                } else {
                    project_title = nProject_name;
                    dbcon.insertProject(nProject_name, last_update, null);
                }

            }
        });
        alert.show();

    }

    public void setToolbarTitle(String project_title) {
        String Project = "";

        if (cc.loadPrefString("currentProject").length() == 0) {
            Project = project_title;
        } else {
            Project = cc.loadPrefString("currentProject");

        }

        Cursor cur;
        cur = dbcon.getProjectIdByName(Project);
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            project_id = cur.getString(0);

            delete_this_project = project_id;

            Cursor cur1 = dbcon.getPicsFromProjectId(project_id);
            if (cur1 != null && cur1.getCount() != 0) {
                cur1.moveToFirst();
                totalImgNumber = cur1.getCount();
                curImgNumber = cur1.getPosition();
            } else {
                curImgNumber = 0;
                totalImgNumber = 0;
            }
            cc.savePrefString("lastUsedProject", Project);
            tv_toolbar_project_name.setText(Project);
            tv_toolbar_page_index.setText(" (" + curImgNumber + "/" + totalImgNumber + ")");
        } else {
            Project = "ConTextUs";
            tv_toolbar_project_name.setText(Project);
            tv_toolbar_page_index.setVisibility(View.INVISIBLE);
            cc.savePrefBoolean("noProject", false);
        }
    }


    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MainActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public void callFrgmentViewImage() {
        fragment = new ImageViewFragment();
        fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    public void ndProjectClickEvent(String Project_name) {
        cc.savePrefString("currentProject", Project_name);

        Cursor cursor = null;
        String p_id = null;
        String last_updated = getCurrentDateandtime();

        cursor = dbcon.getProjectIdByName(Project_name);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            p_id = cursor.getString(0);
        }

        dbcon.updateProjectLastUpdate(p_id, last_updated);
        callFrgmentViewImage();
        setToolbarTitle(Project_name);
        drawer.closeDrawer(GravityCompat.START);
    }
}
