package com.example.yuan.finance;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yuan.finance.utilities.MyAdapter;
import com.example.yuan.finance.utilities.MyDBHelper;
import java.text.SimpleDateFormat;

// TODO: 2017/04/17 取消滑動刪除
// TODO: 2017/04/17 備註改成卡片，備註改成icon info 
// TODO: 2017/04/17 刪除改在dialog 
// TODO: 2017/04/17 滑動翻月 
// TODO: 2017/04/17  圓餅圖統計月
// TODO: 2017/04/17 主題
public class MainActivity extends FragmentActivity
    implements MyDialog.DialogListener, LoaderManager.LoaderCallbacks<Cursor>,
    MyAdapter.ListItemLongClickListener {

    public static final int QUERY_LOADER = 22;
    private FloatingActionButton fab;
    private TextView txtMonth, txtYear, txtTotal, txtDate, txtAmount, txtComment;
    private RecyclerView recyclerView;
    private AppCompatImageButton imgBtnPreMonth, imgBtnAfterMonth;
    private SQLiteDatabase db;
    private MyDBHelper helper;
    private MyAdapter adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = MyDBHelper.getInstance(this);
        db = helper.getWritableDatabase();

        bindUI();

        setTime();

        txtMonth.setOnClickListener(adjustTimeListener);
        txtYear.setOnClickListener(adjustTimeListener);

        imgBtnPreMonth.setOnClickListener(monthChangeListener);
        imgBtnAfterMonth.setOnClickListener(monthChangeListener);

        //set recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(this, null, this);
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
                    getSupportLoaderManager().restartLoader(QUERY_LOADER, null, MainActivity.this);
                }
            }).attachToRecyclerView(recyclerView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Log.d("Main", "fab");
                DialogFragment dialog = MyDialog.newInstance(0, null);
                dialog.show(getFragmentManager(), "add");
            }
        });

        //Loader initial
        getSupportLoaderManager().initLoader(QUERY_LOADER, null, this);
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
            getSupportLoaderManager().restartLoader(QUERY_LOADER, null, this);
            Toast.makeText(MainActivity.this, "新增金額: " + amount, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("ADD", e.toString());
            Log.e("ADD", id + "");
            Toast.makeText(MainActivity.this, "新增失敗", Toast.LENGTH_LONG).show();
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
            getSupportLoaderManager().restartLoader(QUERY_LOADER, null, this);
            Toast.makeText(MainActivity.this, "修改成功!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "修改失敗", Toast.LENGTH_LONG).show();
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

    private void bindUI() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        txtMonth = (TextView) findViewById(R.id.txtMonth);
        txtYear = (TextView) findViewById(R.id.txtYear);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtAmount = (TextView) findViewById(R.id.txtAmount);
        txtComment = (TextView) findViewById(R.id.txtComment);
        txtTotal = (TextView) findViewById(R.id.txtTotal);
        imgBtnPreMonth = (AppCompatImageButton) findViewById(R.id.imgBtnPreMonth);
        imgBtnAfterMonth = (AppCompatImageButton) findViewById(R.id.imgBtAfterMonth);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/chinese.ttf");
        txtDate.setTypeface(font);
        txtAmount.setTypeface(font);
        txtComment.setTypeface(font);
        txtTotal.setTypeface(font);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("main", "onCreateLoader");
        return new CursorLoader(this, null, null, null, null, null) {
            @Override public Cursor loadInBackground() {
                try {
                    final String y = txtYear.getText().toString();
                    final String m = txtMonth.getText().toString().substring(0, 2);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            int sum = querySum(y, m);
                            txtTotal.setText(getString(R.string.total) + sum);
                        }
                    });

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
                getSupportLoaderManager().restartLoader(QUERY_LOADER, null, MainActivity.this);
            }
        };

    TextView.OnClickListener adjustTimeListener = new TextView.OnClickListener() {
        @Override public void onClick(View v) {
            setTime();
            getSupportLoaderManager().restartLoader(QUERY_LOADER, null, MainActivity.this);
        }
    };

    @Override public void onListItemLongClickListener(long id) {
        Toast.makeText(this, "id:" + id, Toast.LENGTH_SHORT).show();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);

        Expense_item item = query(id);

        DialogFragment dialog = MyDialog.newInstance(id, item);
        dialog.show(getFragmentManager(), "add");
    }
}
