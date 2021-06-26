package project.leaveamessage.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import project.leaveamessage.tabs.LeaveMessage;
import project.leaveamessage.tabs.ListMessages;
import project.leaveamessage.R;

public class TabsAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;
    private final Fragment leaveMessage,listMessages;

    public TabsAdapter(Context context, FragmentManager fm, String id) {
        super(fm);
        mContext = context;
        // id is used for build rest calls
        leaveMessage = new LeaveMessage(id);
        listMessages = new ListMessages();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return leaveMessage;
            case 1: return listMessages;
        }
        return leaveMessage;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }
    @Override
    public int getCount() { return 2; }
}