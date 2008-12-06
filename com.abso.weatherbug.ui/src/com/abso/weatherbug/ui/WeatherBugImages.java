package com.abso.weatherbug.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Bundle of all images contributed by the WeatherBug UI plug-in.
 */
public final class WeatherBugImages {

    /** Identifies the image used to add a favorite location. */
    public static final String ADD_TO_FAVORITES = "ADD_TO_FAVORITES";

    /** Identifies the images showing weather conditions. */
    public static final String CONDITION = "CONDITION";

    /** Identifies the EU flag icon. */
    public static final String EU = "EU";

    /** Identifies the image used to manage favorite locations. */
    public static final String FAVORITES = "FAVORITES";

    /** Identifies the image used to manage the location history. */
    public static final String LOCATION_HISTORY = "LOCATION_HISTORY";

    /** Identifies the image used to open a web site. */
    public static final String OPEN_WEB_SITE = "OPEN_WEB_SITE";

    /** Identifies the image used to refresh data. */
    public static final String REFRESH = "REFRESH";

    /** Identifies the image used to search locations. */
    public static final String SEARCH = "SEARCH";

    /** Identifies the U.S. flag icon. */
    public static final String US = "US";

    /**
     * Gets the image with the specified key.
     * 
     * @param key
     *            an image key.
     * @return the image with the specified key.
     */
    public static final Image get(String key) {
        if (key == null) {
            return null;
        }
        return WeatherBugUIPlugin.getDefault().getImageRegistry().get(key.toString());
    }

    /**
     * Gets the image descriptor with the specified key.
     * 
     * @param key
     *            an image key.
     * @return the image descriptor with the specified key.
     */
    public static final ImageDescriptor getDescriptor(String key) {
        if (key == null) {
            return null;
        }
        return WeatherBugUIPlugin.getDefault().getImageRegistry().getDescriptor(key.toString());
    }

}