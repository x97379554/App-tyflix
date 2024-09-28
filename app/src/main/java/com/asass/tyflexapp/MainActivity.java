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
    private final String urldologin = "https://amofunny.com/login/";
    private final String urldosite = "https://amofunny.com/";
    private static final String PLAYER = "player";
    private LinearLayout loginLayout;
    private Button button_back;

    private static final String PREFS_NAME = "MJ2620775@GMAIL.COM";
    private static final String KEY_LAST_URL = "1010aa";

    private FrameLayout fullScreenContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private boolean isFullScreen = false;

    //Button de emergência
    private Button emergencyButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left, insets.getInsets(WindowInsetsCompat.Type.systemBars()).top, insets.getInsets(WindowInsetsCompat.Type.systemBars()).right, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        // Inicializa o WebView
        webView = findViewById(R.id.webV);
        loginLayout = findViewById(R.id.loginLayout);
        button_back = findViewById(R.id.button_back);
        fullScreenContainer = findViewById(R.id.frameLayout);
        emergencyButton = findViewById(R.id.button_SOS);

        emergencyButton.setVisibility(View.GONE);

        initializeWebView();
        button_back.setOnClickListener(v -> webView.loadUrl(urldosite));

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastUrl = preferences.getString(KEY_LAST_URL, urldologin);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(lastUrl);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setDomStorageEnabled(true);
        webViewSettings.setUseWideViewPort(true);
        webViewSettings.setLoadWithOverviewMode(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.loadData("Erro ao carregar a página. Verifique sua conexão.", "text/html", "UTF-8");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.equals(urldologin)) {
                    button_back.setVisibility(View.GONE);
                    loginLayout.setVisibility(View.VISIBLE);
                    emergencyButton.setVisibility(View.GONE);
                } else {
                    loginLayout.setVisibility(View.GONE);
                    button_back.setVisibility(View.VISIBLE);
                    emergencyButton.setVisibility(View.GONE);



                }

                if (url.contains(PLAYER)) {
                    button_back.setVisibility(View.GONE);
                    emergencyButton.setVisibility(View.VISIBLE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    if (!isFullScreen) {
                        setFullScreen();
                    }
                } else {
                    button_back.setVisibility(View.GONE);
                    emergencyButton.setVisibility(View.GONE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    if (isFullScreen) {
                        exitFullScreen();
                    }
                }

                // Salva a URL atual no SharedPreferences
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(KEY_LAST_URL, url);
                editor.apply();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                // Entrando no modo tela cheia
                customView = view;
                customViewCallback = callback;
                fullScreenContainer.addView(view);
                fullScreenContainer.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                setFullScreen();
                isFullScreen = true;
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) {
                    return;
                }

                // Saindo do modo tela cheia
                fullScreenContainer.removeView(customView);
                fullScreenContainer.setVisibility(View.GONE);
                customView = null;
                customViewCallback.onCustomViewHidden();
                webView.setVisibility(View.VISIBLE);
                exitFullScreen();
                isFullScreen = false;
            }
        });
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

    private void setFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

    private void exitFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    // Métodos para copiar o e-mail e senha
    public void copyEmail(View view) {
        TextView emailText = findViewById(R.id.emailTextView);
        String email = emailText.getText().toString().replace("E-mail: ", "");
        copyToClipboard(email);
        Toast.makeText(this, "E-mail copiado", Toast.LENGTH_SHORT).show();
    }

    public void copyPassword(View view) {
        TextView passwordText = findViewById(R.id.senhaTextView);
        String password = passwordText.getText().toString().replace("Senha: ", "");
        copyToClipboard(password);
        Toast.makeText(this, "Senha copiada", Toast.LENGTH_SHORT).show();
    }

    // Método auxiliar para copiar texto para a área de transferência
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copiedText", text);
        clipboard.setPrimaryClip(clip);
    }

    //Botão de emergênciaSe não tiver na tela cheia
    // Eu quero te ver esse botão Esse botão ele vai servir para voltar para a tela inicial do site quer dizer
    public void emergencyButton(View view) {

        if (!isFullScreen) {
            webView.loadUrl(urldosite);
        }
    }

}






