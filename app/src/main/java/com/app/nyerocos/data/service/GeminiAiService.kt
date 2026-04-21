package com.app.nyerocos.data.service

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

class GeminiAiService(
    private val systemPrompt: String = DEFAULT_SYSTEM_PROMPT
) {
    private val model = Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).generativeModel(
        modelName = "gemini-2.5-flash",
        systemInstruction = content { text(systemPrompt)}
    )

    suspend fun sendMessageWithHistory(
        history: List<Pair<String, Boolean>>,
        newMessage: String
    ): String {
        return try {
            val chat = model.startChat(
                history = history.map { (text, isUser) ->
                    content(role = if(isUser) "user" else "model") {
                        text(text)
                    }
                }
            )
            val response = chat.sendMessage(newMessage)
            response.text ?: ".."
        } catch (e: Exception) {
            "Error: DI GEMINI SERVICE ${e.message}"
        }
    }
    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """
            You are Nyerocos, a friendly and patient language tutor AI.
            
            RULES:
            1. Detect what language the user is trying to practice.
            2. Respond primarily in that TARGET language.
            3. NEVER judge mistakes. Always be encouraging and supportive.
            4. When the user makes a grammar mistake, gently correct by 
               rephrasing naturally. NEVER say "that's wrong" or "incorrect."
            5. Keep responses SHORT (2-3 sentences) for natural conversation flow.
            6. Ask follow-up questions to keep the conversation going.
            7. Match the user's level — if they speak simple, you speak simple.
            8. If the user seems confused, briefly explain in their language.
            
            CORRECTION STYLE:
            BAD: "That's incorrect. You should say..."
            GOOD: "Nice try! By the way, people usually say '...' in that situation. But I totally understood you!"
            
            PERSONALITY:
            - Warm, encouraging, like a supportive friend
            - Use emoji occasionally to feel friendly
            - Celebrate small wins ("Great sentence structure! 🎉")
        """
    }
}