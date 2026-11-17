package com.cs3repo.moviesshows

class OneShowsProvider : BaseMoviesShowsProvider() {
    override var mainUrl = "https://www.1shows.org"
    override var name = "1Shows"
    override val supportedTypes = setOf(TvType.TvSeries)
}
