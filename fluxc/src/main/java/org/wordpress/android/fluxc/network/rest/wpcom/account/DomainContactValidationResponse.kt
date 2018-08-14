package org.wordpress.android.fluxc.network.rest.wpcom.account

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import org.wordpress.android.fluxc.network.utils.getBoolean
import org.wordpress.android.fluxc.network.utils.getInt
import org.wordpress.android.fluxc.network.utils.getJsonObject
import java.lang.reflect.Type

@JsonAdapter(DomainContactValidationDeserializer::class)
class DomainContactValidationResponse {
    var isSuccess: Boolean = false
    var validationMessages: Map<String, Array<String>>? = null
}

class DomainContactValidationDeserializer : JsonDeserializer<DomainContactValidationResponse> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type, context: JsonDeserializationContext): DomainContactValidationResponse {
        val jsonObject = json.asJsonObject

        val statusCode = jsonObject.getInt("code")
        val body = jsonObject.getJsonObject("body")
        if (statusCode == 200) {
            val response = DomainContactValidationResponse()
            response.isSuccess = body.getBoolean("success")
            response.validationMessages = HashMap()
            val messagesJsonObj = body.getJsonObject("messages")
            messagesJsonObj?.entrySet().forEach { (messageKey, value) ->
                val messagesJsonArray = value.asJsonArray
                val messagesList = messagesJsonArray.map { it.asString }.toTypedArray()
            }
            return response
        }
    }
}
