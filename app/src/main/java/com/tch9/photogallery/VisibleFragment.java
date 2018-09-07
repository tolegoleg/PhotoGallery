package com.tch9.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

public abstract class VisibleFragment extends Fragment
{
    private static final String TAG = "VisibleFragment";

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(getActivity(), "Got a broadcast: " + intent.getAction(), Toast.LENGTH_SHORT).show();
            // Получение означает, что пользователь видит приложение,
            // поэтому оповещение отменяется
            Log.i(TAG, "canceling notification.");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };

    @Override
    public void onStop()
    {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
    }


}
