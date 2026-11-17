package com.cs3repo.moviesshows

class ArrowTVProvider : BaseMoviesShowsProvider() {
    override var mainUrl = "https://arrowtv.net"
    override var name = "ArrowTV"
    override val supportedTypes = setOf(TvType.TvSeries)
}
