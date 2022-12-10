package uz.sigma.arizabot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;


@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class TgUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatId;

    private String username;

    private String fio;

    private String age;

    private String phoneNumber;

    private String state;

    private String work;

    private String application;

    private boolean isBlock;

    private boolean isAdmin;

    public TgUser(String chatId, String state) {
        this.chatId = chatId;
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TgUser tgUser = (TgUser) o;
        return id != null && Objects.equals(id, tgUser.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
