package tech.dappworld.webpagetoappconverter;

import android.Manifest;

public class Config {

    /**
     * MAIN SETTINGS
     */

    //Set to true to use a drawer, or to false to use tabs (or neither)
    public static boolean USE_DRAWER = true;
    //Set to true if you would like to hide the actionbar at all times
    public static boolean HIDE_ACTIONBAR = false;

    //Set to true if you would like to display a splash screen on boot. Drawable shown is 'vert_loading'
    public static boolean SPLASH = true;
    //Set to true if you would like to hide the tabs. Only applies is USE_DRAWER is false & if you have more then 1 tab
    public static boolean HIDE_TABS = false;
    //Set to true if you would like to enable pull to refresh
    public static boolean PULL_TO_REFRESH = true;
    //Set to true if you would like to hide the actionbar on when scrolling down. Only applies if HIDE_ACTIONBAR is false
    public static boolean COLLAPSING_ACTIONBAR = true;
    //Set to true if you would like to use the round 'pull to refresh style' loading indicator instead of a progress bar
    public static boolean LOAD_AS_PULL = true;

    /**
     * URL / LIST OF URLS
     */
    //The titles of your web items
    public static final Object[] TITLES = new Object[]{"David's Portfolio"};
    //The URL's of your web items
    public static final String[] URLS = new String[]{"http://olebueziobinna.online"};
    //The icons of your web items
    public static final int[] ICONS = new int[]{};

    /**
     * IDS
     */

    //If you would like to use analytics, you can enter an analytics ID here
    public static String ANALYTICS_ID = "UA-46717142-1";

    //Google sender ID and OneSignal ID have to be edited in build.gradle
    //Admob IDs have to be configured in Strings.xml

    /**
     * ADVANCED SETTINGS
     */

    //All urls that should always be opened outside the WebView and in the browser, download manager, or their respective app
    public static final String[] OPEN_OUTSIDE_WEBVIEW = new String[]{"market://", "play.google.com", "plus.google.com", "mailto:", "tel:", "vid:", "geo:", "whatsapp:", "sms:", "intent://"};
    //Defines a set of urls/patterns that should exlusively load inside the webview. All other urls are loaded outside the WebView. Ignored if no urls are defined.
    public static final String[] OPEN_ALL_OUTSIDE_EXCEPT = new String[]{};

    //Set to true if you would like to hide the drawer header. Only applies if USE_DRAWER is true
    public static boolean HIDE_DRAWER_HEADER = false;
    //Set to true if you would like to hide navigation in the toolbar (i.e. back, forward)
    public static boolean HIDE_MENU_NAVIGATION = false;
    //Set to true if you would like to hide navigation in the toolbar (i.e. back, forward)
    public static boolean HIDE_MENU_SHARE = false;
    //Set to true if you would like to show a link to the apps notification settings
    public static boolean SHOW_NOTIFICATION_SETTINGS = true;

    //Set to true if you would like to support popup windows, e.g. for Facebook login
    public static boolean MULTI_WINDOWS = false;
    //If you would like to show the splash screen for an additional amount of time after page load, define it here (MS)
    public static int SPLASH_SCREEN_DELAY = 0;
    //Permissions required to use the app (should also be in manifest.xml)
    public static String[] PERMISSIONS_REQUIRED = new String[]{Manifest.permission.ACCESS_FINE_LOCATION}; //Manifest.permission.PERMISSION_NAME
    //Always use the app name as actionbar title (only applies for if USE_DRAWER is false and number of tabs == 1)
    public static boolean STATIC_TOOLBAR_TITLE = false;
    //Load a webpage when no internet connection was found (must be in assets). Leave empty to show dialog.
    public static String NO_CONNECTION_PAGE = "";
    //The image/icon used for in the drawer header
    public static int DRAWER_ICON = tech.dappworld.webpagetoappconverter.R.mipmap.ic_launcher;

    //The frequency in which interstitial ads are shown for when loading pages
    //('0' to never show, '1' to always show, '2' to show 1 out of 2, etc)
    public static final int INTERSTITIAL_PAGE_INTERVAL = 2;
    //The frequency in which interstitial ads are shown when switching tabs/menu items
    public static final int INTERSTITIAL_NAVIGATION_INTERVAL = 2;
}