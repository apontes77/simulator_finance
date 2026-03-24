package org.example

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class BcbService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    //CÓDIGO DA SELIC NO sgs: 11 (selic acumulada no mês) ou 432 (selic meta)
    private val SELIC_META_CODE = 432

    suspend fun fetchCurrentSelicRate(): Double {
        return try {
            val response: List<BcbRate> = client.get("https://api.bcb.gov.br/dados/serie/bcdata.sgs.$SELIC_META_CODE/dados/ultimos/1?formato=json").body()
            response.firstOrNull()?.valor?.toDouble() ?: 11.25 //fallback caso falhe
        } catch (e: Exception) {
            println("falha ao conectar com o BC: ${e.message}. Usando taxa padrao.")
            11.25
        } finally {
            // Em produção, você manteria o cliente vivo, mas aqui podemos fechá-lo se quiser
            // client.close()
        }
    }
}