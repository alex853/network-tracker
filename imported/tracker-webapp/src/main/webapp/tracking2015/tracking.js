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

Ext.onReady(function () {
    Ext.create('Ext.container.Viewport', {
        layout: 'fit',
        items: [
            {
                layout: 'border',
                tbar: [
                    {
                        id: 'mode-selector',
                        xtype: 'splitbutton',
                        text: 'Snapshot',
                        menu: [
                            {
                                text: 'Snapshot',
                                handler: function() {
                                    Logics.switchMode('Snapshot');
                                }
                            },
                            {
                                text: 'Database',
                                handler: function() {
                                    Logics.switchMode('Database');
                                }
                            }]
                    },
                    '-',


                    {
                        id: 'snapshot-filename',
                        xtype: 'combo',
                        width: 400,
                        store: Ext.create('Ext.data.Store', {
                            autoLoad: true,
                            fields: ['filename'],
                            proxy: {
                                type: 'rest',
                                url: '../rest/tracking2015/snapshot/list'
                            }
                        }),
                        displayField: 'filename',
                        listeners: {
                            select: Logics.refreshInSnapshotMode
                        }
                    },
                    {
                        id: 'snapshot-upload',
                        text: 'Upload',
                        handler: function () {
                            alert('not implemented');
                        }
                    },


                    {
                        id: 'database-text-network',
                        xtype: 'tbtext',
                        text: 'Network'
                    },
                    {
                        id: 'database-network',
                        xtype: 'combo',
                        width: 70,
                        store: Ext.create('Ext.data.Store', {
                            autoLoad: true,
                            fields: ['network'],
                            data: [
                                { network: 'VATSIM' },
                                { network: 'IVAO' }
                            ]
                        }),
                        displayField: 'network',
                        valueField: 'network',
                        value: 'VATSIM',
                        listeners: {
                            select: Logics.refreshInDatabaseMode
                        }
                    },
                    {
                        id: 'database-text-pilot',
                        xtype: 'tbtext',
                        text: 'Pilot #'
                    },
                    {
                        id: 'database-pilot',
                        xtype: 'textfield',
                        width: 70,
                        listeners: {
                            change: Logics.refreshInDatabaseMode
                        }
                    },
                    {
                        id: 'database-text-date',
                        xtype: 'tbtext',
                        text: 'Date'
                    },
                    {
                        id: 'database-date',
                        xtype: 'datefield',
                        value: new Date(),
                        width: 100,
                        listeners: {
                            select: Logics.refreshInDatabaseMode
                        }
                    },
                    {
                        id: 'database-text-time',
                        xtype: 'tbtext',
                        text: 'Time'
                    },
                    {
                        id: 'database-time',
                        xtype: 'timefield',
                        increment: 30,
                        format: 'H:i',
                        value: '00:00',
                        width: 70,
                        listeners: {
                            select: Logics.refreshInDatabaseMode
                        }
                    },
                    {
                        id: 'database-text-reports',
                        xtype: 'tbtext',
                        text: 'Reports'
                    },
                    {
                        id: 'database-reports',
                        xtype: 'combo',
                        width: 70,
                        store: Ext.create('Ext.data.Store', {
                            autoLoad: true,
                            fields: ['count'],
                            data: [
                                { count: 10 },
                                { count: 25 },
                                { count: 50 },
                                { count: 100 },
                                { count: 250 },
                                { count: 500 },
                                { count: 1000 },
                                { count: 2500 }
                            ]
                        }),
                        displayField: 'count',
                        valueField: 'count',
                        value: 10,
                        listeners: {
                            select: Logics.refreshInDatabaseMode
                        }
                    },
                    {
                        id: 'database-refresh',
                        text: 'Refresh',
                        listeners: {
                            click: Logics.refreshInDatabaseMode
                        }
                    },
                    {
                        id: 'database-auto-refresh',
                        text: 'Auto-refresh'
                    },
                    {
                        id: 'database-make-snapshot',
                        text: 'Make snapshot',
                        listeners: {
                            click: Logics.makeSnapshot
                        }
                    }
                ],
                items: [
                    {
                        layout: 'border',
                        region: 'center',
                        flex: 3,
                        items: [
                            {
                                id: 'data-grid',
                                xtype: 'grid',
                                region: 'center',
                                flex: 5,
                                columns: {
                                    defaults: {
                                        menuDisabled: true
                                    },
                                    items: [
                                        {text: 'R.ID', dataIndex: 'reportId', width: 60 },
                                        {text: 'R.Date', dataIndex: 'reportDate', width: 60 },
                                        {text: 'R.Time', dataIndex: 'reportTime', width: 60 },
                                        {text: 'R.Alt', dataIndex: 'reportAltitude', width: 50 },
                                        {text: 'P.Status', dataIndex: 'positionStatus', width: 70 },
                                        //{ text: 'P.GS' },
                                        {text: 'P.FP.Aircraft', dataIndex: 'positionFpAircraft', width: 60 },
                                        {text: 'P.FP.Origin', dataIndex: 'positionFpOrigin', width: 50 },
                                        {text: 'P.FP.Dest', dataIndex: 'positionFpDestination', width: 50 },
                                        {text: 'PS.Events', dataIndex: 'psEvents', width: 150 }
                                    ]
                                },
                                features: [{
                                    ftype: 'grouping',
                                    groupHeaderTpl: '{name}',
                                    hideGroupedHeader: true,
                                    enableGroupingMenu: false
                                }],
                                store: Ext.create('Ext.data.Store', {
                                    autoLoad: false,
                                    fields: [
                                        'reportId',
                                        'reportDate',
                                        'reportTime',
                                        'reportLatitude',
                                        'reportLongitude',
                                        'reportAltitude',
                                        'positionStatus',
                                        'positionFpAircraft',
                                        'positionFpOrigin',
                                        'positionFpDestination',
                                        'psEvents',
                                        'movements'
                                    ],
                                    groupField: 'reportDate',
                                    proxy: {
                                        type: 'rest',
                                        //url: '../rest/tracking2015/tracking/data',
                                        actionMethods: {read: "POST"},
                                        timeout: 300000
                                    },
                                    listeners: {
                                        load: function (store, records, options) {
                                            var gmappanel = Ext.getCmp('gmappanel');
                                            var gmap = gmappanel.gmap;

                                            var myMarkers = gmap.myMarkers;
                                            if (myMarkers) {
                                                Ext.each(myMarkers, function (marker) {
                                                    marker.setMap(null);
                                                });
                                            }
                                            gmap.myMarkers = [];

                                            Ext.each(records, function (record) {
                                                if (!record.get('reportLatitude') || !record.get('reportLongitude')) {
                                                    return;
                                                }

                                                var marker = new google.maps.Marker({
                                                    position: { lat: record.get('reportLatitude'), lng: record.get('reportLongitude') },
                                                    map: gmap,
                                                    title: 'R' + record.get('reportId') + ' FL?' + record.get('reportAltitude') + ' GS?',
                                                    icon: {
                                                        path: google.maps.SymbolPath.CIRCLE,
                                                        scale: 3
                                                    }
                                                });
                                                gmap.myMarkers.push(marker);
                                            });
                                        }
                                    }
                                }),
                                listeners: {
                                    select: function (grid, record, index, eOpts) {
                                        var movementsGrid = Ext.getCmp('movements-grid');
                                        var movements = record.get('movements');
                                        movementsGrid.getStore().loadData(movements);


                                        if (record.get('reportLatitude') && record.get('reportLongitude')) {
                                            var gmappanel = Ext.getCmp('gmappanel');
                                            var gmap = gmappanel.gmap;
                                            gmap.setCenter({ lat: record.get('reportLatitude'), lng: record.get('reportLongitude') });
                                            if (gmap.getZoom() < 7) {
                                                gmap.setZoom(7);
                                            }
                                        }
                                    }
                                }
                            }, {
                                id: 'gmappanel',
                                xtype: 'gmappanel',
                                region: 'east',
                                flex: 3,
                                center: {
                                    lat: 0,
                                    lng: 0
                                },
                                mapOptions: {
                                    zoom: 1,
                                    mapTypeId: google.maps.MapTypeId.ROADMAP
                                }
                            }
                        ]
                    }, {
                        id: 'movements-grid',
                        xtype: 'grid',
                        region: 'south',
                        flex: 1,
                        columns: [
                            {text: 'M.Status', dataIndex: 'status'},
                            {text: 'M.FirstSeen', dataIndex: 'firstSeen'},
                            {text: 'M.Origin', dataIndex: 'origin'},
                            {text: 'M.Destination', dataIndex: 'destination'},
                            {text: 'M.LastSeen', dataIndex: 'lastSeen'},
                            {text: 'M.FP.Aircraft', dataIndex: 'fpAircraft'},
                            {text: 'M.FP.Origin', dataIndex: 'fpOrigin'},
                            {text: 'M.FP.Dest', dataIndex: 'fpDestination'}
                        ]
                    }
                ]
            }
        ]
    });

    Logics.switchMode('Database');
});

