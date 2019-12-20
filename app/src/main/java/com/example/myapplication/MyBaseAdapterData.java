package com.example.myapplication;
/*
    Author: Alan Huang
*/

public class MyBaseAdapterData {
    private int itemIcon;
    private String itemTitle;
    private String itemSubtitle;

    public MyBaseAdapterData(int icon, String title, String subtitle){
        this.itemIcon=icon;
        this.itemTitle=title;
        this.itemSubtitle=subtitle;
    }
    public int getItemIcon(){
        return this.itemIcon;
    }
    public void setItemIcon(int icon){
        this.itemIcon=icon;
    }
    public String getItemTitle(){
        return this.itemTitle;
    }
    public void setItemTitle(String title){
        this.itemTitle=title;
    }
    public String getItemSubtitle(){
        return this.itemSubtitle;
    }
    public void setItemSubtitle(String subtitle){
        this.itemSubtitle=subtitle;
    }
}
