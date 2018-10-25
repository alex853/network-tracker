Ext.require([
    'Ext.panel.*',
    'Ext.toolbar.*',
    'Ext.button.*',
    'Ext.grid.*',
    'Ext.data.*',
    'Ext.container.ButtonGroup',
    'Ext.layout.container.Table',
    'Ext.ux.GMapPanel'
]);

var GMapStylesArray = [
    {
        featureType: "all",
        stylers: [
            {saturation: -80}
        ]
    }, {
        featureType: "road.arterial",
        elementType: "geometry",
        stylers: [
            {hue: "#00ffee"},
            {saturation: 50}
        ]
    }, {
        featureType: "poi.business",
        elementType: "labels",
        stylers: [
            {visibility: "off"}
        ]
    }
];

Ext.onReady(function () {
    Ext.create('Ext.container.Viewport', {
        layout: 'fit',
        items: [
            {
                layout: 'border',
                tbar: [
                    {
                        xtype: 'tbtext',
                        text: 'Tracker Map',
                        width: 200
                    },


                    {
                        id: 'vatsim-toggle-button',
                        xtype: 'button',
                        enableToggle: true,
                        pressed: true,
                        text: 'VATSIM',
                        width: 100,
                        handler: Logics.networkToggleButtonHandler
                    },
                    {
                        id: 'vatsim-status-text',
                        xtype: 'tbtext',
                        text: '',
                        width: 300
                    },


                    {
                        id: 'ivao-toggle-button',
                        xtype: 'button',
                        enableToggle: true,
                        pressed: true,
                        text: 'IVAO',
                        width: 100,
                        handler: Logics.networkToggleButtonHandler
                    },
                    {
                        id: 'ivao-status-text',
                        xtype: 'tbtext',
                        text: '',
                        width: 300
                    }
                ],
                items: [
                    {
                        id: 'gmappanel',
                        xtype: 'gmappanel',
                        region: 'center',
                        center: {
                            lat: 0,
                            lng: 0
                        },
                        mapOptions: {
                            zoom: 2,
                            mapTypeId: google.maps.MapTypeId.ROADMAP,
                            styles: GMapStylesArray
                        }
                    }
                ]
            }
        ]
    });



    Ext.create('Ext.tip.ToolTip', {
        target: 'vatsim-status-text',
        listeners: {
            beforeshow: function (tooltip) {
                tooltip.update(tooltip.target.component.tooltip);
            }
        }
    });

    Ext.create('Ext.tip.ToolTip', {
        target: 'ivao-status-text',
        listeners: {
            beforeshow: function (tooltip) {
                tooltip.update(tooltip.target.component.tooltip);
            }
        }
    });



    Logics.startRefreshByTimer();
    Ext.defer(Logics.refreshByTimer, 100);
});

