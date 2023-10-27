package app.bot.groupInfo;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ListenChatData {
    @Id
    @Column(name = "group_to_listen")
    private Long listenTheGroup;
    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "1_group_to_send")
    private Long sendToTheGroup1;
    @Column(name = "2_group_to_send")
    private Long sendToTheGroup2;
    @Column(name = "3_group_to_send")
    private Long sendToTheGroup3;
    @Column(name = "4_group_to_send")
    private Long sendToTheGroup4;

    public Long getListenTheGroup() {
        return listenTheGroup;
    }

    public void setListenTheGroup(Long listenTheGroup) {
        this.listenTheGroup = listenTheGroup;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Long getSendToTheGroup1() {
        return sendToTheGroup1;
    }

    public void setSendToTheGroup1(Long sendToTheGroup1) {
        this.sendToTheGroup1 = sendToTheGroup1;
    }

    public Long getSendToTheGroup2() {
        return sendToTheGroup2;
    }

    public void setSendToTheGroup2(Long sendToTheGroup2) {
        this.sendToTheGroup2 = sendToTheGroup2;
    }

    public Long getSendToTheGroup3() {
        return sendToTheGroup3;
    }

    public void setSendToTheGroup3(Long sendToTheGroup3) {
        this.sendToTheGroup3 = sendToTheGroup3;
    }

    public Long getSendToTheGroup4() {
        return sendToTheGroup4;
    }

    public void setSendToTheGroup4(Long sendToTheGroup4) {
        this.sendToTheGroup4 = sendToTheGroup4;
    }

    public List<Long> getAllChatIdToSend() {
        List<Long> list = new ArrayList<>();
        list.add(sendToTheGroup1);
        list.add(sendToTheGroup2);
        list.add(sendToTheGroup3);
        list.add(sendToTheGroup4);
        return list;
    }
}
