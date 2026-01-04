package com.hancekim.billboard.core.data.datastore.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.hancekim.billboard.core.data.model.BillboardPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object BillboardPreferenceSerializer : Serializer<BillboardPreference> {
    override val defaultValue: BillboardPreference = BillboardPreference()

    override suspend fun readFrom(input: InputStream): BillboardPreference = try {
        Json.decodeFromString(
            deserializer = BillboardPreference.serializer(),
            string = input.readBytes().decodeToString(),
        )
    } catch (exception: SerializationException) {
        throw CorruptionException("Unable to read BillboardPreference", exception)
    }

    override suspend fun writeTo(
        t: BillboardPreference,
        output: OutputStream
    ) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(BillboardPreference.serializer(), t)
                    .encodeToByteArray(),
            )
        }
    }
    const val BIllBOARD_PREFERENCE_PATH = "billboard_preference"
}