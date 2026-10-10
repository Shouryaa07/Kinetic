// Small progressive enhancements. Pages work without JavaScript.
(function () {
  'use strict';

  // Theme toggle
  document.querySelectorAll('[data-theme-toggle]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', next);
      try { localStorage.setItem('kinetic-theme', next); } catch (e) { /* ignore */ }
    });
  });

  // Confirm before destructive forms
  document.querySelectorAll('form[data-confirm]').forEach(function (form) {
    form.addEventListener('submit', function (e) {
      if (!window.confirm(form.getAttribute('data-confirm'))) { e.preventDefault(); }
    });
  });

  // Show the distance field only for distance-based activity types
  var distanceField = document.querySelector('[data-distance-field]');
  document.querySelectorAll('input[name="type"][data-distance]').forEach(function (radio) {
    radio.addEventListener('change', function () {
      if (distanceField) { distanceField.hidden = radio.getAttribute('data-distance') !== '1'; }
    });
  });

  // Effort slider label
  var effort = document.querySelector('[data-effort]');
  var out = document.querySelector('[data-effort-out]');
  var labels = ['', 'Easy', 'Light', 'Moderate', 'Hard', 'All out'];
  if (effort && out) {
    effort.addEventListener('input', function () { out.textContent = effort.value + ' · ' + labels[effort.value]; });
  }

  // Spell out the chosen date so the browser's locale format (09/10 vs 10/09) cannot be misread
  var dateInput = document.querySelector('[data-date]');
  var dateOut = document.querySelector('[data-date-out]');
  if (dateInput && dateOut) {
    dateInput.addEventListener('input', function () {
      var p = dateInput.value.split('-');
      if (p.length === 3) {
        var d = new Date(Date.UTC(+p[0], +p[1] - 1, +p[2]));
        dateOut.textContent = d.toLocaleDateString('en-GB', { weekday: 'long', timeZone: 'UTC' }) + ', ' +
          d.toLocaleDateString('en-GB', { day: 'numeric', month: 'long', timeZone: 'UTC' });
      }
    });
  }

  // Google button placeholder when no client ID is configured
  var missing = document.querySelector('[data-google-missing]');
  if (missing) {
    missing.addEventListener('click', function () {
      var note = document.getElementById('google-missing');
      if (note) { note.hidden = false; }
    });
  }

  // Auto-dismiss success messages
  document.querySelectorAll('.flash-success').forEach(function (el) {
    setTimeout(function () { el.classList.add('gone'); }, 5000);
  });
})();

// Register the service worker so the app can be installed to a phone's home screen
if ('serviceWorker' in navigator) {
  window.addEventListener('load', function () {
    var base = document.querySelector('link[rel="manifest"]').getAttribute('href').replace('manifest.webmanifest', '');
    navigator.serviceWorker.register(base + 'sw.js').catch(function () { /* optional feature */ });
  });
}
