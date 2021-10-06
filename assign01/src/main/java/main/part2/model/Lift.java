package main.part2.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

/**
 * Stores information for each lift
 */
@Data
@Setter(AccessLevel.NONE)
public final class Lift {
    private final int startIDNumber, endIDNumber, startTime, endTime;
}
