package com.garifullin.catnetwork.models

import com.google.firebase.firestore.DocumentReference

class Post{
    var created: Long = 0
    var description: String = ""
    var imgUrl: String = ""
    var userReference: DocumentReference? = null
    var breed: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Post

        if (created != other.created) return false
        if (description != other.description) return false
        if (imgUrl != other.imgUrl) return false
        if (userReference != other.userReference) return false
        if (breed != other.breed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = created.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + imgUrl.hashCode()
        result = 31 * result + (userReference?.hashCode() ?: 0)
        result = 31 * result + breed.hashCode()
        return result
    }


}
