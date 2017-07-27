package com.example.yuan.finance.items;

/**
 * Created by YUAN on 2017/04/12.
 */

public class Expense_item {

    private String date;
    private int amount;
    private String comment;

    public Expense_item(String date, int amount, String comment){
        this.date = date;
        this.amount = amount;
        this.comment = comment;
    }

    //public void setDate(String date){
    //    this.date = date;
    //}
    //
    //public void setAmount(int amount){
    //    this.amount = amount;
    //}
    //
    //public void setComment(String comment) {
    //    this.comment = comment;
    //}

    public int getAmount() {
        return amount;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }
}
