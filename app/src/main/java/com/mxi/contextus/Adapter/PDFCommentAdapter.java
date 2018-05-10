package com.mxi.contextus.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mxi.contextus.Model.CommentItem;
import com.mxi.contextus.R;
import com.mxi.contextus.Util.CommanClass;

import java.util.Collections;
import java.util.List;

public class PDFCommentAdapter extends RecyclerView.Adapter<PDFCommentAdapter.MyViewHolder> {

    CommanClass cc;
    CommentItem current;

    List<CommentItem> comment_list = Collections.emptyList();
    private Context context;

    public PDFCommentAdapter(Context context, List<CommentItem> comment_list) {
        cc = new CommanClass(context);
        this.context = context;
        this.comment_list = comment_list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_comment_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        current = comment_list.get(position);

        holder.tv_comment_tag.setText(current.getSeq_no()+"-"+current.getComment_tag());
        holder.tv_comment_text.setText(current.getComment_text());

    }

    @Override
    public int getItemCount() {
        return comment_list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_comment_tag, tv_comment_text;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_comment_tag = (TextView) itemView.findViewById(R.id.tv_comment_tag_pdf);
            tv_comment_text = (TextView) itemView.findViewById(R.id.tv_comment_text_pdf);
        }
    }
}

