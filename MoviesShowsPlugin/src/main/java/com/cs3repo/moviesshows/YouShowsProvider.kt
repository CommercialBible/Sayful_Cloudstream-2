package com.cs3repo.moviesshows

class YouShowsProvider : BaseMoviesShowsProvider() {
    override var mainUrl = "https://youshows.org"
    override var name = "YouShows"
    override val supportedTypes = setOf(TvType.TvSeries)
}
