package tech.dappworld.webpagetoappconverter.activity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import tech.dappworld.webpagetoappconverter.App;
import tech.dappworld.webpagetoappconverter.Config;
import tech.dappworld.webpagetoappconverter.R;
import tech.dappworld.webpagetoappconverter.drawer.DrawerFragment;
import tech.dappworld.webpagetoappconverter.widget.SwipeableViewPager;
import tech.dappworld.webpagetoappconverter.widget.webview.WebToAppWebClient;
import com.tjeannin.apprate.AppRate;

import tech.dappworld.webpagetoappconverter.adapter.NavigationAdapter;
import tech.dappworld.webpagetoappconverter.fragment.WebFragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.MenuInflater;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements DrawerFragment.DrawerFragmentListener{

    //Views
    public Toolbar mToolbar;
    public View mHeaderView;
    public TabLayout mSlidingTabLayout;
    public SwipeableViewPager mViewPager;

    //App Navigation Structure
    private NavigationAdapter mAdapter;
    private DrawerFragment drawerFragment;

    private WebFragment CurrentAnimatingFragment = null;
    private int CurrentAnimation = 0;

    //Identify toolbar state
    private static int NO = 0;
    private static int HIDING = 1;
    private static int SHOWING = 2;

    //Keep track of the interstitials we show
    private int interstitialCount = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mHeaderView = (View) findViewById(R.id.header_container);
        mSlidingTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (SwipeableViewPager) findViewById(R.id.pager);

        setSupportActionBar(mToolbar);

        mAdapter = new NavigationAdapter(getSupportFragmentManager(), this);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            String data = intent.getDataString();
            ((App) getApplication()).setPushUrl(data);
        }

        if (Config.HIDE_ACTIONBAR)
            getSupportActionBar().hide();

        if (getHideTabs())
            mSlidingTabLayout.setVisibility(View.GONE);

        hasPermissionToDo(this, Config.PERMISSIONS_REQUIRED);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mViewPager.getLayoutParams();
        if ((Config.HIDE_ACTIONBAR && getHideTabs()) || ((Config.HIDE_ACTIONBAR || getHideTabs()) && getCollapsingActionBar())){
            lp.topMargin = 0;
        } else if ((Config.HIDE_ACTIONBAR || getHideTabs()) || (!Config.HIDE_ACTIONBAR && !getHideTabs() && getCollapsingActionBar())){
            lp.topMargin = getActionBarHeight();
        } else if (!Config.HIDE_ACTIONBAR && !getHideTabs()){
            lp.topMargin = getActionBarHeight() * 2;
        }

        if (Config.USE_DRAWER) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            drawerFragment = (DrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
            drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
            drawerFragment.setDrawerListener(this);
        } else {
            ((DrawerLayout) findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        mViewPager.setLayoutParams(lp);

        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(mViewPager.getAdapter().getCount() - 1);

        mSlidingTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.accent));
        mSlidingTabLayout.setupWithViewPager(mViewPager);
        mSlidingTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (getCollapsingActionBar()) {
                    showToolbar(getFragment());
                }
                mViewPager.setCurrentItem(tab.getPosition());
                showInterstitial();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        for (int i = 0; i < mSlidingTabLayout.getTabCount(); i++) {
            if (Config.ICONS.length > i  && Config.ICONS[i] != 0) {
                mSlidingTabLayout.getTabAt(i).setIcon(Config.ICONS[i]);
            }
        }

        // admob
        if (!getResources().getString(R.string.ad_banner_id).equals("")) {
            // Look up the AdView as a resource and load a request.
            AdView adView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            AdView adView = (AdView) findViewById(R.id.adView);
            adView.setVisibility(View.GONE);
        }

        // application rating
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.rate_title))
                .setMessage(String.format(getString(R.string.rate_message), getString(R.string.app_name)))
                .setPositiveButton(getString(R.string.rate_yes), null)
                .setNegativeButton(getString(R.string.rate_never), null)
                .setNeutralButton(getString(R.string.rate_later), null);

        new AppRate(this)
                .setShowIfAppHasCrashed(false)
                .setMinDaysUntilPrompt(2)
                .setMinLaunchesUntilPrompt(2)
                .setCustomDialog(builder)
                .init();

        // showing the splash screen
        if (Config.SPLASH) {
            findViewById(R.id.imageLoading1).setVisibility(View.VISIBLE);
            //getFragment().browser.setVisibility(View.GONE);
        }
    }

    // using the back button of the device
    @Override
    public void onBackPressed() {
        View customView = null;
        WebChromeClient.CustomViewCallback customViewCallback = null;
        if (getFragment().chromeClient != null) {
            customView = getFragment().chromeClient.getCustomView();
            customViewCallback = getFragment().chromeClient.getCustomViewCallback();
        }

        if ((customView == null)
                && getFragment().browser.canGoBack()) {
            getFragment().browser.goBack();
        } else if (customView != null
                && customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        //Adjust menu item visibility/availability based on settings
        if (Config.HIDE_MENU_SHARE) {
            menu.findItem(R.id.share).setVisible(false);
        }
        if (Config.HIDE_MENU_NAVIGATION){
            menu.findItem(R.id.previous).setVisible(false);
            menu.findItem(R.id.next).setVisible(false);
        }
        if (!Config.SHOW_NOTIFICATION_SETTINGS || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            menu.findItem(R.id.notification_settings).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        WebView browser = getFragment().browser;
        if (item.getItemId() == (R.id.next)) {
            browser.goForward();
            return true;
        } else if (item.getItemId() == R.id.previous) {
            browser.goBack();
            return true;
        } else if (item.getItemId() == R.id.share) {
            getFragment().shareURL();
            return true;
        } else if (item.getItemId() == R.id.about) {
            AboutDialog();
            return true;
        } else if (item.getItemId() == R.id.home) {
            browser.loadUrl(getFragment().mainUrl);
            return true;
        } else if (item.getItemId() == R.id.close) {
            finish();
            Toast.makeText(getApplicationContext(),
                    getText(R.string.exit_message), Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.notification_settings){
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // showing about dialog
    private void AboutDialog() {
        // setting the dialogs text, and making the links clickable
        final TextView message = new TextView(this);
        // i.e.: R.string.dialog_message =>
        final SpannableString s = new SpannableString(
                this.getText(R.string.dialog_about));
        Linkify.addLinks(s, Linkify.WEB_URLS);
        message.setTextSize(15f);
        message.setPadding(20, 15, 15, 15);
        message.setText(Html.fromHtml(getString(R.string.dialog_about)));
        message.setMovementMethod(LinkMovementMethod.getInstance());

        // creating the actual dialog

        AlertDialog.Builder AlertDialog = new AlertDialog.Builder(this);
        AlertDialog.setTitle(Html.fromHtml(getString(R.string.about)))
                // .setTitle(R.string.about)
                .setCancelable(true)
                // .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("ok", null).setView(message).create().show();
    }

    public void setTitle(String title) {
        if (mAdapter != null && mAdapter.getCount() == 1 && !Config.USE_DRAWER && !Config.STATIC_TOOLBAR_TITLE)
            getSupportActionBar().setTitle(title);
    }

    public WebFragment getFragment(){
        return (WebFragment) mAdapter.getCurrentFragment();
    }

    public void hideSplash() {
        if (Config.SPLASH) {
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    // hide splash image
                    if (findViewById(R.id.imageLoading1).getVisibility() == View.VISIBLE) {
                        findViewById(R.id.imageLoading1).setVisibility(
                                View.GONE);
                    }
                }
                // set a delay before splashscreen is hidden
            }, Config.SPLASH_SCREEN_DELAY);
        }
    }

    public void hideToolbar() {
        if (CurrentAnimation != HIDING) {
            CurrentAnimation = HIDING;
            AnimatorSet animSetXY = new AnimatorSet();

            ObjectAnimator animY = ObjectAnimator.ofFloat(getFragment().rl, "y", 0);
            ObjectAnimator animY1 = ObjectAnimator.ofFloat(mHeaderView, "y", -getActionBarHeight());
            animSetXY.playTogether(animY, animY1);

            animSetXY.start();
            animSetXY.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    CurrentAnimation = NO;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

        }
    }

    public void showToolbar(WebFragment fragment) {
        if (CurrentAnimation != SHOWING || fragment != CurrentAnimatingFragment) {
            CurrentAnimation = SHOWING;
            CurrentAnimatingFragment = fragment;
            AnimatorSet animSetXY = new AnimatorSet();
            ObjectAnimator animY = ObjectAnimator.ofFloat(fragment.rl, "y", getActionBarHeight());
            ObjectAnimator animY1 = ObjectAnimator.ofFloat(mHeaderView, "y", 0);
            animSetXY.playTogether(animY, animY1);

            animSetXY.start();
            animSetXY.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    CurrentAnimation = NO;
                    CurrentAnimatingFragment = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

        }
    }

    public int getActionBarHeight() {
        int mHeight = mToolbar.getHeight();

        //Just in case we get a unreliable result, get it from metrics
        if (mHeight == 0){
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            {
                mHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }
        }

        return mHeight;
    }

    boolean getHideTabs(){
        if (mAdapter.getCount() == 1 || Config.USE_DRAWER){
            return true;
        } else {
            return Config.HIDE_TABS;
        }
    }

    public static boolean getCollapsingActionBar(){
        if (Config.COLLAPSING_ACTIONBAR && !Config.HIDE_ACTIONBAR){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onDrawerItemSelected(View view, int position) {
        String url = Config.URLS[position];
        if (WebToAppWebClient.urlShouldOpenExternally(url)){
            try {
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch(ActivityNotFoundException e) {
                if (url.startsWith("intent://")) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url.replace("intent://", "http://"))));
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_app_message), Toast.LENGTH_LONG).show();
                }
            }
            return false;
        } else {
            getFragment().browser.loadUrl("about:blank");
            getFragment().setBaseUrl(Config.URLS[position]);
            showInterstitial();
            return true;
        }
    }

    private static boolean hasPermissionToDo(final Activity context, final String[] permissions) {
        boolean oneDenied = false;
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ContextCompat.checkSelfPermission(context, permission)
                            != PackageManager.PERMISSION_GRANTED)
                oneDenied = true;
        }

        if (!oneDenied) return true;

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setMessage(R.string.common_permission_explaination);
        builder.setPositiveButton(R.string.common_permission_grant, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    context.requestPermissions(permissions,1);
            }
        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();

        return false;
    }

    /**
     * Show an interstitial ad
     */
    private void showInterstitial(){
        //if (fromPager) return;
        if (getResources().getString(R.string.ad_interstitial_id).length() == 0) return;

        if (interstitialCount == (Config.INTERSTITIAL_NAVIGATION_INTERVAL - 1)) {
            final InterstitialAd mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getResources().getString(R.string.ad_interstitial_id));
            AdRequest adRequestInter = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mInterstitialAd.show();
                }
            });
            mInterstitialAd.loadAd(adRequestInter);

            interstitialCount = 0;
        } else {
            interstitialCount++;
        }

    }
}
