package sk.jmurin.android.testy.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import sk.jmurin.android.testy.App;
import sk.jmurin.android.testy.R;
import sk.jmurin.android.testy.gui.ZoomOutPageTransformer;
import sk.jmurin.android.testy.utils.EventBusEvents;


public class TutorialPagerFragment extends Fragment {

    public static int NUM_PAGES = 3;
    public static final String TAG = TutorialPagerFragment.class.getSimpleName();
    public static final int DRAWER_POS = 2;
    public ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    public static final String SHOW_LAST_ITEM = "showLastItem";
    private boolean showLastItem;

    public static TutorialPagerFragment getInstance(boolean showLastItem) {
        TutorialPagerFragment inst = new TutorialPagerFragment();
        Bundle args = new Bundle();
        args.putBoolean(SHOW_LAST_ITEM, showLastItem);
        inst.setArguments(args);
        return inst;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            showLastItem = args.getBoolean(SHOW_LAST_ITEM);
        } else {
            throw new RuntimeException("Pager item fragment without arguments!!!");
        }
        if (!App.USERNAME.equals(App.DEFAULT_USERNAME)) {
            // je zadany username, takze posledny item nam netreba zobrazovat
            NUM_PAGES = 2;
        }
        getActivity().setTitle("Tutoriál");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tutorial_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPagerAdapter = new ScreenSlidePagerAdapter(getActivity().getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        if (showLastItem) {
            mPager.setCurrentItem(NUM_PAGES - 1);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBusEvent(EventBusEvents.DalejButtonClicked event) {
        //Log.d(TAG, "onEventBusEvent: DalejButtonClicked");
        App.zaloguj(App.DEBUG,TAG,"onEventBusEvent: DalejButtonClicked");
        switch (mPager.getCurrentItem()) {
            case 0:
                mPager.setCurrentItem(1);
                break;
            case 1:
                mPager.setCurrentItem(2);
                break;
            default:
                throw new RuntimeException("onEventBusEvent(EventBusEvents.DalejButtonClicked event pager current item==3 not implemented");
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBusEvent(EventBusEvents.ZavrietTutorial zavriet) {
        //Log.d(TAG, "onEventBusEvent(EventBusEvents.ZavrietTutorial zavriet)");
        App.zaloguj(App.DEBUG,TAG,"onEventBusEvent(EventBusEvents.ZavrietTutorial zavriet)");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBusEvent(EventBusEvents.UsernameSelected usernameEvent) {
        //Log.d(TAG, "onEventBusEvent: UsernameSelected " + usernameEvent.meno);
        App.zaloguj(App.DEBUG,TAG,"onEventBusEvent: UsernameSelected " + usernameEvent.meno);
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TutorialPageItemFragment.getInstance(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


}
