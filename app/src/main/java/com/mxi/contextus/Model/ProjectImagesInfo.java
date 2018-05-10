package com.mxi.contextus.Model;

public class ProjectImagesInfo {

    public String pictureID;
    public String projectID;
    public int sequenceNumber;
    public String picSizeX;
    public String picSizeY;
    public String picInString;
    public String picturePath;
    String orientation;
    public String pictureName;

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }



    public String getPicInString() {
        return picInString;
    }

    public void setPicInString(String picInString) {
        this.picInString = picInString;
    }

    public String getPictureID() {
        return pictureID;
    }

    public void setPictureID(String pictureID) {
        this.pictureID = pictureID;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getPicSizeX() {
        return picSizeX;
    }

    public void setPicSizeX(String picSizeX) {
        this.picSizeX = picSizeX;
    }

    public String getPicSizeY() {
        return picSizeY;
    }

    public void setPicSizeY(String picSizeY) {
        this.picSizeY = picSizeY;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }
}
