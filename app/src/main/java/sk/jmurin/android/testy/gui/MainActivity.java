package sk.jmurin.android.testy.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import sk.jmurin.android.testy.App;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.entities.Parser;
import sk.jmurin.android.testy.entities.Test;
import sk.jmurin.android.testy.fragments.HallOfFameFragment;
import sk.jmurin.android.testy.fragments.HomeFragment;
import sk.jmurin.android.testy.fragments.TutorialPagerFragment;
import sk.jmurin.android.testy.utils.EventBusEvents;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private NavigationView navigationView;
    private Map<Integer, Test> testy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d(TAG, "onCreate");


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadUsernameFromPreferences();
        // v tejto DEMO verzii budu testy napevno dane v assetoch,
        // v buducnosti sa spravi stahovanie testov z rest servera ak bude zaujem rozsirovat aplikaciu
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean assetsTestsInitialized = sharedPref.getBoolean(getString(R.string.assets_tests_initialized_preference_key), false);
        // TODO: progress dialog na nacitavanie testov zo suboru?
        if (!assetsTestsInitialized) {
            testy = Parser.initTestsFromAssetsGetTests(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.assets_tests_initialized_preference_key), true);
            boolean commit = editor.commit();
            //Log.d(TAG, "ulozenie assetsTestsInitialized=" + commit);
            App.zaloguj(App.DEBUG, TAG, "ulozenie assetsTestsInitialized=" + commit);
        } else {
            testy = Parser.loadTests(this);
        }
        //Log.d(TAG, "nacitanych testov: " + testy.size());
        App.zaloguj(App.DEBUG, TAG, "nacitanych testov: " + testy.size());

        if (!isUsernameRegistered()) {
            // spustime tutorial aby si zaregistroval meno, pravdepodobne je to prve spustenie aplikacie
            displayTutorial(false);
        } else {
            displayHome();
        }

    }

    private boolean isUsernameRegistered() {
        return !App.USERNAME.equals(App.DEFAULT_USERNAME);
    }


    @Override
    public void onBackPressed() {
        //Log.d(TAG, "onBackPressed");
        App.zaloguj(App.DEBUG, TAG, "onBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // ak sme hocikde okrem homeFragmentu tak backbutton nas vrati do homeFragmentu
            // z homeFragmentu ideme prec z aplikacie
            if (navigationView.getMenu().getItem(HomeFragment.DRAWER_POS).isChecked()) {
                // sme na home fragmente, backbuttonom vyjdeme z aplikacie
                super.onBackPressed();
            } else {
                // nie sme na home fragmente, backbuttonom sa chceme dostat na home fragment
                if (isUsernameRegistered()) {
                    // mame username takze mozeme zobrazit home fragment
                    displayHome();
                } else {
                    // nemame este username
                    // mal by teraz byt zobrazeny tutorial, tak nechceme z neho vyjst a chceme prinutit usera aby zadal nejake meno
                    // ak je user na fragmente kde sa zadava meno a stlaci backbutton tak ho musi vyhodit z aplikacie von
                    // ak je user v tutorialy na prvom alebo druhom screene, tak ho hodi na posledny kde treba zadat meno
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                    if (fragment instanceof TutorialPagerFragment) {
                        TutorialPagerFragment tpf = (TutorialPagerFragment) fragment;
                        int currentItem = tpf.mPager.getCurrentItem();
                        if (currentItem == tpf.NUM_PAGES - 1) {
                            // sme na poslednom iteme kde sa zadava meno, backbuttonom vyjst z aplikacie
                            super.onBackPressed();
                        } else {
                            // sme v tutorialy a stlacili sme back, tak sa posunieme na posledny kde treba zadat meno
                            // NUM_PAGES bude vzdy aspon 2, v tomto pripade bude urcite 3, pretoze 2 by bol len vtedy keby App.USERNAME nebol rovny DEFAULT_USERNAME
                            // a teda isUsernameRegistered() by vratilo true a nemoze sa stat zeby bol v premennej App.USERNAME nekonzistentny stav pretoze zmeny premennej bezia na hlavnom vlakne sekvencne
                            tpf.mPager.setCurrentItem(tpf.NUM_PAGES - 1);
                            // bolo by ok aj keby sme nic neurobili
                        }
                    } else {
                        // nahodou sme na nejakom inom fragmente, toto by sa nemalo nikdy stat
                        Toast.makeText(this, "Nepozname svoje meno. Toto by sa nemalo nikdy stat.", Toast.LENGTH_LONG).show();
                        displayTutorial(true);
                    }
                }
            }
        }
    }

    private void displayTutorial(boolean showLastTutorialItem) {
        if (showLastTutorialItem) {
            Toast.makeText(this, "Je potrebné zadať vaše meno.", Toast.LENGTH_SHORT).show();
        }
        showContentFragment(TutorialPagerFragment.getInstance(showLastTutorialItem), TutorialPagerFragment.TAG);
        navigationView.getMenu().getItem(TutorialPagerFragment.DRAWER_POS).setChecked(true);
    }

    private void displayHome() {
        showContentFragment(HomeFragment.newInstance(testy), HomeFragment.TAG);
        navigationView.getMenu().getItem(HomeFragment.DRAWER_POS).setChecked(true);
    }

    private void displayHallOfFame() {
        showContentFragment(HallOfFameFragment.newInstance(testy), HallOfFameFragment.TAG);
        navigationView.getMenu().getItem(HallOfFameFragment.DRAWER_POS).setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");
        App.zaloguj(App.DEBUG, TAG, "onResume");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // nepustime usera, iba k tutorialu dokym nezada svoje meno
        switch (id) {
            case R.id.home:
                if (!isUsernameRegistered()) {
                    displayTutorial(true);
                } else {
                    displayHome();
                }
                break;
            case R.id.hall_of_fame:
                if (!isUsernameRegistered()) {
                    displayTutorial(true);
                } else {
                    displayHallOfFame();
                }
                break;
            case R.id.tutorial:
                displayTutorial(false);
                break;
//            case R.id.settings:
//                break;
            default:
                //Log.d(TAG, "onNavigationItemSelected default case");
                App.zaloguj(App.DEBUG, TAG, "onNavigationItemSelected default case");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showContentFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment, tag)
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBusEvent(EventBusEvents.UsernameSelected usernameEvent) {
        //Log.d(TAG, "onEventBusEvent: UsernameSelected " + usernameEvent.meno);
        App.zaloguj(App.DEBUG, TAG, "onEventBusEvent: UsernameSelected " + usernameEvent.meno);
        displayHome();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBusEvent(EventBusEvents.ZavrietTutorial zavriet) {
        //Log.d(TAG, "onEventBusEvent(EventBusEvents.ZavrietTutorial zavriet)");
        App.zaloguj(App.DEBUG, TAG, "onEventBusEvent(EventBusEvents.ZavrietTutorial zavriet)");
        displayHome();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Log.d(TAG, "onStart");
        App.zaloguj(App.DEBUG, TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void loadUsernameFromPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        App.USERNAME = sharedPref.getString(getString(R.string.username_preference_key), App.DEFAULT_USERNAME);
        App.DEVICE_ID = sharedPref.getString(getString(R.string.device_id_preference_key), null);
        //Log.d(TAG, "nacitane username a uuid: " + App.USERNAME + " " + App.DEVICE_ID);
        App.zaloguj(App.DEBUG, TAG, "nacitane username a uuid: " + App.USERNAME + " " + App.DEVICE_ID);
    }

    //
//        if(savedInstanceState!=null){
//            getLoaderManager().restartLoader(LOCAL_STATS_LOADER_ID, Bundle.EMPTY, localCursorLoader);
//        }else {
//            getLoaderManager().initLoader(LOCAL_STATS_LOADER_ID, Bundle.EMPTY, localCursorLoader);
//        }
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//    private class LocalStatsCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {
//        MainActivity parent;
//
//        public LocalStatsCursorLoader(MainActivity main) {
//            this.parent = main;
//        }
//
//        @Override
//        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
//            if (id == LOCAL_STATS_LOADER_ID) {
//                System.out.println("creating stats loader");
//                CursorLoader loader = new CursorLoader(parent);
//                Uri uri = DataContract.QuestionStats.CONTENT_URI
//                        .buildUpon()
//                        .build();
//                loader.setUri(uri);
//                return loader;
//            }
//            return null;
//        }
//
//        @Override
//        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//            if (loader.getId() == LOCAL_STATS_LOADER_ID) {
//                App.testStatsMap = getStatsFrom(cursor);
//                Log.d(TAG, "stats loader finished, loaded stats size: " + App.testStatsMap.size());
//            }
//        }
//
//        @Override
//        public void onLoaderReset(Loader<Cursor> loader) {
//            Log.d(TAG,"onLoaderReset(Loader<Cursor> loader)");
//        }
//
//    }
//
//    private Map<String, TestStats> getStatsFrom(Cursor cursor) {
//        System.out.println("getStatsFrom actionPerformed");
//        Map<String, TestStats> stats = new HashMap<>();
//        while (cursor.moveToNext()) {
//            int stat = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.STAT));
//            int db_id = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats._ID));
//            int question_test_id = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.QUESTION_TEST_ID));
//            String test_name = cursor.getString(cursor.getColumnIndex(DataContract.QuestionStats.TEST_NAME));
//            int test_version = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.TEST_VERSION));
//            String tk = test_name + "_" + test_version;
//            if (!stats.keySet().contains(tk)) {
//                TestStats noveStats = new TestStats(test_name, test_version);
//                noveStats.addQuestionStat(question_test_id, stat, db_id);
//                stats.put(tk, noveStats);
//            } else {
//                TestStats testStats = stats.get(tk);
//                testStats.addQuestionStat(question_test_id, stat, db_id);
//            }
//        }
//        cursor.close();
//        return stats;
//    }
//
//    private LocalStatsCursorLoader localCursorLoader = new LocalStatsCursorLoader(this);
//    private static final int LOCAL_STATS_LOADER_ID = 2;

}
