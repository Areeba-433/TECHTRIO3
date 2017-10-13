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
package org.eclipse.kapua.app.console.module.authorization.client.tabs.permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.kapua.app.console.module.authorization.client.messages.ConsolePermissionMessages;
import org.eclipse.kapua.app.console.module.api.client.ui.dialog.entity.EntityAddEditDialog;
import org.eclipse.kapua.app.console.module.api.client.ui.panel.FormPanel;
import org.eclipse.kapua.app.console.module.api.client.util.DialogUtils;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtGroup;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtPermission;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtPermission.GwtAction;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtDomain;
import org.eclipse.kapua.app.console.module.api.shared.model.GwtSession;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtAccessInfo;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtAccessPermission;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtAccessPermissionCreator;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtAccessInfoService;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtAccessInfoServiceAsync;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtAccessPermissionService;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtAccessPermissionServiceAsync;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtDomainService;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtDomainServiceAsync;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtGroupService;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtGroupServiceAsync;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtCheckedItems;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class PermissionEditDialog extends EntityAddEditDialog {

    private final static ConsolePermissionMessages MSGS = GWT.create(ConsolePermissionMessages.class);

    private final static GwtDomainServiceAsync GWT_DOMAIN_SERVICE = GWT.create(GwtDomainService.class);
    private final static GwtAccessPermissionServiceAsync GWT_ACCESS_PERMISSION_SERVICE = GWT.create(GwtAccessPermissionService.class);
    private final static GwtAccessInfoServiceAsync GWT_ACCESS_INFO_SERVICE = GWT.create(GwtAccessInfoService.class);
    private final static GwtGroupServiceAsync GWT_GROUP_SERVICE = GWT.create(GwtGroupService.class);

    private ComboBox<GwtGroup> groupsCombo;
    private CheckBoxGroup forwardableChecboxGroup;
    private CheckBox forwardableChecbox;
    private TreeItem treeItem;
    private TreeItem rootTreeItem;
    private CheckBox rootCheckBox;
    private Tree tree;
    private final GwtGroup allGroup;
    private Map<GwtDomain, CheckBox> rootCheckBoxMapList;
    private Map<GwtAction, CheckBox> childCheckBoxMapList;
    private String accessInfoId;
    private GwtCheckedItems checkedItems;
    private Map<GwtCheckedItems, CheckBox> listOfNewClass;
    private List<GwtAction> childListOfNewClass;
    private String userId;
    private List<CheckBox> listCheckBoxes;
    private List<GwtAccessPermission> checkedPermissionsList;
    private boolean checkingAllCheckBox;
    private CheckBox allCheckBox;
    private TreeItem allTreeItem;

    public PermissionEditDialog(GwtSession currentSession, String userId) {
        super(currentSession);
        rootCheckBoxMapList = new HashMap<GwtDomain, CheckBox>();
        this.userId = userId;
        listOfNewClass = new HashMap<GwtCheckedItems, CheckBox>();
        childListOfNewClass = new ArrayList<GwtAction>();
        allGroup = new GwtGroup();
        allGroup.setId(null);
        allGroup.setGroupName("ALL");

        GWT_ACCESS_INFO_SERVICE.findByUserIdOrCreate(currentSession.getSelectedAccountId(), userId, new AsyncCallback<GwtAccessInfo>() {

            @Override
            public void onSuccess(GwtAccessInfo result) {
                accessInfoId = result.getId();
                submitButton.enable();
            }

            @Override
            public void onFailure(Throwable caught) {
                exitMessage = MSGS.dialogAddPermissionErrorAccessInfo(caught.getLocalizedMessage());
                exitStatus = false;
                hide();
            }
        });

        DialogUtils.resizeDialog(this, 400, 700);
    }

    @Override
    public void submit() {

        for (Map.Entry<GwtCheckedItems, CheckBox> mapDomains : listOfNewClass.entrySet()) {
            for (Map.Entry<GwtAction, CheckBox> mapActions : mapDomains.getKey().getMap().entrySet()) {
                if (mapActions.getValue().getValue()) {
                    if (!mapDomains.getValue().getValue()) {
                        mapDomains.getKey().setChecked(true);
                    }
                }
            }
        }

        List<GwtAccessPermissionCreator> gwtPermissionCreators = new ArrayList<GwtAccessPermissionCreator>();
        List<GwtCheckedItems> rootCheckBoxChecked = new ArrayList<GwtCheckedItems>();
        for (Map.Entry<GwtCheckedItems, CheckBox> checkedChilds : listOfNewClass.entrySet()) {

            if (checkedChilds.getKey().isChecked()) {
                List<GwtAction> listActionsWithoutDomain = new ArrayList<GwtAction>();
                for (Map.Entry<GwtAction, CheckBox> gwtAction : checkedChilds.getKey().getMap().entrySet()) {
                    if (gwtAction.getValue().getValue()) {
                        listActionsWithoutDomain.add(gwtAction.getKey());
                    }

                }
                for (GwtAction actions : listActionsWithoutDomain) {
                    GwtPermission newPermissionWithoutDomain = new GwtPermission(
                            checkedChilds.getKey().getName(),
                            actions,
                            currentSession.getSelectedAccountId(),
                            groupsCombo.getValue().getId(),
                            forwardableChecboxGroup.getValue() != null ? true : false);
                    newPermissionWithoutDomain.setName(checkedChilds.getKey().getName().toString());
                    GwtAccessPermissionCreator gwtAccessPermissionCreatorWithoutDomain = new GwtAccessPermissionCreator();
                    gwtAccessPermissionCreatorWithoutDomain.setScopeId(currentSession.getSelectedAccountId());
                    gwtAccessPermissionCreatorWithoutDomain.setAccessInfoId(accessInfoId);
                    gwtAccessPermissionCreatorWithoutDomain.setPermission(newPermissionWithoutDomain);

                    gwtPermissionCreators.add(gwtAccessPermissionCreatorWithoutDomain);
                }

            }
        }

        for (Map.Entry<GwtCheckedItems, CheckBox> e : listOfNewClass.entrySet()) {
            if (e.getValue().getValue()) {
                rootCheckBoxChecked.add(e.getKey());
            }

        }

        for (GwtCheckedItems gwtCheckedItems : rootCheckBoxChecked) {
            GwtPermission gwtPermission = new GwtPermission(
                    gwtCheckedItems.getName(),
                    GwtAction.ALL,
                    currentSession.getSelectedAccountId(),
                    groupsCombo.getValue().getId(),
                    forwardableChecboxGroup.getValue() != null ? true : false);
            GwtAccessPermissionCreator gwtAccessPermissionCreator2 = new GwtAccessPermissionCreator();
            gwtAccessPermissionCreator2.setScopeId(currentSession.getSelectedAccountId());
            gwtAccessPermissionCreator2.setAccessInfoId(accessInfoId);
            gwtAccessPermissionCreator2.setPermission(gwtPermission);

            gwtPermissionCreators.add(gwtAccessPermissionCreator2);

            List<GwtAction> listAction = new ArrayList<GwtAction>();

            for (Map.Entry<GwtAction, CheckBox> childChecked : gwtCheckedItems.getMap().entrySet()) {

                if (childChecked.getValue().getValue()) {
                    listAction.add(childChecked.getKey());
                }
            }

            for (GwtAction gwtAction : listAction) {
                GwtPermission newPermission = new GwtPermission(
                        gwtCheckedItems.getName(),
                        gwtAction,
                        currentSession.getSelectedAccountId(),
                        groupsCombo.getValue().getId(),
                        forwardableChecboxGroup.getValue() != null ? true : false);
                newPermission.setName(gwtCheckedItems.getName().toString());
                GwtAccessPermissionCreator gwtAccessPermissionCreator = new GwtAccessPermissionCreator();
                gwtAccessPermissionCreator.setScopeId(currentSession.getSelectedAccountId());
                gwtAccessPermissionCreator.setAccessInfoId(accessInfoId);
                gwtAccessPermissionCreator.setPermission(newPermission);

                gwtPermissionCreators.add(gwtAccessPermissionCreator);
            }
        }

        GWT_ACCESS_PERMISSION_SERVICE.createCheck(xsrfToken, gwtPermissionCreators, currentSession.getSelectedAccountId(), userId, new AsyncCallback<GwtAccessPermission>() {

            @Override
            public void onFailure(Throwable caught) {
                unmask();

                submitButton.enable();
                cancelButton.enable();
                status.hide();

                exitStatus = false;
                exitMessage = MSGS.dialogAddError(MSGS.dialogAddPermissionError(caught.getLocalizedMessage()));
                //
                hide();
            }

            @Override
            public void onSuccess(GwtAccessPermission result) {
                exitStatus = true;
                exitMessage = MSGS.dialogAddPermissionConfirmation();   // TODO Localize

                hide();
            }
        });
    }

    @Override
    public String getHeaderMessage() {
        return MSGS.dialogAddPermissionHeader();
    }

    @Override
    public String getInfoMessage() {
        return MSGS.dialogEditPermissionInfo();
    }

    @Override
    public void createBody() {
        final FormPanel permissionFormPanel = new FormPanel(FORM_LABEL_WIDTH);
        tree = new Tree();

        allCheckBox = new CheckBox();
        allCheckBox.setBoxLabel(MSGS.allDomains());
        allTreeItem = new TreeItem(allCheckBox);
        listCheckBoxes = new ArrayList<CheckBox>();

        GWT_DOMAIN_SERVICE.findAll(new AsyncCallback<List<GwtDomain>>() {

            @Override
            public void onFailure(Throwable caught) {
                exitMessage = MSGS.dialogAddPermissionErrorDomains(caught.getLocalizedMessage());
                exitStatus = false;
                hide();
            }

            @Override
            public void onSuccess(List<GwtDomain> result) {

                for (final GwtDomain gwtDomain : result) {
                    GWT_DOMAIN_SERVICE.findActionsByDomainName(gwtDomain.name(), new AsyncCallback<List<GwtAction>>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            exitMessage = MSGS.dialogAddPermissionErrorActions(caught.getLocalizedMessage());
                            exitStatus = false;
                            hide();
                        }

                        @Override
                        public void onSuccess(List<GwtAction> result) {
                            checkedItems = new GwtCheckedItems();
                            checkedItems.setName(gwtDomain);
                            rootCheckBox = new CheckBox();
                            rootCheckBox.setBoxLabel(gwtDomain.getDomainName());
                            listCheckBoxes.add(rootCheckBox);
                            rootTreeItem = new TreeItem(rootCheckBox);
                            for (Map.Entry<GwtCheckedItems, CheckBox> map : listOfNewClass.entrySet()) {
                                boolean checking = true;
                                if (!map.getValue().getValue()) {
                                    checking = false;
                                    break;
                                }
                                checkingAllCheckBox = checking;
                            }
                            childCheckBoxMapList = new HashMap<GwtAction, CheckBox>();
                            checkedItems.setMap(childCheckBoxMapList);
                            for (GwtAccessPermission gwtAccessPermission : checkedPermissionsList) {
                                if (gwtAccessPermission.getPermissionDomain().equals(checkedItems.getName().getDomainName())) {
                                    if (gwtAccessPermission.getPermissionAction().toString().equals(GwtAction.ALL.toString())) {
                                        rootCheckBox.setValue(true);
                                    }
                                }
                            }

                            for (final GwtAction gwtAction : result) {

                                final CheckBox childTreeItemCheckox = new CheckBox();
                                treeItem = new TreeItem(childTreeItemCheckox);
                                childTreeItemCheckox.setBoxLabel(gwtAction.toString());

                                rootTreeItem.addItem(treeItem);

                                childListOfNewClass.add(gwtAction);
                                allTreeItem.addItem(rootTreeItem);
                                childCheckBoxMapList.put(gwtAction, childTreeItemCheckox);
                                for (GwtAccessPermission gwtAccessPermission : checkedPermissionsList) {
                                    if (gwtAccessPermission.getPermissionDomain().toString().equals(gwtDomain.getDomainName())) {

                                        if (gwtAccessPermission.getPermissionAction().toString().equals(gwtAction)) {
                                            childTreeItemCheckox.setValue(true);
                                        }
                                    }
                                }
                            }

                            listOfNewClass.put(checkedItems, rootCheckBox);

                            allCheckBox.setValue(checkingAllCheckBox);
                            rootCheckBox.addListener(Events.OnClick, new Listener<BaseEvent>() {

                                @Override
                                public void handleEvent(BaseEvent be) {
                                    boolean allSelected = true;
                                    for (CheckBox child : listCheckBoxes) {
                                        if (!child.getValue()) {
                                            allSelected = false;
                                            break;
                                        }
                                    }
                                    allCheckBox.setValue(allSelected);
                                }
                            });
                        }

                    });

                }

                allCheckBox.addListener(Events.OnClick, new Listener<BaseEvent>() {

                    @Override
                    public void handleEvent(BaseEvent be) {
                        if (allCheckBox.getValue()) {
                            for (CheckBox checkBox : listCheckBoxes) {
                                if (!checkBox.getValue()) {
                                    checkBox.setValue(true);
                                }
                            }
                        } else {
                            for (CheckBox checkBox : listCheckBoxes) {
                                checkBox.setValue(false);
                            }
                        }
                    }
                });
                tree.addItem(allTreeItem);
            }
        });

        permissionFormPanel.add(tree);

        // Groups
        groupsCombo = new ComboBox<GwtGroup>();
        groupsCombo.setStore(new ListStore<GwtGroup>());
        groupsCombo.setEditable(false);
        groupsCombo.setTypeAhead(false);
        groupsCombo.setAllowBlank(false);
        groupsCombo.setDisplayField("groupName");
        groupsCombo.setValueField("id");
        groupsCombo.setFieldLabel(MSGS.dialogAddPermissionGroupId());
        groupsCombo.setTriggerAction(TriggerAction.ALL);
        groupsCombo.setEmptyText(MSGS.dialogAddPermissionLoading());
        groupsCombo.disable();
        GWT_GROUP_SERVICE.findAll(currentSession.getSelectedAccountId(), new AsyncCallback<List<GwtGroup>>() {

            @Override
            public void onFailure(Throwable caught) {
                exitMessage = MSGS.dialogAddPermissionErrorGroups(caught.getLocalizedMessage());
                exitStatus = false;
                hide();
            }

            @Override
            public void onSuccess(List<GwtGroup> result) {
                groupsCombo.getStore().removeAll();
                groupsCombo.getStore().add(allGroup);
                groupsCombo.getStore().add(result);
                groupsCombo.setValue(allGroup);
                groupsCombo.enable();
            }
        });
        permissionFormPanel.add(groupsCombo);

        //
        // Forwardable
        forwardableChecbox = new CheckBox();
        forwardableChecbox.setBoxLabel("");

        forwardableChecboxGroup = new CheckBoxGroup();
        forwardableChecboxGroup.setFieldLabel(MSGS.applyToChildAccount());
        forwardableChecboxGroup.add(forwardableChecbox);
        permissionFormPanel.add(forwardableChecboxGroup);

        //
        // Add form panel to body
        bodyPanel.add(permissionFormPanel);
        //
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        submitButton.disable();
    }

    public void setCheckedPermissionsList(List<GwtAccessPermission> checkedPermissionsList) {
        this.checkedPermissionsList = checkedPermissionsList;
    }
}