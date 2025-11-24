package com.neonstack.underground;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main entry point for the NEON STACK UNDERGROUND WebView wrapper.
 * Loads the bundled index.html from the assets directory and enforces a fully immersive,
 * landscape-only experience optimized for local HTML5 content.
 */
public class MainActivity extends Activity {

    private static final String LOCAL_APP_URL = "file:///android_asset/index.html";

    private WebView webView;
    private FrameLayout rootContainer;
    private View errorOverlay;

    @Override
    @SuppressLint({"SetJavaScriptEnabled"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        rootContainer = new FrameLayout(this);
        webView = new WebView(this);
        errorOverlay = buildErrorOverlay();

        configureWebView(webView);

        rootContainer.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        rootContainer.addView(errorOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(rootContainer);
        applyImmersiveMode();
        loadApp(webView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
        applyImmersiveMode();
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersiveMode();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            rootContainer.removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private void loadApp(WebView view) {
        view.loadUrl(LOCAL_APP_URL);
    }

    private void configureWebView(WebView view) {
        view.setVerticalScrollBarEnabled(false);
        view.setHorizontalScrollBarEnabled(false);
        view.setOverScrollMode(View.OVER_SCROLL_NEVER);
        view.setLongClickable(false);
        view.setHapticFeedbackEnabled(true);
        view.setOnLongClickListener(v -> true); // Disable context menus for a kiosk-like feel.

        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                // Keep navigation inside the WebView for bundled assets; dispatch external schemes.
                if (Uri.parse(LOCAL_APP_URL).getScheme().equals(uri.getScheme())) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                showErrorOverlay(description);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                showErrorOverlay(error.getDescription().toString());
            }
        });

        view.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
                result.confirm();
                return true; // Consume to avoid default modal dialogs.
            }
        });
    }

    private void applyImmersiveMode() {
        final int flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private View buildErrorOverlay() {
        TextView errorView = new TextView(this);
        errorView.setText("Connection issue. Please relaunch the experience.");
        errorView.setTextSize(18f);
        errorView.setTextColor(0xFFFFFFFF);
        errorView.setBackgroundColor(0xCC000000);
        errorView.setPadding(32, 32, 32, 32);
        errorView.setVisibility(View.GONE);
        errorView.setGravity(android.view.Gravity.CENTER);
        return errorView;
    }

    private void showErrorOverlay(String description) {
        if (errorOverlay instanceof TextView) {
            ((TextView) errorOverlay).setText(
                    String.format("Connection issue: %s. Please relaunch the experience.", description));
        }
        errorOverlay.setVisibility(View.VISIBLE);
    }
}
