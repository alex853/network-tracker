Ext.define('PilotPositions.Store', {
    extend: 'Ext.data.Store',
    alias: 'store.pilot-positions-store',
    model: 'PilotPosition',

    autoLoad: false,
    pageSize: null,

    proxy: {
        type: 'ajax',
        url: '../../../rest/debug/pilotPositions',
        actionMethods: {read: "POST"},
        timeout: 120000,

        reader: {
            type: 'json',
            rootProperty: 'data'
        }
    }
});