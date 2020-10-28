package com.gueg.meteo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CityDb {
    private static final String NAME = "com.gueg.meteo.CITIES";
    private static final String DEFAULT = "com.gueg.meteo.DEFAULT";

    private static CityDb instance;
    private Context context;
    private static final HashMap<String, City> citiesMap = new HashMap<>();

    public static CityDb getInstance(Context context) {
        if(instance == null)
            instance = new CityDb(context);
        return instance;
    }

    private final SharedPreferences db;

    private CityDb(Context context) {
        db = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        this.context = context;
    }

    public ArrayList<City> getCities() {
        Map<String,?> results =  db.getAll();
        ArrayList<City> cities = new ArrayList<>();
        String defaultCity = getDefaultCityName();

        citiesMap.clear();

        for(String cityStr : results.keySet()) {
            if(cityStr.equals(SettingsView.DEFAULT_WEBSITE))
                continue;

            City city = new City(cityStr, (String)results.get(cityStr));
            city.isDefault = cityStr.equals(defaultCity);

            cities.add(city);
            citiesMap.put(cityStr, city);
        }

        return cities;
    }

    public void addCity(City city) {
        db.edit().putString(city.cityName, city.postalCode).apply();
    }

    public void removeCity(City city) {
        db.edit().remove(city.cityName).apply();
    }


    private City getCityByName(String name) {
        return  citiesMap.get(name);
    }

    public void setDefault(City city) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DEFAULT, city.cityName).apply();
    }

    public City getDefault() {
        return getCityByName(getDefaultCityName());
    }

    private String getDefaultCityName() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(DEFAULT, "");
    }
}
