package com.kinetic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.kinetic.model.Activity;
import com.kinetic.model.ActivityFactory;
import com.kinetic.model.ActivityType;
import com.kinetic.model.DistanceActivity;
import com.kinetic.model.GoalMetric;
import com.kinetic.model.GoalProgress;
import com.kinetic.model.Goal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ActivityPolymorphismTest {
    private static Activity make(ActivityType t, int min, double km, int intensity) {
        return ActivityFactory.create(t, 1L, 1, LocalDate.of(2026, 1, 5), min, km, intensity, 0, null);
    }

    @Test
    public void factoryBuildsTheRightSubclass() {
        for (ActivityType t : ActivityType.values()) {
            assertEquals(t, make(t, 30, 3, 3).type());
        }
        assertTrue(make(ActivityType.RUN, 30, 3, 3) instanceof DistanceActivity);
        assertTrue(!(make(ActivityType.YOGA, 30, 0, 3) instanceof DistanceActivity));
    }

    @Test
    public void runCaloriesFollowSpeed() {
        // 10 km in 60 min = 10 km/h = 10 MET; 70 kg for 1 h = 700 kcal
        assertEquals(700, make(ActivityType.RUN, 60, 10, 3).estimateCalories(70));
    }

    @Test
    public void sameFormulaDifferentMetPerType() {
        int run = make(ActivityType.HIIT, 30, 0, 4).estimateCalories(70);
        int yoga = make(ActivityType.YOGA, 30, 0, 4).estimateCalories(70);
        assertTrue("HIIT burns more than yoga for the same time and effort", run > yoga);
    }

    @Test
    public void summariesAndPace() {
        assertEquals("10.0 km, 6:00 /km", make(ActivityType.RUN, 60, 10, 3).summary());
        assertEquals("45 min", make(ActivityType.STRENGTH, 45, 0, 3).summary());
    }

    @Test
    public void naturalOrderingIsByDate() {
        List<Activity> list = new ArrayList<>();
        list.add(ActivityFactory.create(ActivityType.RUN, 2L, 1, LocalDate.of(2026, 3, 1), 30, 0, 3, 0, null));
        list.add(ActivityFactory.create(ActivityType.RUN, 1L, 1, LocalDate.of(2026, 1, 1), 30, 0, 3, 0, null));
        Collections.sort(list);
        assertEquals(LocalDate.of(2026, 1, 1), list.get(0).date());
    }

    @Test
    public void goalMetricsMeasureAnyTrackable() {
        Activity a = make(ActivityType.RUN, 60, 10, 3);
        a.applyCalories(70);
        List<Activity> items = List.of(a, make(ActivityType.WALK, 30, 2, 2));
        assertEquals(2, GoalMetric.WORKOUTS.measure(items), 0.001);
        assertEquals(90, GoalMetric.MINUTES.measure(items), 0.001);
        assertEquals(12, GoalMetric.DISTANCE.measure(items), 0.001);
        GoalProgress gp = new GoalProgress(new Goal(null, 1, GoalMetric.MINUTES, 180), 90);
        assertEquals(50, gp.percent());
    }
}
