package com.cs3repo.moviesshows

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

abstract class BaseMoviesShowsProvider : MainAPI() {
    override var mainUrl = ""
    override var name = ""
    override val hasMainPage = true
    override val hasQuickSupport = true
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    protected open val searchUrl = "/search/"

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val items = mutableListOf<HomePageList>()
        val categories = listOf("trending" to "Trending", "movies" to "Movies", "tv-shows" to "TV Shows")
        for ((slug, title) in categories) {
            try {
                val url = if (mainUrl.endsWith("/")) "$mainUrl$slug" else "$mainUrl/$slug"
                val doc = app.get(url).document
                val entries = doc.select("div.film_list-item, div.movie-item, article.item, .post").mapNotNull { it.toSearchResult() }
                if (entries.isNotEmpty()) {
                    items.add(HomePageList(title, entries))
                }
            } catch (_: Exception) {}
        }
        return HomePageResponse(items)
    }

    protected open fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h3, h2, .title, .name, a.title")?.text()?.trim() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val poster = this.selectFirst("img")?.let { it.attr("data-src").ifEmpty { it.attr("src") } }
        return MovieSearchResponse(title, href, this@BaseMoviesShowsProvider.name, TvType.Movie, poster)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = if (mainUrl.endsWith("/")) "${mainUrl}${searchUrl.trimStart('/')}$query" else "$mainUrl$searchUrl$query"
        val doc = app.get(url).document
        return doc.select("div.film_list-item, div.movie-item, article.item, .post").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1, .movie-title, .title, .entry-title")?.text()?.trim() ?: "Unknown"
        val poster = doc.selectFirst("img.poster, .poster img, .thumbnail img, img")?.let { it.attr("data-src").ifEmpty { it.attr("src") } }
        val description = doc.selectFirst(".description, .summary, .synopsis, .entry-content, p")?.text()?.trim()
        val tags = doc.select(".genres a, .tag a").map { it.text().trim() }
        val type = if (url.contains("tv") || url.contains("series")) TvType.TvSeries else TvType.Movie

        val episodes = if (type == TvType.TvSeries) {
            doc.select(".season-list li a, .episode-list a, .episodes a").map {
                Episode(it.attr("href"), it.text().trim())
            }
        } else emptyList()

        return LoadResponse(
            url, title, this@BaseMoviesShowsProvider.name, type, poster, description, tags = tags, episodes = episodes
        )
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        doc.select("source, iframe, .play-link, .video-link, a[href*=.mp4], a[href*=.mkv]").forEach { element ->
            val link = element.attr("src").ifEmpty { element.attr("href") }
            if (link.isNotEmpty()) {
                val absLink = fixUrl(link)
                callback(ExtractorLink(this@BaseMoviesShowsProvider.name, this@BaseMoviesShowsProvider.name, absLink, "", Qualities.Unknown.value, false))
            }
        }
        return true
    }
}
