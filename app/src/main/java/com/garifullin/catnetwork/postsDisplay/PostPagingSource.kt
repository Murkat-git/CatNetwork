package com.garifullin.catnetwork.postsDisplay

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.garifullin.catnetwork.models.Post
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await


class PostPagingSource(var query: Query) : PagingSource<QuerySnapshot, Post>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Post>): QuerySnapshot? {
//        return state.anchorPosition?.let { anchorPosition ->
//            val anchorPage = state.closestPageToPosition(anchorPosition)
//            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
//        }
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Post> {
        try {
            // Start refresh at page 1 if undefined.
            val pageSize = params.loadSize
            Log.d("mytag", pageSize.toString())
            query.limit(pageSize.toLong())
            val currentPage = params.key ?: query.get().await()
            val lastVisibleProduct = currentPage.documents[currentPage.size() - 1]
            val nextPage = query.startAfter(lastVisibleProduct).get().await()

            //Log.d("mytag", params.key.toString())
            //val nextPageNumber = params.key ?: 1



            //val response = query.get().await()
            //Log.d("mytag", response.size.toString())
            return LoadResult.Page(
                data = currentPage.toObjects(Post::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error if it is an
            // expected error (such as a network failure).
            Log.d("mytag", e.toString())
            return LoadResult.Page(emptyList(), null, null)
            //return LoadResult.Error(e)
        }
    }
}