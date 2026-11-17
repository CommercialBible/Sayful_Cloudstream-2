package com.cs3repo.manga

class WebtoonsProvider : BaseMangaProvider() {
    override var mainUrl = "https://www.webtoons.com"
    override var name = "Webtoons"
    override val searchUrl = "/search?keyword="
}
