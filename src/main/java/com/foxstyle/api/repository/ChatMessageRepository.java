package com.foxstyle.api.repository;

import com.foxstyle.api.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByOrderBySentAtAsc();
    List<ChatMessage> findByChannelIdOrderBySentAtAsc(String channelId);
}
