package com.ua.mytestbot.SpringTestBot.service;

import com.ua.mytestbot.SpringTestBot.config.BotConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@AllArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;

                default:
                    String answer = "Sorry, command wasn't recognized!";
                    sendMessage(chatId, answer);
            }
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hello, " + name + " How can i help you?";
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessageField = new SendMessage();
        sendMessageField.setChatId(String.valueOf(chatId));
        sendMessageField.setText(textToSend);

        try {
            execute(sendMessageField);
        } catch (TelegramApiException e) {
           log.error("Error occurred: " + e.getMessage());
        }
    }
}
