package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.MsgMessage;
import com.example.archivemanagement.mapper.MsgMessageMapper;
import com.example.archivemanagement.service.TodoMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TodoMessageServiceImpl extends ServiceImpl<MsgMessageMapper, MsgMessage> implements TodoMessageService {

    /** 待办消息机器人的用户ID */
    private static final Long BOT_USER_ID = 0L;

    @Override
    public void sendSystemMessage(Long toUserId, String content, Long relatedId) {
        MsgMessage message = new MsgMessage();
        message.setFromUserId(BOT_USER_ID);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setIsRead(0);
        message.setType("SYSTEM");
        message.setRelatedId(relatedId);
        message.setCreateTime(LocalDateTime.now());
        // 系统通知的conversationKey格式：system_用户ID
        message.setConversationKey("system_" + toUserId);
        save(message);
    }

    @Override
    public void sendRelationApplyNotice(Long tutorId, String studentName, Long relationId) {
        String content = String.format("【待办消息】学生 %s 向您发起了绑定申请", studentName);
        sendSystemMessage(tutorId, content, relationId);
    }

    @Override
    public void sendAwardApplyNotice(Long tutorId, String studentName, String awardName, Long awardId) {
        String content = String.format("【待办消息】学生 %s 提交了获奖申请：%s", studentName, awardName);
        sendSystemMessage(tutorId, content, awardId);
    }

    @Override
    public void sendRelationResultNotice(Long studentId, String tutorName, Integer status) {
        String result = status == 1 ? "已通过" : "已拒绝";
        String content = String.format("【待办消息】您对 %s 的绑定申请 %s", tutorName, result);
        sendSystemMessage(studentId, content, null);
    }

    @Override
    public void sendAwardResultNotice(Long studentId, String awardName, Integer auditStatus) {
        String result = auditStatus == 1 ? "已通过" : "未通过";
        String content = String.format("【待办消息】您的获奖申请 \"%s\" %s", awardName, result);
        sendSystemMessage(studentId, content, null);
    }

    @Override
    public List<MsgMessage> getSystemMessages(Long userId) {
        LambdaQueryWrapper<MsgMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MsgMessage::getToUserId, userId)
               .eq(MsgMessage::getType, "SYSTEM")
               .orderByDesc(MsgMessage::getCreateTime);
        return list(wrapper);
    }

    @Override
    public int getUnreadSystemCount(Long userId) {
        LambdaQueryWrapper<MsgMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MsgMessage::getToUserId, userId)
               .eq(MsgMessage::getType, "SYSTEM")
               .eq(MsgMessage::getIsRead, 0);
        return Math.toIntExact(count(wrapper));
    }

    @Override
    public void markAsRead(Long messageId) {
        MsgMessage message = new MsgMessage();
        message.setId(messageId);
        message.setIsRead(1);
        updateById(message);
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<MsgMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MsgMessage::getToUserId, userId)
               .eq(MsgMessage::getType, "SYSTEM")
               .eq(MsgMessage::getIsRead, 0);

        MsgMessage updateMsg = new MsgMessage();
        updateMsg.setIsRead(1);
        update(updateMsg, wrapper);
    }

    @Override
    public void deleteMessage(Long messageId) {
        removeById(messageId);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        LambdaQueryWrapper<MsgMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MsgMessage::getToUserId, userId)
               .eq(MsgMessage::getType, "SYSTEM");
        remove(wrapper);
    }
}

