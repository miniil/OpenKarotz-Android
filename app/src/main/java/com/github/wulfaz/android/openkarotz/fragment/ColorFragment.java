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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.wulfaz.android.openkarotz.R;
import com.github.wulfaz.android.openkarotz.activity.MainActivity;
import com.github.wulfaz.android.openkarotz.karotz.IKarotz.KarotzStatus;
import com.github.wulfaz.android.openkarotz.karotz.Karotz;
import com.github.wulfaz.android.openkarotz.task.GetColorAsyncTask;
import com.github.wulfaz.android.openkarotz.task.GetPulseAsyncTask;
import com.github.wulfaz.android.openkarotz.task.GetStatusAsyncTask;
import com.github.wulfaz.android.openkarotz.task.LedAsyncTask;

import java.io.IOException;

/**
 * Color picker fragment with RGB sliders.
 */
public class ColorFragment extends Fragment {

    private static final String LOG_TAG = ColorFragment.class.getSimpleName();

    // UI Elements
    private View colorPreview;
    private EditText editHexColor;
    private SeekBar seekBarRed, seekBarGreen, seekBarBlue;
    private TextView textRedValue, textGreenValue, textBlueValue;
    private SwitchCompat pulseSwitch;
    private Button buttonApplyColor;

    // Current color values
    private int currentRed = 255;
    private int currentGreen = 0;
    private int currentBlue = 0;

    // Flag to prevent circular updates
    private boolean isUpdating = false;

    public ColorFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Fetch the selected page number
        int index = getArguments().getInt(MainActivity.ARG_PAGE_NUMBER);
        String[] pages = getResources().getStringArray(R.array.pages);
        String pageTitle = pages[index];
        getActivity().setTitle(pageTitle);

