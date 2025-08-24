package com.example.vknewsapp

data class Post(
    val id: Int,
    val text: String,
    val date: Long,
    val images: List<ImageData> = emptyList()
)