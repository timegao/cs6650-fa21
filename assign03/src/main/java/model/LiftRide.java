package model;

import lombok.Data;

@Data
public class LiftRide {
    private final int skierId, resortId, seasonId, dayId, time, liftId;
}
