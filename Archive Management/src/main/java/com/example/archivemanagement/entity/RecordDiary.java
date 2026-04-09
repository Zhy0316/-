package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("record_diary")
public class RecordDiary {
    private Long id;
    private Long studentId;
    private String title;
    private String content;
    private String mood;
    private String attachmentPath;
    private Date recordDate;
    private Date createTime;
    private Integer visibility;
    /** 星标：0=否，1=是 */
    private Integer isStarred;
    /** 分组/分类名称 */
    private String category;
    /** 标签，多个用逗号分隔 */
    private String tags;
}
