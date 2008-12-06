package com.abso.weatherbug.ui.views;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import com.abso.weatherbug.core.WeatherBugServiceException;
import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.core.data.WeatherBugDataUtils;
import com.abso.weatherbug.ui.IWeatherBugConstants;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.actions.AddToFavoritesAction;
import com.abso.weatherbug.ui.actions.FavoritesDropDownAction;
import com.abso.weatherbug.ui.actions.LocationHistoryDropDownAction;
import com.abso.weatherbug.ui.actions.SetUnitTypeAction;

/**
 * An abstract base class for live weather and forecast views.
 */
public abstract class WeatherBugView extends ViewPart {

    /** The drop-down action for managing favorites. */
    private FavoritesDropDownAction favoritesAction;

    /** The action to set the U.S. unit type. */
    private SetUnitTypeAction usUnitTypeAction;

    /** The action to set the metric unit type. */
    private SetUnitTypeAction metricUnitTypeAction;

    /** The drop-down action for managing the location history. */
    private LocationHistoryDropDownAction locationHistoryAction;

    /** Adds the actions for managing favorite locations. */
    protected void addFavoritesActions() {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        toolBarManager.add(new Separator(IWeatherBugConstants.MENU_GROUP_FAVORITES));
        AddToFavoritesAction addToFavoritesAction = new AddToFavoritesAction();
        toolBarManager.appendToGroup(IWeatherBugConstants.MENU_GROUP_FAVORITES, addToFavoritesAction);
        favoritesAction = new FavoritesDropDownAction();
        toolBarManager.appendToGroup(IWeatherBugConstants.MENU_GROUP_FAVORITES, favoritesAction);
    }

    /** Adds the actions for managing the location history. */
    protected void addLocationHistoryAction() {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        toolBarManager.add(new Separator(IWeatherBugConstants.MENU_GROUP_HISTORY));
        locationHistoryAction = new LocationHistoryDropDownAction();
        toolBarManager.appendToGroup(IWeatherBugConstants.MENU_GROUP_HISTORY, locationHistoryAction);
    }

    /** Adds the actions for selecting the unit type. */
    protected void addUnitTypeActions() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        menuManager.add(new Separator(IWeatherBugConstants.MENU_GROUP_PROPERTIES));
        usUnitTypeAction = new SetUnitTypeAction(true);
        menuManager.appendToGroup(IWeatherBugConstants.MENU_GROUP_PROPERTIES, usUnitTypeAction);
        metricUnitTypeAction = new SetUnitTypeAction(false);
        menuManager.appendToGroup(IWeatherBugConstants.MENU_GROUP_PROPERTIES, metricUnitTypeAction);
    }

    /**
     * Sets the unit type.
     * 
     * @param unitType
     *            the unit type.
     * @see IWeatherBugConstants#US_UNIT_TYPE
     * @see IWeatherBugConstants#METRIC_UNIT_TYPE
     */
    public void setUnitType(int unitType) {
        if (unitType == IWeatherBugConstants.US_UNIT_TYPE) {
            usUnitTypeAction.setChecked(true);
            metricUnitTypeAction.setChecked(false);
        } else {
            usUnitTypeAction.setChecked(false);
            metricUnitTypeAction.setChecked(true);
        }
    }

    /** Updates the location history. */
    public void updateHistory() {
        locationHistoryAction.updateEnablement();
    }

    /** Updates the favorite locations. */
    public void updateFavorites() {
        favoritesAction.updateEnablement();
    }

    /**
     * Creates a label.
     * 
     * @param parent
     *            the parent composite.
     * @param text
     *            the label's text.
     * @param font
     *            the font.
     * @param color
     *            the foreground color.
     * @return the newly created label.
     */
    protected Label createLabel(Composite parent, String text, Font font, Color color) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Refreshes a label with a decimal value.
     * 
     * @param label
     *            the label being refreshed.
     * @param prefix
     *            the text prefix.
     * @param value
     *            the decimal value.
     * @param separator
     *            the separator to be used between <i>value</i> and <i>units</i>.
     * @param units
     *            the units.
     */
    protected void refreshDecimal(Label label, String prefix, BigDecimal value, String separator, String units) {
        if (value != null) {
            label.setText(prefix + value + separator + units);
        } else {
            label.setText(prefix + "N/A");
        }
    }

    /**
     * Refreshes a label showing a gust value.
     * 
     * @param label
     *            the label being refreshed.
     * @param prefix
     *            the text prefix.
     * @param gust
     *            the gust value.
     * @param gustUnits
     *            the units.
     */
    protected void refreshGust(Label label, String prefix, BigDecimal gust, String gustUnits) {
        if (gust != null) {
            label.setText(prefix + gustUnits + " " + gust);
        } else {
            label.setText(prefix + "N/A");
        }
    }

    /**
     * Refreshes a label showing a timestamp.
     * 
     * @param label
     *            the label being refreshed.
     * @param prefix
     *            the text prefix.
     * @param time
     *            the timestamp.
     */
    protected void refreshTimestamp(Label label, String prefix, Timestamp time) {
        if (time != null) {
            label.setText(prefix + WeatherBugDataUtils.formatTimestamp(time, "h:mm a"));
        } else {
            label.setText(prefix + "N/A");
        }
    }

    /**
     * A runnable able to perform a search.
     */
    protected static final class SearchRunnable implements IRunnableWithProgress {

        /** The string to match. */
        private String searchString;

        /** The found locations. */
        private Location[] locations;

        /** The exception thrown by the service execution. */
        private WeatherBugServiceException serviceException;

        /**
         * Constructs a new runnable.
         * 
         * @param searchString
         *            the string to match.
         */
        public SearchRunnable(String searchString) {
            this.searchString = searchString;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            try {
                this.locations = WeatherBugUIPlugin.getDefault().searchLocations(searchString);
            } catch (WeatherBugServiceException e) {
                this.serviceException = e;
            }
            if (monitor.isCanceled()) {
                this.locations = null;
                this.serviceException = null;
            }
        }

        /**
         * Returns the found locations.
         * 
         * @return the locations matching the search string.
         */
        public Location[] getLocations() {
            return locations;
        }

        /**
         * Returns the exception (if any) thrown by the service execution.
         * 
         * @return the service exception.
         */
        public WeatherBugServiceException getServiceException() {
            return serviceException;
        }

    }

    /**
     * Returns the WeatherBug site URL associated with the information currently displayed in this view.
     * 
     * @return the site URL, or <code>null</code> if not available.
     */
    public abstract URL getWeatherBugSiteURL();

}
