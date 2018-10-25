
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
                            lat: 20,
                            lng: 0
                        },
                        mapOptions: {
                            zoom: 3,
                            mapTypeId: google.maps.MapTypeId.ROADMAP,
                            styles: GMapStylesArray
                        }
                    },
                    {
                        id: 'detailsSidebar',
                        title: 'Details',
                        region: 'east',
                        width: 300,
                        // split: true,
                        collapsible: true,
                        autoScroll: true,
                        layout: {
                            type: 'vbox',
                            padding: '5'
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


    WebApp.detailsSidebarController = Ext.create('WebApp.map.DetailsSidebarController');

    Logics.startRefreshByTimer();
    Ext.defer(Logics.refreshByTimer, 100);
    
});
