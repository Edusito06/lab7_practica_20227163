package com.lab6.iot;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/** Fuerza el idioma espanol en toda la app, incluidas las pantallas de Firebase UI. */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es-PE"));
    }
}
