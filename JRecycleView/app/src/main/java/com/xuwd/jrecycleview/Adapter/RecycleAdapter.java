package com.xuwd.jrecycleview.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xuwd.jrecycleview.R;

import java.util.ArrayList;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.JViewHolder>{
    private ArrayList<String> mData;

    public RecycleAdapter(ArrayList<String> data){
        this.mData=data;
    }

    @NonNull
    @Override
    public RecycleAdapter.JViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.list_simple_central,parent,false);
        JViewHolder viewHolder=new JViewHolder(view);
        return viewHolder;
    }

    @NonNull


    @Override
    public void onBindViewHolder(@NonNull RecycleAdapter.JViewHolder holder, int position) {
        holder.tv.setText(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class JViewHolder extends RecyclerView.ViewHolder
    {
        TextView tv;
        public JViewHolder(View view)
        {
            super(view);
            tv = (TextView) view.findViewById(R.id.simpleList);
        }
    }

}
