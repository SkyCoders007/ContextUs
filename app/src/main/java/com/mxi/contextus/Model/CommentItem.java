package com.mxi.contextus.Model;

public class CommentItem {
    public String getComment_tag() {
        return comment_tag;
    }

    public void setComment_tag(String comment_tag) {
        this.comment_tag = comment_tag;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }


    public String getSeq_no() {
        return seq_no;
    }

    public void setSeq_no(String seq_no) {
        this.seq_no = seq_no;
    }

    String comment_tag;
    String comment_text;
    String pic_id;
    String seq_no;
}
