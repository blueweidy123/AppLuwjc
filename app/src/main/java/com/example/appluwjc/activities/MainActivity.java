package com.example.appluwjc.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appluwjc.adapters.GridViewAdapter;
import com.example.appluwjc.R;
import com.example.appluwjc.models.Tab;
import com.example.appluwjc.database.DatabaseHelper;
import com.example.appluwjc.dao.tabDAO;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    ImageView addBttn;
    ImageView settingBttn;
    ImageView clearUrl;
    ImageView homeBttn;
    ImageView imgIcon;
    TextView tabBttn;
    EditText urlInput;
    WebView webView;
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressBar progressBar;
    ConstraintLayout searchBar;
    ConstraintLayout webviewHolder;
    GridView tabHolder;

    FloatingActionsMenu readingMenu;
    FloatingActionButton exitReading;
    FloatingActionButton autoScroolUp;
    FloatingActionButton autoScroolDown;
    FloatingActionButton floatingFullScreen;
    FloatingActionButton floatingFullScreen_exit;

    private int currentTab;

    boolean readingMode =false;
    boolean doubleBackToExitPressedOnce = false;
    boolean isScrollingDown = false;
    private int interval = 50;
    private int speed = 1;
    private Handler mHandler;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String lastURL_KEY = "lastURL";

    private int mScrolledDistance = 0;
    private static final int HIDE_THRESHOLD = 450;
    private boolean mControlsVisible = true;
    private boolean isAnimating = false;

    private AlertDialog dialog = null;

    private boolean mHasToRestoreState = false;
    private float mProgressToRestore;
    private int Screen_height;
    private int Screen_width;
    int navigationBarHeight = 0;

    DatabaseHelper db = new DatabaseHelper(this);
    tabDAO tabDAO = new tabDAO(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db.createUrlTable();
//        db.insertTab("https://docln.net/truyen/14734-sau-khi-da-dua-ban-gai-ngoai-tinh-toi-bong-duoc-di-ve-nha-cung-co-gai-xinh-dep-nhat-trong-truong", "today");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;

        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        Screen_height = displayHeight + navigationBarHeight;
//        Screen_height = displayMetrics.heightPixels;
        Screen_width = displayMetrics.widthPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(Color.WHITE);
        }
        setContentView(R.layout.activity_main);

        searchBar = findViewById(R.id.searchBar);
        webviewHolder = findViewById(R.id.webviewHolder);

        readingMenu = findViewById(R.id.reading_Floating_menu);

        exitReading = findViewById(R.id.floating_exitReadingMode);
        exitReading.setOnClickListener(this::onClick);

        autoScroolUp = findViewById(R.id.floating_autoUp);
        autoScroolUp.setOnClickListener(this::onClick);

        autoScroolDown = findViewById(R.id.floating_autoDown);
        autoScroolDown.setOnClickListener(this::onClick);

        floatingFullScreen = findViewById(R.id.floating_fullScreen);
        floatingFullScreen.setOnClickListener(this::onClick);

        floatingFullScreen_exit = findViewById(R.id.floating_exit_fullScreen);
        floatingFullScreen_exit.setOnClickListener(this::onClick);

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            webView.reload();
            Handler handler = new Handler();
            handler.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1500);
        });

        webView = findViewById(R.id.webView);
        webView.getSettings().setAppCacheEnabled(true);

