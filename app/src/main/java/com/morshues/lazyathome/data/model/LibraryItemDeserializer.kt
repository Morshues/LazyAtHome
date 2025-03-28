package com.morshues.lazyathome.data.model

import com.google.gson.*
import java.lang.reflect.Type

class LibraryItemDeserializer : JsonDeserializer<LibraryItem> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LibraryItem {
        val obj = json.asJsonObject
        return when (val type = obj["type"].asString) {
            "folder" -> {
                val childrenJson = obj["children"]?.asJsonArray ?: JsonArray()
                val children = childrenJson.map {
                    deserialize(it, typeOfT, context)
                }
                LibraryItem.FolderItem(
                    name = obj["name"].asString,
                    type = type,
                    children = children
                )
            }

            "video" -> {
                LibraryItem.VideoItem(
                    name = obj["name"].asString,
                    type = type,
                    path = obj["path"].asString,
                    thumbnail = obj["thumbnail"].asString
                )
            }

            else -> throw JsonParseException("Unknown type: $type")
        }
    }
}