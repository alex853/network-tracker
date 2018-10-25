Ext.define('PilotPositions.GridController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.pilot-positions-grid-controller',

    onPrevDateHandler: function () {
        var dateField = this.getView().down('#dateField');
        var date = new Date(dateField.getValue());
        date.setDate(date.getDate() - 1);
        dateField.setValue(date);
        // dateField.fireEvent('select', dateField.getValue());
        this.loadData();
    },

    onNextDateHandler: function () {
        var dateField = this.getView().down('#dateField');
        var date = new Date(dateField.getValue());
        date.setDate(date.getDate() + 1);
        dateField.setValue(date);
        // dateField.fireEvent('select', dateField.getValue());
        this.loadData();
    },

    loadData: function () {
        var grid = this.getView();
        grid.store.load({
            params: {
                pilotNumber: grid.down('#pilotNumberField').getValue(),
                date: Ext.Date.format(grid.down('#dateField').getValue(), "d/m/Y")
            }
        });
    },
    
    altitudeCellRenderer: function (value, meta, record) {
        var onGround = record.get('pgnd');
        var altitude = value;

        var result = value;

        if (onGround) {
            result = 'On ground';
            meta.tdAttr = "data-qtip='Altitude is " + altitude + " feet'";
        } else {
            var fl = record.get('pfl');
            var actualAltitude = record.get('paa');
            if (fl) {
                result = fl;
                meta.tdAttr = "data-qtip='" + altitude + " / " + actualAltitude + " feet'";
            } else {
                result = altitude;
            }
        }

        return this.cellRenderer(result, meta, record);
    },

    cellRenderer: function (value, meta, record) {
        var offline = record.get('rst') == 0;
        if (offline) {
            meta.style = "background-color: #E0E0E0;";
        }
        return value;
    },

    flightStatusRenderer: function (value, meta, record) {
        var statuses = ['Departure', 'Preparing', 'Departing', 'Flying', 'Lost in flight', 'Arrival', 'Arriving', 'Arrived', 'Finished', 'Terminated'];
        var result = value != -1 ? statuses[value] : '';
        return this.flightCellRenderer(result, meta, record);
    },

    flightCellRenderer: function (value, meta, record) {
        var status = record.get('fst');

        switch (status) {
            case -1: // no flight
                meta.style = "background-color: #E0E0E0;"; // gray
                break;
            case 9: // terminated
                meta.style = "background-color: #FFE0E0;"; // red
                break;
            case 8: // finished
                meta.style = "background-color: #80FF80;"; // green
                break;
            case 4: // lost
                meta.style = "background-color: #FFE080;"; // orange
                break;
            case 3: // flying
                meta.style = "background-color: #E0FFFF;"; // light-light blue
                break;
            case 0: // departure
            case 1:
            case 2:
                meta.style = "background-color: #E0FFE0;"; // light-light green
                break;
            case 5: // departure
            case 6:
            case 7:
                meta.style = "background-color: #C0FFC0;"; // light green
                break;
        }

        return value;
    }

});
