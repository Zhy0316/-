package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.LearnResource;
import com.example.archivemanagement.mapper.LearnResourceMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class LearnResourceService extends ServiceImpl<LearnResourceMapper, LearnResource> {

    public Page<Map<String, Object>> listPage(int pageNum, int pageSize,
                                               String category, String keyword) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return (Page<Map<String, Object>>) baseMapper.selectPageWithUploader(page, category, keyword);
    }

    public LearnResource publish(Long uploaderId, String uploaderRole, LearnResource resource) {
        resource.setUploaderId(uploaderId);
        resource.setUploaderRole(uploaderRole);
        resource.setViewCount(0);
        resource.setLikeCount(0);
        resource.setCreateTime(LocalDateTime.now());
        save(resource);
        return resource;
    }

    public boolean like(Long id) {
        return baseMapper.incrementLike(id) > 0;
    }

    public void incrementView(Long id) {
        baseMapper.incrementView(id);
    }

    public boolean canDelete(Long id, Long userId, String roleKey) {
        LearnResource r = getById(id);
        if (r == null) return false;
        return "admin".equals(roleKey) || r.getUploaderId().equals(userId);
    }
}
