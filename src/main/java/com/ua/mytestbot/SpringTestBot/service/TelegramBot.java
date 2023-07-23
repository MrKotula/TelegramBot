package com.ua.mytestbot.SpringTestBot.service;

import com.ua.mytestbot.SpringTestBot.config.BotConfig;
import com.ua.mytestbot.SpringTestBot.model.User;
import com.ua.mytestbot.SpringTestBot.model.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private static final String YES_BUTTON = "YES_BUTTON";
    private static final String NO_BUTTON = "NO_BUTTON";
    public static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";

    private final BotConfig botConfig;

    @Autowired
    private UserRepository userRepository;
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

                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;

                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;

                case "/register":
                    register(chatId);
                    break;

                default:
                    String answer = "Sorry, command wasn't recognized!";
                    sendMessage(chatId, answer);
            }
        }
        callbackAfterForButtons(update);
    }

    private void callbackAfterForButtons(Update update) {
         if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals(YES_BUTTON)) {
                String text = "You pressed YES button";
                EditMessageText editMessageText = new EditMessageText();

                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setMessageId(Long.valueOf(messageId).intValue());
                editMessageText.setText(text);

                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    log.error("Error occurred: " + e.getMessage());
                }

            } else if (callbackData.equals(NO_BUTTON)) {
                String text = "You pressed NO button";
                EditMessageText editMessageText = new EditMessageText();

                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setMessageId(Long.valueOf(messageId).intValue());
                editMessageText.setText(text);

                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    log.error("Error occurred: " + e.getMessage());
                }
            }
        }
    }

    private void register(long chatId) {
        SendMessage sendMessageField = new SendMessage();
        sendMessageField.setChatId(String.valueOf(chatId));
        sendMessageField.setText("Do you really want to register?");

        sendMessageField.setReplyMarkup(createInlineKeyboardMurkup());

        try {
            execute(sendMessageField);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void registerUser(Message message) {
        if(userRepository.findById(message.getChatId()).isEmpty()){
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User(chatId, chat.getFirstName(), chat.getLastName(), chat.getUserName(), new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hello, " + name + " How can i help you?" + " :relaxed:");
        //String answer = "Hello, " + name + " How can i help you?";
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessageField = new SendMessage();
        sendMessageField.setChatId(String.valueOf(chatId));
        sendMessageField.setText(textToSend);

        sendMessageField.setReplyMarkup(createKeyboardMarkup());

        try {
            execute(sendMessageField);
        } catch (TelegramApiException e) {
           log.error("Error occurred: " + e.getMessage());
        }
    }

    private ReplyKeyboardMarkup createKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get random joke");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("/register");
        row.add("check my data");
        row.add("delete my data");
        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup createInlineKeyboardMurkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInline.add(yesButton);
        rowInline.add(noButton);

        rowsInline.add(rowInline);

        inlineKeyboardMarkup.setKeyboard(rowsInline);

        return  inlineKeyboardMarkup;
    }
}
