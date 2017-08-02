package com.example.yuan.finance.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.yuan.finance.R;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private static final String TAG = "MyRecyclerAdapter";
    private buttonClickListener mListener;
    private Cursor mCursor;
    private Context mContext;

    public MyRecyclerAdapter(Context context, Cursor cursor, buttonClickListener listener) {
        this.mContext = context;
        this.mCursor = cursor;
        this.mListener = listener;
    }

    public interface buttonClickListener {
        void onButtonClickListener(long id, String which);
    }

    @Override
    public MyRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {

        if (mCursor == null || !mCursor.moveToPosition(position)) {
            return;
        }

        holder.txtDate.setText(mCursor.getString(mCursor.getColumnIndex("date")).substring(5));
        holder.txtAmount.setText("$" + mCursor.getInt(2) + "");
        //holder.txtComment.setText(mCursor.getString(3));
        holder.item.setTag(mCursor.getLong(mCursor.getColumnIndex("_id")));
    }

    @Override public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;

        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView txtAmount, txtDate;
        private ImageView imgBtnArrow;
        private Button btnEdit, btnDelete;
        private boolean isViewExpanded = false;
        private int originalHeight = 0;
        private LinearLayout expandableView;
        private RelativeLayout item;

        public ViewHolder(View v) {
            super(v);
            item = (RelativeLayout) v.findViewById(R.id.item);
            txtDate = (TextView) v.findViewById(R.id.txtDate);
            txtAmount = (TextView) v.findViewById(R.id.txtAmount);
            expandableView = (LinearLayout) v.findViewById(R.id.linearLayout2);
            imgBtnArrow = (ImageView) v.findViewById(R.id.imgBtnArrow);
            btnEdit = (Button) v.findViewById(R.id.btnEdit);
            btnDelete = (Button) v.findViewById(R.id.btnDelete);

            item.setOnClickListener(this);
            btnEdit.setOnClickListener(this);
            btnDelete.setOnClickListener(this);

            if (!isViewExpanded) {
                expandableView.setVisibility(View.GONE);
                expandableView.setEnabled(false);
                imgBtnArrow.setImageResource(R.drawable.ic_arrow_drop_down_black_24px);
            }
        }

        @Override public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.item:
                    //region 動畫
                    ValueAnimator valueAnimator;
                    if (originalHeight == 0) {
                        originalHeight = v.getHeight();

                        Log.d(TAG, "onClick: originalHeight:" + v.getMeasuredHeight());
                    }

                    if (!isViewExpanded) {
                        expandableView.setVisibility(View.VISIBLE);
                        expandableView.setEnabled(true);
                        imgBtnArrow.setImageResource(R.drawable.ic_arrow_drop_up_black_24px);
                        valueAnimator = ValueAnimator.ofInt(originalHeight,
                            originalHeight + (int) (originalHeight * 0.7));
                    } else {
                        valueAnimator =
                            ValueAnimator.ofInt(originalHeight + (int) (originalHeight * 0.7),
                                originalHeight);
                        Animation a = new AlphaAnimation(1.00f, 0.00f); // Fade out

                        a.setDuration(200);
                        // Set a listener to the animation and configure onAnimationEnd
                        a.setAnimationListener(new Animation.AnimationListener() {
                            @Override public void onAnimationStart(Animation animation) {
                            }

                            @Override public void onAnimationEnd(Animation animation) {
                                expandableView.setVisibility(View.INVISIBLE);
                                expandableView.setEnabled(false);
                                imgBtnArrow.setImageResource(
                                    R.drawable.ic_arrow_drop_down_black_24px);
                            }

                            @Override public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        expandableView.startAnimation(a);
                    }

                    isViewExpanded ^= true;

                    valueAnimator.setDuration(200);
                    valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Integer value = (Integer) animation.getAnimatedValue();
                            v.getLayoutParams().height = value.intValue();
                            v.requestLayout();
                        }
                    });
                    valueAnimator.start();
                    //endregion
                    break;
                case R.id.btnEdit:
                    mListener.onButtonClickListener((long) item.getTag(), "edit");
                    break;
                case R.id.btnDelete:
                    mListener.onButtonClickListener((long) item.getTag(), "delete");
                    break;
            }
        }
    }
}
