package com.kixfobby.security.quickresponse.helper;

import org.jetbrains.annotations.NotNull;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class CountryHelper {
    @NotNull
    public static ArrayList<Country> get() {
        ArrayList<Country> countries = new ArrayList<>();
        String[] isoCountries = Locale.getISOCountries();
        for (String country : isoCountries) {
            Locale locale = new Locale("en", country);
            String iso = locale.getISO3Country();
            String code = locale.getCountry();
            String name = locale.getDisplayCountry();

            if (!"".equals(iso) && !"".equals(code) && !"".equals(name)) {
                countries.add(new Country(iso, code, name));
            }
        }
        Collections.sort(countries, new CountryComparator());
        return countries;
    }

    @NotNull
    public static HashMap<String, String> getCode() {
        HashMap<String, String> countries = new HashMap<>();
        String[] isoCountries = Locale.getISOCountries();
        for (String country : isoCountries) {
            Locale locale = new Locale("en", country);
            String iso = locale.getISO3Country();
            String code = locale.getCountry();
            String name = locale.getDisplayCountry();

            if (!"".equals(iso) && !"".equals(code) && !"".equals(name)) {
                countries.put(iso, name);
            }
        }
        return countries;
    }

    public static class Country {
        private String iso;
        private String code;
        private String name;

        Country(String iso, String code, String name) {
            this.iso = iso;
            this.code = code;
            this.name = name;
        }

        @NotNull
        public String toString() {
            return code + " - " + name + " (" + iso + ") ";
        }
    }

    private static class CountryComparator implements Comparator<Country> {
        private Comparator<Object> comparator;

        CountryComparator() {
            comparator = Collator.getInstance();
        }

        public int compare(@NotNull Country c1, @NotNull Country c2) {
            return comparator.compare(c1.name, c2.name);
        }
    }
}
