package com.gueg.meteo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DialogDefaultCity extends Dialog {

    RecyclerView list;

    public DialogDefaultCity(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_city_list);

        TextView title = findViewById(R.id.dialog_city_list_title);
        title.setText("Ville par défaut");

        list = findViewById(R.id.dialog_city_list_list);
        final ArrayList<City> cities = CityDb.getInstance(getContext()).getCities();
        final CityAdapter adapter = new CityAdapter(getContext(), cities);
        adapter.setListener(new SettingsView.OnSettingsClicked() {
            @Override
            public void onCityClicked(City city) {
                CityDb.getInstance(getContext()).setDefault(city);
                cities.clear();
                cities.addAll(CityDb.getInstance(getContext()).getCities());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onSearch(String city) {}
        });

        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setAdapter(adapter);

        findViewById(R.id.dialog_city_list_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }


}
