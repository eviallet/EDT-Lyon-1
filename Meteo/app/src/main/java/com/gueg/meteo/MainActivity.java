package com.gueg.meteo;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;

import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    FrameLayout _container;
    WeatherView _google;
    WeatherView _meteociel;
    SettingsView _settings;

    ListPopupWindow _menu;
    BottomNavigationView _bnv;

    static final String[] MENU_CHOICES = {
            "Prévisions 3 jours",
            "Prévisions 10 jours",
            "Heure par heure - GFS",
            "Heure par heure - AROME"
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _container = findViewById(R.id.container);
        _bnv = findViewById(R.id.bottom_navigation_view);


        _settings = new SettingsView(this);

        City defaultCity = CityDb.getInstance(this).getDefault();

        _google = new WeatherView(this, WeatherView.GOOGLE, defaultCity);
        _meteociel = new WeatherView(this, WeatherView.METEOCIEL, defaultCity);

        _container.addView(_google);
        _container.addView(_meteociel);
        _container.addView(_settings);

        _settings.setListener(new SettingsView.OnSettingsClicked() {
            @Override
            public void onCityClicked(City city) {
                _google.searchCity(city);
                _meteociel.searchCity(city);

                bringDefaultWebsiteToFront();
                hideKeyboard();
            }

            @Override
            public void onSearch(String city) {
                _google.search(city);
                _meteociel.search(city);

                bringDefaultWebsiteToFront();
                hideKeyboard();
            }
        });

        bringDefaultWebsiteToFront();

        _bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.bn_google)
                    _container.bringChildToFront(_google);
                else if(menuItem.getItemId()==R.id.bn_meteociel)
                    _container.bringChildToFront(_meteociel);
                else
                    _container.bringChildToFront(_settings);
                return true;
            }
        });

        _menu = new ListPopupWindow(this);
        _menu.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MENU_CHOICES));
        _menu.setWidth(650);
        _menu.setOnItemClickListener(this);
        _menu.setBackgroundDrawable(getDrawable(R.drawable.menu_bkg));
        _menu.setAnchorView(_bnv);
        _menu.setHorizontalOffset(150);
        _menu.setModal(true);


        _bnv.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.bn_meteociel)
                    _menu.show();
            }
        });
    }

    private void bringDefaultWebsiteToFront() {
        String defaultWebsite = _settings.getDefaultWebsite();
        _container.bringChildToFront(defaultWebsite.equals(WeatherView.GOOGLE) ? _google : _meteociel);
        _bnv.setSelectedItemId(defaultWebsite.equals(WeatherView.GOOGLE) ? R.id.bn_google : R.id.bn_meteociel);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(position) {
            case 0:
                _meteociel.advancedMode(WeatherView.Advanced.J3);
                break;
            case 1:
                _meteociel.advancedMode(WeatherView.Advanced.J10);
                break;
            case 2:
                _meteociel.advancedMode(WeatherView.Advanced.HPH_GFS);
                break;
            case 3:
                _meteociel.advancedMode(WeatherView.Advanced.HPH_AROME);
                break;
            default:
                break;
        }
        _menu.dismiss();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    }
