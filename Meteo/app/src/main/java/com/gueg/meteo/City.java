package com.gueg.meteo;

public class City {
    public String cityName;
    public String postalCode;
    public boolean isDefault;

    public City(String cityName, String postalCode) {
        this.cityName = cityName;
        this.postalCode = postalCode;
        isDefault = false;
    }

    public String toString() {
        return this.cityName + "+" + this.postalCode;
    }
}
