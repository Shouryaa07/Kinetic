// Minimal service worker: keeps the app installable and serves static files when offline.
// Pages themselves always come from the network, so signed-in data is never cached.
var CACHE = 'kinetic-static-v1';
self.addEventListener('install', function (e) { self.skipWaiting(); });
self.addEventListener('activate', function (e) {
  e.waitUntil(caches.keys().then(function (keys) {
    return Promise.all(keys.filter(function (k) { return k !== CACHE; }).map(function (k) { return caches.delete(k); }));
  }).then(function () { return self.clients.claim(); }));
});
self.addEventListener('fetch', function (e) {
  var url = new URL(e.request.url);
  var isStatic = e.request.method === 'GET' && url.origin === self.location.origin &&
    /\/(css|js|img)\//.test(url.pathname);
  if (!isStatic) { return; }
  e.respondWith(fetch(e.request).then(function (res) {
    var copy = res.clone();
    caches.open(CACHE).then(function (c) { c.put(e.request, copy); });
    return res;
  }).catch(function () { return caches.match(e.request); }));
});
