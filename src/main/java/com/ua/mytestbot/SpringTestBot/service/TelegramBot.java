package com.ua.mytestbot.SpringTestBot.service;

import com.ua.mytestbot.SpringTestBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    public static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";

    private final BotConfig botConfig;
    private final List<BotCommand> listOfCommand = Arrays.asList(new BotCommand("/start", "get a welcome message"),
            new BotCommand("/mydata", "get your data stored"),
            new BotCommand("/deletedata", "delete my data"),
            new BotCommand("/help", "info how to use this bot"),
            new BotCommand("/settings", "set your preferences")
            );

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;

        try {
            this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

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

                case "/help":
                    sendMessage(chatId, HELP_TEXT);
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
