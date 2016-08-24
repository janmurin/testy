package sk.jmurin.android.testy.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import sk.jmurin.android.testy.App;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.utils.EventBusEvents;

/**
 * Created by jan.murin on 24-Aug-16.
 */
public class TutorialPageItemFragment extends Fragment {


    public static final String POS_ID = "posID";
    public static final String TAG = TutorialPageItemFragment.class.getSimpleName();
    private int pos;

    public static TutorialPageItemFragment getInstance(int pos) {
        TutorialPageItemFragment inst = new TutorialPageItemFragment();
        Bundle args = new Bundle();
        args.putInt(POS_ID, pos);
        inst.setArguments(args);
        return inst;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            pos = args.getInt(POS_ID);
        } else {
            throw new RuntimeException("Pager item fragment without arguments!!!");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        switch (pos) {
            case 0:
                view = inflater.inflate(R.layout.tutorial_page1, container, false);
                Button dalejButton = (Button) view.findViewById(R.id.dalejButton);
                dalejButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new EventBusEvents.DalejButtonClicked());
                    }
                });
                break;
            case 1:
                view = inflater.inflate(R.layout.tutorial_page2, container, false);
                Button dalejButton1 = (Button) view.findViewById(R.id.dalejButton);
                if (App.USERNAME.equals(App.DEFAULT_USERNAME)) {// TODO: spolieham sa nato ze vsetky zmeny v App.USERNAME prebiehaju v jednom vlakne (je to skutocne tak?)
                    // este sa musi zobrazit posledny page kde sa nastavuje meno
                    dalejButton1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EventBus.getDefault().post(new EventBusEvents.DalejButtonClicked());
                        }
                    });
                } else {
                    dalejButton1.setText("Zavrieť");
                    dalejButton1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EventBus.getDefault().post(new EventBusEvents.ZavrietTutorial());
                        }
                    });
                }
                break;
            case 2:
                view = inflater.inflate(R.layout.tutorial_page3, container, false);
                final EditText menoEditText = (EditText) view.findViewById(R.id.menoEditText);
                Button dalejButton3 = (Button) view.findViewById(R.id.dalejButton);
                dalejButton3.setText("Uložiť");
                dalejButton3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String meno = menoEditText.getText().toString().trim();
                        if (meno.length() < 5) {
                            final TextView textView = new TextView(getActivity());
                            textView.setText("Zadané meno musí mať aspoň 5 znakov.");
                            textView.setTextColor(Color.BLACK);
                            textView.setPadding(5, 5, 5, 5);
                            new AlertDialog.Builder(getActivity())
                                    .setView(textView)
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {
                            if (meno.equals(App.DEFAULT_USERNAME)) {
                                final TextView textView = new TextView(getActivity());
                                textView.setText("Zadané meno nesmie byť [" + App.DEFAULT_USERNAME + "].");
                                textView.setTextColor(Color.BLACK);
                                textView.setPadding(5, 5, 5, 5);
                                new AlertDialog.Builder(getActivity())
                                        .setView(textView)
                                        .setPositiveButton("OK", null)
                                        .show();
                            } else {
                                // user vlozil korektne meno
                                SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.username_preference_key), meno);
                                boolean commitSuccessful = editor.commit();
                                if (commitSuccessful) {
                                    App.USERNAME = meno;
                                    Log.d(TAG, "zadane username: [" + meno + "]");
                                    EventBus.getDefault().post(new EventBusEvents.UsernameSelected(meno));
                                }
                            }
                        }
                    }
                });
                break;
            default:
                throw new UnsupportedOperationException("nemame page 4 pre tutorial");
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


}
