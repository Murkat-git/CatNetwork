package com.garifullin.catnetwork

import com.google.firebase.firestore.DocumentReference
import java.util.*

class Post{
    var created: Long = 0
    var description: String = ""
    var imgUrl: String = ""
    var user: User? = null
    var userReference: DocumentReference? = null

}
