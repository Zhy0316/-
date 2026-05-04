package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.MsgMessage;
import com.example.archivemanagement.service.TodoMessageService;
import com.example.archivemanagement.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
public class TodoMessageController {

    @Autowired
    private TodoMessageService todoMessageService;

    /**
     * 获取用户的系统通知列表
     */
    @GetMapping("/list/{userId}")
    public Result<List<MsgMessage>> getSystemMessages(@PathVariable Long userId) {
        return Result.ok(todoMessageService.getSystemMessages(userId));
    }

    /**
     * 获取未读系统通知数量
     */
    @GetMapping("/unread/{userId}")
    public Result<Integer> getUnreadCount(@PathVariable Long userId) {
        return Result.ok(todoMessageService.getUnreadSystemCount(userId));
    }

    /**
     * 标记系统通知为已读
     */
    @PutMapping("/read/{messageId}")
    public Result<Void> markAsRead(@PathVariable Long messageId) {
        todoMessageService.markAsRead(messageId);
        return Result.ok();
    }

    /**
     * 标记所有系统通知为已读
     */
    @PutMapping("/read-all/{userId}")
    public Result<Void> markAllAsRead(@PathVariable Long userId) {
        todoMessageService.markAllAsRead(userId);
        return Result.ok();
    }

    /**
     * 删除单条系统通知
     */
    @DeleteMapping("/delete/{messageId}")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        todoMessageService.deleteMessage(messageId);
        return Result.ok();
    }

    /**
     * 批量删除系统通知
     */
    @DeleteMapping("/delete-batch/{userId}")
    public Result<Void> deleteBatch(@PathVariable Long userId) {
        todoMessageService.deleteAllByUserId(userId);
        return Result.ok();
    }
}

