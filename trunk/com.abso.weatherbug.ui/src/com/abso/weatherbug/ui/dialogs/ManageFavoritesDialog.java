package com.abso.weatherbug.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.abso.weatherbug.ui.viewers.LocationLabelProvider;

/**
 * The dialog to manage favorite locations.
 */
public class ManageFavoritesDialog extends TrayDialog {

    /** The list of favorite locations. */
    private List favLocations;

    /** The location viewer. */
    private TableViewer locationViewer;

    /** The button to remove a location from the list of favorites. */
    private Button removeButton;

    /**
     * Constructs a new dialog
     * 
     * @param favLocations
     *            the favorite locations.
     * @param shell
     *            the parent shell.
     */
    public ManageFavoritesDialog(List favLocations, Shell shell) {
        super(shell);
        this.favLocations = favLocations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Manage Favorites");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        comp.setLayout(new GridLayout(2, false));
        createLeftListArea(comp);
        createRightButtonArea(comp);
        refreshButton();
        return comp;
    }

    /**
     * Creates the list area to be placed on the left.
     * 
     * @param parent
     *            the parent composite.
     */
    private void createLeftListArea(Composite parent) {
        Table table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setFont(JFaceResources.getDialogFont());
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.widthHint = convertWidthInCharsToPixels(40);
        gd.heightHint = convertHeightInCharsToPixels(20);
        table.setLayoutData(gd);
        locationViewer = new TableViewer(table);
        locationViewer.setLabelProvider(new LocationLabelProvider());
        locationViewer.setContentProvider(new ArrayContentProvider());
        locationViewer.setInput(favLocations);
        locationViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                refreshButton();
            }
        });
    }

    /**
     * Creates the button area to be placed on the right.
     * 
     * @param parent
     *            the parent composite.
     */
    private void createRightButtonArea(Composite parent) {
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.BEGINNING, SWT.FILL, false, false);
        buttonComposite.setLayoutData(gridData);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonComposite.setLayout(layout);
        removeButton = new Button(buttonComposite, SWT.PUSH);
        removeButton.setText(" Remove ");
        removeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
        removeButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                List selectedLocations = ((IStructuredSelection) locationViewer.getSelection()).toList();
                if (!selectedLocations.isEmpty()) {
                    favLocations.removeAll(selectedLocations);
                    locationViewer.refresh();
                    refreshButton();
                }
            }

        });
    }

    /** Refreshes the button enabled state. */
    private void refreshButton() {
        removeButton.setEnabled(!((IStructuredSelection) locationViewer.getSelection()).toList().isEmpty());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

}
