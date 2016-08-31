package sk.jmurin.android.testy;

import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import junit.framework.Assert;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sk.jmurin.android.testy.content.DataContract;
import sk.jmurin.android.testy.content.Defaults;
import sk.jmurin.android.testy.utils.EventBusEvents;
import sk.jmurin.android.testy.utils.NetworkUtils;


/**
 * Receives info about network state.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public static final String TAG = "NetworkStateReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            Log.d(TAG, "Network state changed");
            Log.d(TAG, String.format("Connected: %s", NetworkUtils.isConnected(context)));

            if(!NetworkUtils.isConnected(context)){
                return;
            }

            Uri uri = DataContract.StatsNotSent.CONTENT_URI
                    .buildUpon()
                    .build();

            AsyncQueryHandler insertHandler = new AsyncQueryHandler(context.getContentResolver()) {
                @Override
                protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                    super.onQueryComplete(token, cookie, cursor);
//                Log.d(TAG, "onQueryComplete token=" + token);
                    App.zaloguj(App.DEBUG, TAG, "onQueryComplete token=" + token);
                    final OkHttpClient client = new OkHttpClient();

                    while (cursor.moveToNext()) {
                        String stats = cursor.getString(cursor.getColumnIndex(DataContract.StatsNotSent.STATS));
                        String device_id = cursor.getString(cursor.getColumnIndex(DataContract.StatsNotSent.DEVICE_ID));
                        String skore = cursor.getString(cursor.getColumnIndex(DataContract.StatsNotSent.SKORE));
                        String test_id = cursor.getString(cursor.getColumnIndex(DataContract.StatsNotSent.TEST_ID));
                        String test_version = cursor.getString(cursor.getColumnIndex(DataContract.StatsNotSent.TEST_VERSION));
                        String time_created = cursor.getString(cursor.getColumnIndex(DataContract.StatsNotSent.TIME_CREATED));
                        String username = cursor.getString(cursor.getColumnIndex(DataContract.StatsNotSent.USERNAME));
                        int db_id2 = cursor.getInt(cursor.getColumnIndex(DataContract.StatsNotSent._ID));

//                        System.out.println("stats=" + stats);
//                        System.out.println("db_id=" + db_id2);
                        RequestBody formBody = new FormBody.Builder()
                                .add("username", username)
                                .add("deviceID", device_id)
                                .add("testID", test_id)
                                .add("testVersion", test_version)
                                .add("timeCreated", time_created)
                                .add("stats", stats)
                                .add("skore", skore)
                                .build();
                        Request request = new Request.Builder()
                                .url(Secrets.TESTY_STATS_INSERT_API_URL)
                                .post(formBody)
                                .build();

                        client.newCall(request).enqueue(new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                //Log.e(TAG, "onFailure");
                                //App.zaloguj(App.DEBUG, TAG, "onFailure");
                                //EventBus.getDefault().post(new EventBusEvents.InsertStatsResponse("insert stats response: " + e.getMessage()));
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    String jsonResponse = response.body().string();
                                    //EventBus.getDefault().post(new EventBusEvents.InsertStatsResponse("insert stats response: " + jsonResponse));
                                } else {
                                    //TODO: upozornit usera ze server neodpovedal spravne
                                    throw new IOException("Unexpected code " + response);
                                }
                            }
                        });
                    }
                    cursor.close();

                    Uri uri = DataContract.StatsNotSent.CONTENT_URI
                            .buildUpon()
                            .build();
                    context.getContentResolver().delete(uri,null,null);
                }

            };

            insertHandler.startQuery(2, Defaults.NO_COOKIE, uri, null, null, Defaults.NO_SELECTION_ARGS, null);
        }
    }
}
