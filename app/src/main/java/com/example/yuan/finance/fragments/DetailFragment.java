package com.example.yuan.finance.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yuan.finance.R;
import com.example.yuan.finance.adapters.MyRecyclerAdapter;
import com.example.yuan.finance.helpers.MyDBHelper;
import com.example.yuan.finance.items.Expense_item;
import java.text.SimpleDateFormat;

//import android.app.DialogFragment;
//import android.app.Fragment;

/**
 * Created by YUAN on 2017/7/26.
 */

public class DetailFragment extends Fragment
    implements MyDialog.DialogListener, LoaderManager.LoaderCallbacks<Cursor>,
    MyRecyclerAdapter.ListItemLongClickListener {

    public static final int QUERY_LOADER = 22;
    private FloatingActionButton fab;
    private TextView txtMonth, txtYear, txtTotal, txtDate, txtAmount, txtComment;
    private RecyclerView recyclerView;
    private AppCompatImageButton imgBtnPreMonth, imgBtnAfterMonth;
    private SQLiteDatabase db;
    private MyDBHelper helper;
    private MyRecyclerAdapter adapter;
    private int sum = 0;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        helper = MyDBHelper.getInstance(getActivity());
        db = helper.getWritableDatabase();

        bindUI(view);

        setTime();

        txtMonth.setOnClickListener(adjustTimeListener);
        txtYear.setOnClickListener(adjustTimeListener);

        imgBtnPreMonth.setOnClickListener(monthChangeListener);
        imgBtnAfterMonth.setOnClickListener(monthChangeListener);

        //set recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MyRecyclerAdapter(getActivity(), null, this);
        recyclerView.setAdapter(adapter);

        //左右滑
        new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                    RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    long id = (long) viewHolder.itemView.getTag();
                    delete(id);
                    getActivity().getSupportLoaderManager()
                        .restartLoader(QUERY_LOADER, null, DetailFragment.this);
                }
            }).attachToRecyclerView(recyclerView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Log.d("Main", "fab");

                DialogFragment dialog = MyDialog.newInstance(0, null);
                dialog.show(getChildFragmentManager(), "add");
            }
        });

        //Loader initial
        getActivity().getSupportLoaderManager().initLoader(QUERY_LOADER, null, DetailFragment.this);

        return view;
    }

    @Override public void onDialogPositiveClick(long id, int amount, String date, String comment) {
        if (id == 0) {
            //add
            insert(amount, date, comment);
        } else {
            update(id, date, amount, comment);
        }
    }

    // add data
    private void insert(int amount, String date, String comment) {
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("amount", amount);
        values.put("comment", comment);
        long id = 0;
        try {
            id = helper.getWritableDatabase().insert("expense", null, values);
            getActivity().getSupportLoaderManager().restartLoader(QUERY_LOADER, null, this);
            Toast.makeText(getActivity(), "新增金額: " + amount, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("ADD", e.toString());
            Log.e("ADD", id + "");
            Toast.makeText(getActivity(), "新增失敗", Toast.LENGTH_LONG).show();
        }
    }

    //query sometime data
    private Cursor query(String y, String m) {
        String query = "date like \'" + y + "/" + m + "%\'";
        Cursor c = helper.getWritableDatabase()
            .query("expense", null, query, null, null, null, "date DESC");

        return c;
    }

    //query given id item
    private Expense_item query(long id) {
        String sId = Long.toString(id);
        Cursor c = helper.getWritableDatabase()
            .query("expense", null, "_id=?", new String[] { sId }, null, null, null);
        if (c.moveToFirst()) {
            String date = c.getString(c.getColumnIndex("date"));
            int amount = c.getInt(2);
            String comment = c.getString(3);
            return new Expense_item(date, amount, comment);
        }
        return null;
    }

    // query amount
    private int querySum(String y, String m) {
        String query = "date like \'" + y + "/" + m + "%\'";
        Cursor c = helper.getWritableDatabase()
            .query("expense", new String[] { "SUM(amount)" }, query, null, null, null, null);

        if (c.moveToFirst()) {
            return c.getInt(0);
        }
        return 0;
    }

    //update data
    private void update(long id, String date, int amount, String comment) {
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("amount", amount);
        values.put("comment", comment);
        try {
            helper.getWritableDatabase().update("expense", values, "_id=" + id, null);
            getActivity().getSupportLoaderManager().restartLoader(QUERY_LOADER, null, this);
            Toast.makeText(getActivity(), "修改成功!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "修改失敗", Toast.LENGTH_LONG).show();
            Log.e("Update", e.toString());
        }
    }

    //sql delete the given id data
    private boolean delete(long id) {
        return db.delete("expense", "_id" + "=" + id, null) > 0;
    }

    //set textView to current time
    private void setTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM");
        String time = sDateFormat.format(new java.util.Date());

        txtYear.setText(time.substring(0, 4));
        txtMonth.setText(time.substring(5) + "月");
    }

    private void bindUI(View view) {
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        txtMonth = (TextView) view.findViewById(R.id.txtMonth);
        txtYear = (TextView) view.findViewById(R.id.txtYear);
        txtDate = (TextView) view.findViewById(R.id.txtDate);
        txtAmount = (TextView) view.findViewById(R.id.txtAmount);
        txtComment = (TextView) view.findViewById(R.id.txtComment);
        txtTotal = (TextView) view.findViewById(R.id.txtTotal);
        imgBtnPreMonth = (AppCompatImageButton) view.findViewById(R.id.imgBtnPreMonth);
        imgBtnAfterMonth = (AppCompatImageButton) view.findViewById(R.id.imgBtAfterMonth);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/chinese.ttf");
        txtDate.setTypeface(font);
        txtAmount.setTypeface(font);
        txtComment.setTypeface(font);
        txtTotal.setTypeface(font);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("main", "onCreateLoader");

        return new CursorLoader(getActivity(), null, null, null, null, null) {
            @Override public Cursor loadInBackground() {
                try {
                    final String y = txtYear.getText().toString();
                    final String m = txtMonth.getText().toString().substring(0, 2);

                    sum = querySum(y, m);


                    //搜尋指定月份的所有資料，並依照日期降冪排序
                    return query(y, m);
                } catch (Exception e) {
                    Log.e("loadInBackground", e.toString());
                }
                return null;
            }
        };
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("main", "onLoadFinished");
        adapter.swapCursor(data);
        txtTotal.setText("本月總金額:" + sum);
    }

    @Override public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    // increase or decrease the time
    private AppCompatImageButton.OnClickListener monthChangeListener =
        new AppCompatImageButton.OnClickListener() {
            @Override public void onClick(View v) {
                int month = Integer.parseInt(txtMonth.getText().toString().substring(0, 2));
                int year = Integer.parseInt(txtYear.getText().toString());

                if (v.getId() == R.id.imgBtnPreMonth) {
                    month--;
                } else if (v.getId() == R.id.imgBtAfterMonth) {
                    month++;
                }

                if (month > 12) {
                    month = 1;
                    year++;
                } else if (month < 1) {
                    month = 12;
                    year--;
                }

                txtMonth.setText(String.format("%02d月", month));
                txtYear.setText(String.format("%04d", year));
                getActivity().getSupportLoaderManager()
                    .restartLoader(QUERY_LOADER, null, DetailFragment.this);
            }
        };

    TextView.OnClickListener adjustTimeListener = new TextView.OnClickListener() {
        @Override public void onClick(View v) {
            setTime();
            getActivity().getSupportLoaderManager()
                .restartLoader(QUERY_LOADER, null, DetailFragment.this);
        }
    };

    @Override public void onListItemLongClickListener(long id) {
        Toast.makeText(getActivity(), "id:" + id, Toast.LENGTH_SHORT).show();
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);

        Expense_item item = query(id);

        DialogFragment dialog = MyDialog.newInstance(id, item);
        dialog.show(getChildFragmentManager(), "add");

    }
}
