package com.kinetic.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.kinetic.AppContext;
import com.kinetic.TestSupport;
import com.kinetic.exception.NotFoundException;
import com.kinetic.exception.ValidationException;
import com.kinetic.model.Activity;
import com.kinetic.model.ActivityType;
import com.kinetic.model.DailyLog;
import com.kinetic.model.DailyTargets;
import com.kinetic.model.User;
import java.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Daily health logs, weight history and workout editing. */
public class HealthServiceTest {
    private AppContext app;
    private User user;
    private final String today = LocalDate.now().toString();

    @Before
    public void setUp() throws Exception {
        app = TestSupport.newApp();
        user = app.auth.register("Sam Doe", "sam@example.com", "Passw0rd1");
    }

    @After
    public void tearDown() {
        app.close();
    }

    @Test
    public void dailyLogUpsertsAndBlankFieldsKeepOldValues() throws Exception {
        app.health.saveDaily(user.getId(), today, "5000", "1000", "7.5");
        DailyLog second = app.health.saveDaily(user.getId(), today, "9000", "", "");
        assertEquals(9000, second.steps());
        assertEquals(1000, second.waterMl());
        assertEquals(450, second.sleepMin());
        assertEquals(1, app.dailyDao.findAll().size());
    }

    @Test
    public void quickWaterAddsAndCaps() throws Exception {
        app.health.addWater(user.getId(), "500");
        assertEquals(1000, app.health.addWater(user.getId(), "500").waterMl());
        assertThrows(ValidationException.class, () -> app.health.addWater(user.getId(), "5000"));
    }

    @Test
    public void invalidDailyValuesAreRejected() {
        assertThrows(ValidationException.class, () -> app.health.saveDaily(user.getId(), today, "-1", "", ""));
        assertThrows(ValidationException.class, () -> app.health.saveDaily(user.getId(), today, "", "", "30"));
        assertThrows(ValidationException.class, () -> app.health.saveDaily(user.getId(), "2999-01-01", "10", "", ""));
    }

    @Test
    public void latestWeighInBecomesTheCurrentWeight() throws Exception {
        String lastWeek = LocalDate.now().minusDays(7).toString();
        app.health.recordWeight(user.getId(), lastWeek, "75");
        app.health.recordWeight(user.getId(), today, "72.5");
        assertEquals(72.5, app.users.findById(user.getId()).orElseThrow().getWeightKg(), 0.001);
        // an older back-dated entry must not overwrite the latest weight
        app.health.recordWeight(user.getId(), LocalDate.now().minusDays(30).toString(), "80");
        assertEquals(72.5, app.users.findById(user.getId()).orElseThrow().getWeightKg(), 0.001);
        assertThrows(ValidationException.class, () -> app.health.recordWeight(user.getId(), today, "10"));
    }

    @Test
    public void sameDayWeighInIsReplacedNotDuplicated() throws Exception {
        app.health.recordWeight(user.getId(), today, "72");
        app.health.recordWeight(user.getId(), today, "71.5");
        assertEquals(1, app.bodyDao.findAll().size());
        assertEquals(71.5, app.bodyDao.latest(user.getId()).orElseThrow().weightKg(), 0.001);
    }

    @Test
    public void dashboardCarriesTodayFigures() throws Exception {
        app.health.saveDaily(user.getId(), today, "8000", "2000", "8");
        app.activities.log(user.getId(), "RUN", today, "30", "5", "3", "");
        Dashboard d = app.stats.dashboard(user.getId());
        assertEquals(8000, d.daily().steps());
        assertEquals(30, d.todayTotals().minutes());
        assertEquals(7, d.dailyWeek().size());
        assertEquals(100, DailyTargets.percent(d.daily().steps(), DailyTargets.STEPS));
    }

    @Test
    public void editingAWorkoutRecalculatesAndKeepsOwnership() throws Exception {
        Activity a = app.activities.log(user.getId(), "RUN", today, "30", "5", "3", "");
        Activity edited = app.activities.update(user.getId(), a.getId(), "STRENGTH", today, "60", "", "4", "heavy");
        assertEquals(a.getId(), edited.getId());
        Activity stored = app.activities.find(user.getId(), a.getId()).orElseThrow();
        assertEquals(ActivityType.STRENGTH, stored.type());
        assertEquals(60, stored.durationMinutes());
        assertTrue(stored.caloriesBurned() != a.caloriesBurned());
        assertEquals(1, app.activities.list(user.getId(), null, 10).size());

        User other = app.auth.register("Other One", "other@example.com", "Passw0rd1");
        assertThrows(NotFoundException.class,
                () -> app.activities.update(other.getId(), a.getId(), "RUN", today, "10", "1", "3", ""));
    }
}
