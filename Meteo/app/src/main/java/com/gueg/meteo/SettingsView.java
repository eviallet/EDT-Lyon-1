package com.gueg.meteo;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingsView extends FrameLayout {

    public static final String DEFAULT_WEBSITE = "com.gueg.meteo.DEFAULT_WEBSITE";

    OnSettingsClicked _listener;
    CityAdapter _adapter;
    RecyclerView _rv;
    EditText _search;
    ImageView _searchIcon;
    ArrayList<City> _cities;


    public SettingsView(final Context context) {
        super(context);

        addView(View.inflate(context, R.layout.view_settings, null));

        _search = findViewById(R.id.view_settings_search_text);
        _search.setImeOptions(EditorInfo.IME_ACTION_DONE);
        _search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(!_search.getText().toString().isEmpty())
                        _listener.onSearch(_search.getText().toString());
                }
                return false;
            }
        });
        _searchIcon = findViewById(R.id.view_settings_search_pic);

        _searchIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!_search.getText().toString().isEmpty())
                    _listener.onSearch(_search.getText().toString());
            }
        });


        _rv = findViewById(R.id.view_settings_rv);
        _rv.setLayoutManager(new LinearLayoutManager(getContext()));
        _rv.setOverScrollMode(OVER_SCROLL_NEVER);
        _cities = CityDb.getInstance(getContext()).getCities();

        if(_cities.size() == 0) {
            City lyon = new City("Lyon", "69000");
            City stPierre = new City("Saint Pierre de Boeuf", "42520");
            // AJoute les villes à la db
            CityDb.getInstance(getContext()).addCity(lyon);
            CityDb.getInstance(getContext()).addCity(stPierre);
            CityDb.getInstance(getContext()).setDefault(lyon);
            // Créé la liste pour l'adaptateur
            _cities = CityDb.getInstance(getContext()).getCities();
        }
        _adapter = new CityAdapter(getContext(), _cities);
        _rv.setAdapter(_adapter);

        findViewById(R.id.view_settings_add_city).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogAddCity(getContext(), new DialogAddCity.AddCityListener() {
                    @Override
                    public void onCityAdded(City city) {
                        CityDb.getInstance(getContext()).addCity(city);
                        refreshCities();
                    }
                }).show();
            }
        });

        findViewById(R.id.view_settings_remove_city).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogRemoveCity dialog = new DialogRemoveCity(getContext());
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        refreshCities();
                    }
                });
                dialog.show();
            }
        });

        findViewById(R.id.view_settings_default_city).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogDefaultCity dialog = new DialogDefaultCity(getContext());
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        refreshCities();
                    }
                });
                dialog.show();
            }
        });

        final Switch sw = findViewById(R.id.view_settings_default_website);
        boolean isChecked = PreferenceManager.getDefaultSharedPreferences(context).getString(DEFAULT_WEBSITE,"").equals(WeatherView.GOOGLE);
        sw.setChecked(isChecked);
        sw.setText(isChecked? "Google" : "Meteociel");
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sw.setText(isChecked ? "Google" : "Meteociel");
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DEFAULT_WEBSITE, isChecked ? WeatherView.GOOGLE : WeatherView.METEOCIEL).apply();
            }
        });
    }

    public String getDefaultWebsite() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getString(DEFAULT_WEBSITE, WeatherView.METEOCIEL);
    }

    public void setListener(OnSettingsClicked listener) {
        _listener = listener;
        _adapter.setListener(listener);
    }

    private void refreshCities() {
        _cities.clear();
        _cities.addAll(CityDb.getInstance(getContext()).getCities());
        _adapter.notifyDataSetChanged();
    }


    public interface OnSettingsClicked {
        void onCityClicked(City city);
        void onSearch(String city);
    }
}
