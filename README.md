# CloudStream 3 Multi-Extension Repository

A custom CloudStream 3 extension repository containing **48 website extensions** across three categories:

| Category | Module | Sites |
|---|---|---|
| Movies & Shows | `MoviesShowsPlugin` | 24 |
| Anime | `AnimePlugin` | 17 |
| Manga | `MangaPlugin` | 7 |

## Repository Structure

```
.
├── .github/workflows/build.yml   # CI workflow that builds .cs3 files
├── settings.gradle.kts           # Gradle multi-project settings
├── build.gradle.kts              # Root build config
├── gradle.properties
├── plugins.json                  # CloudStream plugin manifest
├── repo.json                     # Repository metadata
├── MoviesShowsPlugin/            # 24 movie/show providers
├── AnimePlugin/                  # 17 anime providers
└── MangaPlugin/                  # 7 manga providers
```

## Setup Instructions

### 1. Create a GitHub Repository

Create a new repository and push all the files in this project to it.

### 2. Update JSON Manifests

In `plugins.json` and `repo.json`, replace `USERNAME/REPO` with your actual GitHub username and repository name:

```
https://raw.githubusercontent.com/USERNAME/REPO/builds/builds/MoviesShowsPlugin-release.cs3
```

becomes:

```
https://raw.githubusercontent.com/YourName/YourRepo/builds/builds/MoviesShowsPlugin-release.cs3
```

### 3. GitHub Actions Build

When you push code to the `main` branch, the GitHub Actions workflow automatically:
1. Compiles all three Gradle modules
2. Renames the APK outputs to `.cs3` files
3. Commits them to a `builds` branch
4. Makes them available via `raw.githubusercontent.com`

### 4. Add to CloudStream 3

In the CloudStream 3 app:
1. Go to **Settings → Extensions**
2. Add a remote repository URL pointing to your `repo.json`:
   ```
   https://raw.githubusercontent.com/YourName/YourRepo/builds/repo.json
   ```
3. Install the extensions you want

## Provider List

### Movies & Shows (24)
Cinezo, ArrowTV, GaiaFlix, ThugTV, 1Tube, RedFlix, 1Shows, 1Flex, GhostFlix, PantyFlix, ShuttleTV, YouShows, PrimeShows, Willow, VuFlix, 7Reels, FlixHub Aniflix, CineHD, CineX, EMWBD, HDWebMovies, TellyHD, MovieLinkBD, HDHub4u

### Anime (17)
Yenime, Enma, Animetsu, AniShows, Kaa, AnimeTVPlus, AnikotoTV, JustAnime, HiAnime, FlixAni, AnimeSalt, SenpaiFlix, WatchAnimez, AniWaves, AnimeRulz, Dub AnimePlay, Otanox

### Manga (7)
MangaBall, ComicK, QToon, WeebCentral, KingOfShojo, Webtoons, AniReads

## Architecture

Each module has a **base provider class** that implements CloudStream's `MainAPI` interface with:
- **`getMainPage`**: Fetches trending/recent content from category pages
- **`search`**: Searches the site by query string
- **`load`**: Loads the detail page for a title (poster, description, episodes/chapters)
- **`loadLinks`**: Extracts video/image source links

Each site extends its base class with just the URL and name, making it easy to add or modify providers.

## Customization

### Adding a New Site

Create a new `.kt` file in the appropriate module's `java` directory:

```kotlin
package com.cs3repo.moviesshows

class NewSiteProvider : BaseMoviesShowsProvider() {
    override var mainUrl = "https://newsite.com"
    override var name = "NewSite"
}
```

### Adjusting Selectors

The base classes use generic CSS selectors that work across most WordPress/streaming themes. If a site uses a different HTML structure, override the relevant methods in the site's provider class.

## Notes

- The providers use template-based generic extractors. Sites with non-standard structures may need custom selector overrides.
- The CloudStream dependency is pulled from JitPack (`com.github.recloudstream:cloudstream`).
- .cs3 files are just renamed .apk files — CloudStream treats them as installable extension packages.
