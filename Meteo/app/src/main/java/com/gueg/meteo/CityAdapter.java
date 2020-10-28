package com.gueg.meteo;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private final Context _c;
    private final ArrayList<City> _cities;
    private SettingsView.OnSettingsClicked _listener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        CardView card;
        ImageView check;
        ViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.row_city_text);
            card = v.findViewById(R.id.row_city_card);
            check = v.findViewById(R.id.row_city_default);
        }
    }

    CityAdapter(Context c, ArrayList<City> cities) {
        _c = c;
        _cities = cities;
    }

    @NonNull
    @Override
    public CityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(_c).inflate(R.layout.row_city, viewGroup, false);
        return new CityAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CityAdapter.ViewHolder viewHolder, int i) {
        viewHolder.text.setText(_cities.get(i).cityName);
        viewHolder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _listener.onCityClicked(_cities.get(viewHolder.getAdapterPosition()));
            }
        });
        viewHolder.check.setVisibility(_cities.get(i).isDefault ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return _cities.size();
    }

    public void setListener(SettingsView.OnSettingsClicked listener) {
        _listener = listener;
    }
}
