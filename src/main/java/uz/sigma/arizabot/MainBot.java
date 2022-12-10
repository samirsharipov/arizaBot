package uz.sigma.arizabot;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import uz.sigma.arizabot.entity.TgUser;
import uz.sigma.arizabot.service.BotService;
import uz.sigma.arizabot.state.BotState;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MainBot extends SpringWebhookBot {



    String botUsername;
    String botToken;
    String botPath;

    @SneakyThrows
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {

        if (update.hasMessage()) {
            TgUser tgUser = BotService.getOrCreateTgUser(update.getMessage().getChatId().toString());

            String text = update.getMessage().getText();


            if (update.getMessage().hasContact()) {
                if (tgUser.getState().equals(BotState.PHONE_NUMBER)) {
                    execute(BotService.getContact(update.getMessage().getContact(), update, tgUser));
                }
            }

            if (text != null && text.equals("/start")) {

                GetChatMember getChatMember = new GetChatMember();
                getChatMember.setChatId("-1001760365278");
                getChatMember.setUserId(update.getMessage().getChatId());
                ChatMember chatMember = execute(getChatMember);

                if (chatMember.getStatus().equals("creator") || chatMember.getStatus().equals("administrator") || chatMember.getStatus().equals("member")) {
                    if (tgUser.getPhoneNumber() != null && tgUser.isAdmin()) {
                        execute(BotService.adminStatistics(tgUser));
                    } else if (tgUser.isBlock() && tgUser.getState().equals(BotState.SEND)) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setText("✅ *Siz allaqachon ro'yxatdan o'tib bo'ldingiz*");
                        sendMessage.setChatId(tgUser.getChatId());
                        sendMessage.setParseMode("MarkdownV2");
                        execute(sendMessage);
                    } else {
                        execute(new SendMessage(tgUser.getChatId(), "Assalomu aleykum"));
                        execute(BotService.start(update));
                    }
                } else {
                    execute(BotService.joinChannel(update));
                }


            } else if (text != null && text.equals("Ma'lumotlar")) {
                execute(BotService.statistics(tgUser));
            } else if (tgUser.getState().equals(BotState.FIO)) {
                String fio = update.getMessage().getText();
                execute(BotService.fio(update, fio));
            } else if (tgUser.getState().equals(BotState.AGE)) {
                String age = update.getMessage().getText();
                execute(BotService.age(update, age));
            } else if (tgUser.getState().equals(BotState.WORKING_OR_STUDY)) {
                execute(BotService.working(update));
            } else if (tgUser.getState().equals(BotState.PHONE_NUMBER)) {
                String number = update.getMessage().getText();
                execute(BotService.getContact(update, number));
            }else if (tgUser.getState().equals(BotState.SEND)){
                execute(BotService.sendMessageAdmin(tgUser));
            }
        }
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();

            if (data.equals("join")) {
                TgUser tgUser = BotService.getOrCreateTgUser(update.getCallbackQuery().getFrom().getId().toString());

                GetChatMember getChatMember = new GetChatMember();
                getChatMember.setChatId("-1001760365278");
                getChatMember.setUserId(update.getCallbackQuery().getFrom().getId());
                ChatMember chatMember = execute(getChatMember);

                if (!chatMember.getStatus().equals("member")) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("❗️*Siz hali ham kanalga a'zo bo'lmadingiz*");
                    sendMessage.setChatId(tgUser.getChatId());
                    sendMessage.setParseMode("MarkdownV2");
                    execute(sendMessage);
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("✅*Siz kanalga a'zo bo'libsiz*");
                    sendMessage.setChatId(tgUser.getChatId());
                    sendMessage.setParseMode("MarkdownV2");
                    execute(sendMessage);
                    execute(BotService.start(update));
                }
            }
        }

        return null;
    }

    public MainBot(SetWebhook setWebhook) {
        super(setWebhook);
    }

}
