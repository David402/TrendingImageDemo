package com.github.david402.githubsearchdemo.data

import retrofit2.http.Field

data class Result(
    val data: List<GifObject>,
    val pagination: PaginationObject,
    val meta: MetaObject
)

data class GifObject (
    val type: String,
    val id: String,
    val slug: String,
    val url: String,
    val embed_url: String,
    val username: String,
    val source: String,
    val rating: String,
    val title: String,
    val images: ImagesObject
)

data class ImagesObject(
    val original: ImageObject,
    val downsized: ImageObject
)

data class ImageObject (
    val height: Int,
    val width: Int,
    val url: String
)
data class MetaObject(
    val msg: String,
    val status: String,
    @Field("response_id") val responseId: String
)
data class PaginationObject(
    val offset: Int,
    @Field("total_count") val totalCount: Int,
    val count: Int
)