/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.console.client.data;

import java.util.List;

import org.eclipse.kapua.app.console.client.messages.ConsoleDataMessages;
import org.eclipse.kapua.app.console.client.resources.icons.IconSet;
import org.eclipse.kapua.app.console.client.resources.icons.KapuaIcon;
import org.eclipse.kapua.app.console.client.ui.button.Button;
import org.eclipse.kapua.app.console.client.ui.tab.TabItem;
import org.eclipse.kapua.app.console.shared.model.GwtDatastoreAsset;
import org.eclipse.kapua.app.console.shared.model.GwtDatastoreChannel;
import org.eclipse.kapua.app.console.shared.model.GwtDatastoreDevice;
import org.eclipse.kapua.app.console.shared.model.GwtSession;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;

public class AssetTabItem extends  TabItem{

    private static final ConsoleDataMessages MSGS = GWT.create(ConsoleDataMessages.class);
    
    private GwtSession currentSession;
    private DeviceTable deviceTable;
    private Button queryButton;
    private ResultsTable resultsTable;

    private AssetTable assetTable;

    private ChannelTable channelTable;

    public AssetTabItem(GwtSession currentSession) {
        super(MSGS.assetTabItemTitle(), null);
        this.currentSession = currentSession;
        this.setBorders(false);
        this.setLayout(new BorderLayout());
    }
    
    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        this.setWidth("100%");
        BorderLayoutData messageLayout = new BorderLayoutData(LayoutRegion.NORTH, 0.06f);
        messageLayout.setMargins(new Margins(5));
        Text welcomeMessage = new Text();
        welcomeMessage.setText(MSGS.assetTabItemMessage());
        add(welcomeMessage, messageLayout);
        
        LayoutContainer tables = new LayoutContainer(new BorderLayout());
        BorderLayoutData tablesLayout = new BorderLayoutData(LayoutRegion.CENTER);
        tablesLayout.setMinSize(250);
        add(tables, tablesLayout);
        
        BorderLayoutData deviceLayout = new BorderLayoutData(LayoutRegion.WEST, 0.3f);
        deviceTable = new DeviceTable(currentSession);
        deviceLayout.setMargins(new Margins(0, 5, 0, 0));
        deviceLayout.setSplit(true);
        deviceTable.addSelectionChangedListener(new SelectionChangedListener<GwtDatastoreDevice>() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent<GwtDatastoreDevice> se) {
                assetTable.refresh(se.getSelectedItem());
            }
        });
        tables.add(deviceTable, deviceLayout);
        
        BorderLayoutData assetLayout = new BorderLayoutData(LayoutRegion.CENTER, 0.3f);
        assetLayout.setMargins(new Margins(0, 5, 0, 5));
        assetLayout.setSplit(true);
        assetTable = new AssetTable(currentSession);
        assetTable.addSelectionChangedListener(new SelectionChangedListener<GwtDatastoreAsset>() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent<GwtDatastoreAsset> se) {
                GwtDatastoreDevice selectedDevice = deviceTable.getSelectedDevice();
                channelTable.refresh(selectedDevice, se.getSelectedItem());
            }
        });
        tables.add(assetTable, assetLayout);

        BorderLayoutData channelLayout = new BorderLayoutData(LayoutRegion.EAST, 0.3f);
        channelLayout.setMargins(new Margins(0, 0, 0, 5));
        channelLayout.setSplit(true);
        channelTable = new ChannelTable(currentSession);
        channelTable.addSelectionListener(new SelectionChangedListener<GwtDatastoreChannel>() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent<GwtDatastoreChannel> se) {
                queryButton.setEnabled(!se.getSelection().isEmpty());
            }
        });
        tables.add(channelTable,channelLayout);
        
        BorderLayoutData queryButtonLayout = new BorderLayoutData(LayoutRegion.SOUTH, 0.1f);
        queryButtonLayout.setMargins(new Margins(5));
        queryButton = new Button(MSGS.assetTabItemQueryButtonText(), new KapuaIcon(IconSet.SEARCH), new SelectionListener<ButtonEvent>() {
            
            @Override
            public void componentSelected(ButtonEvent ce) {
                GwtDatastoreDevice gwtDevice = deviceTable.getSelectedDevice();
                GwtDatastoreAsset gwtAsset = assetTable.getSelectedAsset();
                List<GwtDatastoreChannel> gwtChannels = channelTable.getSelectedChannels();
                // TODO Fetch data.
            }
        });
        queryButton.disable();
        TableLayout queryButtonTL = new TableLayout();
        queryButtonTL.setCellPadding(0);
        LayoutContainer queryButtonContainer = new LayoutContainer(queryButtonTL);
        queryButtonContainer.add(queryButton, new TableData());
        tables.add(queryButtonContainer, queryButtonLayout);

        BorderLayoutData resultsLayout = new BorderLayoutData(LayoutRegion.SOUTH);
        resultsLayout.setSplit(true);
        resultsLayout.setMargins(new Margins(5, 0, 0, 0));

        
        TabPanel resultsTabPanel = new TabPanel();
        resultsTable = new ResultsTable(currentSession);
        TabItem resultsTableTabItem = new TabItem(MSGS.resultsTableTabItemTitle(), new KapuaIcon(IconSet.TABLE));
        resultsTableTabItem.setLayout(new FitLayout());
        resultsTableTabItem.add(resultsTable);
        resultsTabPanel.add(resultsTableTabItem);
        
        add(resultsTabPanel, resultsLayout);
    }
}