Logics = {
    networks: {
        VATSIM: {
            markers: {}
        },
        IVAO: {
            markers: {}
        }
    },

    startRefreshByTimer: function () {
        Ext.TaskManager.start({
            run: Logics.refreshByTimer,
            interval: 15000
        });
    },

    refreshByTimer: function () {
        if (Ext.getCmp('vatsim-toggle-button').pressed) {
            Logics.refreshByTimerForNetwork('VATSIM');
        }

        if (Ext.getCmp('ivao-toggle-button').pressed) {
            Logics.refreshByTimerForNetwork('IVAO');
        }
    },

    refreshByTimerForNetwork: function (networkName) {
        var network = Logics.networks[networkName];
        var refreshRequired;

        if (network.actualityTs) {
            var now = new Date().getTime();
            var diff = now - network.actualityTs;
            refreshRequired = diff >= 60000;
        } else {
            refreshRequired = true;
        }

        refreshRequired = refreshRequired && !network.requestSent;

        if (refreshRequired) {
            network.requestSent = true;

            Logics.setNetworkStatusText(networkName, 'Loading...', 'Loading...', 'Gray');

            Logics.requestNetworkStatus(networkName);
        }
    },

    requestNetworkStatus: function (networkName) {
        Ext.Ajax.request({
            url: 'rest/service/map/status',
            method: 'GET',
            params: {
                network: networkName
            },
            timeout: 120000,
            success: Logics.requestNetworkStatus_success,
            failure: Logics.requestNetworkStatus_failure
        });
    },

    requestNetworkStatus_success: function (xhr, opts) {
        if (Ext.isEmpty(xhr.responseText)) {
            // it seems server is still starting
            // Loading... status is remained and we will reload status 15 seconds later
            var networkName = opts.params.network;
            var network = Logics.networks[networkName];
            network.requestSent = false;
            return;
        }

        var networkStatus = Ext.decode(xhr.responseText);

        var networkName = networkStatus.network;
        var network = Logics.networks[networkName];

        network.actualityTs = new Date().getTime();
        network.requestSent = false;
        network.networkStatus = networkStatus;


        Logics.setNetworkStatusTextByNetworkName(networkName);


        // Google Map markers update
        // 1. Iterate pilotPositions
        //    a) If there is no marker, then create new
        //    b) If marker exists, then move it and update data
        // 2. Iterate markers for network
        //    a) If there is no pilotPosition for the marker in the report then mark it offline
        //    b) If marker is offline more than 10 mins then remove it from map
        var gmappanel = Ext.getCmp('gmappanel');
        var gmap = gmappanel.gmap;

        Ext.each(networkStatus.pilotPositions, function (pilotPosition) {
            var marker = network.markers['marker_' + pilotPosition.pilotNumber];
            if (!marker) {
                marker = new google.maps.Marker({
                    position: {
                        lat: pilotPosition.latitude,
                        lng: pilotPosition.longitude
                    },
                    map: gmap,
                    title: pilotPosition.callsign + ' (' + networkName + ')',
                    icon: Logics.getIcon('active', pilotPosition.heading),
                    network: networkName,
                    pilotNumber: pilotPosition.pilotNumber
                });
                marker.addListener('click', function () {
                    Logics.showInfoWindow(marker);
                });
                marker.addListener('mouseover', function() {
                    Logics.showSimpleTrackLines(marker);
                });
                marker.addListener('mouseout', function() {
                    Logics.hideSimpleTrackLines(marker);
                });

                network.markers['marker_' + pilotPosition.pilotNumber] = marker;
            } else {
                var markerPosition = marker.getPosition();
                if (Logics.isDiff(markerPosition.lat(), pilotPosition.latitude) || Logics.isDiff(markerPosition.lng(), pilotPosition.longitude)) {
                    marker.setPosition({
                        lat: pilotPosition.latitude,
                        lng: pilotPosition.longitude
                    });
                }

                marker.setIcon(Logics.getIcon('active', pilotPosition.heading));

                if (marker.getMap() == null) {
                    marker.setMap(gmap);
                }

                if (marker.infoWindow && marker.infoWindow.getMap()) {
                    Logics.showInfoWindow(marker); // it updates content of InfoWindow
                }
            }
        });

        Ext.iterate(network.markers, function (key, marker) {
            var pilotPosition = Logics.getPilotPosition(networkName, marker.pilotNumber);

            if (pilotPosition == undefined) {
                marker.setMap(null);
                delete network.markers[key];
            }
        });
    },

    requestNetworkStatus_failure: function (xhr, opts) {
        // network status text is set to 'Refresh failure'
        // modify all icons to grayed&transparent

        var networkName = opts.params.network;

        var network = Logics.networks[networkName];

        network.actualityTs = new Date().getTime();
        network.requestSent = false;
        var networkStatus = network.networkStatus;

        Logics.setNetworkStatusText(networkName, 'Refresh error', 'Refresh error', 'Crimson');


        Ext.iterate(network.markers, function (key, marker) {
            if (marker.getMap() == null) {
                return;
            }

            var pilotPosition = Logics.getPilotPosition(networkName, marker.pilotNumber);

            marker.setIcon(Logics.getIcon('outdated', pilotPosition.heading));
        });
    },

    networkToggleButtonHandler: function (button) {
        // 1. if button is unpressed then remove all markers from the map
        // 2. if button is pressed then show all markers on the map

        var show = button.pressed;

        var networkName = button.getText();

        var network = Logics.networks[networkName];

        var gmappanel = Ext.getCmp('gmappanel');
        var gmap = gmappanel.gmap;

        Ext.iterate(network.markers, function (key, marker) {
            if (show) {
                marker.setMap(gmap);
            } else { // hide
                marker.setMap(null);
            }
        });

        if (show) {
            Logics.setNetworkStatusTextByNetworkName(networkName);
            Logics.refreshByTimerForNetwork(networkName);
        } else {
            Logics.setNetworkStatusText(networkName, '', '', 'Black');
        }
    },

    showInfoWindow: function (marker) {
        var infoWindow = marker.infoWindow;
        if (!infoWindow) {
            infoWindow = new google.maps.InfoWindow();
            marker.infoWindow = infoWindow;
        }

        var networkName = marker.network;
        var pilotNumber = marker.pilotNumber;

        var pilotPosition = Logics.getPilotPosition(networkName, pilotNumber);

        var content = Ext.String.format(
            '<table class="x-toolbar-text-default">' +
            '<tr><td>Callsign&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>{0}</td></tr>' +
            '<tr><td>Network</td><td>{1}</td></tr>' +
            '<tr><td>Altitude</td><td>{2} ft</td></tr>' +
            '<tr><td>G/S</td><td>{3} kts</td></tr>' +
            '<tr><td>Type</td><td>{4}</td></tr>' +
            '<tr><td>Reg #</td><td>{5}</td></tr>' +
            '<tr><td>Origin</td><td>{6}</td></tr>' +
            '<tr><td>Destination&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>{7}</td></tr>',
            pilotPosition.callsign,
            networkName,
            pilotPosition.altitude,
            pilotPosition.groundspeed,
            pilotPosition.type,
            pilotPosition.regNo,
            pilotPosition.fpOrigin,
            pilotPosition.fpDestination
        );
        infoWindow.setContent(content);

        if (!infoWindow.getMap()) {
            infoWindow.open(marker.getMap(), marker);
        }
    },

    showSimpleTrackLines: function (marker) {
        var networkName = marker.network;
        var pilotNumber = marker.pilotNumber;

        var pilotPosition = Logics.getPilotPosition(networkName, pilotNumber);

        // 1. aircraft - actual origin [quite thick orange line]
        // 2. aircraft - planned origin if planned differs from actual [thin red line]
        // 3. aircraft - planned destination [thin partly opaque orange line]

        if (!Ext.isEmpty(pilotPosition.fpOrigin)) {
            // if (!Ext.isEmpty(pilotPosition.currentFlight.origin) && pilotPosition.currentFlight.origin != pilotPosition.fpOrigin) {
            marker.aircraftToPlannedOrigin = new google.maps.Polyline({
                path: [
                    {lat: pilotPosition.latitude, lng: pilotPosition.longitude},
                    {lat: 53, lng: 51}],
                geodesic: true,
                strokeColor: '#FF0000',
                strokeOpacity: 0.5,
                strokeWeight: 0.75
            });
            marker.aircraftToPlannedOrigin.setMap(marker.getMap());
            // }
        }

        marker.aircraftToActualDestination = new google.maps.Polyline({
            path: [
                {lat: pilotPosition.latitude, lng: pilotPosition.longitude},
                {lat: 51, lng: 0}],
            geodesic: true,
            strokeColor: '#FF9900',
            strokeOpacity: 1,
            strokeWeight: 1.5
        });
        marker.aircraftToActualDestination.setMap(marker.getMap());
    },

    hideSimpleTrackLines: function (marker) {
        if (marker.aircraftToPlannedOrigin) {
            marker.aircraftToPlannedOrigin.setMap(null);
            marker.aircraftToPlannedOrigin = null;
        }

        if (marker.aircraftToActualDestination) {
            marker.aircraftToActualDestination.setMap(null);
            marker.aircraftToActualDestination = null;
        }
    },

    getPilotPosition: function (networkName, pilotNumber) {
        var network = Logics.networks[networkName];

        var pilotPosition;
        Ext.each(network.networkStatus.pilotPositions, function (eachPilotPosition) {
            if (pilotNumber == eachPilotPosition.pilotNumber) {
                pilotPosition = eachPilotPosition;
                return false;
            }
        });

        return pilotPosition;
    },

    getIcon: function (status, heading) {
        var stepSize = 10;
        var dir = (heading + stepSize / 2);
        if (dir >= 360) {
            dir -= 360;
        }
        dir = Math.floor(dir / stepSize);
        dir = dir * stepSize;
        return {
            url: 'rest/service/map/icon?image=images/plane_20.png&angle=' + dir + '&status=' + status,
            size: new google.maps.Size(20, 20),
            origin: new google.maps.Point(0, 0),
            anchor: new google.maps.Point(10, 10)
        };
    },

    setNetworkStatusTextByNetworkName: function (networkName) {
        var network = Logics.networks[networkName];
        var networkStatus = network.networkStatus;

        var color;
        if (networkStatus.currentStatusCode == 'OK') {
            color = 'DarkGreen';
        } else if (networkStatus.currentStatusCode == 'GAP') {
            color = 'Chocolate';
        } else {
            color = 'Crimson';
        }

        Logics.setNetworkStatusText(networkName, networkStatus.currentStatusMessage, networkStatus.currentStatusDetails, color);
    },

    setNetworkStatusText: function (networkName, statusText, statusTooltip, statusColor) {
        var networkStatusTextName = networkName.toLowerCase() + '-status-text';
        var text = Ext.getCmp(networkStatusTextName);
        text.setText(statusText);

        text.tooltip = statusTooltip;

        text.el.dom.style.color = statusColor;
    },

    isDiff: function (float1, float2) {
        var diff = float1 - float2;
        if (diff < 0) {
            diff = -diff;
        }
        return diff >= 0.000001;
    }
};
