Ext.define('WebApp.map.DetailsSidebarController', {
    showPilotDetails: function (pilot) {
        if (!pilot.detailsWindow || pilot.detailsWindow.destroyed) {
            pilot.detailsWindow = new WebApp.map.PilotDetailsWindow({
                pilot: pilot
            });
            Ext.getCmp('detailsSidebar').insert(0, pilot.detailsWindow);
        }
        pilot.detailsWindow.refreshFields();
    },

    closePilotDetails: function (panel) {
        Ext.getCmp('detailsSidebar').remove(panel);
        panel.close();
    }

});
