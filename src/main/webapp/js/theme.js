// Applies the saved (or system) colour theme before first paint to avoid a flash.
(function () {
  var theme = null;
  try { theme = localStorage.getItem('kinetic-theme'); } catch (e) { /* storage blocked */ }
  if (!theme) {
    theme = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  document.documentElement.setAttribute('data-theme', theme);
})();
