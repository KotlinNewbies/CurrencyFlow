package com.fluida.currencyflow.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = BigDecimal::class)
object KonwerterLiczbDziesietnych : KSerializer<BigDecimal> {
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            // Obsługuje formaty: "1.23" oraz 1.23
            BigDecimal(element.jsonPrimitive.content)
        } else {
            BigDecimal(decoder.decodeString())
        }
    }
}
