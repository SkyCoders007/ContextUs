package com.mxi.contextus.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mxi.contextus.Font.BoldMyTextView;
import com.mxi.contextus.Model.NavDrawerItem;
import com.mxi.contextus.R;
import com.mxi.contextus.Util.CommanClass;

import java.util.Collections;
import java.util.List;

public class NavigationListAdapter extends RecyclerView.Adapter<NavigationListAdapter.MyViewHolder> {

    CommanClass cc;
    NavDrawerItem current;
    List<NavDrawerItem> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;

    public NavigationListAdapter(Context context, List<NavDrawerItem> data) {
        cc = new CommanClass(context);
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.nav_list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        current = data.get(position);
        holder.tv_nav_item_title.setText(current.getTitle());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        BoldMyTextView tv_nav_item_title;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_nav_item_title = (BoldMyTextView) itemView.findViewById(R.id.tv_nav_item_text);
        }
    }
}