        View view = inflater.inflate(R.layout.page_color, container, false);
        initializeView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            // Load current color from Karotz
            new GetStatusTask(getActivity()).execute();
            new GetPulseTask(getActivity()).execute();
            new GetColorTask(getActivity()).execute();
        }
    }

    private void initializeView(View view) {
        // Color preview
        colorPreview = view.findViewById(R.id.colorPreview);

        // Hex input
        editHexColor = view.findViewById(R.id.editHexColor);
        editHexColor.addTextChangedListener(new HexTextWatcher());

        // RGB Sliders
        seekBarRed = view.findViewById(R.id.seekBarRed);
        seekBarGreen = view.findViewById(R.id.seekBarGreen);
        seekBarBlue = view.findViewById(R.id.seekBarBlue);

        textRedValue = view.findViewById(R.id.textRedValue);
        textGreenValue = view.findViewById(R.id.textGreenValue);
        textBlueValue = view.findViewById(R.id.textBlueValue);

        seekBarRed.setOnSeekBarChangeListener(new ColorSeekBarListener(0));
        seekBarGreen.setOnSeekBarChangeListener(new ColorSeekBarListener(1));
        seekBarBlue.setOnSeekBarChangeListener(new ColorSeekBarListener(2));

        // Pulse switch
        pulseSwitch = view.findViewById(R.id.switchPulse);
        pulseSwitch.setOnCheckedChangeListener(new PulseSwitchListener());

        // Apply button
        buttonApplyColor = view.findViewById(R.id.buttonApplyColor);
        buttonApplyColor.setOnClickListener(v -> applyColor());

        // Quick color buttons (in carousel order)
        setupQuickColorButton(view, R.id.btnRed, 255, 0, 0);
        setupQuickColorButton(view, R.id.btnOrange, 255, 128, 0);
        setupQuickColorButton(view, R.id.btnYellow, 255, 255, 0);
        setupQuickColorButton(view, R.id.btnGreen, 0, 255, 0);
        setupQuickColorButton(view, R.id.btnCyan, 0, 255, 255);
        setupQuickColorButton(view, R.id.btnBlue, 0, 0, 255);
        setupQuickColorButton(view, R.id.btnMagenta, 255, 0, 255);
        setupQuickColorButton(view, R.id.btnPink, 255, 105, 180);
        setupQuickColorButton(view, R.id.btnWhite, 255, 255, 255);
        setupQuickColorButton(view, R.id.btnPurple, 128, 0, 255);

        // Initial update
        updateColorPreview();
    }

    private void setupQuickColorButton(View view, int buttonId, int r, int g, int b) {
        Button btn = view.findViewById(buttonId);

        // Create rounded background
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.rgb(r, g, b));
        drawable.setCornerRadius(8);
        drawable.setStroke(2, Color.GRAY);
        btn.setBackground(drawable);

        btn.setOnClickListener(v -> {
            setColor(r, g, b);
        });
    }

    private void setColor(int r, int g, int b) {
        currentRed = r;
        currentGreen = g;
        currentBlue = b;

        isUpdating = true;
        seekBarRed.setProgress(r);
        seekBarGreen.setProgress(g);
        seekBarBlue.setProgress(b);
        updateHexFromRgb();
        isUpdating = false;

        updateColorPreview();
    }

    private void updateColorPreview() {
        int color = Color.rgb(currentRed, currentGreen, currentBlue);
        colorPreview.setBackgroundColor(color);

        // Update value labels
        textRedValue.setText(String.valueOf(currentRed));
        textGreenValue.setText(String.valueOf(currentGreen));
        textBlueValue.setText(String.valueOf(currentBlue));
    }

    private void updateHexFromRgb() {
        String hex = String.format("%02X%02X%02X", currentRed, currentGreen, currentBlue);
        editHexColor.setText(hex);
    }

    private void updateRgbFromHex(String hex) {
        if (hex.length() == 6) {
            try {
                currentRed = Integer.parseInt(hex.substring(0, 2), 16);
                currentGreen = Integer.parseInt(hex.substring(2, 4), 16);
                currentBlue = Integer.parseInt(hex.substring(4, 6), 16);

                seekBarRed.setProgress(currentRed);
                seekBarGreen.setProgress(currentGreen);
                seekBarBlue.setProgress(currentBlue);

                updateColorPreview();
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "Invalid hex color: " + hex);
            }
        }
    }

    private void applyColor() {
        int color = Color.rgb(currentRed, currentGreen, currentBlue);
        boolean pulse = pulseSwitch.isChecked();

        Log.d(LOG_TAG, "Applying color: #" + String.format("%06X", color & 0xFFFFFF) + ", pulse: " + pulse);

        new LedChangeTask(getActivity(), color, pulse).execute();
    }

    // ==================== Inner Classes ====================

    private class ColorSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        private final int channel; // 0=R, 1=G, 2=B

        ColorSeekBarListener(int channel) {
            this.channel = channel;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isUpdating) return;

            switch (channel) {
                case 0: currentRed = progress; break;
                case 1: currentGreen = progress; break;
                case 2: currentBlue = progress; break;
            }

            isUpdating = true;
            updateHexFromRgb();
            isUpdating = false;

            updateColorPreview();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    private class HexTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isUpdating) return;

            String hex = s.toString().toUpperCase();
            if (hex.length() == 6) {
                isUpdating = true;
                updateRgbFromHex(hex);
                isUpdating = false;
            }
        }
    }

    private class PulseSwitchListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(LOG_TAG, "Pulse switch: " + isChecked);
        }
    }

    private class GetStatusTask extends GetStatusAsyncTask {
        public GetStatusTask(Activity activity) {
            super(activity);
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);

            KarotzStatus status = (KarotzStatus) result;
            boolean awake = (status != null && status.isAwake());
            buttonApplyColor.setEnabled(awake);
        }
    }

    private class GetPulseTask extends GetPulseAsyncTask {
        public GetPulseTask(Activity activity) {
            super(activity);
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (result != null) {
                boolean pulsing = ((Boolean) result).booleanValue();
                pulseSwitch.setChecked(pulsing);
            }
        }
    }

    private class GetColorTask extends GetColorAsyncTask {
        public GetColorTask(Activity activity) {
            super(activity);
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (result != null) {
                int color = ((Integer) result).intValue();
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                setColor(r, g, b);
            }
        }
    }

    private static class LedChangeTask extends LedAsyncTask {
        private final Activity activity;

        public LedChangeTask(Activity activity, int color, boolean pulse) {
            super(activity, color, pulse);
            this.activity = activity;
        }

        @Override
        public void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (activity != null && !activity.isFinishing()) {
                Toast.makeText(activity, R.string.color_applied, Toast.LENGTH_SHORT).show();
            }
        }
    }
}