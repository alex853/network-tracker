// icon has following elements of state:
// * network - VATSIM and IVAO have a bit different images, tails are painted in different colors
// * active or outdated - it depends on pilot or network data availability, outdated is shown grayed and partly transparent
// * size has 4 grades - 10, 20, 30, 40 pixels
//   size depends on zoom and 'on ground' state
//   'flying'   : 20 / 0-5, 30 / 6-10, 40 / 11-...
//   'on ground': 10 / 0-5, 20 / 6-10, 40 / 11-...

Ext.define('WebApp.map.Pilot', {
    network: null,
    pilotPosition: null,
    trackData: null,
    map: null,

    airplaneMarker: null,

    plannedOriginMarker: null,
    plannedDestinationMarker: null,
    plannedOriginToAirplaneLine: null,
    plannedDestinationToAirplaneLine: null,
    actualTrackLine: null,

    mouseOverAirplane: false,
    mouseOverDetailsWindow: false,

    detailsWindow: null,

    constructor: function(config) {
        this.network = config.network;
        this.pilotPosition = config.pilotPosition;
        this.map = config.map;
    },

    getPilotNumber: function() {
        return this.pilotPosition.pilotNumber;
    },

    setPilotPosition: function(pilotPosition) {
        this.pilotPosition = pilotPosition;

        // add new pilot position to track data if track is shown
        if (this.trackData) {
            this.trackData.unshift(pilotPosition);
        }
    },

    isDetailsWindowVisible: function () {
        return Ext.isObject(this.detailsWindow) && this.detailsWindow.isVisible();
    },

    refresh: function() {
        var showAirplaneMarker = true;

        if (!this.network.visible) {
            showAirplaneMarker = false;
        }

        var highlighted = this.mouseOverAirplane || this.mouseOverDetailsWindow;
        var showRouteInformation = showAirplaneMarker && highlighted;
        var detailsShown = showAirplaneMarker && this.isDetailsWindowVisible();

        if (showAirplaneMarker) {
            if (!this.airplaneMarker) {
                this.airplaneMarker = new MarkerWithLabel({
                    labelContent: this.pilotPosition.callsign + ' (' + this.network.name + ')',
                    labelVisible: false,
                    labelClass: TrackerStyles.AirplaneMarker.LabelClass
                });

                var that = this;
                this.airplaneMarker.addListener('click', function () {
                    that.onAirplaneMarkerClick();
                });
                this.airplaneMarker.addListener('mouseover', function () {
                    that.onAirplaneMarkerMouseOver();
                });
                this.airplaneMarker.addListener('mouseout', function () {
                    that.onAirplaneMarkerMouseOut();
                });
            }

            var markerPosition = this.airplaneMarker.getPosition();
            if (markerPosition == undefined
                || this.isDiff(markerPosition.lat(), this.pilotPosition.latitude)
                || this.isDiff(markerPosition.lng(), this.pilotPosition.longitude)) {
                this.airplaneMarker.setPosition({
                    lat: this.pilotPosition.latitude,
                    lng: this.pilotPosition.longitude
                });
            }

            var status = 'active';
            if (this.network.outdated) {
                status = 'outdated';
            }

            if (this.isDetailsWindowVisible()) {
                status = 'detailed';
                if (highlighted) {
                    status = 'highlighted';
                }
            }

            // todo AK check if it is changed
            this.airplaneMarker.setIcon(this.getIcon(status, this.pilotPosition.heading));

            if (!this.airplaneMarker.getMap()) {
                this.airplaneMarker.setMap(this.map);
            }
        } else {
            if (this.airplaneMarker && this.airplaneMarker.getMap()) {
                this.airplaneMarker.setMap(null);
            }
        }

        if (showRouteInformation) {
            this.airplaneMarker.labelVisible = true;
            this.airplaneMarker.setVisible(true);
        } else {
            if (this.airplaneMarker) {
                this.airplaneMarker.labelVisible = false;
                this.airplaneMarker.setVisible(true);
            }
        }

        if (showRouteInformation && this.pilotPosition.fpOriginCoords) {
            this.removeMapObject('plannedOriginMarker');
            this.plannedOriginMarker = this.showAirportMarker(this.pilotPosition.fpOrigin, this.pilotPosition.fpOriginCoords);

            this.removeMapObject('plannedOriginToAirplaneLine');
            this.plannedOriginToAirplaneLine = new google.maps.Polyline({
                path: [
                    {lat: this.pilotPosition.fpOriginCoords.lat, lng: this.pilotPosition.fpOriginCoords.lon},
                    {lat: this.pilotPosition.latitude, lng: this.pilotPosition.longitude}
                ],
                geodesic: true,
                strokeColor: TrackerStyles.PlannedOriginToAirlineLine.Color,
                strokeOpacity: TrackerStyles.PlannedOriginToAirlineLine.Opacity,
                strokeWeight: TrackerStyles.PlannedOriginToAirlineLine.Weight,
                map: this.map
            });
        } else {
            this.removeMapObject('plannedOriginMarker');
            this.removeMapObject('plannedOriginToAirplaneLine');
        }

        if (detailsShown) {
            this.removeMapObject('actualTrackLine');
            if (Ext.isDefined(this.trackData)) {
                var path = [];
                Ext.each(this.trackData, function (position) {
                    path.push({lat: position.latitude, lng: position.longitude});
                });

                this.actualTrackLine = new google.maps.Polyline({
                    path: path,
                    geodesic: true,
                    strokeColor: TrackerStyles.ActualTrackLine.Color,
                    strokeOpacity: TrackerStyles.ActualTrackLine.Opacity,
                    strokeWeight: TrackerStyles.ActualTrackLine.Weight,
                    map: this.map
                });
            }
        } else {
            this.removeMapObject('actualTrackLine');
        }

        if (showRouteInformation && this.pilotPosition.fpDestinationCoords) {
            this.removeMapObject('plannedDestinationMarker');
            this.plannedDestinationMarker = this.showAirportMarker(this.pilotPosition.fpDestination, this.pilotPosition.fpDestinationCoords);

            this.removeMapObject('plannedDestinationToAirplaneLine');
            this.plannedDestinationToAirplaneLine = new google.maps.Polyline({
                path: [
                    {lat: this.pilotPosition.fpDestinationCoords.lat, lng: this.pilotPosition.fpDestinationCoords.lon},
                    {lat: this.pilotPosition.latitude, lng: this.pilotPosition.longitude}
                ],
                geodesic: true,
                strokeColor: TrackerStyles.PlannedDestinationToAirplaneLine.Color,
                strokeOpacity: TrackerStyles.PlannedDestinationToAirplaneLine.Opacity,
                strokeWeight: TrackerStyles.PlannedDestinationToAirplaneLine.Weight,
                icons: [{
                    icon: {
                        path: 'M 0,-1 0,1',
                        strokeOpacity: TrackerStyles.PlannedDestinationToAirplaneLine.IconOpacity,
                        strokeWeight: TrackerStyles.PlannedDestinationToAirplaneLine.IconWeight,
                        scale: TrackerStyles.PlannedDestinationToAirplaneLine.IconScale
                    },
                    offset: '50%',
                    repeat: '15px'
                }],
                map: this.map
            });
        } else {
            this.removeMapObject('plannedDestinationMarker');
            this.removeMapObject('plannedDestinationToAirplaneLine');
        }
    },

    remove: function() {
        this.removeMapObject('airplaneMarker');

        this.removeMapObject('plannedOriginMarker');
        this.removeMapObject('plannedOriginToAirplaneLine');
        this.removeMapObject('plannedDestinationMarker');
        this.removeMapObject('plannedDestinationToAirplaneLine');

        this.removeMapObject('actualTrackLine');
    },

    removeMapObject: function(mapObjectName) {
        var mapObject = this[mapObjectName];
        if (mapObject) {
            mapObject.setMap(null);
            delete this[mapObjectName];
            this[mapObjectName] = null;
        }
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

    onAirplaneMarkerClick: function() {
        WebApp.detailsSidebarController.showPilotDetails(this);
        this.detailsWindow.highlightFrame(true);
        this.refresh();

        var me = this;

        // send track data request if it has not been yet loaded
        if (me.trackData) {
            return;
        }

        Ext.Ajax.request({
            url: 'rest/service/map/track',
            method: 'GET',
            params: {
                network: me.network.name,
                pilotNumber: me.getPilotNumber()
            },
            timeout: 120000,
            success: function (xhr, opts) {
                //noinspection UnnecessaryLocalVariableJS
                var trackData = Ext.decode(xhr.responseText);
                me.trackData = trackData;

                // todo AK process a case when map shows outdated position comparing with track data (replace pilotPosition and simulate network status reloading for one pilot?)

                Ext.defer(me.refresh, 50, me);
            },
            failure: function () {
                // nothing to do except variable clean-up
                me.trackData = null;
            }
        });

    },

    showAirportMarker: function (text, coords) {
        return new MarkerWithLabel({
            position: { lat: coords.lat, lng: coords.lon },
            icon: {
                strokeColor: TrackerStyles.AirportMarker.Color,
                path: google.maps.SymbolPath.CIRCLE,
                scale: 2
            },
            map: this.map,
            labelContent: text,
            labelClass: TrackerStyles.AirportMarker.LabelClass
        });
    },

    onAirplaneMarkerMouseOver: function() {
        if (!this.mouseOverAirplane) {
            this.mouseOverAirplane = true;
            this.refresh();

            if (this.isDetailsWindowVisible()) {
                this.detailsWindow.highlightFrame(true);
            }
        }
    },

    onAirplaneMarkerMouseOut: function() {
        this.mouseOverAirplane = false;
        this.refresh();

        if (this.isDetailsWindowVisible()) {
            this.detailsWindow.highlightFrame(false);
        }
    },

    onDetailsWindowClose: function() {
        this.trackData = null; // it forces track reload in case of next marker click
        this.mouseOverDetailsWindow = false;
        this.refresh();
    },

    onDetailsWindowMouseEnter: function() {
        this.detailsWindow.highlightFrame(true);
        this.mouseOverDetailsWindow = true;
        this.refresh();
    },

    onDetailsWindowMouseLeave: function() {
        this.detailsWindow.highlightFrame(false);
        this.mouseOverDetailsWindow = false;
        this.refresh();
    },

    isDiff: function (float1, float2) {
        var diff = float1 - float2;
        if (diff < 0) {
            diff = -diff;
        }
        return diff >= 0.000001;
    }
});
