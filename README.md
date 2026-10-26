# Student Opportunity Radar

Student Opportunity Radar is a Java desktop application and a web demo for exploring internships, hackathons, workshops, competitions, scholarships, and placement listings.

## Live demo

- Web app: https://amitmulge2.github.io/student-opportunity-radar/
- Backend health: https://student-opportunity-radar-xr8c.onrender.com/api/health
- Opportunities API: https://student-opportunity-radar-xr8c.onrender.com/api/opportunities

The web app loads listings, saved opportunities, and application statuses through the Java REST API. Listings are sample data for a college project demo, not live employer feeds.

## Run the Java desktop app on macOS

1. Install Java 17 or later (Java 21 works).
2. Double-click **Launch Radar.command**. If macOS blocks it, right-click the file and choose **Open**.

Or run from Terminal:

```sh
javac StudentOpportunityRadar.java
java StudentOpportunityRadar
```

The desktop app stores saved opportunities and application statuses locally in `~/.student-opportunity-radar.properties`.

## Run the backend locally

Double-click **Start Radar Backend.command**. It serves the browser demo at `http://localhost:8080` and the API under `/api`. The local API stores state in `~/.student-opportunity-radar-backend.properties`.

Available endpoints:

- `GET /api/health` — service status
- `GET /api/opportunities` — sample listings
- `GET /api/me` — saved opportunities and application statuses
- `POST /api/saved` — JSON body: {"id":"intern-1","saved":true}
- `POST /api/applications` — JSON body: {"id":"intern-1","status":"Applied"}

## Deployment notes

The hosted backend runs on Render's free instance, which can sleep when idle and may take around 50 seconds to wake. It has no persistent disk, so saved data may be lost when the service restarts or redeploys. The demo API has no sign-in, so saved/application state is shared; do not store personal or sensitive information there.
