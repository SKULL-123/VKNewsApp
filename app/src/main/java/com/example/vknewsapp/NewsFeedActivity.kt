package com.example.vknewsapp

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.util.Log
import kotlin.math.min
import org.json.JSONArray
import android.content.Intent
import android.widget.Toast


class NewsFeedActivity : AppCompatActivity() {

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var loadingLayout: View
    private lateinit var adapter: NewsAdapter
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_feed)

        prefs = getSharedPreferences("vk_prefs", MODE_PRIVATE)
        recyclerView = findViewById(R.id.recyclerView)
        loadingLayout = findViewById(R.id.loadingLayout)

        adapter = NewsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        loadPosts()
//    testWithHardcodedUrl()
    }

    private fun loadPosts() {
        val accessToken = prefs.getString("access_token", null)

        if (accessToken == null) {
            finish()
            return
        }

        val url = "https://api.vk.com/method/wall.get?" +
                "owner_id=-146026097&" +
                "count=50&" +
                "filter=owner&" +
                "extended=1&" +
                "access_token=$accessToken&" +
                "v=5.103"

        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try {
                    Log.d("VKResponse", "Full response: ${response.toString()}")

                    val posts = parsePosts(response)
                    adapter.setPosts(posts)
                    loadingLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadingLayout.visibility = View.GONE
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                loadingLayout.visibility = View.GONE

                val errorMessage = error.message ?: ""
                if (errorMessage.contains("access_token") || errorMessage.contains("authorization failed")) {
                    // Удаляем невалидный токен
                    val prefs = getSharedPreferences("vk_prefs", MODE_PRIVATE)
                    prefs.edit().remove("access_token").apply()

                    val intent = Intent(this@NewsFeedActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Другие ошибки
                    Toast.makeText(this@NewsFeedActivity, "Ошибка загрузки: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun parsePosts(response: JSONObject): List<Post> {
        val posts = mutableListOf<Post>()
        try {
            val items = response.getJSONObject("response").getJSONArray("items")

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val text = item.optString("text", "")
                val date = item.optLong("date", 0)
                val id = item.optInt("id", 0)

                val images = mutableListOf<ImageData>()

                if (item.has("attachments")) {
                    val attachments = item.getJSONArray("attachments")
                    for (j in 0 until attachments.length()) {
                        val attachment = attachments.getJSONObject(j)
                        val type = attachment.optString("type", "")

                        Log.d("VKAttachment", "Processing attachment type: $type")

                        when (type) {
                            "photo" -> {
                                try {
                                    val photo = attachment.getJSONObject("photo")
                                    Log.d("VKPhoto", "Photo object: $photo")

                                    if (photo.has("sizes")) {
                                        val sizes = photo.getJSONArray("sizes")
                                        var largestSize: JSONObject? = null

                                        for (k in 0 until sizes.length()) {
                                            val size = sizes.getJSONObject(k)
                                            if (largestSize == null ||
                                                size.optInt("width", 0) > largestSize.optInt("width", 0)) {
                                                largestSize = size
                                            }
                                        }

                                        if (largestSize != null) {
                                            val url = largestSize.optString("url", "")
                                            val width = largestSize.optInt("width", 0)
                                            val height = largestSize.optInt("height", 0)

                                            if (url.isNotEmpty()) {
                                                images.add(ImageData(url, width, height))
                                                Log.d("VKPhoto", "Added image from sizes: $url")
                                            }
                                        }
                                    } else {
                                        val url = photo.optString("photo_2560", "") ?:
                                        photo.optString("photo_1280", "") ?:
                                        photo.optString("photo_807", "") ?:
                                        photo.optString("photo_604", "") ?:
                                        photo.optString("photo_130", "") ?:
                                        photo.optString("photo_75", "")

                                        if (url.isNotEmpty()) {
                                            images.add(ImageData(url, 0, 0))
                                            Log.d("VKPhoto", "Added image from direct field: $url")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("VKError", "Error processing photo attachment: ${e.message}")
                                }
                            }
                            "video" -> {
                                try {
                                    val video = attachment.getJSONObject("video")
                                    val imageUrl = video.optString("photo_800", "") ?:
                                    video.optString("photo_320", "") ?:
                                    video.optString("image", "")

                                    if (imageUrl.isNotEmpty()) {
                                        images.add(ImageData(imageUrl, 0, 0))
                                        Log.d("VKVideo", "Added video thumbnail: $imageUrl")
                                    }
                                } catch (e: Exception) {
                                    Log.e("VKError", "Error processing video attachment: ${e.message}")
                                }
                            }
                        }
                    }
                }

                Log.d("VKPost", "Post #$id: imagesCount=${images.size}, text='${text.take(50)}...'")
                posts.add(Post(id, text, date, images))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("VKError", "Error parsing posts: ${e.message}")
        }

        return posts
    }

    private fun testWithHardcodedUrl() {
        val testUrl = "https://sun9-42.userapi.com/impg/tCnK9z807ershMiIn_GO9WrwRvJdTHdK5_3mJg/0gdtn8JkAI4.jpg?size=800x450&quality=95&keep_aspect_ratio=1&background=000000&sign=66da3a975bac3d7221503a4b03916116&c_uniq_tag=GjmdCGn0NraRQGSkJ2U-0F6NCD9SJX6GE6U4VsSMc_Y&type=video_thumb"

        val testPost = Post(999, "Тестовый пост с изображением", System.currentTimeMillis() / 1000,
            listOf(ImageData(testUrl, 800, 450)))

        adapter.setPosts(listOf(testPost))
        loadingLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}