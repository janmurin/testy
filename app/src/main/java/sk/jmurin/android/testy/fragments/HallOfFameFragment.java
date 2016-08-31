package sk.jmurin.android.testy.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sk.jmurin.android.testy.App;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.Secrets;
import sk.jmurin.android.testy.entities.SkoreStats;
import sk.jmurin.android.testy.entities.Test;
import sk.jmurin.android.testy.gui.MainActivity;
import sk.jmurin.android.testy.utils.EventBusEvents;
import sk.jmurin.android.testy.utils.NetworkUtils;

/**
 * Created by jan.murin on 26-Aug-16.
 */
public class HallOfFameFragment extends Fragment {

    public static final String TAG = HallOfFameFragment.class.getSimpleName();
    public static final int DRAWER_POS = 1;
    public static final String TESTYMAPA = "testymapa";
    private Map<Integer, Test> testyMapa;
    private Spinner vybranyTestSpinner;
    private RecyclerView rv;
    private Map<Integer, List<SkoreStats>> skoreStatsMap = new HashMap<>();
    private int vybranySpinnerItemPos;
    private List<Integer> spinnerTestIds;
    private SharedPreferences sharedPref;

    public static HallOfFameFragment newInstance(Map<Integer, Test> testy) {
        HallOfFameFragment fragment = new HallOfFameFragment();
        Bundle args = new Bundle();
        args.putSerializable(TESTYMAPA, (Serializable) testy);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Sieň slávy");
        Bundle args = getArguments();
        if (args != null) {
            testyMapa = (Map<Integer, Test>) args.getSerializable(TESTYMAPA);
            // nainicializovanie mapy na tolko testov, kolko je pri volani onCreate testov v testyMapa
            for (Integer testID : testyMapa.keySet()) {
                skoreStatsMap.put(testID, new ArrayList<SkoreStats>());
            }
            if (testyMapa.isEmpty()) {
                throw new RuntimeException("pocet testov musi byt aspon 1.");
            }
        } else {
            throw new RuntimeException("HallOfFame fragment without arguments!!!");
        }
        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hall_of_fame, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.zaloguj(App.DEBUG, TAG, "onViewCreated(view, savedInstanceState);");
        vybranyTestSpinner = (Spinner) view.findViewById(R.id.vybranyTestSpinner);
        final List<String> spinnerVals = new ArrayList<>();
        spinnerTestIds = new ArrayList<>(); // aby sme mohli vyhladavat nakliknuty test podla idcka a nie podla mena
        for (Integer testID : testyMapa.keySet()) {
            spinnerVals.add(testyMapa.get(testID).getName());
            spinnerTestIds.add(testID);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerVals);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vybranyTestSpinner.setAdapter(spinnerAdapter);
        vybranyTestSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vybranySpinnerItemPos = position;
                showSkoreTable(spinnerTestIds.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        rv = (RecyclerView) view.findViewById(R.id.recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));

        ImageView headerImageView = (ImageView) view.findViewById(R.id.halloffameHeaderImageView);

        Glide.with(headerImageView.getContext())
                .load(R.drawable.sien_slavy)
                .fitCenter()
                .into(headerImageView);
    }

