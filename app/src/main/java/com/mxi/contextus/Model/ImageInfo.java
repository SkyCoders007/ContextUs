package com.mxi.contextus.Model;

public class ImageInfo {


    boolean isVisible;

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    String image_path, pic_id, date_taken;
    String sequence_number;

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

    public String getDate_taken() {
        return date_taken;
    }

    public void setDate_taken(String date_taken) {
        this.date_taken = date_taken;
    }

    public String getSequence_number() {
        return sequence_number;
    }

    String image_name;

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public void setSequence_number(String sequence_number) {
        this.sequence_number = sequence_number;
    }

    String img_byte_String_array;
    String orientation;

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getImg_byte_String_array() {
        return img_byte_String_array;
    }

    public void setImg_byte_String_array(String img_byte_String_array) {
        this.img_byte_String_array = img_byte_String_array;
    }
}
