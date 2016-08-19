package sk.jmurin.android.testy;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import sk.jmurin.android.testy.content.DataContract;
import sk.jmurin.android.testy.content.Defaults;
import sk.jmurin.android.testy.content.MyContentProvider;
import sk.jmurin.android.testy.entities.Answer;
import sk.jmurin.android.testy.entities.Question;
import sk.jmurin.android.testy.entities.Statistika;
import sk.jmurin.android.testy.entities.Test;
import sk.jmurin.android.testy.fragments.StatistikaDialogFragment;
import sk.jmurin.android.testy.fragments.TestParametersDialogFragment;

public class QuestionActivity extends AppCompatActivity {

    public static final String TAG = QuestionActivity.class.getSimpleName();
    public static final String TEST_INSTANCIA_BUNDLE_KEY = "test_instance_bundle_key";
    private static final int STATISTIKA_UPDATE_TOKEN = 0;
    private InstanciaTestu instanciaTestu;
    private Button dalejButton;
    private Button predButton;
    private Question aktualnaOtazka;
    private TextView otazkaTextView;
    private ListView odpovedeListView;
    private OdpovedeListViewAdapter odpovedeListViewAdapter;
    private boolean odpovedeListViewClickable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        dalejButton = (Button) findViewById(R.id.dalejButton);
        dalejButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dalejButtonActionPerformed(v);
            }
        });
        predButton = (Button) findViewById(R.id.predButton);
        predButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                predButtonActionPerformed(v);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        otazkaTextView = (TextView) findViewById(R.id.otazkaTextView);
        odpovedeListView = (ListView) findViewById(R.id.odpovedeListView);

        if (savedInstanceState != null) {
            instanciaTestu = (InstanciaTestu) savedInstanceState.get(TEST_INSTANCIA_BUNDLE_KEY);
        } else {
            instanciaTestu = (InstanciaTestu) getIntent().getSerializableExtra(TEST_INSTANCIA_BUNDLE_KEY);
        }

        loadAktualnaOtazka();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(TEST_INSTANCIA_BUNDLE_KEY, instanciaTestu);
    }

    private void loadAktualnaOtazka() {
        System.out.println("loadAktualnaOtazka: ziskavam otazku s id: " + instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][0]);
        aktualnaOtazka = getOtazkaPodlaID(instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][0]);
        setTitle(instanciaTestu.test.name + " " + (instanciaTestu.aktUlohaIdx + 1) + "/" + instanciaTestu.idckaUloh.length);
        refreshOtazkaLabels();
        if (instanciaTestu.ohodnotene[instanciaTestu.aktUlohaIdx]) {
            skontrolujOdpovedeAVykresli();
        } else {
            setClickableOdpovede(true);
        }
    }

    private void refreshOtazkaLabels() {
        Log.d(TAG, "refreshOtazkaLabels");
        otazkaTextView.setText(aktualnaOtazka.question);

        odpovedeListViewAdapter = new OdpovedeListViewAdapter(this, R.layout.answer_list_item, aktualnaOtazka.answers, instanciaTestu.odpovedeOrder[instanciaTestu.aktUlohaIdx]);
        odpovedeListView.setAdapter(odpovedeListViewAdapter);
        for (int i = 0; i < instanciaTestu.POCET_ODPOVEDI; i++) {
            odpovedeListViewAdapter.setBackgroundColor(Color.WHITE, i);
        }
        odpovedeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "odpoved pos: " + position + " clicked");
                if (!odpovedeListViewClickable) {
                    Log.d(TAG, "odpovedeListView currently not clickable");
                    return;
                }
                instanciaTestu.zaskrtnute[instanciaTestu.aktUlohaIdx][position] = 1 - instanciaTestu.zaskrtnute[instanciaTestu.aktUlohaIdx][position];
                if (instanciaTestu.zaskrtnute[instanciaTestu.aktUlohaIdx][position] == 1) {
                    odpovedeListViewAdapter.setBackgroundColor(Color.CYAN, position);
                } else {
                    odpovedeListViewAdapter.setBackgroundColor(Color.WHITE, position);
                }
            }
        });
        Log.d(TAG, "Otazka: " + (instanciaTestu.aktUlohaIdx + 1) + "/" + instanciaTestu.idckaUloh.length);
    }

    public void dalejButtonActionPerformed(View view) {
        System.out.println("dalejButtonActionPerformed");
        // ak je otazka uz ohodnotena, tak sa rovno vykreslia spravne odpovede
        if (instanciaTestu.ohodnotene[instanciaTestu.aktUlohaIdx]) {
            instanciaTestu.aktUlohaIdx++;
            if (instanciaTestu.aktUlohaIdx == instanciaTestu.idckaUloh.length) {
                // iba ak sme v uciacom mode mozeme prechadzat takto
                if (instanciaTestu.isUcenieSelected()) {
                    instanciaTestu.aktUlohaIdx = 0;
                } else {
                    instanciaTestu.aktUlohaIdx--;
                    // ake sme tu tak urcite su vsetky uloh ohodnotene a mozeme zobrazit statistiku
                    // sme na poslednej ulohe a klikli sme na dalej
                    zobrazStatistikaActivity();
                    //odosliStatistikuNaServer();
                    return;
                }
            } else {
                instanciaTestu.aktUlohaIdx %= instanciaTestu.idckaUloh.length;
            }
            loadAktualnaOtazka();
        } else {
            // neni este ohodnotena, tak sa musi ohodnotit a az po dalsom stlaceni sa posunie dalej
            boolean uspesne = skontrolujOdpovedeAVykresli();
            if (uspesne) {
                instanciaTestu.uspesnych++;
            }
            System.out.println("uspesna odpoved= " + uspesne);
            instanciaTestu.ohodnotene[instanciaTestu.aktUlohaIdx] = true;
            updateStatistika(uspesne);
        }
    }

    private Question getOtazkaPodlaID(int id) {
        return instanciaTestu.test.questions.get(id);
    }

    private void zobrazStatistikaActivity() {
        System.out.println("zobrazStatistikaActivity");
        FragmentManager fragmentManager = getSupportFragmentManager();
        StatistikaDialogFragment newFragment = StatistikaDialogFragment.newInstance(instanciaTestu);
        newFragment.show(fragmentManager, StatistikaDialogFragment.TAG);
    }


    public void predButtonActionPerformed(View view) {
        System.out.println("predButtonActionPerformed");
        instanciaTestu.aktUlohaIdx--;
        if (instanciaTestu.aktUlohaIdx < 0) {
            // iba ak sme v uciacom mode mozeme prechadzat takto
            if (instanciaTestu.isUcenieSelected()) {
                instanciaTestu.aktUlohaIdx = instanciaTestu.idckaUloh.length - 1;
            } else {
                instanciaTestu.aktUlohaIdx++;
                return;
            }
        }
        loadAktualnaOtazka();
    }

    private void updateStatistika(boolean uspesne) {
        if (instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][1] >= 0) { // pytame sa na povodny stav
            if (uspesne) {
                if (instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][1] < 3) {// pytame sa na povodny stav
                    // sme uspesni a boli sme oranzovi, tak zmenime na zelenu
                    instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][2] = instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][1] + 1;  // zapisujeme do aktualneho stavu
                } else {
                    // sme uspesni a boli sme zeleny tak ostaneme zeleny s 3kou
                    instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][2] = 3;  // MUSIME ZAPISAT HODNOTU, POLE SA INICIALIZUJE S NULAMI
                }
            } else {
                instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][2] = instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][1] - 1;     // zapisujeme do aktualneho stavu
            }
        } else {
            if (uspesne) {
                // bolo -1 a uhadli sme, tak davame hned na 1
                System.out.println("bolo -1 a uhadli sme, tak davame hned na 1");
                instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][2] = 1;     // zapisujeme do aktualneho stavu
            } else {
                // nie sme uspesni takze do aktualneho stavu musime zapisat -1
                instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][2] = -1;
            }
        }
        for (int i = 0; i < instanciaTestu.idckaUloh.length; i++) {
            System.out.print(String.format("%3s", Integer.toString(instanciaTestu.idckaUloh[i][1])));
        }
        System.out.println();
        for (int i = 0; i < instanciaTestu.idckaUloh.length; i++) {
            System.out.print(String.format("%3s", Integer.toString(instanciaTestu.idckaUloh[i][2])));
        }
        System.out.println();

