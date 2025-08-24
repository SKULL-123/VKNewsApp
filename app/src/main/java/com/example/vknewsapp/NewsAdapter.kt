package com.example.vknewsapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import android.util.Log
import com.bumptech.glide.Glide


class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    private var posts: List<Post> = emptyList()

    fun setPosts(posts: List<Post>) {
        this.posts = posts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position], position)
    }

    override fun getItemCount(): Int = posts.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val text: TextView = itemView.findViewById(R.id.text)
        private val image: ImageView = itemView.findViewById(R.id.image)

        fun bind(post: Post, position: Int) {
            val backgroundColor = if (position % 2 == 0) {
                android.R.color.white
            } else {
                android.R.color.darker_gray
            }
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, backgroundColor))

            title.text = "Пост #${post.id}"
            text.text = post.text

            if (post.images.isNotEmpty()) {
                val firstImage = post.images[0]
                Log.d("NewsAdapter", "Loading image: ${firstImage.url}")

                Picasso.get()
                    .load(firstImage.url)
                    .into(image, object : Callback {
                        override fun onSuccess() {
                            image.visibility = View.VISIBLE
                            Log.d("Picasso", "Image loaded successfully: ${firstImage.url}")
                        }

                        override fun onError(e: Exception?) {
                            image.visibility = View.GONE
                            Log.e("Picasso", "Error loading image: ${firstImage.url}, error: ${e?.message}")
                        }
                    })
            } else {
                image.visibility = View.GONE
                Log.d("NewsAdapter", "No images for post #${post.id}")
            }
        }
    }
}