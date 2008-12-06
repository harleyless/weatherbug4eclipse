package com.abso.weatherbug.ui.viewers;

import org.eclipse.jface.viewers.LabelProvider;

import com.abso.weatherbug.core.data.Location;

/**
 * A specialized label provider for showing locations.
 */
public class LocationLabelProvider extends LabelProvider {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        Location location = (Location) element;
        if (location.getZipCode() != -1) {
            return location.getCityName() + ", " + location.getStateName() + " " + location.getZipCode();
        } else {
            return location.getCityName() + ", " + location.getCountryName();
        }
    }
}
