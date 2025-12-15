package com.github.wulfaz.android.openkarotz.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.wulfaz.android.openkarotz.R;
import com.github.wulfaz.android.openkarotz.activity.MainActivity;
import com.github.wulfaz.android.openkarotz.karotz.Karotz;
import com.github.wulfaz.android.openkarotz.karotz.OpenKarotz;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * TTS (Text-to-Speech) fragment.
 */
public class TtsFragment extends Fragment {

    private static final String LOG_TAG = TtsFragment.class.getSimpleName();
    private static final int MAX_CHARS = 200;

    private Spinner spinnerVoice;
    private EditText editTextTts;
    private TextView textCharCount;
    private Button buttonSpeak;

    private List<Voice> voiceList = new ArrayList<>();

    public TtsFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Page title
        int index = getArguments().getInt(MainActivity.ARG_PAGE_NUMBER);
        String[] pages = getResources().getStringArray(R.array.pages);
        String pageTitle = pages[index];
        getActivity().setTitle(pageTitle);

        View view = inflater.inflate(R.layout.page_tts, container, false);
        initializeView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            // Load voices from Karotz
            new LoadVoicesTask(getActivity()).execute();
        }
    }

    private void initializeView(View view) {
        spinnerVoice = view.findViewById(R.id.spinnerVoice);
        editTextTts = view.findViewById(R.id.editTextTts);
        textCharCount = view.findViewById(R.id.textCharCount);
        buttonSpeak = view.findViewById(R.id.buttonSpeak);

        // Character counter
        editTextTts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                textCharCount.setText(length + "/" + MAX_CHARS);

                // Change color if near limit
                if (length >= MAX_CHARS) {
                    textCharCount.setTextColor(0xFFFF0000); // Red
                } else if (length >= MAX_CHARS - 20) {
                    textCharCount.setTextColor(0xFFFF8800); // Orange
                } else {
                    textCharCount.setTextColor(0xFF888888); // Gray
                }
            }
        });

        // Speak button
        buttonSpeak.setOnClickListener(v -> speak());
    }

    private void speak() {
        String text = editTextTts.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(getActivity(), R.string.tts_error_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (voiceList.isEmpty()) {
            Toast.makeText(getActivity(), R.string.tts_error_no_voices, Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = spinnerVoice.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= voiceList.size()) {
            Toast.makeText(getActivity(), R.string.tts_error_no_voice_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        Voice selectedVoice = voiceList.get(selectedPosition);
        new SpeakTask(getActivity(), selectedVoice.id, text).execute();
    }

    /**
     * Voice data class
     */
    private static class Voice {
        String id;
        String lang;

        Voice(String id, String lang) {
            this.id = id;
            this.lang = lang;
        }

        @Override
        public String toString() {
            return lang;
        }
    }

    /**
     * AsyncTask to load voices from Karotz
     */
    private class LoadVoicesTask extends AsyncTask<Void, Void, List<Voice>> {
        private final Activity activity;

        LoadVoicesTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected List<Voice> doInBackground(Void... params) {
            List<Voice> voices = new ArrayList<>();

            try {
                OpenKarotz karotz = (OpenKarotz) Karotz.getInstance();
                if (karotz != null) {
                    String json = karotz.getVoiceList();
                    if (json != null) {
                        JSONObject response = new JSONObject(json);
                        JSONArray voicesArray = response.getJSONArray("voices");

                        for (int i = 0; i < voicesArray.length(); i++) {
                            JSONObject voiceObj = voicesArray.getJSONObject(i);
                            String id = voiceObj.getString("id");
                            String lang = voiceObj.getString("lang");
                            voices.add(new Voice(id, lang));
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error loading voices: " + e.getMessage(), e);
            }

            return voices;
        }

        @Override
        protected void onPostExecute(List<Voice> voices) {
            if (activity == null || activity.isFinishing()) return;

            if (voices.isEmpty()) {
                Toast.makeText(activity, R.string.tts_error_loading_voices, Toast.LENGTH_SHORT).show();
                return;
            }

            voiceList = voices;

            // Populate spinner
            ArrayAdapter<Voice> adapter = new ArrayAdapter<>(
                    activity,
                    android.R.layout.simple_spinner_item,
                    voices
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerVoice.setAdapter(adapter);

            // Default to French Male (id=1) if available
            for (int i = 0; i < voices.size(); i++) {
                if ("1".equals(voices.get(i).id)) {
                    spinnerVoice.setSelection(i);
                    break;
                }
            }
        }
    }

    /**
     * AsyncTask to send TTS request
     */
    private class SpeakTask extends AsyncTask<Void, Void, Boolean> {
        private final Activity activity;
        private final String voiceId;
        private final String text;

        SpeakTask(Activity activity, String voiceId, String text) {
            this.activity = activity;
            this.voiceId = voiceId;
            this.text = text;
        }

        @Override
        protected void onPreExecute() {
            buttonSpeak.setEnabled(false);
            buttonSpeak.setText(R.string.tts_speaking);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                OpenKarotz karotz = (OpenKarotz) Karotz.getInstance();
                if (karotz != null) {
                    return karotz.tts(voiceId, text);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error sending TTS: " + e.getMessage(), e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            buttonSpeak.setEnabled(true);
            buttonSpeak.setText(R.string.tts_speak_button);

            if (activity == null || activity.isFinishing()) return;

            if (success) {
                Toast.makeText(activity, R.string.tts_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, R.string.tts_error_speaking, Toast.LENGTH_SHORT).show();
            }
        }
    }
}