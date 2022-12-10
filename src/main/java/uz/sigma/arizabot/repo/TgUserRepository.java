package uz.sigma.arizabot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uz.sigma.arizabot.entity.TgUser;

import java.util.Optional;

@EnableJpaRepositories
public interface TgUserRepository extends JpaRepository<TgUser,Long> {
    Optional<TgUser> findByChatId(String chatId);
    Optional<TgUser> findByPhoneNumber(String phoneNumber);
}
