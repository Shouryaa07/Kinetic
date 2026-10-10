(function () {
  'use strict';

  const STORE_KEY = 'kinetic-pages-demo-v1';
  const THEME_KEY = 'kinetic-pages-theme-v1';
  const ACTIVITY_TYPES = {
    Run: { tone: 'run', met: 8.0 }, Walk: { tone: 'walk', met: 3.8 },
    Cycle: { tone: 'cycle', met: 7.5 }, Strength: { tone: 'strength', met: 5.0 },
    Yoga: { tone: 'yoga', met: 2.8 }, HIIT: { tone: 'hiit', met: 8.5 }
  };
  const TYPE_LETTERS = { Run: 'R', Walk: 'W', Cycle: 'C', Strength: 'S', Yoga: 'Y', HIIT: 'H' };
  const VIEW_NAMES = { overview: 'OVERVIEW', activity: 'ACTIVITY', goals: 'GOALS', insights: 'INSIGHTS' };
  const viewRoot = document.getElementById('view-root');
  const dialog = document.getElementById('workout-dialog');
  const form = document.getElementById('workout-form');
  const toastEl = document.getElementById('toast');
  let toastTimer;
  let currentView = 'overview';
  let activityFilter = 'All';
  let activitySearch = '';
  let state = loadState();

  function localDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  function dateAtOffset(days) {
    const date = new Date();
    date.setHours(12, 0, 0, 0);
    date.setDate(date.getDate() - days);
    return localDate(date);
  }

  function startOfWeek(date) {
    const start = new Date(date);
    start.setHours(0, 0, 0, 0);
    start.setDate(start.getDate() - ((start.getDay() + 6) % 7));
    return start;
  }

  function weekOffset(dateString, weeksBack) {
    const date = new Date(`${dateString}T12:00:00`);
    date.setDate(date.getDate() - weeksBack * 7);
    return localDate(date);
  }

  function estimateCalories(type, minutes) {
    const info = ACTIVITY_TYPES[type] || ACTIVITY_TYPES.Run;
    return Math.round(info.met * 70 * (minutes / 60));
  }

  function seedData() {
    const today = new Date();
    const weekday = (today.getDay() + 6) % 7;
    const thisWeek = [
      { day: Math.max(0, weekday - 4), type: 'Cycle', minutes: 52, distance: 13.4, note: 'Long way home' },
      { day: Math.max(0, weekday - 2), type: 'Strength', minutes: 38, distance: 0, note: 'Steady and strong' },
      { day: weekday, type: 'Run', minutes: 42, distance: 6.2, note: 'Easy miles' }
    ];
    const byDay = new Map();
    thisWeek.forEach((item) => byDay.set(item.day, item));
    const entries = Array.from(byDay.entries()).map(([day, item]) => {
      const date = startOfWeek(today);
      date.setDate(date.getDate() + day);
      return makeWorkout({ ...item, date: localDate(date) });
    });
    [
      { days: 8, type: 'Walk', minutes: 31, distance: 2.1, note: 'Fresh air break' },
      { days: 10, type: 'HIIT', minutes: 29, distance: 0, note: 'Quick circuit' },
      { days: 12, type: 'Yoga', minutes: 44, distance: 0, note: 'A little more space' },
      { days: 15, type: 'Run', minutes: 36, distance: 5.0, note: 'Found my pace' },
      { days: 17, type: 'Strength', minutes: 46, distance: 0, note: 'Full body' },
      { days: 20, type: 'Cycle', minutes: 58, distance: 16.7, note: 'Weekend loop' },
      { days: 23, type: 'Walk', minutes: 28, distance: 1.8, note: '' },
      { days: 25, type: 'Run', minutes: 34, distance: 4.7, note: 'Kept it light' },
      { days: 28, type: 'Yoga', minutes: 40, distance: 0, note: '' }
    ].forEach((item) => entries.push(makeWorkout({ ...item, date: dateAtOffset(item.days) })));
    return { workouts: entries, goals: { sessions: 4, minutes: 210 } };
  }

  function makeWorkout(input) {
    const type = ACTIVITY_TYPES[input.type] ? input.type : 'Run';
    const minutes = Number(input.minutes) || 1;
    return {
      id: input.id || makeId(), type, date: input.date || dateAtOffset(0), minutes,
      distance: Math.max(0, Number(input.distance) || 0), note: String(input.note || '').slice(0, 90),
      calories: estimateCalories(type, minutes)
    };
  }

  function makeId() {
    return window.crypto && window.crypto.randomUUID ? window.crypto.randomUUID() : `${Date.now()}-${Math.random().toString(16).slice(2)}`;
  }

  function loadState() {
    try {
      const saved = JSON.parse(localStorage.getItem(STORE_KEY));
      if (saved && Array.isArray(saved.workouts) && saved.goals) {
        return {
          workouts: saved.workouts.filter((item) => item && ACTIVITY_TYPES[item.type] && /^\d{4}-\d{2}-\d{2}$/.test(item.date)).map(makeWorkout),
          goals: { sessions: clampGoal(saved.goals.sessions, 1, 14, 4), minutes: clampGoal(saved.goals.minutes, 30, 1200, 210) }
        };
      }
    } catch (error) { /* Use the sample data if browser storage is unavailable. */ }
    return seedData();
  }

  function clampGoal(value, min, max, fallback) {
    const number = Number(value);
    return Number.isFinite(number) ? Math.min(max, Math.max(min, Math.round(number))) : fallback;
  }

  function saveState() {
    try {
      localStorage.setItem(STORE_KEY, JSON.stringify(state));
      return true;
    } catch (error) {
      showToast('Your browser could not save this change. Check storage settings.');
      return false;
    }
  }

  function esc(value) {
    return String(value).replace(/[&<>"']/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]);
  }

  function formatDate(value, options) {
    const date = new Date(`${value}T12:00:00`);
    return new Intl.DateTimeFormat(undefined, options || { month: 'short', day: 'numeric' }).format(date);
  }

  function formatLongDate(value) {
    return new Intl.DateTimeFormat(undefined, { weekday: 'long', month: 'long', day: 'numeric' }).format(new Date(`${value}T12:00:00`));
  }

  function formatWeekDate(date) {
    return new Intl.DateTimeFormat(undefined, { weekday: 'short' }).format(date);
  }

  function sortedWorkouts(list) {
    return list.slice().sort((a, b) => b.date.localeCompare(a.date) || b.id.localeCompare(a.id));
  }

  function inCurrentWeek(workout) {
    const today = new Date();
    const start = localDate(startOfWeek(today));
    return workout.date >= start && workout.date <= localDate(today);
  }

  function weeklyWorkouts() {
    return state.workouts.filter(inCurrentWeek);
  }

  function totals(list) {
    return list.reduce((sum, item) => ({
      sessions: sum.sessions + 1,
      minutes: sum.minutes + item.minutes,
      calories: sum.calories + item.calories,
      distance: sum.distance + item.distance
    }), { sessions: 0, minutes: 0, calories: 0, distance: 0 });
  }

  function countStreak() {
    const active = new Set(state.workouts.map((item) => item.date));
    const today = localDate(new Date());
    const yesterdayDate = new Date();
    yesterdayDate.setDate(yesterdayDate.getDate() - 1);
    let cursor = active.has(today) ? today : active.has(localDate(yesterdayDate)) ? localDate(yesterdayDate) : null;
    if (!cursor) return 0;
    let count = 0;
    while (active.has(cursor)) {
      count += 1;
      const previous = new Date(`${cursor}T12:00:00`);
      previous.setDate(previous.getDate() - 1);
      cursor = localDate(previous);
    }
    return count;
  }

  function chartSvg() {
    const today = new Date();
    const start = startOfWeek(today);
    const days = Array.from({ length: 7 }, (_, index) => {
      const date = new Date(start);
      date.setDate(date.getDate() + index);
      const iso = localDate(date);
      return { date, iso, minutes: totals(state.workouts.filter((item) => item.date === iso)).minutes };
    });
    const max = Math.max(60, Math.ceil(Math.max(...days.map((item) => item.minutes), 0) / 30) * 30);
    const chartTop = 18, chartBottom = 126, chartHeight = chartBottom - chartTop;
    const guideValues = [max, Math.round(max * .66), Math.round(max * .33), 0];
    const guides = guideValues.map((value) => {
      const y = chartBottom - (value / max) * chartHeight;
      return `<g><line class="chart-gridline" x1="42" y1="${y}" x2="530" y2="${y}"/><text class="chart-label" x="33" y="${y + 3}" text-anchor="end">${value ? `${value}m` : '0'}</text></g>`;
    }).join('');
    const bars = days.map((item, index) => {
      const height = item.minutes ? Math.max(5, (item.minutes / max) * chartHeight) : 3;
      const x = 70 + index * 67;
      const y = chartBottom - height;
      const isToday = item.iso === localDate(today);
      return `<g><title>${esc(formatLongDate(item.iso))}: ${item.minutes} minutes</title><rect class="chart-bar${isToday ? ' today' : ''}" x="${x}" y="${y}" width="34" height="${height}" rx="6"/><text class="chart-label" x="${x + 17}" y="151" text-anchor="middle">${formatWeekDate(item.date)}</text></g>`;
    }).join('');
    return `<svg class="weekly-chart" viewBox="0 0 550 162" role="img" aria-label="Workout minutes by day this week">${guides}${bars}</svg>`;
  }

  function progressRing(percent, label) {
    const bounded = Math.min(100, Math.max(0, percent));
    return `<div class="progress-ring" style="--progress:${bounded}%" role="img" aria-label="${bounded}% of ${esc(label)} goal"><strong>${bounded}%</strong></div>`;
  }

  function sessionRow(item, withDelete) {
    const info = ACTIVITY_TYPES[item.type];
    const meta = item.distance ? `${item.minutes} min · ${item.distance.toFixed(1)} km` : `${item.minutes} min · ${item.calories} kcal`;
    return `<div class="session-row"><span class="session-badge ${info.tone}" aria-hidden="true">${TYPE_LETTERS[item.type]}</span><div class="session-name"><strong>${esc(item.type)}${item.note ? ` · ${esc(item.note)}` : ''}</strong><small>${esc(meta)}</small></div><div class="session-meta"><strong>${esc(formatDate(item.date))}</strong><small>${item.calories} kcal</small></div>${withDelete ? `<button class="delete-workout" type="button" data-delete="${esc(item.id)}" aria-label="Delete ${esc(item.type)} workout on ${esc(formatDate(item.date))}" title="Delete workout">×</button>` : ''}</div>`;
  }

  function typeDistribution(list) {
    const mins = Object.keys(ACTIVITY_TYPES).map((type) => ({ type, minutes: list.filter((item) => item.type === type).reduce((sum, item) => sum + item.minutes, 0) }));
    const max = Math.max(1, ...mins.map((item) => item.minutes));
    return mins.filter((item) => item.minutes).sort((a, b) => b.minutes - a.minutes).map((item) => {
      const info = ACTIVITY_TYPES[item.type];
      return `<div class="distribution-row"><span class="distribution-label"><i class="legend-dot ${info.tone}"></i>${esc(item.type)}</span><span class="distribution-track"><span class="${info.tone}" style="width:${Math.max(4, item.minutes / max * 100)}%"></span></span><span class="distribution-value">${item.minutes}m</span></div>`;
    }).join('') || '<div class="empty-state"><strong>No movement logged yet</strong><p>Log your first session to see your mix.</p></div>';
  }

  function miniSpark() {
    const currentMonday = startOfWeek(new Date());
    const weeks = Array.from({ length: 12 }, (_, index) => {
      const monday = new Date(currentMonday);
      monday.setDate(monday.getDate() - (11 - index) * 7);
      const start = localDate(monday);
      const sunday = new Date(monday);
      sunday.setDate(sunday.getDate() + 6);
      const end = localDate(sunday);
      return totals(state.workouts.filter((item) => item.date >= start && item.date <= end)).minutes;
    });
    const max = Math.max(1, ...weeks);
    return weeks.map((minutes, index) => `<span title="${minutes} active minutes" style="height:${Math.max(9, minutes / max * 100)}%;${index === weeks.length - 1 ? 'background:var(--lime-deep)' : ''}"></span>`).join('');
  }

  function weekBars() {
    const thisMonday = startOfWeek(new Date());
    const weeks = Array.from({ length: 7 }, (_, index) => {
      const monday = new Date(thisMonday);
      monday.setDate(monday.getDate() - (7 * (6 - index)));
      const mondayIso = localDate(monday);
      const sunday = new Date(monday);
      sunday.setDate(sunday.getDate() + 6);
      const sundayIso = localDate(sunday);
      const mins = totals(state.workouts.filter((item) => item.date >= mondayIso && item.date <= sundayIso)).minutes;
      return { monday, mins };
    });
    const currentWeek = weeklyWorkouts();
    weeks.push({ monday: thisMonday, mins: totals(currentWeek).minutes });
    const max = Math.max(30, ...weeks.map((week) => week.mins));
    return weeks.map((week, index) => `<div class="week-bar-col"><div class="week-bar-track"><span class="week-bar-fill" style="height:${Math.max(3, week.mins / max * 100)}%" title="${week.mins} minutes"></span></div><small>${index === weeks.length - 1 ? 'Now' : formatDate(localDate(week.monday), { month: 'numeric', day: 'numeric' })}</small></div>`).join('');
  }

  function renderOverview() {
    const current = totals(weeklyWorkouts());
    const goalPercent = Math.min(100, Math.round(current.sessions / state.goals.sessions * 100));
    const remaining = Math.max(0, state.goals.sessions - current.sessions);
    const streak = countStreak();
    const greeting = new Intl.DateTimeFormat(undefined, { month: 'long' }).format(new Date());
    const goalCopy = goalPercent >= 100 ? 'You met this week’s target. Make room to celebrate.' : `${remaining} ${remaining === 1 ? 'session' : 'sessions'} to go. You have plenty of week left.`;
    const recent = sortedWorkouts(state.workouts).slice(0, 4);
    const recentMarkup = recent.length ? recent.map((item) => sessionRow(item, false)).join('') : emptyMarkup('No sessions yet', 'Log a workout to start building your story.');
    const minutesPercent = Math.min(100, Math.round(current.minutes / state.goals.minutes * 100));
    viewRoot.innerHTML = `
      <section class="view-head"><div><span class="eyebrow">A LITTLE BETTER, EVERY WEEK</span><h1>Good things are in motion.</h1><p>Your movement, your pace. Here’s how ${esc(greeting)} is shaping up.</p></div></section>
      <section class="hero-card"><div class="hero-copy"><span class="eyebrow">YOUR WEEKLY RHYTHM</span><h2>${goalPercent >= 100 ? 'A week well moved.' : 'Consistency looks good on you.'}</h2><p>${esc(goalCopy)} Every session counts — even the small ones.</p><button class="button button-primary" type="button" data-open-workout>＋ <span>Log a session</span></button></div><div class="hero-progress">${progressRing(goalPercent, 'weekly workout')}<div class="progress-copy"><strong>${current.sessions} of ${state.goals.sessions} sessions</strong><span>${current.minutes} active min · ${current.calories.toLocaleString()} kcal</span></div></div></section>
      <section class="metric-grid" aria-label="This week's totals">
        ${metricCard('↗', 'Sessions', current.sessions, `of ${state.goals.sessions}`, goalPercent >= 100 ? 'Goal reached' : 'Keep your rhythm')}
        ${metricCard('◷', 'Active time', current.minutes, 'min', `${minutesPercent}% of your weekly target`)}
        ${metricCard('✳', 'Energy', current.calories.toLocaleString(), 'kcal', 'Estimated from activity')}
        ${metricCard('⌁', 'Current streak', streak, streak === 1 ? 'day' : 'days', streak ? 'One day at a time' : 'Log today to begin')}
      </section>
      <section class="content-grid"><article class="panel"><div class="panel-head"><div><h2>This week, at a glance</h2><p>Active minutes by day</p></div><span class="range-tag">MON — SUN</span></div>${chartSvg()}</article><article class="panel"><div class="panel-head"><div><h2>One goal, one week</h2><p>Small wins add up</p></div><button class="text-link" type="button" data-view="goals">Edit goals ↗</button></div><div class="goal-feature">${progressRing(goalPercent, 'weekly workout')}<div class="goal-info"><span class="eyebrow">WEEKLY SESSIONS</span><h3>${current.sessions} / ${state.goals.sessions} sessions</h3><p>${remaining ? `${remaining} more to reach your target` : 'You made it — nicely done.'}</p></div></div><div class="goal-spark" aria-hidden="true">${miniSpark()}</div><p class="goal-note"><b>YOUR PACE</b><span>${minutesPercent}% of your ${state.goals.minutes}-minute goal is in the books.</span></p></article></section>
      <section class="content-grid"><article class="panel"><div class="panel-head"><div><h2>Recent movement</h2><p>Your latest sessions</p></div><button class="text-link" type="button" data-view="activity">View activity ↗</button></div><div class="session-list">${recentMarkup}</div></article><article class="panel"><div class="panel-head"><div><h2>Your movement mix</h2><p>Minutes by activity this week</p></div></div>${typeDistribution(weeklyWorkouts())}</article></section>`;
  }

  function metricCard(icon, label, value, unit, note) {
    return `<article class="metric-card"><div class="metric-top"><span>${esc(label)}</span><span class="metric-icon" aria-hidden="true">${icon}</span></div><strong>${esc(value)}</strong><span class="metric-unit">${esc(unit)}</span><span class="metric-foot">${esc(note)}</span></article>`;
  }

  function emptyMarkup(title, copy) {
    return `<div class="empty-state"><span aria-hidden="true">＋</span><strong>${esc(title)}</strong><p>${esc(copy)}</p><button class="text-link" type="button" data-open-workout>Log a workout ↗</button></div>`;
  }

  function renderActivity() {
    const filters = ['All', ...Object.keys(ACTIVITY_TYPES)];
    const needle = activitySearch.trim().toLowerCase();
    const entries = sortedWorkouts(state.workouts).filter((item) => (activityFilter === 'All' || item.type === activityFilter)
      && (!needle || `${item.type} ${item.note}`.toLowerCase().includes(needle)));
    const rows = entries.map((item) => {
      const info = ACTIVITY_TYPES[item.type];
      return `<tr><td><span class="row-type"><span class="session-badge ${info.tone}">${TYPE_LETTERS[item.type]}</span>${esc(item.type)}</span></td><td>${esc(formatLongDate(item.date))}</td><td class="muted-cell">${item.minutes} min${item.distance ? ` · ${item.distance.toFixed(1)} km` : ''}</td><td class="muted-cell">${item.calories} kcal</td><td><button class="delete-workout" type="button" data-delete="${esc(item.id)}" aria-label="Delete ${esc(item.type)} workout on ${esc(formatDate(item.date))}" title="Delete workout">×</button></td></tr>`;
    }).join('');
    const current = totals(weeklyWorkouts());
    viewRoot.innerHTML = `<section class="view-head"><div><span class="eyebrow">EVERY EFFORT HAS A PLACE</span><h1>Your activity.</h1><p>${state.workouts.length} ${state.workouts.length === 1 ? 'session' : 'sessions'} captured, one day at a time.</p></div><button class="button button-primary" type="button" data-open-workout>＋ Log a workout</button></section>
      <section class="metric-grid">${metricCard('↗', 'This week', current.sessions, 'sessions', 'Your current rhythm')}${metricCard('◷', 'Active time', current.minutes, 'min', 'Minutes in motion')}${metricCard('✳', 'Energy', current.calories.toLocaleString(), 'kcal', 'Estimated burn')}${metricCard('⌁', 'Distance', current.distance.toFixed(1), 'km', 'From tracked sessions')}</section>
      <section class="panel"><div class="panel-head"><div><h2>The logbook</h2><p>Search, filter, or remove a session.</p></div><span class="range-tag">${entries.length} SHOWN</span></div><div class="activity-toolbar"><div class="filter-list" role="group" aria-label="Filter activity type">${filters.map((type) => `<button class="filter-button${activityFilter === type ? ' active' : ''}" type="button" data-filter="${esc(type)}" aria-pressed="${activityFilter === type}">${esc(type)}</button>`).join('')}</div><label class="sr-only" for="activity-search">Search activity</label><input class="search-input" id="activity-search" type="search" placeholder="Search sessions…" value="${esc(activitySearch)}"></div>
      ${entries.length ? `<div class="table-scroll"><table class="activity-table"><thead><tr><th>Activity</th><th>Date</th><th>Duration / distance</th><th>Energy</th><th><span class="sr-only">Actions</span></th></tr></thead><tbody>${rows}</tbody></table></div>` : emptyMarkup('Nothing here yet', 'Try another filter, or log a new workout.')}</section>`;
  }

  function renderGoals() {
    const current = totals(weeklyWorkouts());
    const sessionsPercent = Math.min(100, Math.round(current.sessions / state.goals.sessions * 100));
    const minutesPercent = Math.min(100, Math.round(current.minutes / state.goals.minutes * 100));
    viewRoot.innerHTML = `<section class="view-head"><div><span class="eyebrow">AIM FOR SOMETHING KIND</span><h1>Goals that feel good.</h1><p>Choose a weekly rhythm that fits your life. You can change it anytime.</p></div></section>
      <section class="target-grid"><article class="target-card"><div class="target-card-head"><div><span class="eyebrow">MOVE OFTEN</span><h2>Workout sessions</h2><p>A little consistency goes a long way.</p></div>${progressRing(sessionsPercent, 'weekly workout')}</div><div class="target-value"><strong>${current.sessions}</strong><span>of ${state.goals.sessions} sessions</span></div><div class="progress-track" role="progressbar" aria-label="Workout session goal progress" aria-valuemin="0" aria-valuemax="${state.goals.sessions}" aria-valuenow="${Math.min(current.sessions, state.goals.sessions)}"><span style="width:${sessionsPercent}%"></span></div><div class="target-controls"><button class="step-button" type="button" data-goal="sessions" data-step="-1" aria-label="Decrease weekly sessions">−</button><output>${state.goals.sessions} / week</output><button class="step-button" type="button" data-goal="sessions" data-step="1" aria-label="Increase weekly sessions">＋</button></div></article>
      <article class="target-card"><div class="target-card-head"><div><span class="eyebrow">MAKE SOME SPACE</span><h2>Active minutes</h2><p>Time for the movement you enjoy.</p></div>${progressRing(minutesPercent, 'weekly active minutes')}</div><div class="target-value"><strong>${current.minutes}</strong><span>of ${state.goals.minutes} minutes</span></div><div class="progress-track" role="progressbar" aria-label="Active minutes goal progress" aria-valuemin="0" aria-valuemax="${state.goals.minutes}" aria-valuenow="${Math.min(current.minutes, state.goals.minutes)}"><span style="width:${minutesPercent}%"></span></div><div class="target-controls"><button class="step-button" type="button" data-goal="minutes" data-step="-30" aria-label="Decrease weekly minutes">−</button><output>${state.goals.minutes} min / week</output><button class="step-button" type="button" data-goal="minutes" data-step="30" aria-label="Increase weekly minutes">＋</button></div></article></section>
      <div class="goal-callout"><span class="callout-icon" aria-hidden="true">✳</span><p><strong>Your goal is a guide, not a grade.</strong><br>Rest days are training days too. Set a target that makes you feel encouraged.</p></div>
      <section class="content-grid" style="margin-top:15px"><article class="panel"><div class="panel-head"><div><h2>Your week</h2><p>So far, so good.</p></div><span class="range-tag">${sessionsPercent}% COMPLETE</span></div>${chartSvg()}</article><article class="panel"><div class="panel-head"><div><h2>A note on progress</h2><p>Good habits grow at their own pace.</p></div></div><div class="insight-highlight"><span class="highlight-icon" aria-hidden="true">↗</span><div><strong>${sessionsPercent >= 100 ? 'You reached your weekly target.' : `${Math.max(0, state.goals.sessions - current.sessions)} sessions left this week.`}</strong><p>${minutesPercent >= 100 ? 'You have also met your active minutes goal.' : `You have ${Math.max(0, state.goals.minutes - current.minutes)} active minutes to go.`}</p></div></div><p class="goal-note"><b>YOUR STREAK</b><span>${countStreak() ? `${countStreak()} active ${countStreak() === 1 ? 'day' : 'days'} in a row.` : 'One session can start a new streak.'}</span></p></article></section>`;
  }

  function renderInsights() {
    const current = totals(weeklyWorkouts());
    const all = totals(state.workouts);
    const sortedTypes = Object.keys(ACTIVITY_TYPES).map((type) => ({ type, minutes: state.workouts.filter((item) => item.type === type).reduce((sum, item) => sum + item.minutes, 0) })).sort((a, b) => b.minutes - a.minutes);
    const favorite = sortedTypes[0];
    const activeDays = new Set(state.workouts.map((item) => item.date)).size;
    const average = activeDays ? Math.round(all.minutes / activeDays) : 0;
    viewRoot.innerHTML = `<section class="view-head"><div><span class="eyebrow">NOTICE THE GOOD STUFF</span><h1>Look how far you’ve come.</h1><p>A few numbers, a little perspective, and plenty to feel good about.</p></div><button class="button button-light" type="button" id="export-csv">↓ Export CSV</button></section>
      <section class="metric-grid">${metricCard('↗', 'Total sessions', all.sessions, 'logged', 'Every one counts')}${metricCard('◷', 'Time in motion', all.minutes.toLocaleString(), 'min', `${(all.minutes / 60).toFixed(1)} hours total`)}${metricCard('⌁', 'Active days', activeDays, 'days', `${average} min per active day`)}${metricCard('✳', 'Your favorite', favorite && favorite.minutes ? favorite.type : '—', favorite && favorite.minutes ? `${favorite.minutes} min` : 'so far', 'Your time, your way')}</section>
      <section class="content-grid equal"><article class="panel"><div class="panel-head"><div><h2>Your weeks, in minutes</h2><p>Eight weeks of showing up</p></div><span class="range-tag">${current.minutes} MIN THIS WEEK</span></div><div class="week-bars" role="img" aria-label="Active minutes by week">${weekBars()}</div></article><article class="panel"><div class="panel-head"><div><h2>What moves you</h2><p>Time spent across activities</p></div></div>${typeDistribution(state.workouts)}<div class="type-legend"><span class="legend-item">Based on ${all.sessions} logged ${all.sessions === 1 ? 'session' : 'sessions'}</span></div></article></section>
      <div class="insight-highlight"><span class="highlight-icon" aria-hidden="true">✳</span><div><strong>${activeDays ? `${activeDays} active ${activeDays === 1 ? 'day' : 'days'} on the board.` : 'Your next chapter starts with one session.'}</strong><p>${activeDays ? `You average ${average} minutes whenever you get moving. Keep that feeling close.` : 'Log a workout to see your personal patterns take shape.'}</p></div></div>`;
  }

  function render() {
    const labels = { overview: 'Good things are in motion.', activity: 'Your activity.', goals: 'Goals that feel good.', insights: 'Look how far you’ve come.' };
    document.getElementById('crumb-current').textContent = VIEW_NAMES[currentView];
    document.title = `${VIEW_NAMES[currentView][0]}${VIEW_NAMES[currentView].slice(1).toLowerCase()} — Kinetic`;
    document.querySelectorAll('.nav-item').forEach((item) => {
      const active = item.dataset.view === currentView;
      item.classList.toggle('active', active);
      item.setAttribute('aria-current', active ? 'page' : 'false');
    });
    if (currentView === 'overview') renderOverview();
    if (currentView === 'activity') renderActivity();
    if (currentView === 'goals') renderGoals();
    if (currentView === 'insights') renderInsights();
    document.getElementById('today-label').textContent = formatLongDate(localDate(new Date()));
    document.querySelector('.top-add').setAttribute('aria-label', 'Log a workout');
    document.querySelector('.top-add span:last-child').textContent = 'Log a workout';
    return labels[currentView];
  }

  function showToast(message) {
    toastEl.textContent = message;
    toastEl.classList.add('show');
    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(() => toastEl.classList.remove('show'), 2800);
  }

  function openDialog() {
    const dateInput = document.getElementById('workout-date');
    dateInput.value = localDate(new Date());
    dateInput.max = localDate(new Date());
    document.getElementById('form-error').hidden = true;
    if (typeof dialog.showModal === 'function') dialog.showModal();
    else dialog.setAttribute('open', '');
    document.getElementById('workout-minutes').focus();
  }

  function closeDialog() {
    if (typeof dialog.close === 'function' && dialog.open) dialog.close();
    else dialog.removeAttribute('open');
  }

  function saveWorkout(event) {
    event.preventDefault();
    const data = new FormData(form);
    const date = String(data.get('date') || '');
    const minutes = Number(data.get('minutes'));
    const type = String(data.get('type') || 'Run');
    const error = document.getElementById('form-error');
    if (!ACTIVITY_TYPES[type] || !/^\d{4}-\d{2}-\d{2}$/.test(date) || date > localDate(new Date()) || !Number.isInteger(minutes) || minutes < 1 || minutes > 1440) {
      error.textContent = 'Add a valid date and a duration between 1 and 1,440 minutes.';
      error.hidden = false;
      return;
    }
    const distance = Number(data.get('distance')) || 0;
    if (!Number.isFinite(distance) || distance < 0 || distance > 500) {
      error.textContent = 'Distance must be between 0 and 500 km.';
      error.hidden = false;
      return;
    }
    state.workouts.push(makeWorkout({ type, date, minutes, distance, note: data.get('note') }));
    saveState();
    closeDialog();
    form.reset();
    render();
    showToast('Workout saved. Nice work showing up.');
  }

  function deleteWorkout(id) {
    const item = state.workouts.find((workout) => workout.id === id);
    if (!item || !window.confirm(`Remove this ${item.type.toLowerCase()} workout?`)) return;
    state.workouts = state.workouts.filter((workout) => workout.id !== id);
    saveState();
    render();
    showToast('Workout removed.');
  }

  function exportCsv() {
    const rows = [['Date', 'Activity', 'Duration (minutes)', 'Distance (km)', 'Calories', 'Note']];
    sortedWorkouts(state.workouts).forEach((item) => rows.push([item.date, item.type, item.minutes, item.distance || '', item.calories, item.note]));
    const csv = rows.map((row) => row.map((cell) => `"${String(cell).replace(/"/g, '""')}"`).join(',')).join('\r\n');
    const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `kinetic-activity-${localDate(new Date())}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    showToast('Your activity export is ready.');
  }

  document.addEventListener('click', (event) => {
    const nav = event.target.closest('[data-view]');
    if (nav) {
      currentView = nav.dataset.view;
      render();
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
    if (event.target.closest('[data-open-workout]') || event.target.closest('#open-workout')) { openDialog(); return; }
    const deletion = event.target.closest('[data-delete]');
    if (deletion) { deleteWorkout(deletion.dataset.delete); return; }
    const filter = event.target.closest('[data-filter]');
    if (filter) { activityFilter = filter.dataset.filter; renderActivity(); return; }
    const stepper = event.target.closest('[data-goal]');
    if (stepper) {
      const key = stepper.dataset.goal;
      const delta = Number(stepper.dataset.step);
      const bounds = key === 'sessions' ? [1, 14] : [30, 1200];
      state.goals[key] = Math.min(bounds[1], Math.max(bounds[0], state.goals[key] + delta));
      saveState();
      renderGoals();
      return;
    }
    if (event.target.closest('#cancel-dialog') || event.target.closest('#close-dialog')) { closeDialog(); return; }
    if (event.target.closest('#theme-toggle')) {
      const next = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
      document.documentElement.dataset.theme = next;
      try { localStorage.setItem(THEME_KEY, next); } catch (error) { /* Theme still applies for this visit. */ }
      document.getElementById('theme-toggle').setAttribute('aria-label', `Switch to ${next === 'dark' ? 'light' : 'dark'} theme`);
      return;
    }
    if (event.target.closest('#export-csv')) { exportCsv(); return; }
    if (event.target.closest('#reset-demo')) {
      if (!window.confirm('Reset this browser’s demo data to the original sample?')) return;
      state = seedData();
      activityFilter = 'All';
      activitySearch = '';
      saveState();
      render();
      showToast('Demo data reset. Your next chapter starts here.');
    }
  });

  document.addEventListener('input', (event) => {
    if (event.target.id === 'activity-search') {
      const position = event.target.selectionStart;
      activitySearch = event.target.value;
      renderActivity();
      const next = document.getElementById('activity-search');
      next.focus();
      next.setSelectionRange(position, position);
    }
  });

  form.addEventListener('submit', saveWorkout);
  document.getElementById('workout-distance').addEventListener('input', (event) => {
    const distance = Number(event.target.value);
    event.target.setCustomValidity(distance > 500 ? 'Distance must be 500 km or less.' : '');
  });

  document.getElementById('reset-demo').addEventListener('keydown', (event) => {
    if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); event.currentTarget.click(); }
  });

  try {
    const theme = localStorage.getItem(THEME_KEY);
    if (theme === 'dark' || theme === 'light') document.documentElement.dataset.theme = theme;
  } catch (error) { /* Keep the default theme. */ }
  const themeNow = document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light';
  document.getElementById('theme-toggle').setAttribute('aria-label', `Switch to ${themeNow === 'dark' ? 'light' : 'dark'} theme`);
  render();
}());
