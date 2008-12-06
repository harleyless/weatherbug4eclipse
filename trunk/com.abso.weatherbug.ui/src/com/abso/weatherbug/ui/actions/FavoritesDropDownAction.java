package com.abso.weatherbug.ui.actions;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.ui.WeatherBugImages;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;

/**
 * The drop down action showing a menu for managing the favorite locations.
 */
public class FavoritesDropDownAction extends Action implements IMenuCreator {

    /**
     * A comparator able to compare label strings associated with locations.
     */
    private static final Comparator LOCATION_COMPARATOR = new Comparator() {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2) {
            return toLabel((Location) o1).compareTo(toLabel((Location) o2));
        }

        /**
         * Returns the label string associated with a location.
         * 
         * @param loc
         *            a location.
         * @return the label string.
         */
        private String toLabel(Location loc) {
            if (loc.getZipCode() != -1) {
                return loc.getCityName() + ", " + loc.getStateName() + " " + loc.getZipCode();
            } else {
                return loc.getCityName() + ", " + loc.getCountryName();
            }
        }

    };

    /** The shown menu. */
    private Menu menu;

    /** Constructs a new action. */
    public FavoritesDropDownAction() {
        setImageDescriptor(WeatherBugImages.getDescriptor(WeatherBugImages.FAVORITES));
        setToolTipText("Favorites");
        setMenuCreator(this);
    }

    /** Updates the enabled state of this action. */
    public void updateEnablement() {
        setEnabled(WeatherBugUIPlugin.getDefault().getFavoriteLocations().length != 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent) {
        disposeMenu();
        menu = new Menu(parent);
        Location[] favLocations = WeatherBugUIPlugin.getDefault().getFavoriteLocations();
        Arrays.sort(favLocations, LOCATION_COMPARATOR);
        Location currentLocation = WeatherBugUIPlugin.getDefault().getCurrentLocation();
        if (favLocations.length != 0) {
            for (int i = 0; i < favLocations.length; i++) {
                SearchFromFavoriteAction action = new SearchFromFavoriteAction(favLocations[i]);
                action.setChecked(favLocations[i].equals(currentLocation));
                addActionToMenu(menu, action);
            }
            new MenuItem(menu, SWT.SEPARATOR);
            addActionToMenu(menu, new ManageFavoritesAction());
            addActionToMenu(menu, new ClearFavoritesAction());
        }
        return menu;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent) {
        return null;
    }

    /**
     * Adds an action to a menu.
     * 
     * @param menu
     *            a menu.
     * @param action
     *            the action to add.
     */
    private void addActionToMenu(Menu menu, Action action) {
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(menu, -1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose() {
        disposeMenu();
    }

    /** Disposes the menu. */
    private void disposeMenu() {
        if (menu != null) {
            menu.dispose();
        }
    }

}
