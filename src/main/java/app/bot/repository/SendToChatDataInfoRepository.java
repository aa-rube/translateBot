package app.bot.repository;

import app.bot.groupInfo.SendChatData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SendToChatDataInfoRepository extends JpaRepository<SendChatData, Long>{

}

