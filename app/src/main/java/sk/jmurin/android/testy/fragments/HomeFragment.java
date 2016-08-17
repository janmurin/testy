package sk.jmurin.android.testy.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sk.jmurin.android.testy.MainActivity;
import sk.jmurin.android.testy.utils.DownloadEvents;
import sk.jmurin.android.testy.utils.NetworkUtils;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.entities.Test;
import sk.jmurin.android.testy.entities.TestInformation;
import sk.jmurin.android.testy.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = HomeFragment.class.getSimpleName();
    private final OkHttpClient client = new OkHttpClient();
    private List<Test> jsonTests;
    private List<TestInformation> jsonTestsInfo;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private TextView statusTextView;
    private Button downloadOkhttpBtn;
    private RecyclerView rv;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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

    private void updateTestsList() {
//        StringBuilder sb = new StringBuilder("Status:\n");
//        for (Test t : jsonTests) {
//            sb.append(t.name + "\n");
//        }
//        statusTextView.setText(sb.toString());
        setupRecyclerView(rv);
        Log.d(TAG, "tests list updated");
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
                jsonTests.add(test);
                TestInformation ti = new TestInformation(test, jsonTests.size() - 1);
                jsonTestsInfo.add(ti);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "nacitanych testov: " + jsonTests.size());
        updateTestsList();
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
                .url("http://81.2.244.134/~vdesktop/testy/testsInfo.txt")
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
                            EventBus.getDefault().post(new DownloadEvents.NewTestsToDownload(nove));
                        } else {
                            EventBus.getDefault().post(new DownloadEvents.NothingToDownload());
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
     * <p/>
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
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(final DownloadEvents.NewTestsDownloaded newTestsDownloaded) {
        Log.d(TAG, "onDownloadEvent: newTestsDownloaded");
        loadTestsFiles();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(final DownloadEvents.NothingToDownload noDownloadEvent) {
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
    public void onDownloadEvent(DownloadEvents.NewTestsToDownload noveTestyDownloadEvent) {
        Log.d(TAG, "onDownloadEvent: treba stiahnut tieto testy: " + noveTestyDownloadEvent.nove);
        List<Integer> ids=new ArrayList<>();
        for(TestInformation ti:noveTestyDownloadEvent.nove){
            ids.add(ti.id);
        }
        DialogFragment dialog = ProgressDialogFragment.newInstance(ids, jsonTests.size());
        dialog.show(getActivity().getSupportFragmentManager(), ProgressDialogFragment.TAG);
    }


    private void setupRecyclerView(RecyclerView recyclerView) {
        String[][] vals = new String[jsonTests.size()][2];
        for (int i = 0; i < jsonTests.size(); i++) {
            Test t = jsonTests.get(i);
            vals[i][0] = t.name;
            vals[i][1] = "0%";
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(), vals));
    }

    public static class SimpleStringRecyclerViewAdapter extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private String[][] mValues;

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
            return mValues[position][0];
        }

        public SimpleStringRecyclerViewAdapter(Context context, String[][] items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mValues = items;
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
            holder.testNameTextView.setText(mValues[position][0]);
            holder.scoreTextView.setText(mValues[position][1]);
//            holder.scoreTextView.setTypeface(tf);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "start test activity");
//                    Context context = v.getContext();
//                    Intent intent = new Intent(context, PagerActivity2.class);
//                    intent.putExtra(PagerActivity2.MENU_ID, position);
//
//                    context.startActivity(intent);
                }
            });

//            Glide.with(holder.testNameTextView.getContext())
//                    .load(Database.getButtonDrawable(position))
//                    .fitCenter()
//                    .into(holder.testNameTextView);
        }

        @Override
        public int getItemCount() {
            return mValues.length;
        }
    }
}
