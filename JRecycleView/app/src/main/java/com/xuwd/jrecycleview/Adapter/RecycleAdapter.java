package com.xuwd.jrecycleview.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xuwd.jrecycleview.R;

import java.util.ArrayList;
/*  adapter实际上是管理ViewHolder，即一个区块。重写adapter就是根据layout重构ViewHolder
//  利用父窗口的Inflater载入区块view，将其作为参数传递给重载的ViewHolder，由ViewHolder对区块进行操作
//  流程：根据getItemCount计数，创建n个ViewHolder,并依次触发onBindViewHolder
*/
public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.JViewHolder>{
    private ArrayList<String> mData;

    public RecycleAdapter(ArrayList<String> data){
        this.mData=data;
    }

    @NonNull
    @Override
    public RecycleAdapter.JViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.list_simple_text,parent,false);
        final JViewHolder viewHolder=new JViewHolder(view);
/*        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),viewHolder.mTextView.getText(),Toast.LENGTH_SHORT).show();
            }
       });
*/        return viewHolder;
    }

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull RecycleAdapter.JViewHolder holder, int position) {
        holder.mTextView.setText(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class JViewHolder extends RecyclerView.ViewHolder
    {
        TextView mTextView;
        public JViewHolder(View view)
        {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.simpleList);
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(),mTextView.getText(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
