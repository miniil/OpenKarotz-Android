package com.github.wulfaz.android.openkarotz.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.github.wulfaz.android.openkarotz.R;
import com.github.wulfaz.android.openkarotz.activity.MainActivity;
import com.github.wulfaz.android.openkarotz.karotz.IKarotz;
import com.github.wulfaz.android.openkarotz.karotz.IKarotz.KarotzStatus;
import com.github.wulfaz.android.openkarotz.karotz.Karotz;
import com.github.wulfaz.android.openkarotz.karotz.OpenKarotzState;
import com.github.wulfaz.android.openkarotz.task.GetStatusAsyncTask;
import com.github.wulfaz.android.openkarotz.task.GetVersionAsyncTask;
import com.github.wulfaz.android.openkarotz.task.SleepAsyncTask;
import com.github.wulfaz.android.openkarotz.task.WakeupAsyncTask;

import com.github.wulfaz.android.openkarotz.karotz.OpenKarotz;

/**
 * System fragment.
 */
public class SystemFragment extends Fragment {

    /**
     * Initialize a new system fragment.
     */
    public SystemFragment() {
        // Nothing to initialize
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            /*new GetStatusTask(getActivity()).execute();
            new GetVersionTask(getActivity()).execute();
            new GetSystemInfoTask(getActivity()).execute();*/

            new GetSystemInfoTask(getActivity()).execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Fetch the selected page number
        int index = getArguments().getInt(MainActivity.ARG_PAGE_NUMBER);

        // List of pages
        String[] pages = getResources().getStringArray(R.array.pages);

        // Page title
        String pageTitle = pages[index];
        getActivity().setTitle(pageTitle);

        View view = inflater.inflate(R.layout.page_system, container, false);

        initializeView(view);

        return view;
    }

    @Override
    public void onPause() {
        onOffSwitch.setOnCheckedChangeListener(null);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        onOffSwitch.setOnCheckedChangeListener(onOffSwitchCheckedChangeListener);
    }

    private void initializeOnOffSwitch(View view) {
        onOffSwitch = view.findViewById(R.id.switchOnOff);
        onOffSwitchCheckedChangeListener = new OnOffSwitchCheckedChangeListener();
        onOffSwitch.setOnCheckedChangeListener(onOffSwitchCheckedChangeListener);
    }

    private void initializeVersionTextView(View view) {
        versionTextView = view.findViewById(R.id.textVersion);
        versionTextView.setText("-");
    }

    private void initializeSystemInfoViews(View view) {
        storageTextView = view.findViewById(R.id.textStorage);
        storageTextView.setText("-");

        wifiMacTextView = view.findViewById(R.id.textWifiMac);
        wifiMacTextView.setText("-");

        moodsTextView = view.findViewById(R.id.textMoods);
        moodsTextView.setText("-");

        soundsTextView = view.findViewById(R.id.textSounds);
        soundsTextView.setText("-");

        tagsTextView = view.findViewById(R.id.textTags);
        tagsTextView.setText("-");
    }

    private void initializeView(View view) {
        // Version
        initializeVersionTextView(view);

        // System info
        initializeSystemInfoViews(view);

        // On/Off status
        initializeOnOffSwitch(view);
    }


    private static class GetStatusTask extends GetStatusAsyncTask {

        public GetStatusTask(Activity activity) {
            super(activity);
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);

            KarotzStatus status = (KarotzStatus) result;
            onOffSwitch.setChecked(status != null && status.isAwake());
        }
    }

    private class GetVersionTask extends GetVersionAsyncTask {

        public GetVersionTask(Activity activity) {
            super(activity);
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (result == null) {
                Toast.makeText(SystemFragment.this.getActivity(), getString(R.string.err_cannot_getversion), Toast.LENGTH_SHORT).show();
            } else {
                IKarotz.KarotzVersion version = (IKarotz.KarotzVersion) result;
                versionTextView.setText(version.getVersion());
            }
        }
    }

    private class GetSystemInfoTask extends GetStatusAsyncTask {

        public GetSystemInfoTask(Activity activity) {
            super(activity);
        }

        @Override
        public void onPostExecute(Object result) {
            // Don't call super - we don't want the dialog handling
            // super.onPostExecute(result);

            // Get extended info from Karotz
            try {
                OpenKarotz karotz = (OpenKarotz) Karotz.getInstance();

                if (karotz != null) {
                    OpenKarotzState state = karotz.getState();

                    if (state != null) {
                        // Status
                        KarotzStatus status = state.getStatus();
                        onOffSwitch.setChecked(status != null && status.isAwake());

                        // Version
                        IKarotz.KarotzVersion version = state.getVersion();
                        if (version != null) {
                            versionTextView.setText(version.getVersion());
                        }

                        // System info
                        storageTextView.setText(state.getFreeSpace() + " free (" + state.getPercentUsed() + "% used)");
                        wifiMacTextView.setText(state.getWlanMac());
                        moodsTextView.setText(state.getNbMoods());
                        soundsTextView.setText(state.getNbSounds());
                        tagsTextView.setText(state.getNbTags());
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error getting system info: " + e.getMessage());
            }
        }
    }

    private class OnOffSwitchCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        public OnOffSwitchCheckedChangeListener() {
            // Nothing to do
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(LOG_TAG, "ON/OFF " + (isChecked ? "" : "un") + "checked");

            if (isChecked) {
                new WakeupTask(getActivity()).execute();
            } else {
                new SleepTask(getActivity()).execute();
            }
        }
    }

    private class SleepTask extends SleepAsyncTask {

        public SleepTask(Activity activity) {
            super(activity);
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (Boolean.FALSE.equals(result)) {
                Toast.makeText(SystemFragment.this.getActivity(), getString(R.string.err_cannot_sleep), Toast.LENGTH_SHORT).show();

                // Check switch, without triggering listener
                onOffSwitch.setOnCheckedChangeListener(null);
                onOffSwitch.setChecked(true);
                onOffSwitch.setOnCheckedChangeListener(onOffSwitchCheckedChangeListener);
            }
        }
    }

    private class WakeupTask extends WakeupAsyncTask {

        public WakeupTask(Activity activity) {
            super(activity);
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (Boolean.FALSE.equals(result)) {
                Toast.makeText(SystemFragment.this.getActivity(), getString(R.string.err_cannot_wakeup), Toast.LENGTH_SHORT).show();

                // Uncheck switch, without triggering listener
                onOffSwitch.setOnCheckedChangeListener(null);
                onOffSwitch.setChecked(false);
                onOffSwitch.setOnCheckedChangeListener(onOffSwitchCheckedChangeListener);
            }
        }
    }


    private static SwitchCompat onOffSwitch = null;
    private static OnOffSwitchCheckedChangeListener onOffSwitchCheckedChangeListener = null;

    private static TextView versionTextView = null;
    private static TextView storageTextView = null;
    private static TextView wifiMacTextView = null;
    private static TextView moodsTextView = null;
    private static TextView soundsTextView = null;
    private static TextView tagsTextView = null;

    private static final String LOG_TAG = SystemFragment.class.getSimpleName();
}