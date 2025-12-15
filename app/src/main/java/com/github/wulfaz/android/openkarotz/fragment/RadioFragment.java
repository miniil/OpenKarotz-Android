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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.wulfaz.android.openkarotz.R;
import com.github.wulfaz.android.openkarotz.activity.MainActivity;
import com.github.wulfaz.android.openkarotz.karotz.Karotz;
import com.github.wulfaz.android.openkarotz.karotz.OpenKarotz;
import com.github.wulfaz.android.openkarotz.task.SoundAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Radio fragment - loads radio stations from Karotz.
 */
public class RadioFragment extends Fragment {

    private static final String LOG_TAG = RadioFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textNoRadios;

    private RadioAdapter adapter;
    private List<RadioStation> radioStations = new ArrayList<>();

    public RadioFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Fetch the selected page number
        int index = getArguments().getInt(MainActivity.ARG_PAGE_NUMBER);
        String[] pages = getResources().getStringArray(R.array.pages);
        String pageTitle = pages[index];
        getActivity().setTitle(pageTitle);

        View view = inflater.inflate(R.layout.page_radio, container, false);
        initializeView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            loadRadioStations();
        }
    }

    private void initializeView(View view) {
        progressBar = view.findViewById(R.id.progressBarRadio);
        textNoRadios = view.findViewById(R.id.textNoRadios);
        recyclerView = view.findViewById(R.id.recyclerViewRadios);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RadioAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadRadioStations() {
        new LoadRadiosTask(getActivity()).execute();
    }

    // ==================== Radio Station Model ====================

    private static class RadioStation {
        int id;
        String name;
        String url;

        RadioStation(int id, String name, String url) {
            this.id = id;
            this.name = name;
            this.url = url;
        }
    }

    // ==================== RecyclerView Adapter ====================

    private class RadioAdapter extends RecyclerView.Adapter<RadioAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_radio, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RadioStation station = radioStations.get(position);
            holder.bind(station);
        }

        @Override
        public int getItemCount() {
            return radioStations.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textName;
            ImageButton buttonPlay;

            ViewHolder(View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.textRadioName);
                buttonPlay = itemView.findViewById(R.id.buttonPlayRadio);
            }

            void bind(RadioStation station) {
                textName.setText(station.name);

                // Click on row or play button
                View.OnClickListener playListener = v -> playRadio(station);
                itemView.setOnClickListener(playListener);
                buttonPlay.setOnClickListener(playListener);
            }
        }
    }

    // ==================== Play Radio ====================

    private void playRadio(RadioStation station) {
        Log.d(LOG_TAG, "Playing radio: " + station.name + " - " + station.url);
        new PlayRadioTask(getActivity(), station.url, station.name).execute();
    }

    private class PlayRadioTask extends SoundAsyncTask {
        private final String name;

        public PlayRadioTask(Activity activity, String url, String name) {
            super(activity, url);
            this.name = name;
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (getActivity() != null && !getActivity().isFinishing()) {
                Toast.makeText(getActivity(),
                        getString(R.string.radio_starting) + " " + name,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ==================== Load Radios Task ====================

    private class LoadRadiosTask extends AsyncTask<Void, Void, List<RadioStation>> {
        private final Activity activity;

        LoadRadiosTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            textNoRadios.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected List<RadioStation> doInBackground(Void... params) {
            List<RadioStation> stations = new ArrayList<>();

            try {
                OpenKarotz karotz = (OpenKarotz) Karotz.getInstance();
                if (karotz != null) {
                    String json = karotz.getRadiosList();
                    if (json != null) {
                        JSONObject response = new JSONObject(json);

                        if ("0".equals(response.optString("return", "1"))) {
                            JSONArray streams = response.getJSONArray("streams");

                            for (int i = 0; i < streams.length(); i++) {
                                JSONObject stream = streams.getJSONObject(i);
                                int id = stream.getInt("id");
                                String name = stream.getString("name");
                                String url = stream.getString("url");
                                stations.add(new RadioStation(id, name, url));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error loading radios: " + e.getMessage(), e);
            }

            return stations;
        }

        @Override
        protected void onPostExecute(List<RadioStation> stations) {
            progressBar.setVisibility(View.GONE);

            if (activity == null || activity.isFinishing()) return;

            radioStations.clear();
            radioStations.addAll(stations);
            adapter.notifyDataSetChanged();

            if (stations.isEmpty()) {
                textNoRadios.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textNoRadios.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}