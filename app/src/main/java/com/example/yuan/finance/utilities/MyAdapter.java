package com.example.yuan.finance.utilities;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.yuan.finance.R;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    final private ListItemLongClickListener mOnClickListener;

    private Cursor mCursor;
    private Context mContext;

    public MyAdapter(Context context, Cursor cursor, ListItemLongClickListener listener){
        this.mContext = context;
        this.mCursor = cursor;
        this.mOnClickListener = listener;
    }

    public interface ListItemLongClickListener{
        void onListItemLongClickListener(long id);
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (mCursor ==null || !mCursor.moveToPosition(position)){
            return;
        }

        holder.txtDate.setText(mCursor.getString(mCursor.getColumnIndex("date")).substring(5));
        holder.txtAmount.setText("$" + mCursor.getInt(2) +"");
        holder.txtComment.setText(mCursor.getString(3));
        holder.item.setTag(mCursor.getLong(mCursor.getColumnIndex("_id")));
    }

    @Override public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor){
        if (mCursor != null){
            mCursor.close();
        }
        mCursor = newCursor;

        if (newCursor != null){
            this.notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public TextView txtAmount, txtDate, txtComment;
        public LinearLayout item;

        public ViewHolder(View v)
        {
            super(v);
            item = (LinearLayout) v.findViewById(R.id.item);
            txtDate = (TextView) v.findViewById(R.id.txtDate);
            txtAmount = (TextView) v.findViewById(R.id.txtAmount);
            txtComment = (TextView) v.findViewById(R.id.txtComment);
            item.setOnLongClickListener(this);
        }

        @Override public boolean onLongClick(View v) {
            mOnClickListener.onListItemLongClickListener((long)v.getTag());
            return true;
        }
    }
}
