package flights.model.events;

public class PilotEvent extends BaseEvent {
    protected PilotEvent(int pilotNumber, long reportId, String type) {
        super(pilotNumber, reportId, type);
    }
}
