package sk.jmurin.android.testy.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sk.jmurin.android.testy.App;
import sk.jmurin.android.testy.gui.InstanciaTestu;
import sk.jmurin.android.testy.gui.QuestionActivity;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.entities.Question;
import sk.jmurin.android.testy.entities.Test;

/**
 * Created by jan.murin on 18-Aug-16.
 */
public class TestParametersDialogFragment extends DialogFragment {

    public static final String TAG = TestParametersDialogFragment.class.getSimpleName();
    private RadioButton vsetkyRozsahRadioButton;
    private RadioButton vybraneRozsahRadioButton;
    private CheckBox cervenaCheckbox;
    private CheckBox bielaCheckBox;
    private CheckBox zltaCheckBox;
    private CheckBox oranzovaCheckBox;
    private CheckBox zelenaCheckBox;
    private Button zacniTestButton;
    private Button zrusitTestButton;
    private LinearLayout rozsahLayout;

    private static final String TEST_PARAM = "param1";
    private Test test;
    private Spinner maxSpinner;
    private Spinner minSpinner;
    private RadioButton vsetkySkoreRadioButton;
    private RadioButton vybraneSkoreRadioButton;
    private LinearLayout checkboxyLayout;
    private TextView sumarTextView;
    private List<Question> vybrane;

    public TestParametersDialogFragment() {
        // Required empty public constructor
    }

