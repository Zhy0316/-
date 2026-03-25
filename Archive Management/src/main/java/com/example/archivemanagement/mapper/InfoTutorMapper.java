package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.InfoTutor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InfoTutorMapper extends BaseMapper<InfoTutor> {
    InfoTutor selectByUserId(Long userId);
}
