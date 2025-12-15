/*
 * OpenKarotz-Android
 * http://github.com/hobbe/OpenKarotz-Android
 *
 * Copyright (c) 2014 Olivier Bagot (http://github.com/hobbe)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * http://opensource.org/licenses/MIT
 *
 */

package com.github.wulfaz.android.openkarotz.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.wulfaz.android.openkarotz.R;
import com.github.wulfaz.android.openkarotz.activity.MainActivity;
import com.github.wulfaz.android.openkarotz.karotz.Karotz;
import com.github.wulfaz.android.openkarotz.karotz.OpenKarotz;

/**
 * Home fragment.
 */
public class HomeFragment extends Fragment {

    private static final String LOG_TAG = HomeFragment.class.getSimpleName();

    private Button buttonRandomMood;
    private Button buttonRandomEars;

    /**
     * Initialize a new home fragment.
     */
    public HomeFragment() {
        // Nothing to do
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

        View view = inflater.inflate(R.layout.page_home, container, false);

        initializeView(view);

        return view;
    }

    private void initializeView(View view) {
        buttonRandomMood = view.findViewById(R.id.buttonRandomMood);
        buttonRandomEars = view.findViewById(R.id.buttonRandomEars);

        buttonRandomMood.setOnClickListener(v -> {
            new RandomMoodTask(getActivity()).execute();
        });

        buttonRandomEars.setOnClickListener(v -> {
            new RandomEarsTask(getActivity()).execute();
        });
    }

    /**
     * AsyncTask for random mood
     */
    private class RandomMoodTask extends AsyncTask<Void, Void, Boolean> {
        private final Activity activity;

        RandomMoodTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            buttonRandomMood.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                OpenKarotz karotz = (OpenKarotz) Karotz.getInstance();
                if (karotz != null) {
                    return karotz.randomMood();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error playing random mood: " + e.getMessage(), e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            buttonRandomMood.setEnabled(true);

            if (activity == null || activity.isFinishing()) return;

            if (success) {
                Toast.makeText(activity, R.string.home_random_mood_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, R.string.home_random_mood_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * AsyncTask for random ears
     */
    private class RandomEarsTask extends AsyncTask<Void, Void, Boolean> {
        private final Activity activity;

        RandomEarsTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            buttonRandomEars.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                OpenKarotz karotz = (OpenKarotz) Karotz.getInstance();
                if (karotz != null) {
                    karotz.earsRandom();
                    return true;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error moving ears random: " + e.getMessage(), e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            buttonRandomEars.setEnabled(true);

            if (activity == null || activity.isFinishing()) return;

            if (success) {
                Toast.makeText(activity, R.string.home_random_ears_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, R.string.home_random_ears_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}