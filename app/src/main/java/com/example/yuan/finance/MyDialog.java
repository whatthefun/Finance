package com.example.yuan.finance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import java.text.SimpleDateFormat;

/**
 * Created by YUAN on 2017/04/11.
 */

public class MyDialog extends DialogFragment {

    private EditText edtAmount, edtComment, edtDate;
    private AppCompatImageButton imgBtnIncrease, imgBtnDecrease, imgBtnPickDate;
    private DialogListener mListener;
    private long id = 0;
    private String date, comment;
    private int amount;

    public interface DialogListener {
        public void onDialogPositiveClick(long id, int amount, String date, String comment);
    }

    static MyDialog newInstance(long id, Expense_item item) {
        MyDialog dialog = new MyDialog();

        if (id > 0){
            Log.d("newInstance", "id > 0");
            Bundle bundle = new Bundle();
            bundle.putLong("id", id);
            bundle.putString("date", item.getDate());
            bundle.putInt("amount", item.getAmount());
            bundle.putString("comment", item.getComment());
            dialog.setArguments(bundle);
        }


        return dialog;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (DialogListener) getActivity();
        }catch (ClassCastException e){
            Log.e("MyDialog", e.toString());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("MyDialog", "onCreateDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add, null);
        edtAmount = (EditText) view.findViewById(R.id.edtAmount);
        edtComment = (EditText) view.findViewById(R.id.edtComment);
        edtDate = (EditText) view.findViewById(R.id.edtDate);
        imgBtnIncrease = (AppCompatImageButton) view.findViewById(R.id.imgBtnIncrease);
        imgBtnDecrease = (AppCompatImageButton) view.findViewById(R.id.imgBtnDecrease);
        imgBtnPickDate = (AppCompatImageButton) view.findViewById(R.id.imgBtnPickDate);

        imgBtnIncrease.setOnClickListener(adjustListener);
        imgBtnDecrease.setOnClickListener(adjustListener);

        if (savedInstanceState != null){
            Log.d("onCreateDialog", "savedInstanceState != null");
            id = savedInstanceState.getLong("id");
            date = savedInstanceState.getString("date");
            amount = savedInstanceState.getInt("amount");
            comment = savedInstanceState.getString("comment");

            edtAmount.setText(amount + "");
            edtComment.setText(comment);
        }else {
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            date = sDateFormat.format(new java.util.Date());
        }



        // make it can not be edited
        edtDate.setKeyListener(null);
        edtDate.setText(date);
        imgBtnPickDate.setOnClickListener(pickDateListener);

        builder.setView(view)
            .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {

                    mListener.onDialogPositiveClick(id,
                                                    Integer.parseInt(edtAmount.getText().toString()),
                                                    edtDate.getText().toString(),
                                                    edtComment.getText().toString());
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    MyDialog.this.getDialog().cancel();
                }
            });

        return builder.create();
    }

    AppCompatImageButton.OnClickListener adjustListener = new AppCompatImageButton.OnClickListener() {
            @Override public void onClick(View v) {
                int amount = Integer.parseInt(edtAmount.getText().toString());
                if (v.getId() == R.id.imgBtnIncrease){
                    amount += 1000;
                }else if (v.getId() == R.id.imgBtnDecrease){
                    if (amount >= 1000){
                        amount -= 1000;
                    }else if (amount > 0){
                        amount = 0;
                    }
                }
                edtAmount.setText(amount + "");
            }
        };

    AppCompatImageButton.OnClickListener pickDateListener =
        new AppCompatImageButton.OnClickListener() {
            @Override public void onClick(View v) {
                String date = edtDate.getText().toString();
                int y = Integer.parseInt(date.substring(0,4));
                int m = Integer.parseInt(date.substring(5,7));
                int d = Integer.parseInt(date.substring(8,10));

                // 跳出日期選擇器
                DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            // 完成選擇，顯示日期
                            edtDate.setText(String.format("%04d/%02d/%02d", year, (monthOfYear + 1), dayOfMonth));
                        }
                    }, y, m - 1, d);
                pickerDialog.show();
            }
        };
}
