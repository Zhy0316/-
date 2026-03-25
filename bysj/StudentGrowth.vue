<template>
  <div class="student-growth">
    <h3>1.3 成长记录与作品展示</h3>
    
    <div class="tabs">
      <div class="tab active">成长日志</div>
      <div class="tab">作品集</div>
      <div class="tab">实习经历</div>
    </div>

    <div class="content-area">
      <!-- 成长日志部分 -->
      <div class="section-card">
        <div class="card-header">
          <h4>新建成长日志</h4>
        </div>
        <div class="journal-editor">
          <input type="text" class="journal-title" placeholder="请输入日志标题" />
          <div class="editor-toolbar">
            <button>B</button>
            <button>I</button>
            <button>U</button>
            <button>插入图片</button>
          </div>
          <textarea class="journal-content" placeholder="记录今天的学习感悟..."></textarea>
          <div class="journal-actions">
            <button class="submit-btn">发布日志</button>
            <button class="draft-btn">存草稿</button>
          </div>
        </div>
      </div>

      <div class="section-card">
        <h4>历史日志</h4>
        <div class="timeline">
          <div class="timeline-item">
            <div class="date">2025-12-10</div>
            <div class="content">
              <h5>参加校级编程马拉松</h5>
              <p>今天和队友一起完成了24小时的开发挑战，虽然很累但是收获满满...</p>
              <div class="tags">
                <span>#竞赛</span>
                <span>#团队合作</span>
              </div>
            </div>
          </div>
          <div class="timeline-item">
            <div class="date">2025-11-28</div>
            <div class="content">
              <h5>阅读《深入理解计算机系统》</h5>
              <p>终于读完了第三章，对内存管理有了更深的理解。</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 作品集部分 -->
      <div class="section-card">
        <div class="card-header">
          <h4>我的作品集</h4>
          <button class="upload-btn">+ 上传新作品</button>
        </div>
        <div class="portfolio-grid">
          <div class="portfolio-item">
            <div class="portfolio-thumb">
              <div class="file-type">PDF</div>
            </div>
            <div class="portfolio-info">
              <h5>个人网页设计稿</h5>
              <p>2025-10-15 上传</p>
              <div class="actions">
                <a href="#">预览</a>
                <a href="#">删除</a>
              </div>
            </div>
          </div>
          <div class="portfolio-item">
            <div class="portfolio-thumb video">
              <div class="file-type">MP4</div>
            </div>
            <div class="portfolio-info">
              <h5>项目演示视频</h5>
              <p>2025-09-20 上传</p>
              <div class="actions">
                <a href="#">预览</a>
                <a href="#">删除</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';

@Component
export default class StudentGrowth extends Vue {}
</script>

<style scoped>
.student-growth {
  max-width: 1000px;
  margin: 0 auto;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #ddd;
  margin-bottom: 20px;
}

.tab {
  padding: 10px 20px;
  cursor: pointer;
  color: #666;
  border-bottom: 2px solid transparent;
}

.tab.active {
  color: #3498db;
  border-bottom-color: #3498db;
  font-weight: bold;
}

.section-card {
  background: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

h4 {
  margin: 0;
  color: #2c3e50;
}

.journal-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.journal-title {
  padding: 10px;
  font-size: 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.editor-toolbar {
  background: #f8f9fa;
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
  display: flex;
  gap: 5px;
}

.editor-toolbar button {
  background: white;
  border: 1px solid #ddd;
  padding: 2px 8px;
  cursor: pointer;
  border-radius: 3px;
}

.journal-content {
  height: 150px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
}

.journal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.submit-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.draft-btn {
  background-color: #95a5a6;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.timeline {
  border-left: 2px solid #e0e0e0;
  padding-left: 20px;
  margin-left: 10px;
}

.timeline-item {
  position: relative;
  margin-bottom: 25px;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: -26px;
  top: 5px;
  width: 10px;
  height: 10px;
  background: #3498db;
  border-radius: 50%;
}

.date {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.timeline-item h5 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.timeline-item p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.tags {
  margin-top: 5px;
}

.tags span {
  background: #f0f2f5;
  color: #666;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 5px;
}

.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.portfolio-item {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}

.portfolio-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.portfolio-thumb {
  height: 120px;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
}

.portfolio-thumb.video {
  background-color: #333;
  color: white;
}

.file-type {
  font-weight: bold;
  font-size: 24px;
}

.portfolio-info {
  padding: 15px;
}

.portfolio-info h5 {
  margin: 0 0 5px 0;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.portfolio-info p {
  margin: 0 0 10px 0;
  font-size: 12px;
  color: #999;
}

.portfolio-info .actions {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.portfolio-info a {
  color: #3498db;
  text-decoration: none;
}

.upload-btn {
  background-color: #2ecc71;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}
</style>
