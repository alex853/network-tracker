Ext.onReady(function () {

    Ext.tip.QuickTipManager.init();

    Ext.create('Ext.container.Viewport', {
        layout: 'fit',
        items: [
            {
                xtype: 'pilot-positions-grid'
            }
        ]
    });

});
