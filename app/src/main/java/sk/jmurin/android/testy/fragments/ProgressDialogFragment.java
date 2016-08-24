package sk.jmurin.android.testy.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.greenrobot.eventbus.EventBus;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import sk.jmurin.android.testy.Secrets;
import sk.jmurin.android.testy.content.DataContract;
import sk.jmurin.android.testy.entities.Question;
import sk.jmurin.android.testy.entities.Test;
import sk.jmurin.android.testy.utils.EventBusEvents;
import sk.jmurin.android.testy.utils.Utils;


public class ProgressDialogFragment extends DialogFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = ProgressDialogFragment.class.getSimpleName();
    private ArrayList<Integer> ids;
    private ProgressDialog dialog;
    private int testsSize;

    public ProgressDialogFragment() {
        // Required empty public constructor
    }

    public static ProgressDialogFragment newInstance(List<Integer> ids, int testsSize) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_PARAM1, (ArrayList<Integer>) ids);
        args.putInt(ARG_PARAM2, testsSize);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ids = getArguments().getIntegerArrayList(ARG_PARAM1);
            testsSize = getArguments().getInt(ARG_PARAM2);
        }
        setCancelable(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new ProgressDialog(getActivity(), getTheme());
        //dialog.setTitle(getString(R.string.pleaseWait));
        dialog.setMessage("Načítavam testy...");
        dialog.setProgressStyle(dialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setMax(ids.size());
        DownloadAsyncTask dat = new DownloadAsyncTask();
        dat.execute(ids);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "cancelled");
    }

    private class DownloadAsyncTask extends AsyncTask<ArrayList<Integer>, Integer, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            EventBus.getDefault().post(new EventBusEvents.NewTestsDownloaded());
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values[values.length - 1]);
            dialog.setProgress(values[values.length - 1]);
        }

        @Override
        protected Void doInBackground(ArrayList<Integer>... params) {
            int count = 0;
            ArrayList<Integer> nove = params[0];
            //TODO: odoslat viacej idciek a vratit viacej testov naraz
            for (Integer ti : nove) {
                InputStream is = null;

                try {
                    URL url = new URL(Secrets.DOMAIN + "/testy/" + ti + ".txt");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(5000 /* milliseconds */);// TODO: ohandlovat chyby
                    conn.setConnectTimeout(7000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    int response = conn.getResponseCode();
                    Log.d(TAG, "The response is: " + response);
                    if (response == HttpURLConnection.HTTP_OK) {
                        is = conn.getInputStream();

                        String responseJson = Utils.getStringUTFFromInputStream(is);
                        Log.d(TAG, responseJson);

                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            Test test = mapper.readValue(responseJson, Test.class);
                            //jsonTests.add(test);
                            testsSize++;
                            // pri parsovani nevyskocila vynimka takze mozeme ukladat tieto data do suboru
                            String filename = "test" + testsSize;
                            Log.d(TAG, "filename=[" + filename + "]");
                            FileOutputStream outputStream;
                            outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                            outputStream.write(responseJson.getBytes());
                            outputStream.close();
                            Log.d(TAG, "ulozeny test " + test);

                            // ulozit do databazy statistiky
                            Uri insertUri = DataContract.QuestionStats.CONTENT_URI
                                    .buildUpon()
                                    .build();

                            List<ContentValues> contentValues = new ArrayList<>();
                            int question_test_id = 0;
                            for (Question q : test.questions) {
                                ContentValues values = new ContentValues();
                                values.put(DataContract.QuestionStats.QUESTION_TEST_ID, question_test_id);
                                values.put(DataContract.QuestionStats.STAT, (int) (Math.random() * 5 - 1));
                                values.put(DataContract.QuestionStats.TEST_NAME, test.name);
                                values.put(DataContract.QuestionStats.TEST_VERSION, test.version);
                                contentValues.add(values);
                                question_test_id++;
                            }
                            int inserted = getActivity().getContentResolver().bulkInsert(insertUri, contentValues.toArray(new ContentValues[contentValues.size()]));
                            Log.d(TAG, "inserted QuestionStats rows: " + inserted);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // TODO: ohandlovat chybu ze sa nepodarilo naparsovat resource z rest servera
                        }
                        count++;
                        publishProgress(count);
                    } else {
                        //TODO: ohandlovat chybu ze server nevratil kod 200
                        Log.e(TAG, "nepodarilo sa ziskat resource: [" + url.toString() + "]");
                    }
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return null;
        }
    }
}
