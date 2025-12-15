/*
 * Karotz Controller
 * https://github.com/miniil/OpenKarotz-Android
 *
 * Copyright (c) 2024 miniil (https://www.miniil.be)
 *
 * Based on OpenKarotz-Android by Olivier Bagot (https://github.com/hobbe)
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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.wulfaz.android.openkarotz.R;
import com.github.wulfaz.android.openkarotz.activity.MainActivity;

/**
 * About fragment - displays app information, credits, and contact options.
 */
public class AboutFragment extends Fragment {

    private static final String URL_WEBSITE = "https://www.miniil.be";
    private static final String URL_ORIGINAL_AUTHOR = "https://github.com/hobbe";
    private static final String URL_OPENKAROTZ = "https://github.com/hobbe/OpenKarotz";
    private static final String URL_SUPPORT = "https://fr.tipeee.com/miniil";
    private static final String EMAIL_CONTACT = "webmaster@miniil.be";

    public AboutFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Fetch the selected page number
        int index = getArguments().getInt(MainActivity.ARG_PAGE_NUMBER);
        String[] pages = getResources().getStringArray(R.array.pages);
        String pageTitle = pages[index];
        getActivity().setTitle(pageTitle);

        View view = inflater.inflate(R.layout.page_about, container, false);
        initializeView(view);
        return view;
    }

    private void initializeView(View view) {
        // Clickable links
        TextView textWebsite = view.findViewById(R.id.textWebsite);
        textWebsite.setOnClickListener(v -> openUrl(URL_WEBSITE));

        TextView textOriginalAuthor = view.findViewById(R.id.textOriginalAuthor);
        textOriginalAuthor.setOnClickListener(v -> openUrl(URL_ORIGINAL_AUTHOR));

        TextView textOpenKarotz = view.findViewById(R.id.textOpenKarotz);
        textOpenKarotz.setOnClickListener(v -> openUrl(URL_OPENKAROTZ));

        // Action buttons
        Button buttonContact = view.findViewById(R.id.buttonContact);
        buttonContact.setOnClickListener(v -> sendEmail());

        Button buttonWebsite = view.findViewById(R.id.buttonWebsite);
        buttonWebsite.setOnClickListener(v -> openUrl(URL_WEBSITE));

        Button buttonSupport = view.findViewById(R.id.buttonSupport);
        buttonSupport.setOnClickListener(v -> openUrl(URL_SUPPORT));
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + EMAIL_CONTACT));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_email_subject));
        startActivity(Intent.createChooser(intent, getString(R.string.about_email_chooser)));
    }
}