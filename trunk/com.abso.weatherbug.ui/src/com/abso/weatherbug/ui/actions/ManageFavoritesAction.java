package com.abso.weatherbug.ui.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;

import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.dialogs.ManageFavoritesDialog;

/**
 * The action able to open the dialog for managing favorite locations.
 */
public class ManageFavoritesAction extends Action {

    /** Constructs a new action. */
    public ManageFavoritesAction() {
        super("Favorites...");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        List tempFavLocations = new ArrayList();
        tempFavLocations.addAll(Arrays.asList(WeatherBugUIPlugin.getDefault().getFavoriteLocations()));
        if (new ManageFavoritesDialog(tempFavLocations, WeatherBugUIPlugin.getActiveWorkbenchShell()).open() == Window.OK) {
            WeatherBugUIPlugin.getDefault().updateFavoriteLocations(tempFavLocations);
        }
    }

}
