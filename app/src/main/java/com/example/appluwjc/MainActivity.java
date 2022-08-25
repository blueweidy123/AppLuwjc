package com.example.appluwjc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    ImageView addBttn;
    ImageView settingBttn;
    ImageView clearUrl;
    ImageView homeBttn;
    TextView tabBttn;
    EditText urlInput;
    WebView webView;
    ProgressBar progressBar;

    FloatingActionsMenu readingMenu;
    FloatingActionButton exitReading;
    FloatingActionButton autoScroolUp;
    FloatingActionButton autoScroolDown;
    FloatingActionButton floatingFullScreen;
    FloatingActionButton floatingFullScreen_exit;

    boolean readingMode =false;
    boolean doubleBackToExitPressedOnce = false;
    boolean isScrollingDown = false;
    private int interval = 50;
    private int speed = 1;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(Color.WHITE);
        }
        setContentView(R.layout.activity_main);

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

        webView = findViewById(R.id.webView);

        tabBttn = findViewById(R.id.tabBttn);
        tabBttn.setOnClickListener(this::onClick);

        addBttn = findViewById(R.id.addBttn);
        addBttn.setOnClickListener(this::onClick);

        settingBttn = findViewById(R.id.settingBttn);
        settingBttn.setOnClickListener(this::onClick);
        registerForContextMenu(settingBttn);

        urlInput = findViewById(R.id.url_input);
        urlInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasF) {
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

        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }
        });

        loadURL("google.com");

        urlInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_DONE){
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
                    loadURL(urlInput.getText().toString());
                    urlInput.clearFocus();
                    return true;
                }
                return false;
            }
        });

        mHandler = new Handler();

    }

    void loadURL(String url){
        boolean matchURL = Patterns.WEB_URL.matcher(url).matches();
        if(matchURL){
            webView.loadUrl(url);
        }else {
            webView.loadUrl("google.com/search?q="+url);
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
        webView.post(new Runnable() {
            @Override
            public void run() {
                if(webView.getContentHeight() * webView.getScale() >= webView.getScrollY()){
                    webView.scrollBy(0,speed);
                }
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
                Toast.makeText(this, "On CONSTRUCTION", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settingBttn:
                showMenu(view);
                break;
            case R.id.tabBttn:
                Toast.makeText(this, "ON CONSTRUCTION!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "setting", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.fullScreen:
                goFullScreen();
                return true;
            case R.id.exitFullScreen:
                exitFullScreen();
                return true;
            case R.id.history:
                Toast.makeText(this, "history", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoScrool();
    }
}