    public static TestParametersDialogFragment newInstance(Test test) {
        TestParametersDialogFragment fragment = new TestParametersDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(TEST_PARAM, test);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            test = (Test) getArguments().getSerializable(TEST_PARAM);
        }
        setCancelable(true);
    }

    /**
     * The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parameters_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View.OnClickListener filterParamListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                odfiltrujOtazky();
            }
        };
        sumarTextView = (TextView) view.findViewById(R.id.sumarTextView);

        // FILTER PODLA ROZSAHU
        vsetkyRozsahRadioButton = (RadioButton) view.findViewById(R.id.vsetkyRadioButton);
        vsetkyRozsahRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // schovat rozsah layout
                rozsahLayout.setVisibility(View.GONE);
                odfiltrujOtazky();
            }
        });
        vybraneRozsahRadioButton = (RadioButton) view.findViewById(R.id.vybraneRadioButton);
        vybraneRozsahRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rozsahLayout.setVisibility(View.VISIBLE);
                odfiltrujOtazky();
            }
        });
        rozsahLayout = (LinearLayout) view.findViewById(R.id.rozsahLayout);
        minSpinner = (Spinner) view.findViewById(R.id.minSpinner);
        maxSpinner = (Spinner) view.findViewById(R.id.maxSpinner);
        initSpinnerAdapters(1, test.getQuestions().size());

        // FILTER PODLA SKORE OTAZKY
        vsetkySkoreRadioButton = (RadioButton) view.findViewById(R.id.vsetkySkoreRadioButton);
        vybraneSkoreRadioButton = (RadioButton) view.findViewById(R.id.vybraneSkoreRadioButton);
        checkboxyLayout = (LinearLayout) view.findViewById(R.id.checkboxyLayout);
        cervenaCheckbox = (CheckBox) view.findViewById(R.id.cervenaCheckBox);
        cervenaCheckbox.setOnClickListener(filterParamListener);
        bielaCheckBox = (CheckBox) view.findViewById(R.id.bielaCheckBox);
        bielaCheckBox.setOnClickListener(filterParamListener);
        zltaCheckBox = (CheckBox) view.findViewById(R.id.zltaCheckBox);
        zltaCheckBox.setOnClickListener(filterParamListener);
        oranzovaCheckBox = (CheckBox) view.findViewById(R.id.oranzovaCheckBox);
        oranzovaCheckBox.setOnClickListener(filterParamListener);
        zelenaCheckBox = (CheckBox) view.findViewById(R.id.zelenaCheckBox);
        zelenaCheckBox.setOnClickListener(filterParamListener);

        vsetkySkoreRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // schovat rozsah layout
                checkboxyLayout.setVisibility(View.GONE);
                odfiltrujOtazky();
            }
        });
        vybraneSkoreRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkboxyLayout.setVisibility(View.VISIBLE);
                odfiltrujOtazky();
            }
        });

        zacniTestButton = (Button) view.findViewById(R.id.zacniTestButton);
        zacniTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (vybrane == null) {
                    odfiltrujOtazky();
                }

                InstanciaTestu it = new InstanciaTestu(test, vybrane);

                Intent intent = new Intent(getActivity(), QuestionActivity.class);
                intent.putExtra(QuestionActivity.TEST_INSTANCIA_BUNDLE_KEY, it);
                startActivity(intent);
            }
        });
        zrusitTestButton = (Button) view.findViewById(R.id.zrusitTestButton);
        zrusitTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void odfiltrujOtazky() {
        HashMap<Integer, Integer> mapa = new HashMap<>();
        mapa.put(-1, 0);
        mapa.put(0, 0);
        mapa.put(1, 0);
        mapa.put(2, 0);
        mapa.put(3, 0);

        int min;
        int max;
        if (vsetkyRozsahRadioButton.isChecked()) {
            min = 1;
            max = test.getQuestions().size();
        } else {
            min = (int) minSpinner.getSelectedItem();
            max = (int) maxSpinner.getSelectedItem();
            if (min > max) {
                minSpinner.setSelection(max - 1); // najmensia hodnota v spinneri je 1 a najvyssia pocet otazok v teste
                maxSpinner.setSelection(min - 1); // nastavuje sa index v array adapteri, nie hodnota
                int pom = min;
                min = max;
                max = pom;
            }
        }
        //System.out.println("vybrane hodnoty min: " + min + " max: " + max);
        App.zaloguj(App.DEBUG,TAG,"vybrane hodnoty min: " + min + " max: " + max);
        vybrane = new ArrayList<>();
        for (int i = min - 1; i < max; i++) { // 1..10 == 0..9 v liste
            int stat = test.getQuestions().get(i).getStat(); // otazky v teste su v rovnakom poradi ako v teststats
            if (vyhovujeCheckboxomOtazka(stat)) {
                vybrane.add(test.getQuestions().get(i));
            }
            mapa.put(stat, mapa.get(stat) + 1);
        }

        cervenaCheckbox.setText("+" + mapa.get(-1));
        bielaCheckBox.setText("+" + mapa.get(0));
        zltaCheckBox.setText("+" + mapa.get(1));
        oranzovaCheckBox.setText("+" + mapa.get(2));
        zelenaCheckBox.setText("+" + mapa.get(3));
        //System.out.println("vybranych otazok celkom: " + vybrane.size());
        App.zaloguj(App.DEBUG,TAG,"vybranych otazok celkom: " + vybrane.size());
        sumarTextView.setText("Vybraných otázok celkom: " + vybrane.size());
    }

    private boolean vyhovujeCheckboxomOtazka(int stat) {
        if (vsetkySkoreRadioButton.isChecked()) {
            // berieme vsetky farby
            return true;
        }
        if (stat == 0 && bielaCheckBox.isChecked()) {
            return true;
        }
        if (stat == 1 && zltaCheckBox.isChecked()) {
            return true;
        }
        if (stat == 2 && oranzovaCheckBox.isChecked()) {
            return true;
        }
        if (stat < 0 && cervenaCheckbox.isChecked()) {
            return true;
        }
        if (stat > 2 && zelenaCheckBox.isChecked()) {
            return true;
        }

        return false;
    }


    private void initSpinnerAdapters(int min, int max) {
        List<Integer> spinnerVals = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            spinnerVals.add(i);
        }
        ArrayAdapter<Integer> minSpinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerVals);
        minSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minSpinner.setAdapter(minSpinnerAdapter);
        minSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                odfiltrujOtazky();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        minSpinner.setSelection(0);

        ArrayAdapter<Integer> maxSpinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerVals);
        maxSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maxSpinner.setAdapter(maxSpinnerAdapter);
        maxSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                odfiltrujOtazky();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        maxSpinner.setSelection(spinnerVals.size() - 1);
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Výber otázok");
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
