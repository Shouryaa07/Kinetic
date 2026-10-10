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
import com.kinetic.model.GoalMetric;
import com.kinetic.model.User;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** End-to-end through services, DAOs and JDBC against an in-memory database. */
public class ActivityFlowTest {
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
    public void logListFilterDelete() throws Exception {
        Activity run = app.activities.log(user.getId(), "RUN", today, "30", "5", "3", "easy");
        app.activities.log(user.getId(), "YOGA", today, "45", "", "2", "");
        assertTrue(run.caloriesBurned() > 0);
        assertEquals(2, app.activities.list(user.getId(), null, 50).size());
        List<Activity> runs = app.activities.list(user.getId(), ActivityType.RUN, 50);
        assertEquals(1, runs.size());
        assertEquals(ActivityType.RUN, runs.get(0).type());
        app.activities.delete(user.getId(), run.getId());
        assertEquals(1, app.activities.list(user.getId(), null, 50).size());
    }

    @Test
    public void cannotDeleteAnotherMembersWorkout() throws Exception {
        User other = app.auth.register("Other One", "other@example.com", "Passw0rd1");
        Activity a = app.activities.log(user.getId(), "RUN", today, "30", "5", "3", "");
        assertThrows(NotFoundException.class, () -> app.activities.delete(other.getId(), a.getId()));
        assertEquals(1, app.activities.list(user.getId(), null, 50).size());
    }

    @Test
    public void badInputIsRejected() {
        assertThrows(ValidationException.class, () -> app.activities.log(user.getId(), "NOPE", today, "30", "", "3", ""));
        assertThrows(ValidationException.class, () -> app.activities.log(user.getId(), "RUN", today, "0", "", "3", ""));
        assertThrows(ValidationException.class, () -> app.activities.log(user.getId(), "RUN", "2999-01-01", "30", "", "3", ""));
        assertThrows(ValidationException.class, () -> app.activities.log(user.getId(), "RUN", today, "30", "", "9", ""));
    }

    @Test
    public void dashboardReflectsDataAndCacheIsInvalidated() throws Exception {
        assertEquals(0, app.stats.dashboard(user.getId()).thisWeek().workouts());
        app.activities.log(user.getId(), "RUN", today, "60", "10", "3", "");
        Dashboard d = app.stats.dashboard(user.getId());
        assertEquals(1, d.thisWeek().workouts());
        assertEquals(60, d.thisWeek().minutes());
        assertEquals(1, d.streak());
        assertEquals(StatsService.WEEKS, d.weeks().size());
    }

    @Test
    public void goalsSaveAtomicallyAndDriveProgress() throws Exception {
        Map<GoalMetric, String> raw = new EnumMap<>(GoalMetric.class);
        raw.put(GoalMetric.WORKOUTS, "4");
        raw.put(GoalMetric.MINUTES, "120");
        raw.put(GoalMetric.CALORIES, "");
        raw.put(GoalMetric.DISTANCE, "");
        app.goals.saveAll(user.getId(), raw);
        app.activities.log(user.getId(), "RUN", today, "60", "10", "3", "");
        Dashboard d = app.stats.dashboard(user.getId());
        assertEquals(2, d.goals().size());
        assertEquals(50, d.goals().stream().filter(g -> g.goal().metric() == GoalMetric.MINUTES).findFirst().orElseThrow().percent());
    }

    @Test
    public void invalidGoalRollsBackTheWholeSave() throws Exception {
        Map<GoalMetric, String> good = new EnumMap<>(GoalMetric.class);
        good.put(GoalMetric.WORKOUTS, "4");
        app.goals.saveAll(user.getId(), good);
        Map<GoalMetric, String> bad = new EnumMap<>(GoalMetric.class);
        bad.put(GoalMetric.WORKOUTS, "9");
        bad.put(GoalMetric.MINUTES, "abc");
        assertThrows(ValidationException.class, () -> app.goals.saveAll(user.getId(), bad));
        assertEquals(4.0, app.goals.forUser(user.getId()).get(0).target(), 0.001);
    }

    @Test
    public void transactionRollsBackOnFailure() {
        long before = app.users.findAll().size();
        assertThrows(IllegalStateException.class, () -> app.db.inTransaction(() -> {
            User u = new User();
            u.setEmail("tx@example.com");
            u.setDisplayName("Tx");
            u.setProvider(com.kinetic.model.AuthProvider.LOCAL);
            app.users.save(u);
            throw new IllegalStateException("boom");
        }));
        assertEquals(before, app.users.findAll().size());
    }

    @Test
    public void deletingAUserCascadesToTheirData() throws Exception {
        app.activities.log(user.getId(), "RUN", today, "30", "5", "3", "");
        app.users.deleteById(user.getId());
        assertEquals(0, app.activityDao.findAll().size());
    }
}
