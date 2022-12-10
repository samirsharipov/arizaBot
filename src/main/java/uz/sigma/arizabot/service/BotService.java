package uz.sigma.arizabot.service;


import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.sigma.arizabot.entity.TgUser;
import uz.sigma.arizabot.repo.DB;
import uz.sigma.arizabot.state.BotState;

import java.util.ArrayList;
import java.util.List;

public class BotService {


    public static String getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
        return "";
    }

    public static SendMessage start(Update update) {
        String chatId = getChatId(update);
        TgUser user = getOrCreateTgUser(chatId);


        return showMenu(user);
    }

    private static SendMessage showMenu(TgUser user) {
        if (user.isAdmin()) {
            adminStatistics(user);
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Ism familyangizni kiriting*:");
        user.setState(BotState.FIO);
        saveUserChanges(user);
        return sendMessage;
    }

    public static void saveUserChanges(TgUser changedUser) {
        for (TgUser user : DB.tgUsers) {
            if (user.getChatId().equals(changedUser.getChatId())) {
                user = changedUser;
            }
        }
    }


    public static TgUser getOrCreateTgUser(String chatId) {

        for (TgUser user : DB.tgUsers) {
            if (user.getChatId().equals(chatId)) {
                return user;
            }
        }
        TgUser user = new TgUser(chatId, BotState.START);
        DB.tgUsers.add(user);
        return user;
    }

    public static SendMessage fio(Update update, String fio) {
        String chatId = getChatId(update);
        TgUser user = getOrCreateTgUser(chatId);
        user.setFio(fio);
        user.setState(BotState.AGE);
        saveUserChanges(user);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Yoshingizni kiriting:*");
        return sendMessage;
    }

    public static SendMessage age(Update update, String age) {
        String chatId = getChatId(update);
        TgUser user = getOrCreateTgUser(chatId);
        user.setAge(age);
        user.setState(BotState.WORKING_OR_STUDY);
        saveUserChanges(user);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Ish yoki O'qish joyingizni kiriting:*");
        return sendMessage;
    }

    public static SendMessage working(Update update) {
        String chatId = getChatId(update);
        TgUser user = getOrCreateTgUser(chatId);
        user.setWork(update.getMessage().getText());
        user.setState(BotState.PHONE_NUMBER);
        saveUserChanges(user);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Tel raqamingizni kiriting yoki Jo'natish tugmasini bosing:*");
        sendMessage.setReplyMarkup(generateReplyKeyboardMarkup(user));
        return sendMessage;
    }

    private static ReplyKeyboardMarkup generateReplyKeyboardMarkup(TgUser tgUser) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(false);
        KeyboardButton button = new KeyboardButton();
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();


        if (tgUser.getState().equals(BotState.PHONE_NUMBER)) {
            button.setText("Jo'natish");
            button.setRequestContact(true);
            keyboardRow.add(button);
            keyboardRowList.add(keyboardRow);
            markup.setKeyboard(keyboardRowList);
        } else if (tgUser.getState().equals(BotState.ADMIN)) {
            button.setText("Ma'lumotlar");
            keyboardRow.add(button);
            keyboardRowList.add(keyboardRow);
            markup.setKeyboard(keyboardRowList);
        }
        return markup;

    }

    public static SendMessage getContact(Contact contact, Update update, TgUser tgUser) {
        String chatId = getChatId(update);
        TgUser user = getOrCreateTgUser(chatId);
        String number = checkPhoneNumber(contact.getPhoneNumber());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("MarkdownV2");

        user.setPhoneNumber(number);
        if (user.getPhoneNumber().equals("+998911979798")) {
            user.setState(BotState.ADMIN);
            user.setAdmin(true);
            sendMessage.setText("*Hush kelibsiz Admin botni qayta ishga tushiring*");
            sendMessage.setChatId(tgUser.getChatId());
        } else {
            user.setState(BotState.SEND);
            sendMessage.setText("*Arizangiz qabul qilindi*");
            sendMessage.setChatId(tgUser.getChatId());
            user.setBlock(true);
        }
        saveUserChanges(user);


        return sendMessage;
    }


    public static String checkPhoneNumber(String phoneNumber) {
        return phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
    }

    public static SendMessage sendMessageAdmin(TgUser tgUser) {

        SendMessage sendMessage = new SendMessage();

        for (TgUser tgUser1 : DB.tgUsers) {
            if (tgUser1.getPhoneNumber().equals("+998911979798")) {
                sendMessage.setChatId(tgUser1.getChatId());
                sendMessage.setText("FIO: " + tgUser.getFio() + " \n"
                        + "Yosh: " + tgUser.getAge() + " \n"
                        + "Ish yoki O'qish joy: " + tgUser.getWork() + " \n"
                        + "Telefon raqam: " + tgUser.getPhoneNumber());
            }
        }

        return sendMessage;
    }

    public static SendMessage adminStatistics(TgUser tgUser) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Qo'llanma foydalanuvchilar ro'yhatini ko'rish uchun 'Ma'lumotlar' tugmasini bosing*");
        sendMessage.setReplyMarkup(generateReplyKeyboardMarkup(tgUser));
        return sendMessage;
    }

    public static SendMessage statistics(TgUser tgUser) {
        int size = DB.tgUsers.size();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Foydalanuvchilar soni:* " + (size - 1) + " *ta*");
        return sendMessage;
    }

    public static SendMessage getContact(Update update, String number) {
        String chatId = getChatId(update);
        TgUser user = getOrCreateTgUser(chatId);


        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setChatId(user.getChatId());

        boolean b = number.startsWith("+");

        if (b) {
            number = number.substring(1, number.length() - 1);
        }
        boolean isNumeric = number.chars().allMatch(Character::isDigit);

        if (isNumeric) {
            user.setPhoneNumber(number);
            if (user.getPhoneNumber().equals("+998911979798")) {
                user.setState(BotState.ADMIN);
                user.setAdmin(true);
                sendMessage.setText("*Hush kelibsiz Admin botni qayta ishga tushiring*");
            } else {
                user.setState(BotState.SEND);
                sendMessage.setText("*Arizangiz qabul qilindi*");
                user.setBlock(true);
            }
        } else {
            user.setState(BotState.PHONE_NUMBER);
            sendMessage.setText("*Iltimos telefon raqamini to'gri kiriting*");
        }
        saveUserChanges(user);

        return sendMessage;
    }

    public static SendMessage joinChannel(Update update) {
        String chatId = getChatId(update);
        TgUser user = getOrCreateTgUser(chatId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("\uD83D\uDCCE *Salom botdan foydalanish uchun kanalga a'zo bo'ling\n *" +
                "\n ❗️ *Kanallarga a'zo bo'lgach, pastdagi* \"\uD83D\uDD14 *A'zo bo'ldim*\" *tugmasini bosing*");
        sendMessage.setReplyMarkup(generateMarkup());
        return sendMessage;
    }

    private static ReplyKeyboard generateMarkup() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Jizzax yoshlari / rasmiy kanal \uD83C\uDDFA\uD83C\uDDFF");
        button.setUrl("https://t.me/mychannelforbotapplication");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("\uD83D\uDD14 A'zo bo'ldim");
        button1.setCallbackData("join");
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(button1);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row1);


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
}
