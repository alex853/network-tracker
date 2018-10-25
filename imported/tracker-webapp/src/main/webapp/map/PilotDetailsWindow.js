Ext.define('WebApp.map.PilotDetailsWindow', {
    // extend: 'Ext.window.Window',
    extend: 'Ext.panel.Panel',

    title: '-=callsign=-',
    header: false,
    headerPosition: 'left',
    frame: true,
    closable: true,
    width: '100%',
    height: 115,
    resizable: false,
    layout: {
        type: 'fit',
        padding: 5
    },
    margin: '0 0 5 0',

    pilot: null,

    listeners: {
        el: {
            mouseenter: function (event, html, eOpts) {
                event.stopEvent();

                this.component.onMouseEnter();
            },
            mouseleave: function (event, html, eOpts) {
                event.stopEvent();

                this.component.onMouseLeave();
            }
        }
    },

    items: [{
        xtype: 'tabpanel',
        tabBar: {
            items: [{
                xtype: 'tbfill'
            }, {
                xtype: 'tool',
                type: 'close',
                handler: function (btn, e) {
                    WebApp.detailsSidebarController.closePilotDetails(this.up('panel').up('panel'));
                }
            }]
        },
        items: [{
            itemId: 'generalInfoTab',
            icon: null,
            title: '-=callsign=-',
            layout: {
                type: 'table',
                columns: 4
            },
            width: '100%',
            defaults: {
                bodyStyle: 'padding: 5px',
                width: 60
            },
            items: [{ // row 1
                itemId: 'networkCell',
                html: '-=network=-'
            }, {
                itemId: 'pilotCell',
                html: '-=pilot=-'
            }, {
                itemId: 'statusCell',
                html: '-=status=-',
                colspan: 2
            }, { // row 2
                itemId: 'fromCell',
                html: '-=f/p from=-'
            }, {
                itemId: 'toCell',
                html: '-=f/p to=-'
            }, {
                html: 'Alt'
            }, {
                itemId: 'altCell',
                html: '-=alt=-'
            }, { // row 3
                itemId: 'typeCell',
                html: '-=type=-'
            }, {
                itemId: 'tailCell',
                html: '-=tail=-'
            }, {
                html: 'G/S'
            }, {
                itemId: 'gsCell',
                html: '-=g/s=-'
            }]
        }, {
            title: 'Pilot',
            html: 'Hello world 2'
        }, {
            title: 'Airframe',
            html: 'Hello world 3'
        }]
    }],

    constructor: function (config) {
        this.callParent(arguments);

        this.pilot = config.pilot;
    },

    refreshFields: function () {
        this.setTitle(this.pilot.pilotPosition.callsign);

        // todo AK tooltips with explanations
        var generalInfoTab = this.down('#generalInfoTab');
        generalInfoTab.setIcon('images/icon-' + this.pilot.network.name.toLowerCase() + '.png');
        generalInfoTab.setTitle(this.pilot.pilotPosition.callsign);
        generalInfoTab.down('#networkCell').setHtml(this.pilot.network.name);
        generalInfoTab.down('#pilotCell').setHtml(this.pilot.pilotPosition.pilotNumber);
        generalInfoTab.down('#statusCell').setHtml(this.pilot.pilotPosition.status);
        generalInfoTab.down('#fromCell').setHtml(this.pilot.pilotPosition.fpOrigin);
        generalInfoTab.down('#toCell').setHtml(this.pilot.pilotPosition.fpDestination);
        generalInfoTab.down('#altCell').setHtml(this.pilot.pilotPosition.altitude); // todo AK FL?
        generalInfoTab.down('#typeCell').setHtml(this.pilot.pilotPosition.type);
        generalInfoTab.down('#tailCell').setHtml(this.pilot.pilotPosition.regNo);
        generalInfoTab.down('#gsCell').setHtml(this.pilot.pilotPosition.groundspeed);
    },

    onMouseEnter: function() {
        Ext.defer(this.pilot.onDetailsWindowMouseEnter, 50, this.pilot);
    },

    onMouseLeave: function() {
        Ext.defer(this.pilot.onDetailsWindowMouseLeave, 50, this.pilot);
    },

    onDestroy: function () {
        Ext.defer(this.pilot.onDetailsWindowClose, 50, this.pilot);
    },

    highlightFrame: function (highlight) {
        this.getEl().dom.style.border = highlight ? 'solid 1px red' : '';
    }
});
