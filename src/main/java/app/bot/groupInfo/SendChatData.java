package app.bot.groupInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SendChatData {
    @Id
    @Column(name = "chat_id_to_send_msg")
    Long sendToChatId;
    @Column(name = "deepl_api_key")
    String key;
    @Column(name = "translate_direction")
    String direction;

    public Long getSendToChatId() {
        return sendToChatId;
    }

    public void setSendToChatId(Long sendToChatId) {
        this.sendToChatId = sendToChatId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
