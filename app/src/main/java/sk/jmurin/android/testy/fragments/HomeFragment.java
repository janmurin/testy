package sk.jmurin.android.testy.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sk.jmurin.android.testy.MainActivity;
import sk.jmurin.android.testy.Secrets;
import sk.jmurin.android.testy.content.DataContract;
import sk.jmurin.android.testy.entities.TestStats;
import sk.jmurin.android.testy.utils.EventBusEvents;
import sk.jmurin.android.testy.utils.NetworkUtils;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.entities.Test;
import sk.jmurin.android.testy.entities.TestInformation;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = HomeFragment.class.getSimpleName();
    public static final int DRAWER_POS = 0;
    private final OkHttpClient client = new OkHttpClient();
    private List<Test> jsonTests;
    private List<TestInformation> jsonTestsInfo;

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private TextView statusTextView;
    private Button downloadOkhttpBtn;
    private RecyclerView rv;
    private boolean mIsLargeLayout;

    public HomeFragment() {
        // Required empty public constructor
    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment HomeFragment.
//     */
//    public static HomeFragment newInstance(String param1, String param2) {
//        HomeFragment fragment = new HomeFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statusTextView = (TextView) view.findViewById(R.id.statusTextView);
        statusTextView.setText("Stiahnuté testy");
        downloadOkhttpBtn = (Button) view.findViewById(R.id.downloadButton);
        downloadOkhttpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDownloadOkhttpClicked();
            }
        });
        rv = (RecyclerView) view.findViewById(R.id.recyclerview);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadTestsFiles();
    }


    private synchronized void loadTestsFiles() {
        String dirPath = getActivity().getFilesDir().getAbsolutePath();
        File projDir = new File(dirPath);
        if (!projDir.exists())
            projDir.mkdirs();
        File[] files = projDir.listFiles();
        jsonTests = new ArrayList<>();
        jsonTestsInfo = new ArrayList<>();

        for (File f : files) {
            if (!f.getName().startsWith("test")) {
                continue;
            }
            ObjectMapper mapper = new ObjectMapper();
            try {
                Test test = mapper.readValue(f, Test.class);
                // nastavime kazdej otazke idcko podla toho v akom poradi bolo nacitane zo suboru
                for (int i = 0; i < test.questions.size(); i++) {
                    test.questions.get(i).id = i;
                }
                jsonTests.add(test);
                TestInformation ti = new TestInformation(test, jsonTests.size() - 1);
                jsonTestsInfo.add(ti);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "nacitanych testov: " + jsonTests.size());
        refreshRecyclerView();
    }

    private void refreshRecyclerView() {
        Log.d(TAG,"refreshRecyclerView()");
        Cursor cursor = getActivity().getContentResolver().query(DataContract.QuestionStats.CONTENT_URI, null, null, null, DataContract.QuestionStats._ID);
        Map<String, TestStats> testStats = getStatsFrom(cursor);

        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(), jsonTests, testStats));
        Log.d(TAG, "tests list and stats updated");
    }


    private Map<String, TestStats> getStatsFrom(Cursor cursor) {
        System.out.println("getStatsFrom actionPerformed");
        Map<String, TestStats> stats = new HashMap<>();
        while (cursor.moveToNext()) {
            int stat = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.STAT));
            int db_id = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats._ID));
            int question_test_id = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.QUESTION_TEST_ID));
            String test_name = cursor.getString(cursor.getColumnIndex(DataContract.QuestionStats.TEST_NAME));
            int test_version = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.TEST_VERSION));
            String tk = test_name + "_" + test_version;
            if (!stats.keySet().contains(tk)) {
                TestStats noveStats = new TestStats(test_name, test_version);
                noveStats.addQuestionStat(question_test_id, stat, db_id);
                stats.put(tk, noveStats);
            } else {
                TestStats testStats = stats.get(tk);
                testStats.addQuestionStat(question_test_id, stat, db_id);
            }
        }
        cursor.close();
        return stats;
    }

    private void onDownloadOkhttpClicked() {
        if (!NetworkUtils.isConnected(getActivity())) {
            //Toast.makeText(this, "Pripojte sa na internet a zopakujte operáciu.", Toast.LENGTH_SHORT).show();
            final TextView textView = new TextView(getActivity());
            textView.setText("Nedá sa pripojiť na server!\nPripojte sa na internet a zopakujte operáciu.");
            textView.setTextColor(Color.BLACK);
            textView.setPadding(5, 5, 5, 5);
            new AlertDialog.Builder(getActivity())
                    .setTitle("Oznam")
                    .setView(textView)
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        Request request = new Request.Builder()
                .url(Secrets.DOMAIN + "/testy/testsInfo.txt")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    //throw new IOException("Unexpected code " + response);

                    //       Headers responseHeaders = response.headers();
//                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
//                    Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
//                }

                    String jsonResponse = response.body().string();
                    Log.d(TAG, jsonResponse);
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List<TestInformation> testsInfo = mapper.readValue(jsonResponse, new TypeReference<List<TestInformation>>() {
                        });
                        List<TestInformation> nove = new ArrayList<>();
                        for (TestInformation ti : testsInfo) {
                            if (!jsonTestsInfo.contains(ti)) {
                                nove.add(ti);
                            }
                        }
                        // upovedomime aktivitu o vysledku requestu
                        if (!nove.isEmpty()) {
                            Log.d(TAG, "spustam stahovanie novych testov size: " + nove.size());
                            EventBus.getDefault().post(new EventBusEvents.NewTestsToDownload(nove));
                        } else {
                            EventBus.getDefault().post(new EventBusEvents.NothingToDownload());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // TODO: ohandlovat chybu ked sa nenaparsuje testsInfo
                    }
                } else {
                    //TODO: upozornit usera ze server neodpovedal spravne
                }
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        refreshRecyclerView();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(final EventBusEvents.NewTestsDownloaded newTestsDownloaded) {
        Log.d(TAG, "onDownloadEvent: newTestsDownloaded");
        loadTestsFiles();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(final EventBusEvents.NothingToDownload noDownloadEvent) {
        Log.d(TAG, "onDownloadEvent: netreba stahovat nove testy: ");
        final TextView textView = new TextView(getActivity());
        textView.setText("Všetky testy sú aktuálne.");
        textView.setTextColor(Color.BLACK);
        textView.setPadding(10, 10, 10, 10);
        new AlertDialog.Builder(getActivity())
                .setView(textView)
                .setPositiveButton("OK", null)
                .show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(EventBusEvents.NewTestsToDownload noveTestyDownloadEvent) {
        Log.d(TAG, "onDownloadEvent: treba stiahnut tieto testy: " + noveTestyDownloadEvent.nove);
        List<Integer> ids = new ArrayList<>();
        for (TestInformation ti : noveTestyDownloadEvent.nove) {
            ids.add(ti.id);
        }
        DialogFragment dialog = ProgressDialogFragment.newInstance(ids, jsonTests.size());
        dialog.show(getActivity().getSupportFragmentManager(), ProgressDialogFragment.TAG);
    }


    public static class SimpleStringRecyclerViewAdapter extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private final MainActivity context;
        private final List<Test> tests;
        private final String[][] vals;
        private final Map<String, TestStats> testStatsMap;
        private int mBackground;

        public SimpleStringRecyclerViewAdapter(Context context, List<Test> jsonTests, Map<String, TestStats> testStatsMap) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            this.context = (MainActivity) context;
            mBackground = mTypedValue.resourceId;
            this.tests = jsonTests;
            this.testStatsMap = testStatsMap;
            vals = new String[jsonTests.size()][2];
            for (int i = 0; i < jsonTests.size(); i++) {
                Test t = jsonTests.get(i);
                TestStats ts = testStatsMap.get(t.name + "_" + t.version);
                vals[i][0] = t.name;
                vals[i][1] = ts.getSkorePercento() + "%";
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
            return vals[position][0];
        }

//        public SimpleStringRecyclerViewAdapter(Context context, String[][] items) {
//            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
//            this.context = (MainActivity) context;
//            mBackground = mTypedValue.resourceId;
//            mValues = items;
//        }

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
            holder.testNameTextView.setText(vals[position][0]);
            holder.scoreTextView.setText(vals[position][1]);
//            holder.scoreTextView.setTypeface(tf);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "start test activity");
                    //TODO: pouzit embedded dialog na velke layouty
                    FragmentManager fragmentManager = context.getSupportFragmentManager();
                    Test test = tests.get(position);
                    TestParametersDialogFragment newFragment = TestParametersDialogFragment.newInstance(test, testStatsMap.get(test.name + "_" + test.version));
                    newFragment.show(fragmentManager, TestParametersDialogFragment.TAG);

                }
            });

        }

        @Override
        public int getItemCount() {
            return vals.length;
        }
    }

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
