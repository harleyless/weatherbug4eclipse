package com.abso.weatherbug.ui;

import org.eclipse.swt.graphics.Color;

import com.abso.weatherbug.ui.views.ForecastView;
import com.abso.weatherbug.ui.views.LiveWeatherView;

/**
 * A set of UI-aware WeatherBug constants.
 */
public interface IWeatherBugConstants {

    /** The black color. */
    public static final Color BLACK_COLOR = new Color(null, 0, 0, 0);

    /**
     * The browser identifier used to share a browser instead of open a new one each time a action needs to open a URL.
     */
    public static final String BROWSER_ID = WeatherBugUIPlugin.PLUGIN_ID + ".Browser";

    /** The dark yellow color. */
    public static final Color DARK_YELLOW_COLOR = new Color(null, 217, 162, 10);

    /** The Germantown city name. */
    public static final String GERMANTOWN_CITY_NAME = "Germantown";

    /** The Germantown state name. */
    public static final String GERMANTOWN_STATE_NAME = "MD";

    /** The Germantown ZIP code. */
    public static final int GERMANTOWN_ZIP_CODE = 20874;

    /** The high temperature color. */
    public static final Color HIGH_TEMP_COLOR = new Color(null, 214, 81, 0);

    /** The identifier of the Forecast view. */
    public static final String ID_FORECAST_VIEW = ForecastView.class.getName();

    /** The identifier of the Live Weather view. */
    public static final String ID_LIVE_WEATHER_VIEW = LiveWeatherView.class.getName();

    /** The low temperature color. */
    public static final Color LOW_TEMP_COLOR = new Color(null, 0, 60, 183);

    /** The favorites menu group. */
    public static final String MENU_GROUP_FAVORITES = "group.favorites";

    /** The history menu group. */
    public static final String MENU_GROUP_HISTORY = "group.history";

    /** The properties menu group. */
    public static final String MENU_GROUP_PROPERTIES = "group.properties";

    /** The metric unit type. */
    public static final int METRIC_UNIT_TYPE = 1;

    /** The city name preference key. */
    public static final String PREF_CITY_NAME = "cityName";

    /** The country name preference key. */
    public static final String PREF_COUNTRY_NAME = "countryName";

    /** The favorite locations preference key. */
    public static final String PREF_FAV_LOCATIONS = "favLocations";

    /** The forecast refresh rate preference key. */
    public static final String PREF_FORECAST_REFRESH_RATE = "forecastRefreshRate";

    /** The live weather refresh rate preference key. */
    public static final String PREF_LIVE_WEATHER_REFRESH_RATE = "liveWeatherRefreshRate";

    /** The location code preference key. */
    public static final String PREF_LOCATION_CODE = "locationCode";

    /** The state name preference key. */
    public static final String PREF_STATE_NAME = "stateName";

    /** The unit type preference key. */
    public static final String PREF_UNIT_TYPE = "unitType";

    /** The U.S. location preference key. */
    public static final String PREF_US_LOCATION = "isUSLocation";

    /** The U.S. unit type. */
    public static final int US_UNIT_TYPE = 0;

    /** The default view background color. */
    public static final Color VIEW_BG_COLOR = new Color(null, 38, 78, 191);

    /** The white color. */
    public static final Color WHITE_COLOR = new Color(null, 255, 255, 255);


}