//        tabHolder = findViewById(R.id.tabs_grid_container);

        imgIcon = findViewById(R.id.webIcon);

        tabBttn = findViewById(R.id.tabBttn);
        tabBttn.setOnClickListener(this::onClick);

        addBttn = findViewById(R.id.addBttn);
        addBttn.setOnClickListener(this::onClick);

        settingBttn = findViewById(R.id.settingBttn);
        settingBttn.setOnClickListener(this::onClick);
        registerForContextMenu(settingBttn);

        urlInput = findViewById(R.id.url_input);
        urlInput.setOnFocusChangeListener((view, hasF) -> {
            if (hasF){
                clearUrl.setVisibility(View.VISIBLE);
                addBttn.setVisibility(View.GONE);
                tabBttn.setVisibility(View.GONE);
                settingBttn.setVisibility(View.GONE);
            }else {
                clearUrl.setVisibility(View.GONE);
                addBttn.setVisibility(View.VISIBLE);
                tabBttn.setVisibility(View.VISIBLE);
                settingBttn.setVisibility(View.VISIBLE);
            }
        });

        progressBar = findViewById(R.id.progress_bar);

        clearUrl = findViewById(R.id.clearBttn);
        clearUrl.setOnClickListener(this::onClick);

        homeBttn = findViewById(R.id.homeBttn);
        homeBttn.setOnClickListener(this::onClick);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webView.setWebViewClient(new MyWebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                if (mHasToRestoreState) {
                    mHasToRestoreState = false;
                    view.postDelayed(() -> {
                        float webviewsize = webView.getContentHeight() - webView.getTop();
                        float positionInWV = webviewsize * mProgressToRestore;
                        int positionY = Math.round(webView.getTop() + positionInWV);
                        webView.scrollTo(0, positionY);
                    }, 300);
                }
                addNewURL(url, view.getTitle());
                super.onPageFinished(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                try {
                    Bitmap logo = Bitmap.createScaledBitmap(icon, 64,64, false);
                    imgIcon.setImageBitmap(logo);
                }catch (Exception e){
                    imgIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_link_24));
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                urlInput.setText(webView.getUrl(), TextView.BufferType.EDITABLE);
                progressBar.setProgress(newProgress);
                saveURL();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int l, int t, int oldL, int oldT) {
                    if (view.getScrollY()==0){
                        if (!mControlsVisible && !isAnimating){
                            showSearchBar(searchBar);
                            mControlsVisible = true;
                        }
                    }else{
                        if (mScrolledDistance > HIDE_THRESHOLD && mControlsVisible && !isAnimating){
                            hideSearchBar(searchBar);
                            mControlsVisible = false;
                            mScrolledDistance = 0;
                        }else if (mScrolledDistance < -HIDE_THRESHOLD && !mControlsVisible && !isAnimating){
                            showSearchBar(searchBar);
                            mControlsVisible = true;
                            mScrolledDistance = 0;
                        }
                    }
                    if((mControlsVisible && t-oldT > 0) || (!mControlsVisible && t-oldT < 0)){
                        mScrolledDistance += (t-oldT);
                    }
                }
            });
        }

        urlInput.setOnEditorActionListener((textView, i, keyEvent) -> {
            if(i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_DONE){
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
                loadURL(urlInput.getText().toString());
                urlInput.clearFocus();
                saveURL();
                return true;
            }
            return false;
        });

        mHandler = new Handler();
        loadLastURL();
