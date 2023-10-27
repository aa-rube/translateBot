package app.bot.service;

import app.bot.groupInfo.SendChatData;
import app.bot.repository.SendToChatDataInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendToChatDataService {
    @Autowired
    private SendToChatDataInfoRepository repository;

    public boolean findByChatIdToSendNews(Long chatId) {
        return repository.findById(chatId).isPresent();
    }

    public void save(SendChatData key) {
        repository.save(key);
    }

    public List<SendChatData> getAllRecords() {
        return repository.findAll();
    }
}
