/*******************************************************************************
* Copyright (c) 2011, 2016 Eurotech and/or its affiliates                       
*                                                                               
* All rights reserved. This program and the accompanying materials              
* are made available under the terms of the Eclipse Public License v1.0         
* which accompanies this distribution, and is available at                      
* http://www.eclipse.org/legal/epl-v10.html                                     
*                                                                               
* Contributors:                                                                 
*     Eurotech - initial API and implementation                                 
*                                                                               
*******************************************************************************/
import { DeviceConnectionStatus } from "../models/DeviceConnectionStatus";

export default class DevicesListCtrl {
  private devices: Device[];

  constructor(private $http: angular.IHttpService,
              private $modal: angular.ui.bootstrap.IModalService) {
    this.getDevices().then((result) => {
      $(() => {
        this.devices = result.data.items.item;
        // DataTable Config
        $("#table1").DataTable({
          columns: [
            {
              data: null,
              className: "table-view-pf-select",
              render: function (data, type, full, meta) {
                // Select row checkbox renderer
                let id = "select" + meta.row;
                return `<label class="sr-only" for="` + id + `">Select row ` + meta.row +
                  `</label><input type="checkbox" id="` + id + `" name="` + id + `">`;
              },
              sortable: false,
              width: "10px"
            },
            {
              data: "connectionId",
              render: function(data, type, full, meta) {
                    return data ? `<i class="fa fa-plug" style="color: rgb(29,158,116)"></i>` :
                                  `<i class="fa fa-plug" style="color: rgb(230,200,29)"></i>`;
              },
              width: "10px"
            },
            { data: "clientId" },
            { data: "displayName" },
            { data: "osVersion" },
            { data: "serialNumber" }
          ],
          data: this.devices,
          dom: "t",
          language: {
            zeroRecords: "No records found"
          },
          order: [[1, "asc"]],
          pfConfig: {
            emptyStateSelector: "#emptyState1",
            filterCols: [
              null,
              {
                default: true,
                optionSelector: "#filter1",
                placeholder: "Filter By Rendering Engine..."
              }, {
                optionSelector: "#filter2",
                placeholder: "Filter By Browser..."
              }, {
                optionSelector: "#filter3",
                placeholder: "Filter By Platform(s)..."
              }, {
                optionSelector: "#filter4",
                placeholder: "Filter By Engine Version..."
              }, {
                optionSelector: "#filter5",
                placeholder: "Filter By CSS Grade..."
              }
            ],
            toolbarSelector: "#toolbar1",
            selectAllSelector: `th:first-child input[type="checkbox"]`
          },
          select: {
            selector: `td:first-child input[type="checkbox"]`,
            style: "multi"
          },
        } as DataTables.Settings);

        /**
         * Utility to find items in Table View
         */
        let findTableViewUtil = function (config) {
          // Upon clicking the find button, show the find dropdown content
          $(".btn-find").click(function () {
            $(this).parent().find(".find-pf-dropdown-container").toggle();
          });

          // Upon clicking the find close button, hide the find dropdown content
          $(".btn-find-close").click(function () {
            $(".find-pf-dropdown-container").hide();
          });
        };

        // Initialize find util
        new findTableViewUtil(null);

        let dataTable = ($(".datatable") as any).dataTable();
      });
    });
  }

  getDevices() {
    return this.$http.get<ListResult<Device>>("api/devices");
  }

  deleteDevices() {
    let modal = this.$modal.open({
      template: require("../views/delete-devices-modal.html"),
      controller: "DeleteDevicesModalCtrl as vm",
      resolve: {
        id: () => this.devices[0].id
      }
    });
    modal.result.then((result: any) => {
      console.info(result);
    },
    (result) => {
      console.warn(result);
    });
  }
}