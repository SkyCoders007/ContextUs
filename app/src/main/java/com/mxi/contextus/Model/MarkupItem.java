package com.mxi.contextus.Model;

public class MarkupItem {

    public MarkupItem(String pic_id, String markup_count, String c_x, String c_y, String markup_details) {
        this.pic_id = pic_id;
        this.markup_count = markup_count;
        this.c_x = c_x;
        this.c_y = c_y;
        this.markup_details = markup_details;
    }

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

    public String getMarkup_count() {
        return markup_count;
    }

    public void setMarkup_count(String markup_count) {
        this.markup_count = markup_count;
    }

    public String getC_x() {
        return c_x;
    }

    public void setC_x(String c_x) {
        this.c_x = c_x;
    }

    public String getC_y() {
        return c_y;
    }

    public void setXc_y(String xc_y) {
        this.c_y = c_y;
    }

    public String getMarkup_details() {
        return markup_details;
    }

    public void setMarkup_details(String markup_details) {
        this.markup_details = markup_details;
    }

    String pic_id,markup_count,c_x,c_y,markup_details;
}
