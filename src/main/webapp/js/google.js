// Sign in with Google (Google Identity Services). Google returns a signed ID token (the
// "credential"); we post it to the server, which verifies it before creating a session.
(function () {
  'use strict';
  var slot = document.getElementById('google-btn');
  if (!slot) { return; }

  function submit(credential) {
    var form = document.createElement('form');
    form.method = 'post';
    form.action = slot.getAttribute('data-action');
    [['credential', credential], ['_csrf', slot.getAttribute('data-csrf')]].forEach(function (pair) {
      var input = document.createElement('input');
      input.type = 'hidden';
      input.name = pair[0];
      input.value = pair[1];
      form.appendChild(input);
    });
    document.body.appendChild(form);
    form.submit();
  }

  function init() {
    if (!window.google || !google.accounts || !google.accounts.id) { return false; }
    google.accounts.id.initialize({
      client_id: slot.getAttribute('data-client-id'),
      callback: function (response) { submit(response.credential); },
      ux_mode: 'popup'
    });
    google.accounts.id.renderButton(slot, {
      type: 'standard', theme: 'outline', size: 'large', text: 'continue_with',
      shape: 'rectangular', logo_alignment: 'left', width: Math.min(slot.offsetWidth || 360, 400)
    });
    return true;
  }

  window.onGoogleLibraryLoad = init;
  if (!init()) {
    var tries = 0;
    var timer = setInterval(function () { if (init() || ++tries > 50) { clearInterval(timer); } }, 100);
  }
})();
