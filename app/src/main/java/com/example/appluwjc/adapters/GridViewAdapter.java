package com.example.appluwjc.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.appluwjc.R;
import com.example.appluwjc.database.DatabaseHelper;
import com.example.appluwjc.models.Tab;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GridViewAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<Tab> tabs;
    DatabaseHelper db;

    ImageView clearTab;


    public GridViewAdapter(Context context, ArrayList<Tab> tabs) {
        this.context = context;
        this.tabs = tabs;
        db = new DatabaseHelper(context);
    }



    @Override
    public int getCount() {
        return tabs.size();
    }

    @Nullable
    @Override
    public Tab getItem(int position) {
        return tabs.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.single_tab_layout, null);
        }

//        ImageView img = convertView.findViewById(R.id.small_tab_icon);
        TextView title = (TextView) view.findViewById(R.id.tab_Title);
//        CardView webholder = convertView.findViewById(R.id.tab_container);
        Tab tab = getItem(position);
//        img.setImageBitmap(getBitmapFromURL(tab.getURL()));
//        title.setText(getWebTitle(tab.getURL()));

//        title.setText(tab.getURL());
        title.setText(tab.getURL());

        clearTab = view.findViewById(R.id.clear_tab);
        long itemID = getItemId(position);
        clearTab.setTag(itemID);
        clearTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearTab(position);
            }
        });

        return view;
    }

    public String getWebTitle(String url) {
        String title = "title";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.example.com/")
                .build();

        try {
            Response response = client.newCall(request).execute();
            String html = response.body().string();
            Document document = Jsoup.parse(html);
            return title = document.title();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return title;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    void clearTab(int pos){
        Tab t = getItem(pos);
        db.delete(getItem(pos).getTabID());
        tabs.remove(t);
        notifyDataSetChanged();
    }
}
