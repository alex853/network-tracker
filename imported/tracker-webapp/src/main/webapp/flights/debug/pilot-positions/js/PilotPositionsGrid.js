Ext.define('PilotPositions.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.pilot-positions-grid',

    controller: 'pilot-positions-grid-controller',
    store: {
        type: 'pilot-positions-store'
    },

    title: 'Positions',

    viewConfig: {
        stripeRows: true
    },

    sortableColumns: false,
    enableColumnHide: false,

    columns: {
        defaults: {
            menuDisabled: true
        },
        items: [
            {
                text: 'Time',
                dataIndex: 'rtime',
                width: 100
            }, {
                header: 'Position',
                columns: [
                    {
                        text: 'Altitude',
                        dataIndex: 'ralt',
                        width: 80,
                        renderer: 'altitudeCellRenderer'
                    }, {
                        text: 'Location',
                        dataIndex: 'pport',
                        width: 80,
                        renderer: 'cellRenderer'
                    }
                ]
            }, {
                header: 'Flightplan',
                columns: [
                    {
                        text: 'Type',
                        dataIndex: 'fpAcft',
                        width: 80,
                        renderer: 'cellRenderer'
                    }, {
                        text: 'Origin',
                        dataIndex: 'fpOri',
                        width: 80,
                        renderer: 'cellRenderer'
                    }, {
                        text: 'Destination',
                        dataIndex: 'fpDest',
                        width: 80,
                        renderer: 'cellRenderer'
                    }
                ]
            }, {
                header: 'Flight',
                columns: [
                    {
                        text: 'Status',
                        dataIndex: 'fst',
                        width: 100,
                        renderer: 'flightStatusRenderer'
                    }, {
                        text: 'First Seen',
                        dataIndex: 'ffs',
                        width: 120,
                        renderer: 'flightCellRenderer'
                    }, {
                        text: 'Origin',
                        dataIndex: 'forig',
                        width: 120,
                        renderer: 'flightCellRenderer'
                    }, {
                        text: 'Destination',
                        dataIndex: 'fdest',
                        width: 120,
                        renderer: 'flightCellRenderer'
                    }, {
                        text: 'Last Seen',
                        dataIndex: 'fls',
                        width: 120,
                        renderer: 'flightCellRenderer'
                    }, {
                        text: 'Time',
                        dataIndex: 'ftime',
                        width: 80,
                        renderer: 'flightCellRenderer'
                    }, {
                        text: 'Distance',
                        dataIndex: 'fdist',
                        width: 80,
                        renderer: 'flightCellRenderer'
                    }
                ]
            }
        ]
    },

    tbar: [
        {
            xtype: 'tbtext',
            text: 'Network'
        }, {
            xtype: 'combo',
            width: 100,
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
                // select: Logics.refreshInDatabaseMode
            }
        }, '-', {
            xtype: 'tbtext',
            text: 'Pilot #'
        }, {
            xtype: 'textfield',
            itemId: 'pilotNumberField',
            value: '1274811',
            width: 100,
            listeners: {
                change: 'loadData'
            }
        }, '-', {
            text: '<<',
            handler: 'onPrevDateHandler'
        }, {
            xtype: 'datefield',
            itemId: 'dateField',
            value: new Date(),
            format: 'd/m/Y',
            listeners: {
                select: 'loadData'
            }
        }, {
            text: '>>',
            handler: 'onNextDateHandler'
        }
    ]
});
