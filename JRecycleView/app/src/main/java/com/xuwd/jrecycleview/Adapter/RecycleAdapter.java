package com.xuwd.jrecycleview.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xuwd.jrecycleview.R;

import java.util.ArrayList;
/*  adapter实际上是管理ViewHolder，即一个区块。重写adapter就是根据layout重构ViewHolder
//  利用父窗口的Inflater载入区块view，将其作为参数传递给重载的ViewHolder，由ViewHolder对区块进行操作
//  流程：根据getItemCount计数，创建n个ViewHolder,并依次触发onBindViewHolder
*/
public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.JViewHolder>{
    private int listItemId;
    private ArrayList<String> mData;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(RecycleAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public RecycleAdapter(int listItemId,ArrayList<String> data){
        this.listItemId=listItemId;
        this.mData=data;
    }

    @NonNull
    @Override
    public RecycleAdapter.JViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(listItemId,parent,false);
        final JViewHolder viewHolder=new JViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecycleAdapter.JViewHolder holder, int position) {
        holder.mTextView.setText(mData.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener!=null){
                    int pos=holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView,pos);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(onItemClickListener != null) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView, pos);
                }
                //表示此事件已经消费，不会触发单击事件
                return true;
            }
        });
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
            mTextView = (TextView) view.findViewById(R.id.listItemText);

        }
    }

    public interface OnItemClickListener{
        void onItemClick(View view,int position);
        void onItemLongClick(View view, int position);
    }
}
