package com.cs3repo.manga

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

abstract class BaseMangaProvider : MainAPI() {
    override var mainUrl = ""
    override var name = ""
    override val hasMainPage = true
    override val hasQuickSupport = true
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(TvType.Documentary, TvType.Movie, TvType.TvSeries, TvType.Anime, TvType.Others)

    protected open val searchUrl = "/search?q="

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val items = mutableListOf<HomePageList>()
        val categories = listOf("popular" to "Popular", "latest" to "Latest", "trending" to "Trending")
        for ((slug, title) in categories) {
            try {
                val url = if (mainUrl.endsWith("/")) "$mainUrl$slug" else "$mainUrl/$slug"
                val doc = app.get(url).document
                val entries = doc.select("div.manga-item, div.list-item, article.item, .entry, .post").mapNotNull { it.toSearchResult() }
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
        return MangaSearchResponse(title, href, this@BaseMangaProvider.name, poster)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = if (mainUrl.endsWith("/")) "${mainUrl}${searchUrl.trimStart('/')}$query" else "$mainUrl$searchUrl$query"
        val doc = app.get(url).document
        return doc.select("div.manga-item, div.list-item, article.item, .entry, .post").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1, .manga-title, .title, .entry-title")?.text()?.trim() ?: "Unknown"
        val poster = doc.selectFirst("img.poster, .poster img, .thumbnail img, img")?.let { it.attr("data-src").ifEmpty { it.attr("src") } }
        val description = doc.selectFirst(".description, .summary, .synopsis, .entry-content, p")?.text()?.trim()
        val tags = doc.select(".genres a, .tag a").map { it.text().trim() }

        val chapters = doc.select(".chapter-list a, .chapters a, .list-chapter a").map {
            Episode(it.attr("href"), it.text().trim())
        }

        return LoadResponse(
            url, title, this@BaseMangaProvider.name, TvType.Others, poster, description, tags = tags, episodes = chapters
        )
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        doc.select("img.page-img, .reading-content img, .manga-page img, img.chapter-img").forEach { element ->
            val link = element.attr("data-src").ifEmpty { element.attr("src") }
            if (link.isNotEmpty()) {
                val absLink = fixUrl(link)
                callback(ExtractorLink(this@BaseMangaProvider.name, this@BaseMangaProvider.name, absLink, "", Qualities.Unknown.value, false))
            }
        }
        return true
    }
}