Logics = {
    switchMode: function(mode) {
        Ext.getCmp('mode-selector').setText(mode);

        var visible = mode == 'Snapshot';
        Ext.getCmp('snapshot-filename').setVisible(visible);
        Ext.getCmp('snapshot-upload').setVisible(visible);

        visible = mode == 'Database';
        Ext.getCmp('database-text-network').setVisible(visible);
        Ext.getCmp('database-network').setVisible(visible);
        Ext.getCmp('database-text-pilot').setVisible(visible);
        Ext.getCmp('database-pilot').setVisible(visible);
        Ext.getCmp('database-text-date').setVisible(visible);
        Ext.getCmp('database-date').setVisible(visible);
        Ext.getCmp('database-text-time').setVisible(visible);
        Ext.getCmp('database-time').setVisible(visible);
        Ext.getCmp('database-text-reports').setVisible(visible);
        Ext.getCmp('database-reports').setVisible(visible);
        Ext.getCmp('database-refresh').setVisible(visible);
        Ext.getCmp('database-auto-refresh').setVisible(visible);
        Ext.getCmp('database-make-snapshot').setVisible(visible);
    },

    refreshInSnapshotMode: function(combo, record, eOpts) {
        var dataGrid = Ext.getCmp('data-grid');
        dataGrid.getStore().load({
            url: '../rest/tracking2015/tracking/data/snapshot',
            params: {
                snapshotName: record.get('filename')
            }
        });
    },

    refreshInDatabaseMode: function() {
        var params = Logics.getDatabaseModeParams();

        var dataGrid = Ext.getCmp('data-grid');
        dataGrid.getStore().load({
            url: '../rest/tracking2015/tracking/data/database',
            params: params
        });
    },

    makeSnapshot: function() {
        var params = Logics.getDatabaseModeParams();

        window.location.href = '../rest/tracking2015/tracking/data/makeSnapshot?' + Ext.Object.toQueryString(params);
    },

    getDatabaseModeParams: function () {
        var network = Ext.getCmp('database-network').getValue();
        var pilot = Ext.getCmp('database-pilot').getValue();

        var date = Ext.getCmp('database-date').getValue();
        var time = Ext.getCmp('database-time').getValue();
        var fromReportDt = Ext.Date.format(date, "Ymd") + Ext.Date.format(time, "His");

        var reports = Ext.getCmp('database-reports').getValue();

        return {
            network: network,
            pilot: pilot,
            fromReportDt: fromReportDt,
            reports: reports
        };
    }
};
