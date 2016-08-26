package sk.jmurin.android.testy.entities;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import sk.jmurin.android.testy.App;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.content.DataContract;

/**
 * Created by jan.murin on 25-Aug-16.
 */

public class Parser {

    public static final String TAG = Parser.class.getSimpleName();
    public static final String TEST_FILE_PREFIX = "test_";

    /**
     * nacita z assetov testy, ulozi ich do adresara aplikacie a inicializuje statistiku k otazkam v databaze
     *
     * @param activity
     * @return
     */
    public static Map<Integer, Test> initTestsFromAssetsGetTests(FragmentActivity activity) {
        //Log.d(TAG, "initializujem testy z assetov");
        App.zaloguj(App.DEBUG,TAG,"initializujem testy z assetov");
// iba parser vie nastavovat hodnoty do testu lebo je v balicku entities
        String[] fileNames = null;
        try {
            fileNames = activity.getAssets().list("tests");
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: co robit ked nenacita ziadne asset testy?
        }
        //Log.d(TAG, "assets [tests] dir filenames: " + Arrays.toString(fileNames));
        App.zaloguj(App.DEBUG,TAG,"assets [tests] dir filenames: " + Arrays.toString(fileNames));
        Map<Integer, Test> tests = new HashMap<>();

        for (String fileName : fileNames) {
            if (!fileName.startsWith(TEST_FILE_PREFIX)) {
                continue;
            }

            // 1. naparsovat z json suboru test
            Test test;
            String testJSON;
            ObjectMapper mapper = new ObjectMapper();
            try {
                InputStream is = activity.getAssets().open("tests/" + fileName);
                testJSON = getStringUTFFromInputStream(is);
                is.close();
                //Log.d(TAG, "nacitany json string z asset suboru [" + fileName + "]: " + testJSON);
                App.zaloguj(App.DEBUG,TAG,"nacitany json string z asset suboru [" + fileName + "]: " + testJSON);
                test = mapper.readValue(testJSON, Test.class);
                // nastavime kazdej otazke idcko podla toho v akom poradi bolo nacitane zo suboru
                for (int i = 0; i < test.getQuestions().size(); i++) {
                    test.getQuestions().get(i).setTestQuestionIndex(i);
                }
                tests.put(test.getId(), test);
            } catch (IOException e) {
                e.printStackTrace();
                // neuspesne parsovanie, dalej to nema zmysel
                continue;
            }

            // 2.ulozit test do privatneho adresara
            try {
                String filename = TEST_FILE_PREFIX + test.getId();
                //Log.d(TAG, "ukladam nacitany json string do privatneho suboru [" + filename + "]");
                App.zaloguj(App.DEBUG,TAG,"ukladam nacitany json string do privatneho suboru [" + filename + "]");
                FileOutputStream outputStream;
                outputStream = activity.openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(testJSON.getBytes());
                outputStream.close();
                //Log.d(TAG, "ulozeny test " + test);
                App.zaloguj(App.DEBUG,TAG,"ulozeny test " + test);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            // 3. ulozit do databazy statistiky
            Uri insertUri = DataContract.QuestionStats.CONTENT_URI
                    .buildUpon()
                    .build();
            List<ContentValues> contentValues = new ArrayList<>();
            int question_test_id = 0;
            for (Question q : test.getQuestions()) {
                q.setStat(0); // defaultna stat hodnota
                ContentValues values = new ContentValues();
                values.put(DataContract.QuestionStats.TEST_QUESTION_INDEX, question_test_id);
                values.put(DataContract.QuestionStats.STAT, q.getStat());
                values.put(DataContract.QuestionStats.TEST_ID, test.getId());
                contentValues.add(values);
                question_test_id++;
            }
            int inserted = activity.getContentResolver().bulkInsert(insertUri, contentValues.toArray(new ContentValues[contentValues.size()]));
            //Log.d(TAG, "inserted QuestionStats rows: " + inserted);
            App.zaloguj(App.DEBUG,TAG,"inserted QuestionStats rows: " + inserted);
        }

        loadDatabaseDataIntoTests(tests, activity);

        return tests;
    }

    public static Map<Integer, Test> loadTests(FragmentActivity activity) {
        //Log.d(TAG, "loadujem testy z app dir");
        App.zaloguj(App.DEBUG,TAG,"loadujem testy z app dir");
        String dirPath = activity.getFilesDir().getAbsolutePath();
        File projDir = new File(dirPath);
        if (!projDir.exists())
            projDir.mkdirs();
        File[] files = projDir.listFiles();
        Map<Integer, Test> testy = new HashMap<>();

        for (File f : files) {
            if (!f.getName().startsWith(TEST_FILE_PREFIX)) {
                continue;
            }
            // 1. naparsovat z json suboru test
            Test test;
            ObjectMapper mapper = new ObjectMapper();
            try {
                test = mapper.readValue(f, Test.class);
                //Log.d(TAG, "nacitany json test: " + test);
                App.zaloguj(App.DEBUG,TAG,"nacitany json test: " + test);
                // nastavime kazdej otazke idcko podla toho v akom poradi bolo nacitane zo suboru
                // spolieham sa nato ze vzdy v rovnakom poradi nacita vsetky data, inak by to nebolo konzistentne s databazou
                for (int i = 0; i < test.getQuestions().size(); i++) {
                    test.getQuestions().get(i).setTestQuestionIndex(i);
                }
                testy.put(test.getId(), test);
            } catch (IOException e) {
                e.printStackTrace();
                // neuspesne parsovanie, dalej to nema zmysel
                continue;
            }
        }

        loadDatabaseDataIntoTests(testy, activity);

        return testy;
    }

    private static void loadDatabaseDataIntoTests(Map<Integer, Test> testy, FragmentActivity activity) {
        // ked mame vsetky testy nacitane, selectneme statistiky z databazy aby sme vedeli idcka pre updaty
        Cursor cursor = activity.getContentResolver().query(DataContract.QuestionStats.CONTENT_URI, null, null, null, DataContract.QuestionStats._ID);
        List<QuestionData> data = getStatsFrom(cursor);
        for (QuestionData dd : data) {
            // moze sa stat ze ked sa zmaze privatny subor s json testom a sa nezmazu statistiky k nemu tak teraz hodi vynimku ze nevie priradit statistiku k testu
            // alebo len sa nebude zhodovat idcko testu v databaze s idckom testu v json teste
            try {
                testy.get(dd.test_id).getQuestions().get(dd.test_question_index).setStat(dd.stat);
                testy.get(dd.test_id).getQuestions().get(dd.test_question_index).setDbID(dd.db_id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static List<QuestionData> getStatsFrom(Cursor cursor) {
        //Log.d(TAG, "getStatsFrom(Cursor cursor)");
        App.zaloguj(App.DEBUG,TAG,"getStatsFrom(Cursor cursor)");
        List<QuestionData> data = new ArrayList<>();
        while (cursor.moveToNext()) {
            int stat = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.STAT));
            int db_id = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats._ID));
            int test_question_index = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.TEST_QUESTION_INDEX));
            int test_id = cursor.getInt(cursor.getColumnIndex(DataContract.QuestionStats.TEST_ID));
            data.add(new QuestionData(stat, db_id, test_question_index, test_id));
        }
        cursor.close();
        return data;
    }

    public static String getStringUTFFromInputStream(InputStream is) {
        Scanner scanner = new Scanner(is, "utf-8");
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine());
        }
        return sb.toString();
    }
}
