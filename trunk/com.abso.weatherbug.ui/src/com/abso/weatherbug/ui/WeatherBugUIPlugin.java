package com.abso.weatherbug.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.abso.weatherbug.core.WeatherBugService;
import com.abso.weatherbug.core.WeatherBugServiceException;
import com.abso.weatherbug.core.data.Forecasts;
import com.abso.weatherbug.core.data.LiveWeather;
import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.core.data.Station;
import com.abso.weatherbug.ui.preferences.PreferenceUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class WeatherBugUIPlugin extends AbstractUIPlugin {

    /** The plug-in identifier. */
    public static final String PLUGIN_ID = "com.abso.weatherbug.ui";

    /** The shared plug-in instance. */
    private static WeatherBugUIPlugin plugin;

    /** The WeatherBug license key (registered for http://abso.freehostia.com). */
    private static final String WEATHER_BUG_LICENSE_KEY = "A4571437773";

    /** The location history. */
    private List locationHistory;

    /** The list of favorite locations. */
    private List favLocations;

    /** The current location. */
    private Location currentLocation;

    /** The current station. */
    private Station currentStation;

    /** Constructs a new plug-in. */
    public WeatherBugUIPlugin() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        this.locationHistory = new ArrayList();
        this.favLocations = PreferenceUtils.stringToLocations(getPreferenceStore().getString(IWeatherBugConstants.PREF_FAV_LOCATIONS));
        getPreferenceStore().setDefault(IWeatherBugConstants.PREF_LIVE_WEATHER_REFRESH_RATE, 300);  // 5 minutes
        getPreferenceStore().setDefault(IWeatherBugConstants.PREF_FORECAST_REFRESH_RATE, 1800);  // 30 minutes
        getPreferenceStore().setDefault(IWeatherBugConstants.PREF_US_LOCATION, true);
        getPreferenceStore().setDefault(IWeatherBugConstants.PREF_CITY_NAME, IWeatherBugConstants.GERMANTOWN_CITY_NAME);
        getPreferenceStore().setDefault(IWeatherBugConstants.PREF_STATE_NAME, IWeatherBugConstants.GERMANTOWN_STATE_NAME);
        getPreferenceStore().setDefault(IWeatherBugConstants.PREF_LOCATION_CODE, IWeatherBugConstants.GERMANTOWN_ZIP_CODE);
        getPreferenceStore().setDefault(IWeatherBugConstants.PREF_UNIT_TYPE, IWeatherBugConstants.US_UNIT_TYPE);
        boolean usLocation = getPreferenceStore().getBoolean(IWeatherBugConstants.PREF_US_LOCATION);
        int locationCode = getPreferenceStore().getInt(IWeatherBugConstants.PREF_LOCATION_CODE);
        this.currentLocation = new Location(getPreferenceStore().getString(IWeatherBugConstants.PREF_CITY_NAME), getPreferenceStore()
                .getString(IWeatherBugConstants.PREF_STATE_NAME), getPreferenceStore().getString(
                IWeatherBugConstants.PREF_COUNTRY_NAME), usLocation ? locationCode : -1, usLocation ? -1 : locationCode,
                usLocation ? Location.US_CITY_TYPE : Location.NON_US_CITY_TYPE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    protected void initializeImageRegistry(ImageRegistry reg) {
        reg.put(WeatherBugImages.ADD_TO_FAVORITES, imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/clcl16/add_to_favorites.png"));
        reg.put(WeatherBugImages.EU, imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/clcl16/eu.png"));
        reg.put(WeatherBugImages.FAVORITES, imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/clcl16/favorites.png"));
        reg.put(WeatherBugImages.LOCATION_HISTORY, imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/clcl16/location_history.png"));
        reg.put(WeatherBugImages.OPEN_WEB_SITE, imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/clcl16/open_web_site.png"));
        reg.put(WeatherBugImages.REFRESH, imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/clcl16/refresh.png"));
        reg.put(WeatherBugImages.SEARCH, imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/clcl16/search.png"));
        reg.put(WeatherBugImages.US, imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/clcl16/us.png"));
        DecimalFormat intFormat3 = new DecimalFormat("000");
        for (int i = 0; i < 176; i++) {
            String index = intFormat3.format(i);
            reg
                    .put(WeatherBugImages.CONDITION + index, imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/obj16/cond" + index
                            + ".gif"));
        }
        reg.put(WeatherBugImages.CONDITION + "999", imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/obj16/cond999.gif"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static WeatherBugUIPlugin getDefault() {
        return plugin;
    }

    /**
     * Creates a WeatherBug service.
     * 
     * @return the newly created service.
     */
    private WeatherBugService createService() {
        return new WeatherBugService(WEATHER_BUG_LICENSE_KEY);
    }

    /**
     * Returns the live weather for the current location.
     * 
     * @return the live weather.
     * @throws WeatherBugServiceException
     *             if an error occurred returning the live weather.
     */
    public LiveWeather getLiveWeather() throws WeatherBugServiceException {
        if (currentStation != null) {
            return createService().getLiveWeatherByStationID(currentStation.getId(), getUnitType());
        } else {
            boolean isUSLocation = getPluginPreferences().getBoolean(IWeatherBugConstants.PREF_US_LOCATION);
            int locationCode = getPluginPreferences().getInt(IWeatherBugConstants.PREF_LOCATION_CODE);
            if (isUSLocation) {
                return createService().getLiveWeatherByUSZipCode(locationCode, getUnitType());
            } else {
                return createService().getLiveWeatherByCityCode(locationCode, getUnitType());
            }
        }
    }

    /**
     * Returns the forecasts for the current location.
     * 
     * @return the forecasts.
     * @throws WeatherBugServiceException
     *             if an error occurred returning the forecasts.
     */
    public Forecasts getForecasts() throws WeatherBugServiceException {
        boolean isUSLocation = getPluginPreferences().getBoolean(IWeatherBugConstants.PREF_US_LOCATION);
        int locationCode = getPluginPreferences().getInt(IWeatherBugConstants.PREF_LOCATION_CODE);
        if (isUSLocation) {
            return createService().getForecastByUSZipCode(locationCode, getUnitType());
        } else {
            return createService().getForecastByCityCode(locationCode, getUnitType());
        }
    }

    /**
     * Searches the locations matching a given search string.
     * 
     * @param searchString
     *            the matching string.
     * @return the locations matching the search string.
     * @throws WeatherBugServiceException
     *             if an error occurred searching locations.
     */
    public Location[] searchLocations(String searchString) throws WeatherBugServiceException {
        return createService().getLocationList(searchString);
    }

    /**
     * Searches the stations for a given location code.
     * 
     * @param usLocation
     *            <code>true</code> if <i>locationCode</i> is a U.S. location.
     * @param locationCode
     *            the ZIP code or city code.
     * @return the stations in the area.
     * @throws WeatherBugServiceException
     *             if an error occurred searching stations.
     */
    public Station[] searchStations(boolean usLocation, int locationCode) throws WeatherBugServiceException {
        if (usLocation) {
            return createService().getStationListByUSZipCode(locationCode);
        } else {
            return createService().getStationListByCityCode(locationCode);
        }
    }

    /**
     * Returns the label for the current location.
     * 
     * @return the label's text for the current location.
     */
    public String getCurrentLocationLabel() {
        if (getPluginPreferences().getBoolean(IWeatherBugConstants.PREF_US_LOCATION)) {
            return getPluginPreferences().getString(IWeatherBugConstants.PREF_CITY_NAME) + ", "
                    + getPluginPreferences().getString(IWeatherBugConstants.PREF_STATE_NAME) + " "
                    + getPluginPreferences().getString(IWeatherBugConstants.PREF_LOCATION_CODE);
        } else {
            return getPluginPreferences().getString(IWeatherBugConstants.PREF_CITY_NAME) + ", "
                    + getPluginPreferences().getString(IWeatherBugConstants.PREF_COUNTRY_NAME);
        }
    }

    /**
     * Saves a location into preferences.
     * 
     * @param location
     *            the new current location.
     */
    public void saveCurrentLocation(Location location) {
        if (location.getZipCode() != -1) {
            getPluginPreferences().setValue(IWeatherBugConstants.PREF_US_LOCATION, true);
            getPluginPreferences().setValue(IWeatherBugConstants.PREF_CITY_NAME, location.getCityName());
            getPluginPreferences().setValue(IWeatherBugConstants.PREF_STATE_NAME, location.getStateName());
            getPluginPreferences().setValue(IWeatherBugConstants.PREF_LOCATION_CODE, location.getZipCode());
        } else {
            getPluginPreferences().setValue(IWeatherBugConstants.PREF_US_LOCATION, false);
            getPluginPreferences().setValue(IWeatherBugConstants.PREF_CITY_NAME, location.getCityName());
            getPluginPreferences().setValue(IWeatherBugConstants.PREF_COUNTRY_NAME, location.getCountryName());
            getPluginPreferences().setValue(IWeatherBugConstants.PREF_LOCATION_CODE, location.getCityCode());
        }
        if (locationHistory.indexOf(location) == -1) {
            locationHistory.add(location);
        }
        currentLocation = location;
        currentStation = null;
    }

    /**
     * Gets the current location.
     * 
     * @return the current location.
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Saves temporarily the current station.
     * 
     * @param station
     *            the current station.
     */
    public void saveCurrentStation(Station station) {
        currentStation = station;
    }

    /** Clears the location history. */
    public void clearLocationHistory() {
        locationHistory.clear();
    }

    /**
     * Gets the current location history.
     * 
     * @return the location history.
     */
    public Location[] getLocationHistory() {
        return (Location[]) locationHistory.toArray(new Location[0]);
    }

    /**
     * Gets the unit type.
     * 
     * @return the unit type.
     */
    public int getUnitType() {
        return getPluginPreferences().getInt(IWeatherBugConstants.PREF_UNIT_TYPE);
    }

    /**
     * Sets the unit type.
     * 
     * @param unitType
     *            the unit type.
     */
    public void setUnitType(int unitType) {
        getPluginPreferences().setValue(IWeatherBugConstants.PREF_UNIT_TYPE, unitType);
    }

    /**
     * Adds the current location to the list of favorite locations.
     * 
     * @return <code>true</code> if the location is added to favorites; <code>false</code> if it is already a favorite location.
     */
    public boolean addCurrentLocationToFavorites() {
        if (favLocations.indexOf(currentLocation) == -1) {
            favLocations.add(currentLocation);
            getPluginPreferences().setValue(IWeatherBugConstants.PREF_FAV_LOCATIONS, PreferenceUtils.locationsToString(favLocations));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the favorite locations.
     * 
     * @return the favorite locations.
     */
    public Location[] getFavoriteLocations() {
        return (Location[]) favLocations.toArray(new Location[0]);
    }

    /**
     * Clears the favorite locations.
     */
    public void clearFavoriteLocations() {
        favLocations.clear();
        getPluginPreferences().setValue(IWeatherBugConstants.PREF_FAV_LOCATIONS, PreferenceUtils.locationsToString(favLocations));
    }

    /**
     * Update the set of favorite locations.
     * 
     * @param favLocationsToPreserve
     *            the list of <i>Location</i> to preserve.
     * @see Location
     */
    public void updateFavoriteLocations(List favLocationsToPreserve) {
        Set favLocationsSet = new HashSet(favLocationsToPreserve);
        boolean updated = false;
        for (Iterator i = favLocations.iterator(); i.hasNext();) {
            Location location = (Location) i.next();
            if (!favLocationsSet.contains(location)) {
                i.remove();
                updated = true;
            }
        }
        if (updated) {
            getPluginPreferences().setValue(IWeatherBugConstants.PREF_FAV_LOCATIONS, PreferenceUtils.locationsToString(favLocations));
        }

    }
    
    /**
     * Returns the live weather refresh rate (in seconds).
     * 
     * @return   the live weather refresh rate
     */
    public int getLiveWeatherRefreshRate() {
        return getPluginPreferences().getInt(IWeatherBugConstants.PREF_LIVE_WEATHER_REFRESH_RATE);
    }

    /**
     * Returns the forecast refresh rate (in seconds).
     * 
     * @return   the forecast refresh rate
     */
    public int getForecastRefreshRate() {
        return getPluginPreferences().getInt(IWeatherBugConstants.PREF_FORECAST_REFRESH_RATE);
    }

    /**
     * Gets the active workbench window.
     * 
     * @return the active workbench window.
     */
    public static final IWorkbenchWindow getActiveWorkbenchWindow() {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    /**
     * Gets the active workbench shell.
     * 
     * @return the active workbench shell.
     */
    public static final Shell getActiveWorkbenchShell() {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        return window.getShell();
    }

    /**
     * Shows an error dialog.
     * 
     * @param dialogTitle
     *            the title to use for this dialog.
     * @param dialogMessage
     *            the message to show in this dialog.
     * @param statusMessage
     *            the error to show to the user.
     * @param t
     *            the exception to show to the user.
     */
    public void showErrorDialog(final String dialogTitle, final String dialogMessage, final String statusMessage, final Throwable t) {
        runUIAsync(new Runnable() {
            public void run() {
                ErrorDialog.openError(getActiveWorkbenchShell(), dialogTitle, dialogMessage, new Status(IStatus.ERROR, PLUGIN_ID, 0,
                        statusMessage, t));
            }
        });
    }

    /**
     * Shows a message dialog.
     * 
     * @param dialogTitle
     *            the title to use for this dialog.
     * @param dialogMessage
     *            the message to show in this dialog.
     */
    public void showWarningDialog(final String dialogTitle, final String dialogMessage) {
        runUIAsync(new Runnable() {
            public void run() {
                MessageDialog.openWarning(getActiveWorkbenchShell(), dialogTitle, dialogMessage);
            }
        });
    }

    /**
     * Runs a runnable in the user-interface thread.
     * 
     * @param runnable
     *            the runnable being run.
     */
    public void runUIAsync(Runnable runnable) {
        Display display = getWorkbench().getDisplay();
        if (display != null) {
            display.asyncExec(runnable);
        }
    }

}
