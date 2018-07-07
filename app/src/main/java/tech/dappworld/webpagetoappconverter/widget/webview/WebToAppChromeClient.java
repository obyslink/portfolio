package tech.dappworld.webpagetoappconverter.widget.webview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import tech.dappworld.webpagetoappconverter.Config;
import tech.dappworld.webpagetoappconverter.R;
import tech.dappworld.webpagetoappconverter.activity.MainActivity;
import tech.dappworld.webpagetoappconverter.widget.AdvancedWebView;

/**
 * Created by imac on 05-04-16.
 */
public class WebToAppChromeClient extends WebChromeClient {

    protected Activity activity;
    protected FrameLayout container;
    protected WebView popupView;

    protected AdvancedWebView browser;
    public SwipeRefreshLayout swipeLayout;
    public ProgressBar progressBar;

    public View mCustomView;
    public WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalSystemUiVisibility;
    private int mOriginalOrientation;

    public WebToAppChromeClient(
            Activity activity,
            FrameLayout container,
            AdvancedWebView browser,
            SwipeRefreshLayout swipeLayout,
            ProgressBar progressBar)
    {
        super();
        this.activity = activity;
        this.container = container;
        this.browser = browser;
        this.swipeLayout = swipeLayout;
        this.progressBar = progressBar;
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {

        this.browser.setVisibility(WebView.GONE);

        this.popupView = new WebView(this.activity);

        // setup popuview and add
        this.popupView.getSettings().setJavaScriptEnabled(true);
        this.popupView.setWebChromeClient(this);
        this.popupView.setWebViewClient(new WebToAppWebClient(this.activity, popupView));
        this.popupView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT
        ));
        this.container.addView(this.popupView);

        // send popup window infos back to main (cross-document messaging)
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(popupView);
        resultMsg.sendToTarget();

        return true;
    }

    // remove new added webview on close
    @Override
    public void onCloseWindow(WebView window) {
        this.popupView.setVisibility(WebView.GONE);
        this.browser.setVisibility(WebView.VISIBLE);
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        if (Config.LOAD_AS_PULL && swipeLayout != null){
            swipeLayout.setRefreshing(true);
            if (progress == 100)
                swipeLayout.setRefreshing(false);
        } else {
            progressBar.setProgress(0);

            progressBar.setVisibility(View.VISIBLE);

            progressBar.setProgress(progress);

            progressBar.incrementProgressBy(progress);

            if (progress > 99) {
                progressBar.setVisibility(View.GONE);

                if (swipeLayout != null && swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                }
            }
        }
    }

    @Override
    public void onPermissionRequest(PermissionRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            request.grant(request.getResources());
        }
    }

    // Setting the title
    @Override
    public void onReceivedTitle(WebView view, String title) {
        ((MainActivity) activity).setTitle(browser.getTitle());
    }

    @SuppressWarnings("unused")
    @Override
    public Bitmap getDefaultVideoPoster() {
        if (activity == null) {
            return null;
        }

        return BitmapFactory.decodeResource(activity
                        .getApplicationContext().getResources(),
                R.mipmap.vert_loading);
    }

    @SuppressLint("InlinedApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onShowCustomView(View view,
                                 WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (mCustomView != null) {
            onHideCustomView();
            return;
        }

        // 1. Stash the current state
        mCustomView = view;
        mCustomView.setBackgroundColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mOriginalSystemUiVisibility = activity.getWindow()
                    .getDecorView().getSystemUiVisibility();
        }
        mOriginalOrientation = activity.getRequestedOrientation();

        // 2. Stash the custom view callback
        mCustomViewCallback = callback;

        // 3. Add the custom view to the view hierarchy
        FrameLayout decor = (FrameLayout) activity.getWindow()
                .getDecorView();
        decor.addView(mCustomView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // 4. Change the state of the window
        activity
                .getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
        activity
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onHideCustomView() {
        // 1. Remove the custom view
        FrameLayout decor = (FrameLayout) activity.getWindow()
                .getDecorView();
        decor.removeView(mCustomView);
        mCustomView = null;

        // 2. Restore the state to it's original form
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            activity.getWindow().getDecorView()
                    .setSystemUiVisibility(mOriginalSystemUiVisibility);
        }
        activity.setRequestedOrientation(mOriginalOrientation);

        // 3. Call the custom view callback
        mCustomViewCallback.onCustomViewHidden();
        mCustomViewCallback = null;

    }

    public View getCustomView(){
        return mCustomView;
    }

    public CustomViewCallback getCustomViewCallback(){
        return mCustomViewCallback;
    }

}