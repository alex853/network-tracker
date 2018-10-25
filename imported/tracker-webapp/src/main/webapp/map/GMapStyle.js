
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

var TrackerStyles = {
    AirportMarker: {
        Color: '#000000',
        LabelClass: undefined
    },

    PlannedOriginToAirlineLine: {
        Color: '#FF0000',
        Opacity: 0.5,
        Weight: 1
    },

    PlannedDestinationToAirplaneLine: {
        Color: '#FF6000',
        Opacity: 0,
        Weight: 1,

        IconOpacity: 0.75,
        IconWeight: 1,
        IconScale: 3
    },

    ActualTrackLine: {
        Color: '#FFA020',
        Opacity: 1,
        Weight: 1.5
    }
};
