package app.bot.service;

import app.bot.groupInfo.ListenChatData;
import app.bot.repository.GroupChatInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupChatInfoService {
    @Autowired
    GroupChatInfoRepository groupChatInfoRepository;

    public void save(ListenChatData info) {
        groupChatInfoRepository.save(info);
    }

    public List<ListenChatData> getAllGroups() {
        return groupChatInfoRepository.findAll();
    }

    public void deleteByObject(ListenChatData object) {
        groupChatInfoRepository.delete(object);
    }
}
