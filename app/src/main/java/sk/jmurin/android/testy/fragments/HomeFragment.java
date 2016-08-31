package sk.jmurin.android.testy.fragments;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sk.jmurin.android.testy.App;
import sk.jmurin.android.testy.content.DataContract;
import sk.jmurin.android.testy.content.Defaults;
import sk.jmurin.android.testy.entities.Parser;
import sk.jmurin.android.testy.entities.QuestionData;
import sk.jmurin.android.testy.gui.MainActivity;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.entities.Test;

public class HomeFragment extends Fragment {

    public static final String TAG = HomeFragment.class.getSimpleName();
    public static final int DRAWER_POS = 0;
    public static final String TESTYMAPA = "testymapa";
    // private final OkHttpClient client = new OkHttpClient();
    private Map<Integer, Test> testy;

    private TextView statusTextView;
    //private Button downloadOkhttpBtn;
    private RecyclerView rv;

    public static Fragment newInstance(Map<Integer, Test> testy) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(TESTYMAPA, (Serializable) testy);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Testy");
        Bundle args = getArguments();
        if (args != null) {
            testy = (Map<Integer, Test>) args.getSerializable(TESTYMAPA);
        } else {
            throw new RuntimeException("Home fragment without arguments!!!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Log.d(TAG, "onViewCreated(view, savedInstanceState);");
        App.zaloguj(App.DEBUG, TAG, "onViewCreated(view, savedInstanceState);");
        statusTextView = (TextView) view.findViewById(R.id.statusTextView);
        statusTextView.setText("Aktívne testy");
//        downloadOkhttpBtn = (Button) view.findViewById(R.id.downloadButton);
//        downloadOkhttpBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onDownloadOkhttpClicked();
//            }
//        });
        rv = (RecyclerView) view.findViewById(R.id.recyclerview);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Log.d(TAG, "onActivityCreated(savedInstanceState);");
        App.zaloguj(App.DEBUG, TAG, "onActivityCreated(savedInstanceState);");
    }

    private void refreshRecyclerView() {
        //Log.d(TAG, "refreshRecyclerView()");
        App.zaloguj(App.DEBUG, TAG, "refreshRecyclerView()");
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(), testy));
        //Log.d(TAG, "tests list and stats updated");
        App.zaloguj(App.DEBUG, TAG, "tests list and stats updated");
    }


    // TODO: refreshovat statistiky k otazkam pri onStart alebo onResume? pridat progress bar?
    @Override
    public void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
        //Log.d(TAG, "onStart");
        App.zaloguj(App.DEBUG, TAG, "onStart");

        // refresh data z databazy
        Uri uri = DataContract.QuestionStats.CONTENT_URI
                .buildUpon()
                .build();

        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);
                //Log.d(TAG, "onQueryComplete token=" + token);
                App.zaloguj(App.DEBUG, TAG, "onQueryComplete token=" + token);
                List<QuestionData> data = new ArrayList<>();
                while (cursor.moveToNext()) {
                    int stat = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.STAT));
                    int db_id = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats._ID));
                    int test_question_index = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.TEST_QUESTION_INDEX));
                    int test_id = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.TEST_ID));
                    data.add(new QuestionData(stat, db_id, test_question_index, test_id));
                }
                cursor.close();

                for (QuestionData dd : data) {
                    try {
                        testy.get(dd.test_id).getQuestions().get(dd.test_question_index).setStat(dd.stat);
                        // testy.get(dd.test_id).getQuestions().get(dd.test_question_index).setDbID(dd.db_id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                refreshRecyclerView();
            }
        };
        queryHandler.startQuery(2, Defaults.NO_COOKIE, uri, null, null, Defaults.NO_SELECTION_ARGS, DataContract.QuestionStats._ID);
    }

    @Override
    public void onStop() {
        super.onStop();
        //EventBus.getDefault().unregister(this);
    }


    public static class SimpleStringRecyclerViewAdapter extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private final MainActivity context;
        private final Map<Integer, Test> tests;
        private final String[][] listItemValues;
        private final int[] testIDs;
        private int mBackground;

        public SimpleStringRecyclerViewAdapter(Context context, Map<Integer, Test> jsonTests) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            this.context = (MainActivity) context;
            mBackground = mTypedValue.resourceId;
            this.tests = jsonTests;
            listItemValues = new String[jsonTests.size()][2];
            testIDs = new int[jsonTests.size()];
            int i = 0;
            for (Integer testID : jsonTests.keySet()) {
                Test t = jsonTests.get(testID);
                listItemValues[i][0] = t.getName();
                listItemValues[i][1] = t.getSkorePercento() + " %";
                testIDs[i] = testID;
                i++;
            }
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
//            public String mBoundString;

            public final View mView;
            public final TextView testNameTextView;
            public final TextView scoreTextView;
            private final TextView numberTextView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                numberTextView = (TextView) view.findViewById(R.id.numberTextView);
                testNameTextView = (TextView) view.findViewById(R.id.testNameTextView);
                scoreTextView = (TextView) view.findViewById(R.id.scoreTextView);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + scoreTextView.getText();
            }
        }

        public String getValueAt(int position) {
            return listItemValues[position][0];
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //holder.mBoundString = mValues.get(position);
            holder.numberTextView.setText((position + 1) + ".");
            holder.testNameTextView.setText(listItemValues[position][0]);
            holder.scoreTextView.setText(listItemValues[position][1]);
//            holder.scoreTextView.setTypeface(tf);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d(TAG, "start test activity");
                    App.zaloguj(App.DEBUG, TAG, "start test activity");
                    FragmentManager fragmentManager = context.getSupportFragmentManager();
                    Test test = tests.get(testIDs[position]);
                    TestParametersDialogFragment newFragment = TestParametersDialogFragment.newInstance(test);
                    newFragment.show(fragmentManager, TestParametersDialogFragment.TAG);

                }
            });

        }

        @Override
        public int getItemCount() {
            return listItemValues.length;
        }
    }

    //    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onDownloadEvent(final EventBusEvents.NewTestsDownloaded newTestsDownloaded) {
