package com.example.camerasecuritysystem.models.jsonData

import org.json.JSONObject

data class Camera(
    val id:          Int,
    val name:        String,
    val description: String,
    val password:    String)  {

    constructor(json: JSONObject) : this(
        json["id"] as Int,
        json["name"] as String,
        json["description"] as String,
        json["password"] as String
    )

    constructor(json: String)  : this(JSONObject(json))

    fun toJsonString(): String {
        val jObject = JSONObject()
        jObject.put("id",          this.id)
        jObject.put("name",        this.name)
        jObject.put("description", this.description)
        jObject.put("password",    this.password)
        return jObject.toString()
    }

}
