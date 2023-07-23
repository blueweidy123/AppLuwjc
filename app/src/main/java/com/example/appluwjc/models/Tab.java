package com.example.appluwjc.models;


public class Tab {

    int tabID;
    String URL;
    String title;
    String date;

//    Boolean Incognito;


    public Tab(String URL) {
        this.URL = URL;
    }

    public Tab(int tabID, String URL, String title, String date) {
        this.tabID = tabID;
        this.URL = URL;
        this.title = title;
        this.date = date;
    }

    public int getTabID() {
        return tabID;
    }

    public void setTabID(int tabID) {
        this.tabID = tabID;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Tab{" +
                "tabID='" + tabID + '\'' +
                ", URL='" + URL + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
