package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.ChatMessage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends BaseMapper<ChatMessage> {
    default List<ChatMessage> findChatHistory(Long user1Id, Long user2Id) {
        return selectList(new LambdaQueryWrapper<ChatMessage>()
                .and(w -> w.eq(ChatMessage::getSenderId, user1Id).eq(ChatMessage::getReceiverId, user2Id))
                .or(w -> w.eq(ChatMessage::getSenderId, user2Id).eq(ChatMessage::getReceiverId, user1Id))
                .orderByAsc(ChatMessage::getTimestamp));
    }
}
