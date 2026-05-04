package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.MsgMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MsgMessageMapper extends BaseMapper<MsgMessage> {

    /** 查询某会话的聊天记录（按时间升序） */
    @Select("SELECT * FROM msg_message WHERE conversation_key = #{convKey} ORDER BY create_time ASC")
    List<MsgMessage> selectByConversation(@Param("convKey") String convKey);

    /** 标记某会话中发给我的消息为已读 */
    @Update("UPDATE msg_message SET is_read = 1 WHERE conversation_key = #{convKey} AND to_user_id = #{userId} AND is_read = 0")
    int markRead(@Param("convKey") String convKey, @Param("userId") Long userId);

    /** 查询我的未读消息总数 */
    @Select("SELECT COUNT(*) FROM msg_message WHERE to_user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);
}
