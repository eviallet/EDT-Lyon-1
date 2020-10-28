package com.gueg.meteo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DialogRemoveCity extends Dialog {

    RecyclerView list;

    public DialogRemoveCity(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_city_list);

        TextView title = findViewById(R.id.dialog_city_list_title);
        title.setText("Supprimer une ville");

        list = findViewById(R.id.dialog_city_list_list);
        final ArrayList<City> cities = CityDb.getInstance(getContext()).getCities();
        final CityAdapter adapter = new CityAdapter(getContext(), cities);
        adapter.setListener(new SettingsView.OnSettingsClicked() {
            @Override
            public void onCityClicked(City city) {
                boolean wasDefault = city.isDefault;
                if(cities.size() == 1) {
                    Toast.makeText(getContext(), "Il doit rester au moins une ville dans la liste.", Toast.LENGTH_SHORT).show();
                    return;
                }
                CityDb.getInstance(getContext()).removeCity(city);
                cities.clear();
                cities.addAll(CityDb.getInstance(getContext()).getCities());

                if(wasDefault) {
                    CityDb.getInstance(getContext()).setDefault(cities.get(0));
                    cities.get(0).isDefault = true;
                }

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
