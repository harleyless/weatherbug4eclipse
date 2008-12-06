package com.abso.weatherbug.ui.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.abso.weatherbug.core.data.Location;

/**
 * Utility methods for managing preferences.
 */
public class PreferenceUtils {

    /** The location delimiter. */
    private static final char LOCATION_DELIMITER = '^';

    /** The field delimiter. */
    private static final char FIELD_DELIMITER = '|';

    /**
     * Converts a preference string value to a list of locations.
     * 
     * @param string
     *            a preference string value.
     * @return a list of <code>Location</code>s.
     * @see Location
     */
    public static List stringToLocations(String string) {
        String[] locStrings = StringUtils.splitPreserveAllTokens(StringUtils.defaultString(string), LOCATION_DELIMITER);
        List locations = new ArrayList();
        for (int i = 0; i < locStrings.length; i++) {
            Location location = stringToLocation(locStrings[i]);
            if (location != null) {
                locations.add(location);
            }
        }
        return locations;
    }

    /**
     * Converts a string value to a location.
     * 
     * @param string
     *            a preference string value.
     * @return a <code>Location</code>.
     * @see Location
     */
    private static Location stringToLocation(String string) {
        String[] fields = StringUtils.splitPreserveAllTokens(string, FIELD_DELIMITER);
        if (fields.length < 6) {
            return null;
        }
        try {
            return new Location(fields[0], fields[1], fields[2], Integer.parseInt(fields[3]), Integer.parseInt(fields[4]), Integer
                    .parseInt(fields[5]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts a list of locations to a preference string value.
     * 
     * @param favLocations
     *            a list of <code>Location</code>s.
     * @return a preference string value.
     */
    public static String locationsToString(List favLocations) {
        List strings = new ArrayList();
        for (Iterator i = favLocations.iterator(); i.hasNext();) {
            strings.add(locationToString((Location) i.next()));
        }
        return StringUtils.join(strings, LOCATION_DELIMITER);
    }

    /**
     * Converts a location to a preference string value.
     * 
     * @param location
     *            a location.
     * @return a preference string value.
     */
    private static String locationToString(Location location) {
        return StringUtils.defaultString(location.getCityName()) + FIELD_DELIMITER
                + StringUtils.defaultString(location.getStateName()) + FIELD_DELIMITER
                + StringUtils.defaultString(location.getCountryName()) + FIELD_DELIMITER + location.getZipCode() + FIELD_DELIMITER
                + location.getCityCode() + FIELD_DELIMITER + location.getCityType();
    }

}
