package uz.sigma.arizabot.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import uz.sigma.arizabot.MainBot;

@Configuration
@AllArgsConstructor
public class SpringConfig {
    private final TelegramConfig telegramConfig;

    @Bean
    public SetWebhook setWebhook(){
        return SetWebhook.builder().url(telegramConfig.getWebhookPath()).build();
    }



    @Bean
    public MainBot springWebhookBot(SetWebhook setWebhook){
        MainBot bot = new MainBot(setWebhook);

        bot.setBotPath(telegramConfig.getWebhookPath());
        bot.setBotUsername(telegramConfig.getBotName());
        bot.setBotToken(telegramConfig.getBotToken());

        return bot;
    }
}
