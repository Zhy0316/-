package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.MsgMessage;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.mapper.MsgMessageMapper;
import com.example.archivemanagement.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MsgMessageService extends ServiceImpl<MsgMessageMapper, MsgMessage> {

    private final SysUserMapper sysUserMapper;

    /** 生成会话唯一键 */
    public static String buildConvKey(Long a, Long b) {
        return Math.min(a, b) + "_" + Math.max(a, b);
    }

    /** 发送消息 */
    public MsgMessage send(Long fromId, Long toId, String content) {
        MsgMessage msg = new MsgMessage();
        msg.setFromUserId(fromId);
        msg.setToUserId(toId);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setConversationKey(buildConvKey(fromId, toId));
        msg.setCreateTime(LocalDateTime.now());
        save(msg);
        return msg;
    }

    /** 获取聊天记录 */
    public List<MsgMessage> getHistory(Long myId, Long otherId) {
        String convKey = buildConvKey(myId, otherId);
        return baseMapper.selectByConversation(convKey);
    }

    /** 标记已读 */
    public void markRead(Long myId, Long otherId) {
        String convKey = buildConvKey(myId, otherId);
        baseMapper.markRead(convKey, myId);
    }

    /** 获取会话列表（每个联系人最新一条消息） */
    public List<Map<String, Object>> getConversations(Long myId) {
        // 查询所有与我相关的消息，按会话分组取最新一条
        QueryWrapper<MsgMessage> qw = new QueryWrapper<>();
        qw.eq("from_user_id", myId).or().eq("to_user_id", myId);
        qw.orderByDesc("create_time");
        List<MsgMessage> all = list(qw);

        // 按 conversationKey 去重，保留最新一条
        Map<String, MsgMessage> latestMap = new LinkedHashMap<>();
        for (MsgMessage m : all) {
            latestMap.putIfAbsent(m.getConversationKey(), m);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (MsgMessage latest : latestMap.values()) {
            Long otherId = latest.getFromUserId().equals(myId)
                    ? latest.getToUserId() : latest.getFromUserId();
            SysUser other = sysUserMapper.selectById(otherId);
            if (other == null) continue;

            // 统计未读数
            QueryWrapper<MsgMessage> unreadQw = new QueryWrapper<>();
            unreadQw.eq("conversation_key", latest.getConversationKey())
                    .eq("to_user_id", myId)
                    .eq("is_read", 0);
            long unread = count(unreadQw);

            Map<String, Object> item = new HashMap<>();
            item.put("userId", otherId);
            item.put("realName", other.getRealName());
            item.put("avatar", other.getAvatar());
            item.put("lastMessage", latest.getContent());
            item.put("lastTime", latest.getCreateTime());
            item.put("unread", unread);
            result.add(item);
        }
        return result;
    }

    /** 未读消息总数 */
    public int countUnread(Long userId) {
        return baseMapper.countUnread(userId);
    }
}
