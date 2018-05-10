package com.mxi.contextus.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class SQLitehelper {
    Context mcon;
    private static String dbname = "ContextUs.db";
//    static String db_path = Environment.getExternalStorageDirectory() + "/DCIM/lessunknown/" + dbname;
    static String db_path = Environment.getExternalStorageDirectory()+ "/lessunknown/database/" + dbname;

    SQLiteDatabase db;

    public SQLitehelper(Context mcon) {
        this.mcon = mcon;
        final File direct = new File(Environment.getExternalStorageDirectory()+ "/lessunknown/database/");
        if (!direct.exists()) {
            direct.mkdirs();
        }

        db = mcon.openOrCreateDatabase(db_path, Context.MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS Projects(project_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "project_name VARCHAR, last_used VARCHAR, header_detail VARCHAR );");

        db.execSQL("CREATE TABLE IF NOT EXISTS Pics(pic_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "project_id VARCHAR, sequence_number VARCHAR, pic_sizeX VARCHAR, pic_sizeY VARCHAR,path VARCHAR, date_taken VARCHAR, marked_image VARCHAR,image_name VARCHAR,orientation VARCHAR); ");

        db.execSQL("CREATE TABLE IF NOT EXISTS Markups(markup_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "pic_id VARCHAR, markups_name VARCHAR, coordinate_x VARCHAR, coordinate_y VARCHAR, comment_detail VARCHAR); ");

    }

//-------------------------------Projects -------------------------------------------------------------------------------------

    public void insertProject(String project_name, String last_used, String header_detail) {

        String query = "INSERT INTO Projects(project_name,last_used,header_detail)VALUES ('"
                + project_name
                + "','"
                + last_used
                + "','"
                + header_detail
                + "')";
        Log.e("Query Projects", query);
        try {
            db.execSQL(query);
        } catch (SQLException e) {
            Log.e("Error Projects", e.getMessage());
        }

    }

    public void insertPics(String project_id, String sequence_number, String pic_sizeX, String pic_sizeY, String path, String date_taken, String marked_image, String image_name, String orientation) {
        String query = "INSERT INTO Pics(project_id,sequence_number,pic_sizeX,pic_sizeY,path,date_taken,marked_image,image_name,orientation)VALUES ('"
                + project_id
                + "','"
                + sequence_number
                + "','"
                + pic_sizeX
                + "','"
                + pic_sizeY
                + "','"
                + path
                + "','"
                + date_taken
                + "','"
                + marked_image
                + "','"
                + image_name
                + "','"
                + orientation
                + "')";
        Log.e("Query Pics", query);
        try {
            db.execSQL(query);
        } catch (SQLException e) {
            Log.e("Error Pics", e.getMessage());
        }

    }



    public void updatePicSequenceNumber(String pic_id, String sequenceNumber) {

        String query = "UPDATE Pics SET sequence_number = '" + sequenceNumber
                + "'" + " WHERE pic_id = " + "'" + pic_id + "'";

        Log.e("Query updatePref", query);
        try {
            db.execSQL(query);
        } catch (SQLException e) {
            Log.e("Error updatePref", e.getMessage());
        }

    }


    public void updateProjectLastUpdate(String project_id, String last_used) {

        String query = "UPDATE Projects SET last_used = '" + last_used
                + "'" + " WHERE project_id = " + "'" + project_id + "'";

        Log.e("Query updateLast_used", query);
        try {
            db.execSQL(query);
        } catch (SQLException e) {
            Log.e("Error updateLast_used", e.getMessage());
        }

    }

    public void updateProjectHeader(String project_id, String header_detail) {

        String query = "UPDATE Projects SET header_detail = '" + header_detail
                + "'" + " WHERE project_id = " + "'" + project_id + "'";

        Log.e("Query updateHeader", query);
        try {
            db.execSQL(query);
        } catch (SQLException e) {
            Log.e("Error updateHeader", e.getMessage());
        }

    }

    public String getProjectHeaderByName(String project_name) {
        Cursor cur = null;
        String header = "";
        try {
            String query = "SELECT  * FROM  Projects WHERE project_name = "
                    + "'" + project_name + "'";
            cur = db.rawQuery(query, null);
            if (cur != null && cur.getCount() != 0) {
                cur.moveToFirst();
                String temp_header = cur.getString(3);
                if (!temp_header.equals("null") && !temp_header.isEmpty() && temp_header != null) {
                    header = temp_header;
                } else {
                    header = "";
                }
            } else {
                header = "";
            }
        } catch (Exception e) {
            Log.e("Error:getProjectFromID", e.getMessage());
        }

        return header;
    }


    public String getProjectIdFromPicId(String pic_id) {
        Cursor cur = null;
        String project_id = "";
        try {
            String query = "SELECT  * FROM  Pics WHERE pic_id = "
                    + "'" + pic_id + "'";
            cur = db.rawQuery(query, null);
            if (cur != null && cur.getCount() != 0) {
                cur.moveToFirst();
                project_id = cur.getString(1);
            }
        } catch (Exception e) {
            Log.e("E:getProjectIdFromPicId", e.getMessage());
        }
        return project_id;
    }


    public Cursor getProjects() {
        Cursor cur = null;
        try {
            String query = "SELECT  * FROM  Projects ";
            cur = db.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("Error: getProjects ", e.getMessage());
        }

        return cur;
    }

    public Cursor getProjectsOnLastUsed() {
        Cursor cur = null;
        try {
            String query = "SELECT  * FROM  Projects ORDER BY last_used DESC;";
            cur = db.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("Error: getProjects ", e.getMessage());
        }

        return cur;
    }

    public Cursor getProjectIdByName(String project_name) {
        Cursor cur = null;
        try {
            String query = "SELECT  * FROM  Projects WHERE project_name = "
                    + "'" + project_name + "'";
            cur = db.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("Error:getProjectFromID", e.getMessage());
        }
        return cur;
    }

    public String getProjectNameById(String project_id) {
        String projectName = "";
        Cursor cur = null;
        try {
            String query = "SELECT  * FROM  Projects WHERE project_id = "
                    + "'" + project_id + "'";
            cur = db.rawQuery(query, null);
            if (cur != null && cur.getCount() != 0) {
                cur.moveToFirst();
                projectName = cur.getString(1);
            } else {
                projectName = "";
            }

        } catch (Exception e) {
            Log.e("Error:getProjectFromID", e.getMessage());
        }
        return projectName;
    }

    public Cursor getProjectDetailsByName(String project_name) {
        Cursor cur = null;
        try {
            String query = "SELECT  * FROM  Projects WHERE project_name = "
                    + "'" + project_name + "'";
            cur = db.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("Error: getProjects ", e.getMessage());
        }

        return cur;
    }

    public void deleteProject(String project_id) {
        try {
            db.execSQL("delete from Projects" + " where project_id='" + project_id + "'");
            Log.e("deletePic", "delete from Projects" + " where project_id='" + project_id + "'" + "");
        } catch (Exception e) {
            Log.e("Error: getProjects ", e.getMessage());
        }

    }


    public Cursor getPicsFromProjectId(String project_id) {
        Cursor cur = null;
        try {
            String query = "SELECT  * FROM  Pics WHERE project_id = "
                    + "'" + project_id + "'";
            cur = db.rawQuery(query, null);

        } catch (Exception e) {
            Log.e("getPicsFromProjectId", e.getMessage());
        }

        return cur;
    }


    public Cursor getPicsId(String project_id, String sequence_number) {
        Cursor cur = null;
        try {
            String query = "SELECT * FROM  Pics WHERE project_id ="
                    + "'" + project_id + "' AND  sequence_number =" + "'" + sequence_number + "'";
            Log.e("query GetPicId", query);
            cur = db.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("Error: getPics", e.getMessage());
        }

        return cur;
    }

    public Cursor getPicFromPicId(String pic_id) {

        Cursor cur = null;
        try {
            String query = "SELECT * FROM  Pics WHERE pic_id ="
                    + "'" + pic_id + "'";
            Log.e("query getPicFromPicId", query);
            cur = db.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("Error: getPicFromPicId", e.getMessage());
        }

        return cur;
    }

    public void deletePic(String id) {

        try {

            db.execSQL("delete from Pics" + " where pic_id='" + id + "'");
            Log.e("deletePic", "delete from Pics" + " where pic_id='" + id + "'" + "");

        } catch (SQLException e) {
            Log.e("Error: DeleteDataPic", e.getMessage());


        }

    }

    public void updateMarkedImage(String pic_id, String marked_image) {

        String query = "UPDATE Pics SET marked_image = '" + marked_image
                + "'" + " WHERE pic_id = " + "'" + pic_id + "'";
        try {
            db.execSQL(query);
        } catch (SQLException e) {
            Log.e("Error updateMarkedImage", e.getMessage());
        }

    }


    public void insertMarkups(String pic_id, String markups_name, String coordinate_x, String coordinate_y, String comment_detail) {

        String query = "INSERT INTO Markups(pic_id,markups_name,coordinate_x,coordinate_y,comment_detail)VALUES ('"
                + pic_id
                + "','"
                + markups_name
                + "','"
                + coordinate_x
                + "','"
                + coordinate_y
                + "','"
                + comment_detail
                + "')";
        Log.e("Query Markups", query);
        try {
            db.execSQL(query);
        } catch (SQLException e) {
            Log.e("Error Markups", e.getMessage());
        }

    }

    public Cursor getMarkupsFromPicId(String pic_id) {
        Cursor cur = null;
        try {
            String query = "SELECT  * FROM  Markups WHERE pic_id ='" + pic_id + "'";
            cur = db.rawQuery(query, null);
            Log.e("CURSOR", cur.getCount() + "");
        } catch (Exception e) {
            Log.e("Error: getMarkups", e.getMessage());
        }

        return cur;
    }


    public void deleteMarkUpFromId(String markup_id) {
        try {
            db.execSQL("delete from Markups where markup_id='" + markup_id + "'");
            Log.e("delete markup_id", "delete from Markups" + " where markup_id='" + markup_id + "'" + "");
        } catch (Exception e) {
            Log.e("Error:delete markup_id ", e.getMessage());
        }

    }


    public void updateMarkupSequenceNumber(String markup_id, String markups_name) {

        String query = "UPDATE Markups SET markups_name = '" + markups_name
                + "'" + " WHERE markup_id = " + "'" + markup_id + "'";

        Log.e("Query updateSeqMarkUp", query);
        try {
            db.execSQL(query);
        } catch (SQLException e) {
            Log.e("Error updateSeqMarkUp", e.getMessage());
        }

    }

    public void updateMarkupDetail(String markup_id, String comment_detail) {

        String query = "UPDATE Markups SET comment_detail = '" + comment_detail
                + "'" + " WHERE markup_id = " + "'" + markup_id + "'";

        Log.e("Query updateMarkUpText", query);
        try {
            db.execSQL(query);
        } catch (SQLException e) {
            Log.e("Error updateMarkUpText", e.getMessage());
        }

    }


}
