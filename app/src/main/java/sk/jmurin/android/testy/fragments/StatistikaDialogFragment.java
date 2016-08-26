package sk.jmurin.android.testy.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sk.jmurin.android.testy.App;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.Secrets;
import sk.jmurin.android.testy.entities.Question;
import sk.jmurin.android.testy.entities.Statistika;
import sk.jmurin.android.testy.gui.InstanciaTestu;
import sk.jmurin.android.testy.gui.QuestionActivity;

/**
 * Created by jan.murin on 18-Aug-16.
 */
public class StatistikaDialogFragment extends DialogFragment {

    public static final String TAG = StatistikaDialogFragment.class.getSimpleName();
    private Button zacniTestButton;
    private Button zavrietButton;

    private static final String ARG_PARAM1 = "param1";
    private InstanciaTestu instanciaTestu;
    private TextView uspesnostTextView;
    private TextView spravnychTextView;
    private TextView skoreTextView;
    private TextView hintTextView;

    public StatistikaDialogFragment() {
        // Required empty public constructor
    }

    public static StatistikaDialogFragment newInstance(InstanciaTestu inst) {
        StatistikaDialogFragment fragment = new StatistikaDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, inst);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            instanciaTestu = (InstanciaTestu) getArguments().getSerializable(ARG_PARAM1);
        }
        setCancelable(true);
    }

    private Statistika getStatistika(InstanciaTestu instanciaTestu) {
        Statistika st = new Statistika();
        st.minusBodov = instanciaTestu.pocetMinusBodov;
        st.uspesnost = (int) (instanciaTestu.uspesnych / (double) instanciaTestu.getOhodnotenych() * 100);
        st.uspesnych = instanciaTestu.uspesnych;
        st.vyriesenych = instanciaTestu.getOhodnotenych();
        st.zleZodpovedane = instanciaTestu.getZleZodpovedane();
        st.pribudlo = instanciaTestu.getPribudlo();
        st.ucenie = instanciaTestu.isUcenieSelected();
        int[] serverStatiskikaArray = new int[instanciaTestu.test.getQuestions().size()];
        for (int i = 0; i < instanciaTestu.test.getQuestions().size(); i++) {
            serverStatiskikaArray[i] = instanciaTestu.test.getQuestions().get(i).getStat();
        }
        st.serverStatistika = Arrays.toString(serverStatiskikaArray);
        return st;
    }

    /**
     * The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistika_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Statistika statistika = getStatistika(instanciaTestu);
        DecimalFormat df = new DecimalFormat("##.##");

        uspesnostTextView = (TextView) view.findViewById(R.id.uspesnostTextView);
        spravnychTextView = (TextView) view.findViewById(R.id.spravnychTextView);
        skoreTextView = (TextView) view.findViewById(R.id.skoreTextView);
        hintTextView = (TextView) view.findViewById(R.id.hintTextView);

        uspesnostTextView.setText(df.format(statistika.uspesnost) + " %");
        spravnychTextView.setText(statistika.uspesnych + " z " + statistika.vyriesenych);
        skoreTextView.setText(instanciaTestu.test.getSkorePercento() + " %");
        if (statistika.uspesnych == statistika.vyriesenych) {
            hintTextView.setVisibility(View.GONE);
        }

        zacniTestButton = (Button) view.findViewById(R.id.zacniTestButton);
        if (statistika.zleZodpovedane.length > 0) {
            zacniTestButton.setText("Zopakovať (" + statistika.zleZodpovedane.length + ") otázok");
        } else {
            zacniTestButton.setVisibility(View.GONE);
        }
        zacniTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //Log.d(TAG, "opakujeme test so zle zodpovedanymi otazkami");
                App.zaloguj(App.DEBUG,TAG,"opakujeme test so zle zodpovedanymi otazkami");
                List<Question> otazky = new ArrayList<>();
                for (int i = 0; i < statistika.zleZodpovedane.length; i++) {
                    otazky.add(instanciaTestu.test.getQuestions().get(statistika.zleZodpovedane[i]));// idcko otazky je zhodne s poradovym cislom v zozname otazok
                }
                dismiss();
                InstanciaTestu it = new InstanciaTestu(instanciaTestu.test, otazky);
                it.setUcenieSelected(false);
                instanciaTestu = null;

                Intent testIntent = new Intent(getActivity(), QuestionActivity.class);
                testIntent.putExtra(QuestionActivity.TEST_INSTANCIA_BUNDLE_KEY, it);
                testIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(testIntent);
            }
        });
        zavrietButton = (Button) view.findViewById(R.id.zrusitTestButton);
        zavrietButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                getActivity().finish();
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        RequestBody formBody = new FormBody.Builder()
                .add("username", App.USERNAME)
                .add("deviceID", App.DEVICE_ID)
                .add("testID", "" + instanciaTestu.test.getId())
                .add("testVersion", "" + instanciaTestu.test.getVersion())
                .add("timeCreated", sdf.format(instance.getTime()))
                .add("stats", statistika.serverStatistika)
                .add("skore", "" + instanciaTestu.test.getSkorePercento())
                .build();
        Request request = new Request.Builder()
                .url(Secrets.TESTY_STATS_INSERT_API_URL)
                .post(formBody)
                .build();

        final OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Log.e(TAG, "onFailure");
                App.zaloguj(App.DEBUG,TAG,"onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Headers responseHeaders = response.headers();
                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                        Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }

                    String jsonResponse = response.body().string();
                    Log.d(TAG, jsonResponse);
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
                } else {
                    //TODO: upozornit usera ze server neodpovedal spravne
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Vyhodnotenie");
        return dialog;
    }


}
