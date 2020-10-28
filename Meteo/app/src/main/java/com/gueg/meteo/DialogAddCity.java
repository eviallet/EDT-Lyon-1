package com.gueg.meteo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class DialogAddCity extends Dialog {

    AddCityListener listener;
    EditText city;
    EditText postalCode;

    public DialogAddCity(@NonNull Context context, AddCityListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_city_add);

        city = findViewById(R.id.dialog_city_add_city);
        postalCode = findViewById(R.id.dialog_city_add_postalcode);

        findViewById(R.id.dialog_city_add_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(city.getText().toString().isEmpty() || postalCode.getText().toString().isEmpty())
                    return;
                listener.onCityAdded(new City(city.getText().toString(), postalCode.getText().toString()));
                dismiss();
            }
        });

        findViewById(R.id.dialog_city_add_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    interface AddCityListener {
        void onCityAdded(City city);
    }
}
