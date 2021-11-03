package db.model;

import lombok.Data;

@Data
public class Skier {
    private final int skierId, resortId, seasonId, dayId, time, liftId;
}
