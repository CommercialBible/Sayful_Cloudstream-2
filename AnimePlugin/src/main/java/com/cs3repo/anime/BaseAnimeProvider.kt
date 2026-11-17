package com.cs3repo.anime

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

abstract class BaseAnimeProvider : MainAPI() {
    override var mainUrl = ""
    override var name = ""
    override val hasMainPage = true
    override val hasQuickSupport = true
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeSeries)

    protected open val searchUrl = "/search?query="

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val items = mutableListOf<HomePageList>()
        val categories = listOf("recent" to "Recently Added", "popular" to "Popular", "ongoing" to "Ongoing")
        for ((slug, title) in categories) {
            try {
                val url = if (mainUrl.endsWith("/")) "$mainUrl$slug" else "$mainUrl/$slug"
                val doc = app.get(url).document
                val entries = doc.select("div.anime-item, div.flim-item, article.item, .post, .list-item").mapNotNull { it.toSearchResult() }
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
        return AnimeSearchResponse(title, href, this@BaseAnimeProvider.name, TvType.Anime, poster)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = if (mainUrl.endsWith("/")) "${mainUrl}${searchUrl.trimStart('/')}$query" else "$mainUrl$searchUrl$query"
        val doc = app.get(url).document
        return doc.select("div.anime-item, div.flim-item, article.item, .post, .list-item").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1, .anime-title, .title, .entry-title")?.text()?.trim() ?: "Unknown"
        val poster = doc.selectFirst("img.poster, .poster img, .thumbnail img, img")?.let { it.attr("data-src").ifEmpty { it.attr("src") } }
        val description = doc.selectFirst(".description, .summary, .synopsis, .entry-content, p")?.text()?.trim()
        val tags = doc.select(".genres a, .tag a").map { it.text().trim() }

        val episodes = doc.select(".episode-list a, .episodes a, .season-list li a").map {
            Episode(it.attr("href"), it.text().trim())
        }

        return AnimeLoadResponse(
            url, title, this@BaseAnimeProvider.name, TvType.Anime, poster, description, tags = tags, episodes = episodes
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
                callback(ExtractorLink(this@BaseAnimeProvider.name, this@BaseAnimeProvider.name, absLink, "", Qualities.Unknown.value, false))
            }
        }
        return true
    }
}