//        Log.d(TAG, "onDownloadEvent: newTestsDownloaded");
//        loadTestsFiles();
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onDownloadEvent(final EventBusEvents.NothingToDownload noDownloadEvent) {
//        Log.d(TAG, "onDownloadEvent: netreba stahovat nove testy: ");
//        final TextView textView = new TextView(getActivity());
//        textView.setText("Všetky testy sú aktuálne.");
//        textView.setTextColor(Color.BLACK);
//        textView.setPadding(10, 10, 10, 10);
//        new AlertDialog.Builder(getActivity())
//                .setView(textView)
//                .setPositiveButton("OK", null)
//                .show();
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onDownloadEvent(EventBusEvents.NewTestsToDownload noveTestyDownloadEvent) {
//        Log.d(TAG, "onDownloadEvent: treba stiahnut tieto testy: " + noveTestyDownloadEvent.nove);
//        List<Integer> ids = new ArrayList<>();
//        for (TestInformation ti : noveTestyDownloadEvent.nove) {
//            ids.add(ti.id);
//        }
//        DialogFragment dialog = ProgressDialogFragment.newInstance(ids, jsonTests.size());
//        dialog.show(getActivity().getSupportFragmentManager(), ProgressDialogFragment.TAG);
//    }

    //    private void onDownloadOkhttpClicked() {
//        if (!NetworkUtils.isConnected(getActivity())) {
//            //Toast.makeText(this, "Pripojte sa na internet a zopakujte operáciu.", Toast.LENGTH_SHORT).show();
//            final TextView textView = new TextView(getActivity());
//            textView.setText("Nedá sa pripojiť na server!\nPripojte sa na internet a zopakujte operáciu.");
//            textView.setTextColor(Color.BLACK);
//            textView.setPadding(5, 5, 5, 5);
//            new AlertDialog.Builder(getActivity())
//                    .setTitle("Oznam")
//                    .setView(textView)
//                    .setPositiveButton("OK", null)
//                    .show();
//            return;
//        }
//
//        Request request = new Request.Builder()
//                .url(Secrets.DOMAIN + "/testy/testsInfo.txt")
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e(TAG, "onFailure");
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    //throw new IOException("Unexpected code " + response);
//
//                    //       Headers responseHeaders = response.headers();
////                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
////                    Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
////                }
//
//                    String jsonResponse = response.body().string();
//                    Log.d(TAG, jsonResponse);
//                    try {
//                        ObjectMapper mapper = new ObjectMapper();
//                        List<TestInformation> testsInfo = mapper.readValue(jsonResponse, new TypeReference<List<TestInformation>>() {
//                        });
//                        List<TestInformation> nove = new ArrayList<>();
//                        for (TestInformation ti : testsInfo) {
//                            if (!jsonTestsInfo.contains(ti)) {
//                                nove.add(ti);
//                            }
//                        }
//                        // upovedomime aktivitu o vysledku requestu
//                        if (!nove.isEmpty()) {
//                            Log.d(TAG, "spustam stahovanie novych testov size: " + nove.size());
//                            EventBus.getDefault().post(new EventBusEvents.NewTestsToDownload(nove));
//                        } else {
//                            EventBus.getDefault().post(new EventBusEvents.NothingToDownload());
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        // TODO: ohandlovat chybu ked sa nenaparsuje testsInfo
//                    }
//                } else {
//                    //TODO: upozornit usera ze server neodpovedal spravne
//                }
//            }
//        });
//    }


//    private void startDialogFragment(){
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        TestParametersDialogFragment newFragment = new TestParametersDialogFragment();
//
//        if (mIsLargeLayout) {
//            // The device is using a large layout, so show the fragment as a dialog
//            newFragment.show(fragmentManager, TestParametersDialogFragment.TAG);
//        } else {
//            newFragment.show(fragmentManager, TestParametersDialogFragment.TAG);
////            // The device is smaller, so show the fragment fullscreen
////            FragmentTransaction transaction = fragmentManager.beginTransaction();
////            // For a little polish, specify a transition animation
////            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
////            // To make it fullscreen, use the 'content' root view as the container
////            // for the fragment, which is always the root view for the activity
////            transaction.add(android.R.id.content, newFragment)
////                    .addToBackStack(null).commit();
//        }
//    }
}