//        translateSearchBar();

    }

    void loadLastURL(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String last_URL = sharedPreferences.getString(lastURL_KEY, "");

            loadURL(last_URL);
            urlInput.setText(last_URL, TextView.BufferType.EDITABLE);

        OrientationChangeData data = (OrientationChangeData) getLastNonConfigurationInstance();
        if (data != null) {
            mHasToRestoreState = true;
            mProgressToRestore = data.mProgress;
        }
    }

    void loadURL(String url){
        boolean matchURL = Patterns.WEB_URL.matcher(url).matches();
        if(matchURL){
            webView.loadUrl(url);
            urlInput.setText(url, TextView.BufferType.EDITABLE);
        }else {
            webView.loadUrl("google.com/search?q="+url);
            urlInput.setText("google.com/search?q="+url, TextView.BufferType.EDITABLE);
        }
    }

    void translateSearchBar(){
        try {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                webviewHolder.setLayoutParams(new ConstraintLayout.LayoutParams(Screen_width, Screen_height));
                webviewHolder.animate()
                        .translationYBy(searchBar.getMeasuredHeight())
//                        .scaleY((Screen_height-searchBar.getMeasuredHeight()*2)/Screen_height)
//                        .scaleY(f)
                        .setDuration(100);

            }, 100);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void goFullScreen(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    );
        }
    }

    void exitFullScreen(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    void scrollDown(){
        webView.post(() -> {
            if(webView.getContentHeight() * webView.getScale() >= webView.getScrollY()){
                webView.scrollBy(0,speed);
            }
        });
    }

    Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                scrollDown();
            }finally {
                    mHandler.postDelayed(statusChecker, interval);
            }
        }
    };

    void autoScroolDown(){
        statusChecker.run();
    }

    void stopAutoScrool(){
        mHandler.removeCallbacks(statusChecker);
    }

    void readingMode(){
        if(!readingMode){
            goFullScreen();
            readingMenu.setVisibility(View.VISIBLE);
            readingMode=true;
        }else{
            exitFullScreen();
            readingMenu.setVisibility(View.GONE);
            readingMode = false;
        }
    }

    public void showSearchBar(ConstraintLayout searchBar){
        searchBar.animate()
                .translationYBy(searchBar.getHeight())
                .setDuration(300)
                .alpha(1.0f)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        isAnimating=true;
                        progressBar.animate()
                                .translationYBy(searchBar.getMeasuredHeight())
                                .setDuration(300);
                        webviewHolder.animate()
//                                .translationYBy(searchBar.getMeasuredHeight())
//                                .scaleY((Screen_height-searchBar.getMeasuredHeight())/Screen_height)
                                .setDuration(300);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        isAnimating=false;
                        mControlsVisible = true;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }


    public void hideSearchBar(ConstraintLayout searchBar){
        searchBar.animate()
                .translationYBy(-searchBar.getHeight())
                .setDuration(300)
                .alpha(0.0f)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        isAnimating=true;
                        progressBar.animate()
                                .translationYBy(-searchBar.getMeasuredHeight())
                                .setDuration(300);
                        webviewHolder.animate()
//                                .translationYBy(-searchBar.getMeasuredHeight())
//                                .scaleY(1)
                                .setDuration(300);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        isAnimating=false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });

    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.homeBttn:
                loadURL("google.com");
                break;
            case R.id.clearBttn:
                urlInput.setText("");
                break;
            case R.id.addBttn:
                addNewTab();
                break;
            case R.id.settingBttn:
                showMenu(view);
                break;
            case R.id.tabBttn:
                ArrayList<Tab> tabs = tabDAO.getAllTabs();
                showDialog(tabs);
                break;
            case R.id.floating_exitReadingMode:
                readingMode();
                break;
            case R.id.floating_autoUp:
                if (isScrollingDown){
                    speed -= 1;
                }
                if (speed <= 0){
                    stopAutoScrool();
                    isScrollingDown = false;
                    speed = 0;
                }
                break;
            case R.id.floating_autoDown:
                if(!isScrollingDown){
                    autoScroolDown();
                    isScrollingDown = true;
                }else {
                    speed += 1;
                }
                break;
            case R.id.floating_fullScreen:
                goFullScreen();
                break;
            case R.id.floating_exit_fullScreen:
                exitFullScreen();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return true;
    }

    public void showMenu(View v){
        PopupMenu settingMenu = new PopupMenu(MainActivity.this, v);
        settingMenu.getMenuInflater().inflate(R.menu.setting_menu, settingMenu.getMenu());
        settingMenu.setOnMenuItemClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            settingMenu.setForceShowIcon(true);
        }
        settingMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.reading_Mode:
                readingMode();
                return true;
            case R.id.setting_App:
                Toast.makeText(this, "COMING SOON!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.fullScreen:
                goFullScreen();
                return true;
            case R.id.exitFullScreen:
                exitFullScreen();
                return true;
            case R.id.history:
                Toast.makeText(this, "COMING SOON!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.test_in4:
                Toast.makeText(this, "W: "+Screen_width+" H: "+Screen_height+ " SB: "+searchBar.getMeasuredHeight(), Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void showDialog(ArrayList<Tab> tabs){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this, R.style.tabs_dialog);
        View mView = getLayoutInflater().inflate(R.layout.tab_dialog, null);
        mBuilder.setView(mView);
        dialog = mBuilder.create();

        GridView tabHolder = mView.findViewById(R.id.tabs_grid_container);
        tabHolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Tab t = (Tab) adapterView.getAdapter().getItem(i);
                loadURL(t.getTitle());
                Toast.makeText(MainActivity.this, t.getTitle(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });


        GridViewAdapter adapter = new GridViewAdapter(this, tabs);

        tabHolder.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        Button newTab = mView.findViewById(R.id.tabs_newTab_Button);
        newTab.setOnClickListener(view -> {
            addNewTab();
            dialog.dismiss();
        });
        ImageView setting = mView.findViewById(R.id.tabs_more_Button);
        
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "setting", Toast.LENGTH_SHORT).show();
//                tabDAO.updateUrl(tabs.get(0).getTabID(),"name");
            }
        });

        dialog.show();
    }

    public void addNewTab(){
        GridViewAdapter adapter = new GridViewAdapter(this, tabDAO.getAllTabs());
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = dateFormat.format(currentDate);
        db.insertTab("new tab","new tab" ,dateString);
        adapter.notifyDataSetChanged();
    }

    public void addNewURL(String url, String title){
        GridViewAdapter adapter = new GridViewAdapter(this, tabDAO.getAllTabs());
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = dateFormat.format(currentDate);
        db.insertTab(url, title, dateString);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if(urlInput.hasFocus()){
            urlInput.clearFocus();
        }else{
            if(webView.canGoBack()){
                webView.goBack();
            }else{
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2000);
            }
        }
    }

    void saveURL(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(lastURL_KEY, webView.getUrl().toString().trim());
        editor.apply();
    }

    private float calculateProgression(WebView content) {
        float positionTopView = content.getTop();
        float contentHeight = content.getContentHeight();
        float currentScrollPosition = content.getScrollY();
        float percentWebview = (currentScrollPosition - positionTopView) / contentHeight;
        return percentWebview;
    }

    @Nullable
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        OrientationChangeData objectToSave = new OrientationChangeData();
        objectToSave.mProgress = calculateProgression(webView);
        return objectToSave;
    }

    private final static class OrientationChangeData {
        public float mProgress;
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onDestroy() {
        stopAutoScrool();
        super.onDestroy();
    }
}