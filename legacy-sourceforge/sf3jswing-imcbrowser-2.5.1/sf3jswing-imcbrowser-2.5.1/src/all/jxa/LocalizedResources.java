/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxa;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Tiana
 */
public class LocalizedResources {
    /**
     * current displayed language
     */
    static Locale language = Locale.getDefault();
    /**
     * various available languages for the ResourceBundle
     */
    static Locale[] languages = new Locale[]{new Locale("en"), new Locale("fr"), new Locale("de")};

     /**
     * localized bundle
     */
    public static ResourceBundle localized = getLocalizedRB(language);
    /**
     * constructs an array of the specified key in {@link #localized localized}
     * ResourceBundle
     *
     * @param key ResourceBundle key
     * @param n number of elements for the key-n (e.g. mylabel-0, mylabel-1 are
     * 2 elements for key mylabel).
     */
    public static String[] localizedGetStringArray(String key, int n) {
        ArrayList<String> v = new ArrayList<String>();
        for (Enumeration<String> keys = localized.getKeys(); keys.hasMoreElements();) {
            String k = keys.nextElement();
            for (int i = 0; i < n; i++) {
                if (k.equals(key + "-" + i)) {
                    if (i >= v.size()) {
                        v.add(localized.getString(k));
                    } else {
                        v.add(i, localized.getString(k));
                    }
                }
            }
        }
        return v.toArray(new String[]{});
    }

    /**
     *
     *
     * @return the localized ResourceBundle with the correct available locale
     */
    static ResourceBundle getLocalizedRB(Locale locale) {
        return ResourceBundle.getBundle("jxa.localized", locale);
    }
   
}
