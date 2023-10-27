package app.bot.repository;

import app.bot.groupInfo.ListenChatData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupChatInfoRepository extends JpaRepository<ListenChatData, Long> {
}
