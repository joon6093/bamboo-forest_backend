package org.jungppo.bambooforest.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatBotPurchaseRequest {
    @NotBlank(message = "ChatBot item name cannot be blank")
    @Size(max = 15, message = "ChatBot item name must be less than or equal to 15 characters")
    private String chatBotItemName;
}
