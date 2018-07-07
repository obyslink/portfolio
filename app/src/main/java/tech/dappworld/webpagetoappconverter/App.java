package tech.dappworld.webpagetoappconverter;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import tech.dappworld.webpagetoappconverter.activity.MainActivity;

import org.json.JSONObject;

public class App extends Application { 
	
	  public static GoogleAnalytics analytics;
	  public static Tracker tracker;

      private String push_url = null;

    @Override public void onCreate() {
        super.onCreate();

        if (Config.ANALYTICS_ID.length() > 0) {
            analytics = GoogleAnalytics.getInstance(this);
            analytics.setLocalDispatchPeriod(1800);

            tracker = analytics.newTracker(Config.ANALYTICS_ID); // Replace with actual tracker/property Id
            tracker.enableExceptionReporting(true);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableAutoActivityTracking(true);
        }

        //OneSignal Push
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationHandler())
                .init();

    }

    // This fires when a notification is opened by tapping on it or one is received while the app is running.
    class NotificationHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            try {
                JSONObject data = result.notification.payload.additionalData;
                String url = (data != null) ? data.optString("url", null) : null;
                if (url != null) {
                    if (result.notification.isAppInFocus) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                        Log.v("INFO", "Received notification while app was on foreground");
                    } else {
                        push_url = url;
                    }
                } else if (!result.notification.isAppInFocus) {
                    Intent mainIntent;
                    mainIntent = new Intent(App.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                }


            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

    public synchronized String getPushUrl(){
        String url = push_url;
        push_url = null;
        return url;
    }

    public synchronized void setPushUrl(String url){
        this.push_url = url;
    }
} 