    private void showSkoreTable(Integer testID) {
        App.zaloguj(App.DEBUG, TAG, "showSkoreTable(" + testID + ")");
        rv.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(), skoreStatsMap.get(testID)));
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Log.d(TAG, "onActivityCreated(savedInstanceState);");
        App.zaloguj(App.DEBUG, TAG, "onActivityCreated(savedInstanceState);");
    }

    /**
     * tahaju sa data zo sharedPreferences. ak su data starsie ako 30 minut tak sa tahaju z webu
     * ak sa nepodari z webu stiahnut tak sa zobrazi hlaska ze data nie su aktualne
     */
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        App.zaloguj(App.DEBUG, TAG, "onStart");

        // zistime kedy boli data naposledy aktualizovane
        long latestSkoreStatsTimestamp = sharedPref.getLong(getString(R.string.latest_skore_stats_timestamp), 0L);
        String statsJSON = sharedPref.getString(getString(R.string.latest_skore_stats), null);

        if (System.currentTimeMillis() - latestSkoreStatsTimestamp > 30 * 60 * 1000) {
            // stare data mame, potrebujeme aktualizovat
            if (NetworkUtils.isConnected(getActivity())) {
                // refresh statistiku zo servera
                Request request = new Request.Builder()
                        .url(Secrets.SKORE_STATS_API_URL)
                        .build();

                final OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        App.zaloguj(App.DEBUG, TAG, "onFailure");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String jsonResponse = response.body().string();
                            ObjectMapper mapper = new ObjectMapper();
                            List<SkoreStats> skores;
                            try {
                                skores = mapper.readValue(jsonResponse, new TypeReference<List<SkoreStats>>() {
                                });
                                EventBus.getDefault().post(new EventBusEvents.SkoreStatsDownloaded(skores));
                                // ulozime najnovsie skore stats, predpokladam ze sa vojdu do stringu do shared preferences
                                // TODO: osetrit velkost json stringu
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.latest_skore_stats), jsonResponse);
                                editor.putLong(getString(R.string.latest_skore_stats_timestamp), System.currentTimeMillis());
                                editor.commit();

                            } catch (Exception e) {
                                e.printStackTrace();
                                EventBus.getDefault().post(new EventBusEvents.DownloadError("Server odpovedal, ale nepodarilo sa nacitat data zo servera."));
                            }
                        } else {
                            EventBus.getDefault().post(new EventBusEvents.DownloadError("Server neodpovedal, data nie su dostupne."));
                        }
                    }
                });
            } else {
                if (latestSkoreStatsTimestamp == 0) {
                    Toast.makeText(getActivity(), "Nepodarilo sa pripojiť k internetu. Nie sú dostupné aktuálne dáta.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Nepodarilo sa pripojiť k internetu. Posledná aktualizácia " + App.sdf.format(new Date(latestSkoreStatsTimestamp)) + ".", Toast.LENGTH_SHORT).show();
                    refreshSkoreStats(statsJSON);
                }
            }
        } else {
            App.zaloguj(App.DEBUG, TAG, "mame akoze aktualne data, refreshujem zo shared preferences");
            refreshSkoreStats(statsJSON);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(final EventBusEvents.SkoreStatsDownloaded skoreDownloaded) {
        App.zaloguj(App.DEBUG, TAG, "onDownloadEvent: SkoreStatsDownloaded");
        refreshSkoreStats(skoreDownloaded.skores);
    }

    private void refreshSkoreStats(String skoresJSON) {
        // mame "aktualne data", aktualizujeme zo sharedPreferences
        ObjectMapper mapper = new ObjectMapper();
        List<SkoreStats> skores;
        try {
            skores = mapper.readValue(skoresJSON, new TypeReference<List<SkoreStats>>() {
            });
            refreshSkoreStats(skores);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshSkoreStats(List<SkoreStats> skores) {
        // resetneme stare skore staty
        for (Integer testID : skoreStatsMap.keySet()) {
            skoreStatsMap.put(testID, new ArrayList<SkoreStats>());
        }
        // skore staty, teraz ich nahadzeme do nasej mapy
        for (SkoreStats ss : skores) {
            if (skoreStatsMap.keySet().contains(ss.testID)) {
                // tento stat nas zaujima lebo je medzi nasimi aktivnymi testami v mape
                skoreStatsMap.get(ss.testID).add(ss);
            }
        }
        vybranyTestSpinner.setSelection(vybranySpinnerItemPos); // zlyha ak sme mali v oncreate prazdny zoznam testov
        showSkoreTable(spinnerTestIds.get(vybranySpinnerItemPos));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(final EventBusEvents.DownloadError downloadError) {
        App.zaloguj(App.DEBUG, TAG, downloadError.s);
        Toast.makeText(getActivity(), downloadError.s, Toast.LENGTH_SHORT).show();
    }


    public static class SimpleStringRecyclerViewAdapter extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        //        private final MainActivity context;
        private final String[][] listItemValues;
        private int mBackground;

        public SimpleStringRecyclerViewAdapter(Context context, List<SkoreStats> skoreStats) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
//            this.context = (MainActivity) context;
            mBackground = mTypedValue.resourceId;
            if (skoreStats.isEmpty()) {
                listItemValues = new String[1][2];
                listItemValues[0][0] = "žiadne dáta";
                listItemValues[0][1] = " ";
            } else {
                listItemValues = new String[skoreStats.size()][2];
                int i = 0;
                for (SkoreStats ss : skoreStats) {
                    listItemValues[i][0] = ss.username;
                    listItemValues[i][1] = ss.skore + " %";
                    i++;
                }
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
            holder.numberTextView.setText((position + 1) + ".");
            holder.testNameTextView.setText(listItemValues[position][0]);
            holder.scoreTextView.setText(listItemValues[position][1]);
        }

        @Override
        public int getItemCount() {
            return listItemValues.length;
        }
    }
}
/*

CREATE TABLE statistika(
   id INT NOT NULL AUTO_INCREMENT,
   username VARCHAR(10) NOT NULL,
   deviceID VARCHAR(40) NOT NULL,
   testID INT NOT NULL,
    testVersion INT NOT NULL,
    stats VARCHAR(20000) NOT NULL,
    skore INT NOT NULL,
    time_inserted DATETIME,
    time_created DATETIME,
   PRIMARY KEY ( id )
   );

create table skore(
id INT not null auto_increment,
deviceID VARCHAR(40) NOT NULL,
testID INT NOT NULL,
skore INT NOT NULL,
PRIMARY KEY ( id )
);


* */
