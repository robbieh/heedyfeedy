
const cacheName = "heedyfeedy";

const appShellFiles = [
	"index.html",
	"js/compiled/app.js",
	"main.css",
	"pwa-icons/android-192x192.png",
	"pwa-icons/android-512x512.png",
	"icons/account_box.svg",
	"icons/arrow-downward.svg",
	"icons/backspace.svg",
	"icons/delete-forever.svg",
	"icons/refresh.svg",
	"icons/sync_problem.svg",
	"icons/upload.svg"
];

self.addEventListener("install", (e) =>
	{console.log("[Service Worker] Install")
		e.waitUntil(
			(async () => {
				const cache = await caches.open(cacheName);
				console.log("[Service Worker] Caching all: shell content");
				await cache.addAll(appShellFiles);
			})(),
		);
	});

self.addEventListener("fetch", (e) => {
	e.respondWith(
		(async () => {
			const r = await caches.match(e.request);
			console.log(`[Service Worker] Fetching resource: ${e.request.url}`);
			if (r) {
				return r;
			}
			const response = await fetch(e.request);
			const cache = await caches.open(cacheName);
			console.log(`[Service Worker] Caching new resource: ${e.request.url}`);
			cache.put(e.request, response.clone());
			return response;
		})(),
	);
});
