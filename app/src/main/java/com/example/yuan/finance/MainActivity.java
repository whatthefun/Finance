package com.example.yuan.finance;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

public class MainActivity extends FragmentActivity implements MyDialog.DialogListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int QUERY_LOADER = 22;
    private FloatingActionButton fab;
    private TextView txtMonth, txtYear, txtTotal;
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

        //get view
        fab = (FloatingActionButton) findViewById(R.id.fab);
        txtMonth = (TextView) findViewById(R.id.txtMonth);
        txtYear = (TextView) findViewById(R.id.txtYear);
        txtTotal = (TextView) findViewById(R.id.txtTotal);
        imgBtnPreMonth = (AppCompatImageButton) findViewById(R.id.imgBtnPreMonth);
        imgBtnAfterMonth = (AppCompatImageButton) findViewById(R.id.imgBtAfterMonth);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        setTime();

        txtMonth.setOnClickListener(adjustTimeListener);
        txtYear.setOnClickListener(adjustTimeListener);

        imgBtnPreMonth.setOnClickListener(monthChangeListener);
        imgBtnAfterMonth.setOnClickListener(monthChangeListener);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(this, null);
        recyclerView.setAdapter(adapter);

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
                MyDialog dialog = new MyDialog();
                dialog.show(getFragmentManager(), "add");
            }
        });

        getSupportLoaderManager().initLoader(QUERY_LOADER, null, this);
    }

    @Override public void onDialogPositiveClick(int amount, String date, String comment) {
        Toast.makeText(this, "新增金額: " + amount + " 備註: " + comment, Toast.LENGTH_LONG).show();

        //add
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("amount", amount);
        values.put("comment", comment);
        long id = helper.getWritableDatabase().insert("expense", null, values);

        getSupportLoaderManager().restartLoader(QUERY_LOADER, null, this);
        Log.d("ADD" , id + "");
    }

    //sql query sometime data
    //private Cursor query(){
    //    String y = txtYear.getText().toString();
    //    String m = txtMonth.getText().toString().substring(0, 2);
    //    String where = "date like '?-?'";
    //    Cursor c = helper.getReadableDatabase().query("expense", null, null, null, null, null, null);
    //    return c;
    //}

    //sql delete the given id data
    private boolean delete(long id){
        return db.delete("expense", "_id" + "=" + id, null) > 0;
    }

    //set textView to current time
    private void setTime(){
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM");
        String time = sDateFormat.format(new java.util.Date());

        txtYear.setText(time.substring(0,4));
        txtMonth.setText(time.substring(5) + "月");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("main", "onCreateLoader");
        return new CursorLoader(this, null, null,null,null,null) {
            @Override
            public Cursor loadInBackground()
            {
                try {
                    String y = txtYear.getText().toString();
                    String m = txtMonth.getText().toString().substring(0, 2);
                    String query = "date like \'" + y + "/" + m + "%\'";
                    final Cursor c1 = helper.getWritableDatabase().query("expense", new String[] {"SUM(amount)"}, query, null, null, null, null);
                    if(c1.moveToFirst())
                    {
                        runOnUiThread(new Runnable() {
                            public void run()
                            {
                                txtTotal.setText(getString(R.string.total) +  c1.getInt(0));
                            }
                        });
                    }

                    Cursor c2 = helper.getWritableDatabase().query("expense", null, query, null, null, null, null);

                    return c2;
                }catch (Exception e){
                    Log.e("loadInBackground", e.toString());
                }
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("main", "onLoadFinished");
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    // increase or decrease the time
    private AppCompatImageButton.OnClickListener monthChangeListener = new AppCompatImageButton.OnClickListener() {
            @Override public void onClick(View v) {
                int month = Integer.parseInt(txtMonth.getText().toString().substring(0, 2));
                int year = Integer.parseInt(txtYear.getText().toString());
                //Toast.makeText(MainActivity.this, month + "", Toast.LENGTH_SHORT).show();
                if (v.getId() == R.id.imgBtnPreMonth){
                    month--;
                }else if(v.getId() == R.id.imgBtAfterMonth){
                    month++;
                }

                if (month >12){
                    month = 1;
                    year++;
                }else if (month < 1){
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
}
