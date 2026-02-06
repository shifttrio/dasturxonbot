package org.mohirlar;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class MyBot extends TelegramLongPollingBot {

    private final UserRepository userRepository = new UserRepository();

    // Web App URL (HTTPS bo‘lishi kerak)
    private static final String WEB_APP_URL = "https://dasturxon-front-production.up.railway.app";

    @Override
    public String getBotUsername() {
        // Bot username @siz ham ishlaydi, lekin odatda @siz yoziladi
        return "@dasturxon_24Bot";
    }

    @Override
    public String getBotToken() {
        // Tokenni kodinga qotirib qo'ymaslik tavsiya
        return "7928952266:AAGZJf0G8AFM5XvhvhJgkRw5MbfLuCkTwu4";
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallback(update);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Update update) throws TelegramApiException {
        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if ("/start".equals(text)) {
            User tgUser = update.getMessage().getFrom();

            long telegramId = tgUser.getId();
            String username = tgUser.getUserName(); // null bo‘lishi mumkin
            String fullName = buildFullName(tgUser.getFirstName(), tgUser.getLastName());

            if (fullName == null || fullName.isBlank()) {
                fullName = "Unknown";
            }

            // ✅ Postgresga saqlash (upsert)
            userRepository.upsertUser(telegramId, username, fullName);

            String display = fullName.equals("Unknown") ? "do'stim" : fullName;

            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText("Assalomu aleykum, " + display + "!");
            msg.setReplyMarkup(menuKeyboardWebApp()); // ✅ web app tugma

            execute(msg);
        }
    }

    private void handleCallback(Update update) throws TelegramApiException {
        String data = update.getCallbackQuery().getData();
        String callbackId = update.getCallbackQuery().getId();

        // Hozircha misol: keyin boshqa callback tugmalar qo‘shsang ishlaydi
        if ("SOME_CALLBACK".equals(data)) {
            AnswerCallbackQuery ack = new AnswerCallbackQuery();
            ack.setCallbackQueryId(callbackId);
            ack.setText("Callback ishladi 🙂");
            ack.setShowAlert(false);
            execute(ack);
        }
    }

    /**
     * ✅ Telegram ichida Web App (mini app) sifatida ochadigan inline tugma.
     * Eslatma: URL HTTPS bo‘lishi kerak.
     */
    private InlineKeyboardMarkup menuKeyboardWebApp() {

        // 1) WebAppInfo
        WebAppInfo webAppInfo = new WebAppInfo();
        webAppInfo.setUrl(WEB_APP_URL);

        // 2) Tugma
        InlineKeyboardButton menuButton = new InlineKeyboardButton();
        menuButton.setText("Menu");
        menuButton.setWebApp(webAppInfo); // ✅ web app

        // setCallbackData() qo‘ymaymiz! (web app tugmada callback bo‘lmaydi)
        // setUrl() ham qo‘ymaymiz! (web app uchun alohida setWebApp ishlatiladi)

        // 3) Qator
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(menuButton);

        // 4) Keyboard
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        return markup;
    }

    private String buildFullName(String firstName, String lastName) {
        String f = firstName == null ? "" : firstName.trim();
        String l = lastName == null ? "" : lastName.trim();
        String full = (f + " " + l).trim();
        return full.isBlank() ? null : full;
    }
}
