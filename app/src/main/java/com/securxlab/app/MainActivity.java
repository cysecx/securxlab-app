package com.securxlab.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/**
 * Securxlab app: a full-screen window onto your live dashboard. It holds no dashboard code of its own,
 * so every change you make to the website shows up here the next time the app opens.
 */
public class MainActivity extends Activity {
    private WebView web;
    private PermissionRequest pendingMic;
    private static final int REQ_MIC = 7;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#060A10"));
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#060A10"));
        root.addView(web, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
        hideBars();

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUserAgentString(s.getUserAgentString() + " SecurxlabApp/1.0"
                + (getPackageManager().hasSystemFeature("android.software.leanback") ? " SecurxlabTV/1.0" : ""));
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) { return false; }
            @Override public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) {
                    v.loadData("<html><body style='margin:0;background:#060a10;color:#e6f1ff;font-family:sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;text-align:center'>"
                        + "<div><h2>No connection</h2><p style='color:#8195ae'>Securxlab can't be reached.<br>Check your home network or VPN.</p>"
                        + "<p style='color:#8195ae'>Press back, then open the app again.</p></div></body></html>", "text/html", "utf-8");
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(final PermissionRequest req) {
                runOnUiThread(() -> {
                    boolean wantsMic = false;
                    for (String r : req.getResources()) if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r)) wantsMic = true;
                    if (!wantsMic) { req.deny(); return; }
                    if (Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        req.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                    } else {
                        pendingMic = req;
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_MIC);
                    }
                });
            }
        });
        if (b != null) web.restoreState(b); else web.loadUrl(BuildConfig.DASH_URL);
    }

    @Override public void onRequestPermissionsResult(int code, String[] p, int[] g) {
        super.onRequestPermissionsResult(code, p, g);
        if (code == REQ_MIC && pendingMic != null) {
            if (g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) pendingMic.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
            else pendingMic.deny();
            pendingMic = null;
        }
    }

    private void hideBars() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) hideBars(); }
    @Override protected void onSaveInstanceState(Bundle o) { super.onSaveInstanceState(o); web.saveState(o); }
    @Override protected void onResume() { super.onResume(); web.onResume(); }
    @Override protected void onPause() { web.onPause(); super.onPause(); }
    @Override public void onBackPressed() {
        // let the dashboard handle Back first (close an enlarged camera, leave monitor mode)
        web.evaluateJavascript("(function(){try{return window.__tvBack?window.__tvBack()===true:false}catch(e){return false}})()",
            v -> { if ("true".equals(v)) return; if (web.canGoBack()) web.goBack(); else super.onBackPressed(); });
    }
    @Override protected void onDestroy() { web.destroy(); super.onDestroy(); }
}
