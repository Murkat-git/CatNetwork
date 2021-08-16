package com.garifullin.catnetwork.models

import com.google.firebase.firestore.DocumentReference

class Post{
    var created: Long = 0
    var description: String = ""
    var imgUrl: String = ""
    var userReference: DocumentReference? = null
    var breed: String = ""
}
