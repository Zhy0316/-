package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息实体（对应 msg_message 表）
 */
@Data
@TableName("msg_message")
public class MsgMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者ID */
    private Long fromUserId;

    /** 接收者ID */
    private Long toUserId;

    /** 消息内容 */
    private String content;

    /** 是否已读：0未读 1已读 */
    private Integer isRead;

    /** 会话唯一键：min(fromId,toId)_max(fromId,toId) */
    private String conversationKey;

    /** 消息类型：NORMAL普通消息，SYSTEM系统通知 */
    private String type;

    /** 关联ID（绑定申请ID或获奖申请ID） */
    private Long relatedId;

    /** 发送时间 */
    private LocalDateTime createTime;
}
