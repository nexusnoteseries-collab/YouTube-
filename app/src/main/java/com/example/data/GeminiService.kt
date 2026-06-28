package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    /**
     * Checks if the API key is set and is valid (not placeholder).
     */
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.contains("PLACEHOLDER")
    }

    /**
     * Generates a summary or answers a question about a video.
     */
    suspend fun getAiResponse(video: Video, userPrompt: String? = null): String {
        if (!isApiKeyConfigured()) {
            return generateMockResponse(video, userPrompt)
        }

        val promptText = if (userPrompt != null) {
            "User wants to know: \"$userPrompt\" in relation to the video \"${video.title}\". " +
                    "Video description: \"${video.description}\". Please answer their question concisely."
        } else {
            "Generate a structured, elegant summary of the video \"${video.title}\". " +
                    "Use bullet points to highlight key takeaways. Here is the video description: \"${video.description}\""
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = promptText)))
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = "You are a helpful, professional AI Video Assistant for the VidTube app. " +
                        "Keep responses structured, engaging, and in Indonesian (Bahasa Indonesia) or the language of the prompt."))
            )
        )

        return try {
            val response = api.generateContent(BuildConfig.GEMINI_API_KEY, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Asisten AI tidak dapat merespons saat ini. Silakan coba lagi nanti."
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed", e)
            // Gracefully fall back to local simulated generation if network fails
            generateMockResponse(video, userPrompt) + "\n\n*(Catatan: Mode offline diaktifkan karena gangguan koneksi)*"
        }
    }

    /**
     * Context-aware, highly realistic local generation as a fallback or for offline testing.
     */
    private fun generateMockResponse(video: Video, userPrompt: String?): String {
        if (userPrompt != null) {
            val promptLower = userPrompt.lowercase()
            return when {
                promptLower.contains("bunny") || promptLower.contains("kelinci") -> {
                    "Karakter utama, Big Buck Bunny, melambangkan kedamaian alam yang terganggu oleh keisengan hewan-hewan kecil (tupai Frank, dkk). Video ini dibuat untuk menunjukkan kapabilitas mesin 3D Blender dalam memproses tekstur bulu lembut dan simulasi benturan fisik."
                }
                promptLower.contains("dragon") || promptLower.contains("naga") || promptLower.contains("sintel") -> {
                    "Perjalanan Sintel adalah pencarian emosional yang penuh rintangan untuk menyelamatkan naga kesayangannya, Scales. Film ini mengajarkan bahwa persahabatan sejati tidak mengenal batas ruang dan waktu."
                }
                promptLower.contains("amsterdam") || promptLower.contains("robot") || promptLower.contains("steel") -> {
                    "Teknologi VFX yang ditonjolkan dalam film ini meliputi camera tracking 3D, motion capture aktor nyata, dan integrasi robot CGI ke lingkungan kota Amsterdam secara mulus."
                }
                promptLower.contains("relaksasi") || promptLower.contains("camp") || promptLower.contains("api") -> {
                    "Video api unggun di hutan ini direkam untuk memberikan sensasi relaksasi mendalam. Suara kayu yang berderak dikombinasikan dengan visual api hangat dapat membantu menurunkan tingkat stres dan mempermudah tidur."
                }
                else -> {
                    "Berdasarkan detail video \"${video.title}\", topik ini membahas tema ${video.category}. Saluran ${video.channelName} menyajikan konten ini secara visual yang menarik untuk memberikan wawasan mendalam tentang ${video.category}."
                }
            }
        } else {
            // General summary
            return """
                ### 📺 Ringkasan AI untuk "${video.title}"
                
                *   **Tema Utama:** Mengangkat topik bertema **${video.category}** yang dikemas secara estetik oleh **${video.channelName}**.
                *   **Sorotan Video:**
                    *   Menyajikan visual resolusi tinggi dengan sinematografi modern.
                    *   Dukungan audio atmosferik yang memperkuat cerita dan visualisasi.
                    *   Menampilkan interaksi mendalam antar elemen atau lingkungan di sekitarnya.
                *   **Kenapa Harus Menonton?**
                    *   Video berdurasi **${video.duration}** ini telah ditonton sebanyak **${video.views}** karena menyajikan representasi kreatif terbaik di kelasnya.
                    *   Memberikan inspirasi artistik, pengetahuan baru, atau efek relaksasi yang luar biasa sesuai dengan tujuan audiens.
                
                *(Konfigurasikan kunci Gemini API Anda di panel Secrets untuk menikmati chat asisten dinamis!)*
            """.trimIndent()
        }
    }
}
