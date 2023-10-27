package app.bot.model;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MediaGroupData {
    private List<String> listPhotoFilesId = new ArrayList<>();
    private List<String> listVideoFilesId = new ArrayList<>();
    private List<String> listDocumentsFilesId = new ArrayList<>();
    private List<String> listAudioFilesId = new ArrayList<>();
    private List<MessageEntity> entities = new ArrayList<>();
    private LocalDateTime lastUpdate;
    private String caption;
    private Long chatIdToSendMsg;
    private String direction;
    private String head;
    String deeplKey;
    public List<String> getListVideoFilesId() {
        return listVideoFilesId;
    }

    public List<String> getListPhotoFilesId() {
        return listPhotoFilesId;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Long getChatIdToSendMsg() {
        return chatIdToSendMsg;
    }

    public void setChatIdToSendMsg(Long chatIdToSendMsg) {
        this.chatIdToSendMsg = chatIdToSendMsg;
    }

    public List<MessageEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<MessageEntity> entities) {
        this.entities = entities;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public List<String> getListDocumentsFilesId() {
        return listDocumentsFilesId;
    }

    public List<String> getListAudioFilesId() {
        return listAudioFilesId;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getDeeplKey() {
        return deeplKey;
    }

    public void setDeeplKey(String deeplKey) {
        this.deeplKey = deeplKey;
    }
}
