package sk.jmurin.android.testy.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.jmurin.android.testy.R;

/**
 * Created by jan.murin on 18-Aug-16.
 */
public class TestParametersDialogFragment extends DialogFragment {

    public static final String TAG = TestParametersDialogFragment.class.getSimpleName();
    private RadioButton vsetkyRadioButton;
    private RadioButton vybraneRadioButton;
    private EditText minEditText;
    private EditText maxEditText;
    private CheckBox cervenaCheckbox;
    private CheckBox bielaCheckBox;
    private CheckBox zltaCheckBox;
    private CheckBox oranzovaCheckBox;
    private CheckBox zelenaCheckBox;
    private Button zacniTestButton;
    private Button zrusitTestButton;
    private LinearLayout rozsahLayout;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = ProgressDialogFragment.class.getSimpleName();
    private ArrayList<Integer> ids;
    private ProgressDialog dialog;
    private int testsSize;

    public TestParametersDialogFragment() {
        // Required empty public constructor
    }

    public static TestParametersDialogFragment newInstance(List<Integer> ids, int testsSize) {
        TestParametersDialogFragment fragment = new TestParametersDialogFragment();
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

    /**
     * The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.test_parameters, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView infoTextView = (TextView) view.findViewById(R.id.infoTextView);
        infoTextView.setText("");
//        vsetkyRadioButton = (RadioButton) view.findViewById(R.id.vsetkyRadioButton);
//        vybraneRadioButton = (RadioButton) view.findViewById(R.id.vybraneRadioButton);
//        rozsahLayout = (LinearLayout) view.findViewById(R.id.rozsahLayout);
//        minEditText = (EditText) view.findViewById(R.id.minEditText);
//        maxEditText = (EditText) view.findViewById(R.id.maxEditText);
//        cervenaCheckbox = (CheckBox) view.findViewById(R.id.cervenaCheckBox);
//        bielaCheckBox = (CheckBox) view.findViewById(R.id.bielaCheckBox);
//        zltaCheckBox = (CheckBox) view.findViewById(R.id.zltaCheckBox);
//        oranzovaCheckBox = (CheckBox) view.findViewById(R.id.oranzovaCheckBox);
//        zelenaCheckBox = (CheckBox) view.findViewById(R.id.zelenaCheckBox);
//        zacniTestButton = (Button) view.findViewById(R.id.zacniTestButton);
//        zrusitTestButton = (Button) view.findViewById(R.id.zrusitTestButton);
//
//        vsetkyRadioButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // schovat rozsah layout
//                rozsahLayout.setVisibility(View.GONE);
//            }
//        });
//        vybraneRadioButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rozsahLayout.setVisibility(View.VISIBLE);
//            }
//        });
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
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
