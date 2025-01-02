package com.asass.tyflexapp;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String URL_LOGIN = "https://amofunny.com/login/";
    private static final String URL_SITE = "https://amofunny.com/";
    private static final String PREFS_NAME = "Machadofilho19@gmail.com";
    private static final String KEY_LAST_URL = "787878";

    private LinearLayout loginLayout;
    private Button buttonBack;
    private FrameLayout fullScreenContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private boolean isFullScreen = false;
    private Button emergencyButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupWindowInsets();

        webView = findViewById(R.id.webV);
        loginLayout = findViewById(R.id.loginLayout);
        buttonBack = findViewById(R.id.button_back);
        fullScreenContainer = findViewById(R.id.frameLayout);
        emergencyButton = findViewById(R.id.button_SOS);

        emergencyButton.setVisibility(View.GONE);
        initializeWebView();

        buttonBack.setOnClickListener(v -> webView.loadUrl(URL_SITE));

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastUrl = preferences.getString(KEY_LAST_URL, URL_LOGIN);
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(lastUrl);
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setDomStorageEnabled(true);
        webViewSettings.setUseWideViewPort(true);
        webViewSettings.setLoadWithOverviewMode(true);

        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient());
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            webView.loadData("Erro ao carregar a página. Verifique sua conexão.", "text/html", "UTF-8");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            updateUIBasedOnUrl(url);
            saveLastUrl(url);
        }
    }

    private void updateUIBasedOnUrl(String url) {
        if (url.equals(URL_LOGIN)) {
            buttonBack.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
            emergencyButton.setVisibility(View.GONE);
        } else {
            loginLayout.setVisibility(View.GONE);
            buttonBack.setVisibility(View.VISIBLE);
            emergencyButton.setVisibility(url.contains("player") ? View.VISIBLE : View.GONE);
            buttonBack.setVisibility(url.contains("player") ? View.GONE : View.VISIBLE);

            if (url.contains("player")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                enterFullScreen();
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                exitFullScreen();
            }
        }
    }

    private void saveLastUrl(String url) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_LAST_URL, url);
        editor.apply();
    }

    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (customView != null) {
                callback.onCustomViewHidden();
                return;
            }
            customView = view;
            customViewCallback = callback;
            fullScreenContainer.addView(view);
            fullScreenContainer.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            enterFullScreen();
            isFullScreen = true;
        }

        @Override
        public void onHideCustomView() {
            if (customView == null) return;
            fullScreenContainer.removeView(customView);
            fullScreenContainer.setVisibility(View.GONE);
            customView = null;
            customViewCallback.onCustomViewHidden();
            webView.setVisibility(View.VISIBLE);
            exitFullScreen();
            isFullScreen = false;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    private void enterFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

    private void exitFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void copyEmail(View view) {
        copyToClipboard(((TextView) findViewById(R.id.emailTextView)).getText().toString().replace("E-mail: ", ""), "E-mail copiado");
    }

    public void copyPassword(View view) {
        copyToClipboard(((TextView) findViewById(R.id.senhaTextView)).getText().toString().replace("Senha: ", ""), "Senha copiada");
    }

    private void copyToClipboard(String text, String toastMessage) {
        ClipData clip = ClipData.newPlainText("copiedText", text);
        ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(clip);
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    public void emergencyButton(View view) {
        if (!isFullScreen) {
            webView.loadUrl(URL_SITE);
        }
    }
}
