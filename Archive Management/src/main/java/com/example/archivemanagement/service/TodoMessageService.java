package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.archivemanagement.entity.MsgMessage;

import java.util.List;

public interface TodoMessageService extends IService<MsgMessage> {

    /**
     * 发送系统通知（待办消息机器人）
     */
    void sendSystemMessage(Long toUserId, String content, Long relatedId);

    /**
     * 发送绑定申请通知给导师
     */
    void sendRelationApplyNotice(Long tutorId, String studentName, Long relationId);

    /**
     * 发送获奖申请通知给导师
     */
    void sendAwardApplyNotice(Long tutorId, String studentName, String awardName, Long awardId);

    /**
     * 发送绑定申请结果通知给学生
     */
    void sendRelationResultNotice(Long studentId, String tutorName, Integer status);

    /**
     * 发送获奖审核结果通知给学生
     */
    void sendAwardResultNotice(Long studentId, String awardName, Integer auditStatus);

    /**
     * 获取用户的系统通知列表
     */
    List<MsgMessage> getSystemMessages(Long userId);

    /**
     * 获取未读系统通知数量
     */
    int getUnreadSystemCount(Long userId);

    /**
     * 标记系统通知为已读
     */
    void markAsRead(Long messageId);

    /**
     * 标记所有系统通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除单条系统通知
     */
    void deleteMessage(Long messageId);

    /**
     * 删除用户的所有系统通知
     */
    void deleteAllByUserId(Long userId);
}

