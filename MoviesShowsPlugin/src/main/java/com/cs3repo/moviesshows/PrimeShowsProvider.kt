package com.cs3repo.moviesshows

class PrimeShowsProvider : BaseMoviesShowsProvider() {
    override var mainUrl = "https://primeshows.org"
    override var name = "PrimeShows"
    override val supportedTypes = setOf(TvType.TvSeries)
}
