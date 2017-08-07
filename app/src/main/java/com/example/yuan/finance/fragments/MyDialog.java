package com.example.yuan.finance.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yuan.finance.R;
import com.example.yuan.finance.items.Expense_item;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by YUAN on 2017/04/11.
 */

public class MyDialog extends android.support.v4.app.DialogFragment {

    private static final String TAG = "MyDialog";
    private final static String FILE_NAME = "account.txt";
    private final static String FILE_PATH = "MyFile";
    private TextView txtTitle, txtSource;
    private EditText edtAmount, edtComment, edtDate;
    private AppCompatImageButton imgBtnIncrease, imgBtnDecrease, imgBtnPickDate;
    private DialogListener mListener;
    private long id = 0;
    private String date, comment;
    private int amount;
    private int index;
    private boolean isChange = false;

    public interface DialogListener {
        public void onDialogPositiveClick(long id, int amount, String date, String comment);
    }

    public static MyDialog newInstance(long id, Expense_item item) {
        MyDialog dialog = new MyDialog();

        if (id > 0) {
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
        try {
            mListener = (DialogListener) getParentFragment();
        } catch (ClassCastException e) {
            Log.e("MyDialog", e.toString());
        }
        super.onAttach(context);
    }

    // TODO: 2017/04/18 了解oncreate oncreateview oncreatedialog 
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("MyDialog", "onCreateDialog");
        Builder builder = new Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_expense, null);

        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        edtAmount = (EditText) view.findViewById(R.id.edtAmount);
        edtComment = (EditText) view.findViewById(R.id.edtComment);
        edtDate = (EditText) view.findViewById(R.id.edtDate);
        txtSource = (TextView) view.findViewById(R.id.txtSource);
        imgBtnIncrease = (AppCompatImageButton) view.findViewById(R.id.imgBtnIncrease);
        imgBtnDecrease = (AppCompatImageButton) view.findViewById(R.id.imgBtnDecrease);
        imgBtnPickDate = (AppCompatImageButton) view.findViewById(R.id.imgBtnPickDate);


        edtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                String number;
                str = str.replaceAll("[$,]", "");
                if (!isChange){
                    if (str.length() > 3){
                        int remainder = str.length() %3;
                        if (remainder != 0) {
                            number = str.substring(0, remainder) + ",";
                        }else {
                            number = str.substring(0, remainder);
                        }

                        for (int i = remainder; i < str.length(); i += 3) {
                            number += str.substring(i, i+3) + ",";
                        }
                        isChange = true;
                        edtAmount.setText("$" + number.substring(0, number.length()-1));
                        edtAmount.setSelection(edtAmount.getText().length());
                        isChange = false;
                    }else {
                        isChange = true;
                        edtAmount.setText("$" + str);
                        edtAmount.setSelection(edtAmount.getText().length());
                        isChange = false;
                    }
                }
            }

            @Override public void afterTextChanged(Editable s) {

            }
        });

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/chinese.ttf");
        txtTitle.setTypeface(font);
        imgBtnIncrease.setOnClickListener(adjustListener);
        imgBtnDecrease.setOnClickListener(adjustListener);
        txtSource.setOnClickListener(txtSourceOnClickListener);

        if (getArguments() != null && !getArguments().isEmpty()) {
            Log.d("onCreateDialog", "savedInstanceState != null");
            id = getArguments().getLong("id");
            date = getArguments().getString("date");
            amount = getArguments().getInt("amount");
            comment = getArguments().getString("comment");

            edtAmount.setText(amount + "");
            edtComment.setText(comment);
        } else {
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            date = sDateFormat.format(new java.util.Date());
        }

        // make it can not be edited
        edtDate.setKeyListener(null);
        edtDate.setText(date);
        String[] tmp = readFile();
        if (tmp.length > 0) {
            txtSource.setText(readFile()[0]);
        }
        imgBtnPickDate.setOnClickListener(pickDateListener);

        builder.setView(view).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                if (edtAmount.getText().toString().length() > 0
                    && edtDate.getText().toString().length() > 0) {
                    mListener.onDialogPositiveClick(id,
                        Integer .parseInt(edtAmount.getText().toString().replaceAll("[$,]", "")),
                        edtDate.getText().toString(), edtComment.getText().toString());
                } else {
                    Toast.makeText(getActivity(), "新增失敗! 金額或日期不可為空白", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return builder.create();
    }

    AppCompatImageButton.OnClickListener adjustListener =
        new AppCompatImageButton.OnClickListener() {
            @Override public void onClick(View v) {
                int amount = Integer.parseInt(edtAmount.getText().toString().replaceAll("[$,]", ""));
                if (v.getId() == R.id.imgBtnIncrease) {
                    amount += 1000;
                } else if (v.getId() == R.id.imgBtnDecrease) {
                    if (amount >= 1000) {
                        amount -= 1000;
                    } else if (amount > 0) {
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
                int y = Integer.parseInt(date.substring(0, 4));
                int m = Integer.parseInt(date.substring(5, 7));
                int d = Integer.parseInt(date.substring(8, 10));

                // 跳出日期選擇器
                DatePickerDialog pickerDialog =
                    new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker view, int year, int monthOfYear,
                            int dayOfMonth) {
                            // 完成選擇，顯示日期
                            edtDate.setText(String.format("%04d/%02d/%02d", year, (monthOfYear + 1),
                                dayOfMonth));
                        }
                    }, y, m - 1, d);
                pickerDialog.show();
            }
        };

    TextView.OnClickListener txtSourceOnClickListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            Log.d(TAG, "onClick: txtSource");
            createMutiItemDialog(readFile());
        }
    };

    private void createMutiItemDialog(final String[] items) {
        index = 0;
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                index = which;
            }
        }).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                txtSource.setText(items[index]);
            }
        }).setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                ArrayList<String> data = new ArrayList<String>();
                for (int i = 0; i < items.length; i++) {
                    if (index != i) {
                        data.add(items[i] + "\n");
                    }
                }
                writeFile(data.toArray(new String[0]));
            }
        }).setNeutralButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                createAddAccountDialog();
            }
        }).create();

        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getActivity().getResources().getColor( R.color.red));
        dialog.getButton(dialog.BUTTON_NEUTRAL).setTextColor(getActivity().getResources().getColor( R.color.bright_blue));
    }

    private void createAddAccountDialog() {
        final View view =
            LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_account, null);
        new Builder(getActivity()).setView(view)
            .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    EditText edtAccount = (EditText) view.findViewById(R.id.edtAccount);
                    if (edtAccount.getText().length() > 0) {
                        writeFile(edtAccount.getText().toString() + "\n");
                    } else {
                        Toast.makeText(getActivity(), "失敗!", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .show();

    }

    private void writeFile(String input) {
        Log.d(TAG, "writeFile: " + input);
        File file;
        try {
            ContextWrapper contextWrapper =
                new ContextWrapper(getActivity().getApplicationContext());
            File directory = contextWrapper.getDir(FILE_PATH, Context.MODE_APPEND);
            file = new File(directory, FILE_NAME);
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(input.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "writeFile: " + e.toString());
        }
    }

    private void writeFile(String[] inputs) {
        Log.d(TAG, "writeFile: " + inputs);
        File file;
        try {
            ContextWrapper contextWrapper =
                new ContextWrapper(getActivity().getApplicationContext());
            File directory = contextWrapper.getDir(FILE_PATH, Context.MODE_APPEND);
            file = new File(directory, FILE_NAME);
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            for (String input : inputs) {
                fileOutputStream.write(input.getBytes());
            }
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "writeFile: " + e.toString());
        }
    }

    private String[] readFile() {
        ArrayList<String> output = new ArrayList<>();
        File file;
        try {
            ContextWrapper contextWrapper =
                new ContextWrapper(getActivity().getApplicationContext());
            File directory = contextWrapper.getDir(FILE_PATH, Context.MODE_APPEND);
            file = new File(directory, FILE_NAME);
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fileInputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String tmp;
            while ((tmp = br.readLine()) != null) {
                output.add(tmp);
            }
            fileInputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "readFile: " + e.toString());
        }

        return output.toArray(new String[0]);
    }
}
