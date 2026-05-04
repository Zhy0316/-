package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.MsgMessage;
import com.example.archivemanagement.service.MsgMessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MsgMessageService msgMessageService;

    /** 发送消息 POST /api/message/send */
    @PostMapping("/send")
    public Result<MsgMessage> send(@RequestBody Map<String, Object> body,
                                   HttpServletRequest request) {
        Long fromId = (Long) request.getAttribute("userId");
        Long toId = Long.valueOf(body.get("toUserId").toString());
        String content = body.get("content").toString().trim();
        if (content.isEmpty()) throw new BusinessException("消息内容不能为空");
        return Result.ok(msgMessageService.send(fromId, toId, content));
    }

    /** 获取会话列表 GET /api/message/conversations */
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> conversations(HttpServletRequest request) {
        Long myId = (Long) request.getAttribute("userId");
        return Result.ok(msgMessageService.getConversations(myId));
    }

    /** 获取与某人的聊天记录 GET /api/message/history?otherId=xxx */
    @GetMapping("/history")
    public Result<List<MsgMessage>> history(@RequestParam Long otherId,
                                            HttpServletRequest request) {
        Long myId = (Long) request.getAttribute("userId");
        // 标记已读
        msgMessageService.markRead(myId, otherId);
        return Result.ok(msgMessageService.getHistory(myId, otherId));
    }

    /** 标记已读 PUT /api/message/read?otherId=xxx */
    @PutMapping("/read")
    public Result<Void> markRead(@RequestParam Long otherId,
                                 HttpServletRequest request) {
        Long myId = (Long) request.getAttribute("userId");
        msgMessageService.markRead(myId, otherId);
        return Result.ok("已标记已读");
    }

    /** 未读消息总数 GET /api/message/unread-count */
    @GetMapping("/unread-count")
    public Result<Integer> unreadCount(HttpServletRequest request) {
        Long myId = (Long) request.getAttribute("userId");
        return Result.ok(msgMessageService.countUnread(myId));
    }
}