//        // updatneme statistiku
        int novyStat = instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][2];
        int db_id = instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][3];
        // update testStatu vytiahnuteho z databazy TODO: bude mat homefragment referenciu na tuto zmenu?
        instanciaTestu.testStats.stats.get(instanciaTestu.idckaUloh[instanciaTestu.aktUlohaIdx][0]).stat = novyStat;
        // update hodnoty v databaze
        Uri uri = DataContract.QuestionStats.CONTENT_URI
                .buildUpon()
                .appendPath(String.valueOf(db_id))
                .build();
        ContentValues values = new ContentValues();
        values.put(DataContract.QuestionStats.STAT, novyStat);
        System.out.println("updating db with " + novyStat + " for id " + db_id);

        AsyncQueryHandler updateHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onUpdateComplete(int token, Object cookie, int result) {
                super.onUpdateComplete(token, cookie, result);
                Log.d(TAG, "onUpdateComplete token: " + token + " result: " + result);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);
                Log.d(TAG, "onQueryComplete token=" + token);
                if (cursor.moveToNext()) {
                    int stat = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.STAT));
                    int db_id = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats._ID));
                    int question_test_id = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.QUESTION_TEST_ID));
                    String test_name = cursor.getString(cursor.getColumnIndex(DataContract.QuestionStats.TEST_NAME));
                    int test_version = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.TEST_VERSION));
                    System.out.println("stat=" + stat);
                    System.out.println("db_id=" + db_id);
                    System.out.println("question_test_id=" + question_test_id);
                    System.out.println("test_name=" + test_name);
                    System.out.println("test_version=" + test_version);
                }
                cursor.close();
            }
        };
        updateHandler.startUpdate(STATISTIKA_UPDATE_TOKEN, Defaults.NO_COOKIE, uri, values, Defaults.NO_SELECTION, Defaults.NO_SELECTION_ARGS);
        updateHandler.startQuery(2, Defaults.NO_COOKIE, uri, null, null, Defaults.NO_SELECTION_ARGS, null);
    }

    private void setClickableOdpovede(boolean value) {
        //odpovedeListView.setClickable(value);
        odpovedeListViewClickable = value;
    }

    private boolean skontrolujOdpovedeAVykresli() {
        System.out.println("skontrolujOdpovedeAVykresli");
        int nespravnaOdpovedFarba = Color.RED;
        int spravnaOdpovedFarba = Color.GREEN;
        int spravnaNeoznacenaFarba = Color.MAGENTA;
        boolean uspesne = true;

        if (instanciaTestu.isUcenieSelected()) {
            nespravnaOdpovedFarba = spravnaOdpovedFarba;
            spravnaNeoznacenaFarba = spravnaOdpovedFarba;
        }
        //System.out.print("zaskrtnute: ");
        for (int i = 0; i < instanciaTestu.POCET_ODPOVEDI; i++) {
            //System.out.print(String.format("%3s", test.zaskrtnute[test.aktUlohaIdx][i]));
            // vyhodnotime itu odpoved, pozerame sa na odpovedeOrder lebo su v pomiesanom poradi
            if (instanciaTestu.zaskrtnute[instanciaTestu.aktUlohaIdx][i] == 1) {
                if (aktualnaOtazka.answers.get(instanciaTestu.odpovedeOrder[instanciaTestu.aktUlohaIdx][i]).isCorrect) {
                    // je oznacena spravna odpoved
                    //odpovedeTextView[i].setBackgroundColor(spravnaOdpovedFarba);
                    odpovedeListViewAdapter.setBackgroundColor(spravnaOdpovedFarba, i);
                } else {
                    // je oznacena nespravna odpoved
                    if (!instanciaTestu.ohodnotene[instanciaTestu.aktUlohaIdx]) {
                        instanciaTestu.pocetMinusBodov--;
                    }
                    uspesne = false;
                    //odpovedeTextView[i].setBackgroundColor(nespravnaOdpovedFarba);
                    odpovedeListViewAdapter.setBackgroundColor(nespravnaOdpovedFarba, i);
                }
            } else {
                if (aktualnaOtazka.answers.get(instanciaTestu.odpovedeOrder[instanciaTestu.aktUlohaIdx][i]).isCorrect) {
                    // nie je oznacena spravna odpoved
                    if (!instanciaTestu.ohodnotene[instanciaTestu.aktUlohaIdx]) {
                        instanciaTestu.pocetMinusBodov--;
                    }
                    uspesne = false;
                    //odpovedeTextView[i].setBackgroundColor(spravnaNeoznacenaFarba);
                    odpovedeListViewAdapter.setBackgroundColor(spravnaNeoznacenaFarba, i);
                }
            }
        }
        System.out.println("skontrolujOdpovedeAVykresli: minus bodov " + instanciaTestu.pocetMinusBodov);
        //System.out.println();
        setClickableOdpovede(false);
        return uspesne;
    }

    public static class OdpovedeListViewAdapter extends ArrayAdapter<Answer> {
        private final QuestionActivity context;
        public List<Answer> answers;
        private final String[] orderings = {"a)", "b)", "c)", "d)", "e)", "f)", "g)", "h)"};
        public int[] ordering;
        private LayoutInflater inflater;
        private int[] backgroundColors;

        public OdpovedeListViewAdapter(Context context, int resource, List<Answer> answers, int[] ordering) {
            super(context, resource);
            this.context = (QuestionActivity) context;
            this.answers = answers;
            this.ordering = ordering;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            backgroundColors = new int[answers.size()];
            if (answers.size() > orderings.length) {
                throw new RuntimeException("prilis vela odpovedi na otazku. max 8");
            }
        }

        public OdpovedeListViewAdapter(Context context, int resource) {
            super(context, resource);
            this.context = (QuestionActivity) context;
        }

        public void setBackgroundColor(int backgroundColor, int position) {
            backgroundColors[position] = backgroundColor;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return answers.size();
        }

        @Override
        public Answer getItem(int position) {
            return answers.get(ordering[position]);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null)
                vi = inflater.inflate(R.layout.answer_list_item, null);
            TextView answerTextView = (TextView) vi.findViewById(R.id.answerTextView);
            answerTextView.setText(answers.get(ordering[position]).text);
            answerTextView.setBackgroundColor(backgroundColors[position]);
            TextView orderTextView = (TextView) vi.findViewById(R.id.orderTextView);
            orderTextView.setText(orderings[position]);
            orderTextView.setBackgroundColor(backgroundColors[position]);
            vi.setBackgroundColor(backgroundColors[position]);

            return vi;
        }
    }


}
