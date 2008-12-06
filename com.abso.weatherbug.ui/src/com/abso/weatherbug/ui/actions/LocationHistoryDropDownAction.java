package com.abso.weatherbug.ui.actions;

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
 * The drop down action showing a menu for managing the location history.
 */
public class LocationHistoryDropDownAction extends Action implements IMenuCreator {

    /** The shown menu. */
    private Menu menu;

    /** Constructs a new action. */
    public LocationHistoryDropDownAction() {
        setImageDescriptor(WeatherBugImages.getDescriptor(WeatherBugImages.LOCATION_HISTORY));
        setToolTipText("Location History");
        setMenuCreator(this);
    }

    /** Updates the enabled state of this action. */
    public void updateEnablement() {
        Location[] locationHistory = WeatherBugUIPlugin.getDefault().getLocationHistory();
        setEnabled((locationHistory != null) && (locationHistory.length > 0));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent) {
        disposeMenu();
        menu = new Menu(parent);
        Location[] locationHistory = WeatherBugUIPlugin.getDefault().getLocationHistory();
        Location currentLocation = WeatherBugUIPlugin.getDefault().getCurrentLocation();
        if ((locationHistory != null) && (locationHistory.length > 0)) {
            for (int i = 0; i < locationHistory.length; i++) {
                SearchFromHistoryAction action = new SearchFromHistoryAction(locationHistory[i]);
                action.setChecked(locationHistory[i].equals(currentLocation));
                addActionToMenu(menu, action);
            }
            new MenuItem(menu, SWT.SEPARATOR);
            addActionToMenu(menu, new ClearHistoryAction());
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
