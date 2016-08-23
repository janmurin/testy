package sk.jmurin.android.testy;

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

import sk.jmurin.android.testy.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // check if there's any retained content fragment, if not, show home
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment == null) {
            showContentFragment(new HomeFragment(), HomeFragment.TAG);
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

        switch(id){
            case R.id.home:break;
            case R.id.hall_of_fame:break;
            case R.id.tutorial:break;
            case R.id.settings:break;
            default: Log.d(TAG,"onNavigationItemSelected default case");
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

//    @Override
//    protected void onStart() {
//        super.onStart();
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        EventBus.getDefault().unregister(this);
//    }


}
