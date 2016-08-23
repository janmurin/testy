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
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import sk.jmurin.android.testy.InstanciaTestu;
import sk.jmurin.android.testy.QuestionActivity;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.entities.Question;
import sk.jmurin.android.testy.entities.Statistika;

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
//        TextView infoTextView = (TextView) view.findViewById(R.id.infoTextView);
        DecimalFormat df = new DecimalFormat("##.##");
//        String textik = "Vyriešených otázok: " + statistika.vyriesenych + "\n" +
//                "Úspešných: otázok: " + statistika.uspesnych + "\n" +
//                "Úspešnosť: " + df.format(statistika.uspesnost) + " %\n" +
//                "Mínus bodov: " + statistika.minusBodov + "\n" +
//                "cervenych: " + String.format("%+d", statistika.pribudlo[0]) + "\n" +
//                "bielych: " + String.format("%+d", statistika.pribudlo[1]) + "\n" +
//                "zltych: " + String.format("%+d", statistika.pribudlo[2]) + "\n" +
//                "oranzovych: " + String.format("%+d", statistika.pribudlo[3]) + "\n" +
//                "zelenych: " + String.format("%+d", statistika.pribudlo[4]);
//        infoTextView.setText(textik);

        uspesnostTextView = (TextView) view.findViewById(R.id.uspesnostTextView);
        spravnychTextView = (TextView) view.findViewById(R.id.spravnychTextView);
        skoreTextView = (TextView) view.findViewById(R.id.skoreTextView);
        hintTextView = (TextView) view.findViewById(R.id.hintTextView);

        uspesnostTextView.setText(df.format(statistika.uspesnost) + " %");
        spravnychTextView.setText(statistika.uspesnych + " z " + statistika.vyriesenych);
        skoreTextView.setText(instanciaTestu.testStats.getSkorePercento() + " %");
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
                Log.d(TAG, "opakujeme test so zle zodpovedanymi otazkami");
                List<Question> otazky = new ArrayList<>();
                for (int i = 0; i < statistika.zleZodpovedane.length; i++) {
                    otazky.add(instanciaTestu.test.questions.get(statistika.zleZodpovedane[i]));// idcko otazky je zhodne s poradovym cislom v zozname otazok
                }
                dismiss();
                InstanciaTestu it = new InstanciaTestu(instanciaTestu.test, otazky, instanciaTestu.testStats);
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
