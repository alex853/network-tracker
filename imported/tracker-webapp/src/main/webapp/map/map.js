
Logics = {
    networks: {
        VATSIM: {
            name: 'VATSIM',
            pilots: {},
            visible: true
        },
        IVAO: {
            name: 'IVAO',
            pilots: {},
            visible: true
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
        network.outdated = false;


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

        var newPilots = {};
        Ext.each(networkStatus.pilotPositions, function (pilotPosition) {
            var pilot = network.pilots['id' + pilotPosition.pilotNumber];
            if (!pilot) {
                pilot = new WebApp.map.Pilot({
                    network: network,
                    pilotPosition: pilotPosition,
                    map: gmap
                });
            }
            pilot.setPilotPosition(pilotPosition);
            newPilots['id' + pilotPosition.pilotNumber] = pilot;
            
            pilot.refresh();
        });

        Ext.iterate(network.pilots, function (key, pilot) {
            var newPilot = newPilots['id' + pilot.getPilotNumber()];

            if (newPilot == undefined) {
                pilot.remove();
            }
        });
        
        network.pilots = newPilots;
    },

    requestNetworkStatus_failure: function (xhr, opts) {
        // network status text is set to 'Refresh failure'
        // modify all icons to grayed&transparent

        var networkName = opts.params.network;

        var network = Logics.networks[networkName];

        network.actualityTs = new Date().getTime();
        network.requestSent = false;

        Logics.setNetworkStatusText(networkName, 'Refresh error', 'Refresh error', 'Crimson');

        network.outdated = true;
        
        Logics.refreshPilots(network);
    },

    networkToggleButtonHandler: function (button) {
        // 1. if button is unpressed then remove all markers from the map
        // 2. if button is pressed then show all markers on the map

        var show = button.pressed;

        var networkName = button.getText();

        var network = Logics.networks[networkName];

        var gmappanel = Ext.getCmp('gmappanel');

        network.visible = show;
        Logics.refreshPilots(network);

        if (show) {
            Logics.setNetworkStatusTextByNetworkName(networkName);
            Logics.refreshByTimerForNetwork(networkName);
        } else {
            Logics.setNetworkStatusText(networkName, '', '', 'Black');
        }
    },

    refreshPilots: function(network) {
        Ext.iterate(network.pilots, function (key, pilot) {
            pilot.refresh();
        });
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
    }
};
