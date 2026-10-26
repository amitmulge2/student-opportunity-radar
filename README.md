# Student Opportunity Radar

A standalone Java Swing desktop application for discovering student opportunities and tracking applications.

## Run on macOS

1. Install Java 17 or later (Java 21 works).
2. Double-click **Launch Radar.command**. If macOS blocks it, right-click the file and choose **Open**.

Or run from Terminal:

```sh
javac StudentOpportunityRadar.java
java StudentOpportunityRadar
```

## Features

- Browse sample internships, hackathons, workshops, competitions, scholarships, and placement listings.
- Search listings and filter by opportunity type or profile match.
- Save opportunities and manage an application status board.
- Saved items and application statuses persist locally in `~/.student-opportunity-radar.properties`.

The current listings are sample data for a college project demo; this version does not connect to live opportunity feeds.
