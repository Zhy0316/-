<template>
  <div class="student-growth">
    <h3>成长记录与作品展示</h3>
    
    <div class="tabs">
      <div class="tab" :class="{ active: activeTab === 'diary' }" @click="activeTab = 'diary'">成长日志</div>
      <div class="tab" :class="{ active: activeTab === 'portfolio' }" @click="activeTab = 'portfolio'">作品集</div>
      <div class="tab" :class="{ active: activeTab === 'practice' }" @click="activeTab = 'practice'">实习经历</div>
    </div>

    <div class="content-area" v-show="activeTab === 'diary'">
      <!-- 左侧：筛选 + 新建入口 -->
      <div class="sidebar">

        <!-- 新建日志按钮 -->
        <div class="section-card filter-card" style="padding:12px">
          <el-button type="primary" style="width:100%" @click="startNew">
            ✏️ 新建日志
          </el-button>
        </div>

        <!-- 搜索框 -->
        <div class="section-card filter-card">
          <el-input v-model="filterKeyword" placeholder="搜索标题/内容" clearable @input="applyFilter">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>

        <!-- 快捷筛选 -->
        <div class="section-card filter-card">
          <div class="filter-group">
            <div class="filter-item" :class="{ active: activeFilter === 'all' }" @click="setFilter('all')">
              📋 全部日志 <span class="count">{{ records.length }}</span>
            </div>
            <div class="filter-item" :class="{ active: activeFilter === 'starred' }" @click="setFilter('starred')">
              ⭐ 星标日志 <span class="count">{{ records.filter(r=>r.isStarred===1).length }}</span>
            </div>
          </div>
        </div>

        <!-- 分组 -->
        <div class="section-card filter-card">
          <div class="filter-section-title">
            <span>📁 分组</span>
            <el-button size="small" text type="primary" @click="showAddCategory = true">+ 新建</el-button>
          </div>
          <div class="filter-group">
            <div v-for="cat in categories" :key="cat"
              class="filter-item" :class="{ active: activeCategory === cat }"
              @click="setCategory(cat)">
              {{ cat }}
            </div>
            <div v-if="!categories.length" style="color:#c0c4cc;font-size:12px;padding:4px 0">暂无分组</div>
          </div>
          <div v-if="showAddCategory" style="margin-top:8px;display:flex;gap:6px">
            <el-input v-model="newCategoryName" size="small" placeholder="分组名称" @keyup.enter="addCategory" />
            <el-button size="small" type="primary" @click="addCategory">确定</el-button>
            <el-button size="small" @click="showAddCategory=false;newCategoryName=''">取消</el-button>
          </div>
        </div>

        <!-- 标签云 -->
        <div class="section-card filter-card">
          <div class="filter-section-title"><span>🏷️ 标签</span></div>
          <div class="tag-cloud">
            <el-tag v-for="tag in allTags" :key="tag"
              :type="activeTag === tag ? 'primary' : 'info'"
              size="small" style="cursor:pointer;margin:3px"
              @click="setTag(tag)">{{ tag }}</el-tag>
            <span v-if="!allTags.length" style="color:#c0c4cc;font-size:12px">暂无标签</span>
          </div>
        </div>

        <!-- 历史日志列表 -->
        <div class="section-card">
          <h4 style="margin:0 0 10px 0">历史日志</h4>
          <div class="timeline" v-if="filteredRecords.length > 0">
            <div class="timeline-item" v-for="record in filteredRecords" :key="record.id"
              :class="{ 'timeline-active': currentRecord?.id === record.id && !editingId }"
              @click="openDetail(record)">
              <div class="date">{{ formatDate(record.recordDate || record.createTime) }}</div>
              <div class="content list-content">
                <h5 class="clickable-title">
                  <span v-if="record.isStarred === 1" style="color:#f5a623">⭐</span>
                  {{ record.title }}
                </h5>
                <div style="display:flex;gap:4px;flex-wrap:wrap;margin-top:4px">
                  <el-tag v-if="record.category" size="small" type="warning">{{ record.category }}</el-tag>
                  <el-tag v-for="t in parseTags(record.tags)" :key="t" size="small" type="info">{{ t }}</el-tag>
                  <el-tag size="small" :type="record.visibility === 1 ? 'success' : ''">
                    {{ record.visibility === 1 ? '公开' : '私密' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无日志" :image-size="60" />
        </div>
      </div>

      <!-- 右侧：日志详情 / 编辑器 -->
      <div class="main-content">

        <!-- 编辑器（新建 or 编辑） -->
        <div v-if="editingId !== null" class="section-card diary-editor-card">
          <div class="card-header" style="margin-bottom:16px">
            <h4 style="margin:0">{{ editingId === 0 ? '新建日志' : '编辑日志' }}</h4>
            <el-button size="small" @click="cancelEdit">取消</el-button>
          </div>

          <input type="text" class="journal-title" v-model="form.title" placeholder="日志标题（必填）" />

          <el-row :gutter="12" style="margin-top:12px">
            <el-col :span="8">
              <el-input v-model="form.mood" placeholder="心情，如：#开心" size="small" />
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.category" placeholder="选择分组" size="small" clearable
                style="width:100%" allow-create filterable>
                <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.tagList" placeholder="添加标签" size="small" multiple clearable
                style="width:100%" allow-create filterable>
                <el-option v-for="tag in allTags" :key="tag" :label="tag" :value="tag" />
              </el-select>
            </el-col>
          </el-row>

          <el-row :gutter="12" style="margin-top:10px;align-items:center">
            <el-col :span="14">
              <el-radio-group v-model="form.visibility" size="small">
                <el-radio :value="1">导师可见</el-radio>
                <el-radio :value="0">仅自己可见</el-radio>
              </el-radio-group>
            </el-col>
            <el-col :span="10">
              <el-checkbox v-model="form.isStarred" :true-value="1" :false-value="0">⭐ 星标</el-checkbox>
            </el-col>
          </el-row>

          <!-- wangEditor 富文本编辑器 -->
          <WangEditor
            ref="wangEditorRef"
            v-model="form.htmlContent"
            height="380px"
            style="margin-top:14px"
          />

          <!-- 附件上传 -->
          <div style="margin-top:10px;display:flex;align-items:center;gap:10px">
            <el-button size="small" plain @click="triggerFileInput">📎 选择附件</el-button>
            <span v-if="selectedFile" style="font-size:12px;color:#666">{{ selectedFile.name }}</span>
            <input type="file" ref="fileInput" @change="handleFileChange" style="display:none" />
          </div>

          <div class="journal-actions" style="margin-top:16px">
            <el-button type="primary" @click="submitRecord">{{ editingId === 0 ? '发布日志' : '保存修改' }}</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </div>
        </div>

        <!-- 日志详情 -->
        <div v-else-if="currentRecord" class="section-card diary-detail-card">
          <div class="diary-detail-header">
            <div>
              <h2 style="margin:0 0 8px 0">
                <span v-if="currentRecord.isStarred === 1" style="color:#f5a623;margin-right:6px">⭐</span>
                {{ currentRecord.title }}
              </h2>
              <div style="display:flex;gap:8px;flex-wrap:wrap;align-items:center">
                <span style="color:#909399;font-size:13px">{{ formatDate(currentRecord.recordDate || currentRecord.createTime) }}</span>
                <el-tag v-if="currentRecord.mood" size="small">{{ currentRecord.mood }}</el-tag>
                <el-tag v-if="currentRecord.category" size="small" type="warning">{{ currentRecord.category }}</el-tag>
                <el-tag v-for="t in parseTags(currentRecord.tags)" :key="t" size="small" type="info">{{ t }}</el-tag>
                <el-tag size="small" :type="currentRecord.visibility === 1 ? 'success' : ''">
                  {{ currentRecord.visibility === 1 ? '导师可见' : '私密' }}
                </el-tag>
              </div>
            </div>
            <div style="display:flex;gap:8px;flex-shrink:0">
              <el-button size="small" :type="currentRecord.isStarred === 1 ? 'warning' : ''"
                @click="toggleStar(currentRecord)">
                {{ currentRecord.isStarred === 1 ? '⭐ 取消星标' : '☆ 加星标' }}
              </el-button>
              <el-button size="small" type="primary" @click="openEdit(currentRecord)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteDiary(currentRecord.id)">删除</el-button>
            </div>
          </div>
          <el-divider />
          <div class="diary-body" v-html="currentRecord.content"></div>
          <div v-if="currentRecord.attachmentPath" class="detail-attachment">
            <a :href="getAttachmentUrl(currentRecord.attachmentPath)" target="_blank">📎 查看 / 下载附件</a>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="section-card" style="text-align:center;padding:80px 20px">
          <el-empty description="从左侧选择日志查看，或点击「新建日志」开始写作" />
        </div>

      </div>
    </div>

    <!-- 作品集部分 -->
    <div v-show="activeTab === 'portfolio'">
      <div class="section-card">
        <div class="card-header">
          <h4>我的作品集 (从成长日志附件聚合)</h4>
        </div>
        <div class="portfolio-grid" v-if="portfolioItems.length > 0">
          <div class="portfolio-item" v-for="item in portfolioItems" :key="item.id">
            <div class="portfolio-thumb">
              <div class="file-type">FILE</div>
            </div>
            <div class="portfolio-info">
              <h5>{{ item.title }} 的附件</h5>
              <p>{{ formatDate(item.recordDate || item.createTime) }} 上传</p>
              <div class="actions">
                <a :href="getAttachmentUrl(item.attachmentPath)" target="_blank">预览下载</a>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无具有附件的作品" />
      </div>
      
      <div class="section-card">
        <div class="card-header">
          <h4>我的作品集</h4>
          <el-button type="primary" @click="portfolioDialogVisible = true">上传作品</el-button>
        </div>
        <div class="portfolio-grid" v-if="portfolios.length >0">
          <div class="portfolio-card" v-for="item in portfolios" :key="item.id">
            <div class="portfolio-card-cover" v-if="item.coverUrl">
              <img :src="getAttachmentUrl(item.coverUrl)" alt="封面" @error="handleImageError">
            </div>
            <div class="portfolio-card-cover placeholder" v-else>
              <div class="placeholder-icon">📁</div>
            </div>
            <div class="portfolio-card-content">
              <h5 class="portfolio-card-title">{{ item.title }}</h5>
              <div class="portfolio-card-meta">
                <span class="portfolio-card-date">{{ formatDate(item.uploadTime) }}</span>
                <el-tag size="small">{{ item.workType }}</el-tag>
              </div>
              <div class="portfolio-card-preview" v-html="item.description"></div>
              <div class="portfolio-card-actions">
                <el-button size="small" type="primary" @click="openPortfolioDetail(item)">详情</el-button>
                <el-button size="small" @click="downloadPortfolio(item.id)">下载</el-button>
                <el-button size="small" type="danger" @click="deletePortfolio(item.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无作品" />
      </div>

      <!-- 上传作品弹窗 -->
      <el-dialog title="上传作品" v-model="portfolioDialogVisible" width="600px">
        <el-form :model="portfolioForm" label-width="100px">
          <el-form-item label="作品标题">
            <el-input v-model="portfolioForm.title" placeholder="请输入作品标题"></el-input>
          </el-form-item>
          <el-form-item label="作品类型">
            <el-select v-model="portfolioForm.workType" placeholder="请选择作品类型">
              <el-option label="代码" value="代码"></el-option>
              <el-option label="设计图" value="设计图"></el-option>
              <el-option label="文章" value="文章"></el-option>
              <el-option label="其他" value="其他"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="作品简介">
            <div
              ref="portfolioEditor"
              contenteditable="true"
              class="rich-editor"
              placeholder="请输入作品简介，支持粘贴图片..."
              @paste="handlePortfolioPaste"
            ></div>
          </el-form-item>
          <el-form-item label="上传项目">
            <el-upload
              class="upload-demo"
              action="#"
              :auto-upload="false"
              :on-change="handlePortfolioFileChange"
              :show-file-list="true"
              accept=".zip,.rar,.7z,.jpg,.jpeg,.png,.pdf,.doc,.docx"
            >
              <el-button type="primary">选择项目文件夹/文件</el-button>
              <div slot="tip" class="el-upload__tip">
                <span style="color: #3498db; font-weight: bold;">推荐上传项目压缩包（.zip/.rar）</span>，支持文件夹结构展示<br>
                也支持单个文件上传（图片、文档等）
              </div>
            </el-upload>
          </el-form-item>
          <el-form-item label="封面图片">
            <el-upload
              class="upload-demo"
              action="#"
              :auto-upload="false"
              :on-change="handleCoverFileChange"
              :show-file-list="true"
              accept=".jpg,.jpeg,.png"
            >
              <el-button type="primary">选择封面图片</el-button>
              <div slot="tip" class="el-upload__tip">
                支持JPG、PNG格式，建议尺寸：800x600像素
              </div>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="portfolioDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitPortfolio">上传</el-button>
          </span>
        </template>
      </el-dialog>
    </div>

    <!-- 实习经历部分 -->
    <div v-show="activeTab === 'practice'">
      <div class="section-card">
        <div class="card-header">
          <h4>我的实习经历</h4>
          <el-button type="primary" @click="practiceDialogVisible = true">添加实习经历</el-button>
        </div>
        <div class="practice-list" v-if="practices.length > 0">
          <div class="practice-item" v-for="item in practices" :key="item.id">
            <div class="practice-header">
              <h5>{{ item.activityName }}</h5>
              <span class="practice-organization">{{ item.organization }}</span>
            </div>
            <div class="practice-meta">
              <span>{{ formatDate(item.startDate) }} 至 {{ formatDate(item.endDate) }}</span>
            </div>
            <div class="practice-content" v-html="item.content"></div>
            <div v-if="item.proofFile" class="practice-attachment">
              <a :href="`/api/practice/preview/${item.id}`" target="_blank">
                📎 在线查看证明材料
              </a>
            </div>
            <div class="practice-actions">
              <span class="delete-btn" @click="deletePractice(item.id)">删除</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无实习经历" />
      </div>
    </div>

    <!-- 作品集详情对话框 -->
    <el-dialog title="项目详情" v-model="portfolioDetailVisible" width="900px">
      <div v-if="currentPortfolio" class="portfolio-detail">
        <div class="detail-header">
          <div class="detail-cover" v-if="currentPortfolio.coverUrl">
            <img :src="getAttachmentUrl(currentPortfolio.coverUrl)" alt="封面">
          </div>
          <div class="detail-title-area">
            <h4>{{ currentPortfolio.title }}</h4>
            <el-tag size="small">{{ currentPortfolio.workType }}</el-tag>
          </div>
        </div>
        <div class="detail-meta">
          <span>{{ formatDate(currentPortfolio.uploadTime) }} 上传</span>
        </div>
        
        <!-- 项目结构展示 -->
        <div class="detail-file-tree">
          <h5>项目结构</h5>
          <div class="file-tree-container">
            <div class="project-structure" v-if="fileTree.length > 0">
              <div v-for="node in fileTree" :key="node.name">
                <file-tree-item :node="node" :level="0" />
              </div>
            </div>
            <div v-else class="single-file">
              <div class="structure-item">
                <span class="structure-icon">
                  {{ currentPortfolio.fileUrl && currentPortfolio.fileUrl.includes('.jpg') || currentPortfolio.fileUrl.includes('.png') || currentPortfolio.fileUrl.includes('.jpeg') ? '🖼️' : 
                    currentPortfolio.fileUrl && currentPortfolio.fileUrl.includes('.pdf') ? '📕' :
                    currentPortfolio.fileUrl && currentPortfolio.fileUrl.includes('.doc') || currentPortfolio.fileUrl.includes('.docx') ? '📘' : '📄' }}
                </span>
                <span class="structure-name">{{ currentPortfolio.title }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 项目简介 -->
        <div class="detail-description">
          <h5>项目简介</h5>
          <div class="description-content" v-html="currentPortfolio.description"></div>
        </div>
        
        <!-- 下载按钮 -->
        <div class="detail-download">
          <a href="javascript:void(0)" @click="downloadPortfolio(currentPortfolio.id)" class="download-btn">
            📥 下载项目文件
          </a>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="portfolioDetailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    


    <!-- 实习经历上传弹窗 -->
    <el-dialog title="添加实习经历" v-model="practiceDialogVisible" width="600px">
      <el-form :model="practiceForm" label-width="100px">
        <el-form-item label="实习主题">
          <el-input v-model="practiceForm.activityName" placeholder="请输入实习主题名称"></el-input>
        </el-form-item>
        <el-form-item label="实习单位">
          <el-input v-model="practiceForm.organization" placeholder="请输入实习单位/组织"></el-input>
        </el-form-item>
        <el-form-item label="实习时间">
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-date-picker
              v-model="practiceForm.startDate"
              type="date"
              placeholder="开始日期"
              value-format="YYYY-MM-DD"
              style="width: 180px;"
            ></el-date-picker>
            <span>至</span>
            <el-date-picker
              v-model="practiceForm.endDate"
              type="date"
              placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 180px;"
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="实习内容">
          <div
            ref="practiceEditor"
            contenteditable="true"
            class="rich-editor"
            placeholder="请输入实习内容和心得..."
            @paste="handlePracticePaste"
          ></div>
        </el-form-item>
        <el-form-item label="证明材料">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handlePracticeFileChange"
            :show-file-list="true"
            accept=".jpg,.jpeg,.png,.pdf,.doc,.docx"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="practiceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPractice">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, defineComponent, nextTick } from 'vue';
import { useUserStore } from '@/stores/user';
import { getDiaries, createDiary } from '@/services/diary';
import { getPortfolios, addPortfolio, deletePortfolio as deletePortfolioApi, downloadPortfolio } from '@/services/portfolio';
import { api } from '@/services/api';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import WangEditor from '@/components/WangEditor.vue';

// 文件树节点组件
const FileTreeItem = defineComponent({
  name: 'FileTreeItem',
  props: {
    node: { type: Object, required: true },
    level: { type: Number, default: 0 }
  },
  setup(props) {
    const fileIcons = {
      'js': '📄', 'vue': '📄', 'html': '🌐', 'css': '🎨', 'scss': '🎨',
      'md': '📝', 'json': '📋', 'xml': '📋', 'txt': '📄',
      'jpg': '🖼️', 'png': '🖼️', 'jpeg': '🖼️', 'gif': '🎞️',
      'pdf': '📕', 'doc': '📘', 'docx': '📘', 'xlsx': '📗',
      'zip': '📦', 'rar': '📦', '7z': '📦',
      'py': '🐍', 'java': '☕', 'cpp': '💻', 'c': '💻', 'h': '💻',
      'sql': '🗄️', 'db': '🗄️', 'default': '📄'
    };
    const toggleExpand = (node) => { if (node.type === 'folder') node.expanded = !node.expanded; };
    const getFileIcon = (ext) => fileIcons[ext] || fileIcons.default;
    return { toggleExpand, getFileIcon };
  },
  template: `
    <div class="file-tree-node" :style="{ paddingLeft: level * 20 + 'px' }">
      <div class="file-tree-item" @click="toggleExpand(node)">
        <span class="tree-icon">{{ node.type === 'folder' ? (node.expanded ? '▼' : '▶') : '' }}</span>
        <span class="file-icon">{{ node.type === 'folder' ? '📁' : getFileIcon(node.extension) }}</span>
        <span class="file-name">{{ node.name }}</span>
      </div>
      <div v-if="node.type === 'folder' && node.expanded && node.children" class="file-tree-children">
        <file-tree-item v-for="child in node.children" :key="child.name" :node="child" :level="level + 1" />
      </div>
    </div>
  `
});

const userStore = useUserStore();
const activeTab = ref('diary');

// ===== 日记相关状态 =====
const records = ref([]);
const allTags = ref([]);
const categories = ref([]);
const fileInput = ref(null);
const selectedFile = ref(null);
const editor = ref(null);
const editingId = ref(null);
const currentRecord = ref(null);

// wangEditor
const wangEditorRef = ref(null);

// 筛选状态
const filterKeyword = ref('');
const activeFilter = ref('all');   // 'all' | 'starred'
const activeCategory = ref('');
const activeTag = ref('');

// 分组管理
const showAddCategory = ref(false);
const newCategoryName = ref('');

const form = ref({
  title: '',
  content: '',
  mood: '',
  visibility: 1,
  isStarred: 0,
  category: '',
  tagList: [],   // 前端用数组，提交时转逗号字符串
  recordDate: new Date().toISOString().split('T')[0],
});

// 筛选后的日记列表
const filteredRecords = computed(() => {
  let list = records.value;
  if (activeFilter.value === 'starred') list = list.filter(r => r.isStarred === 1);
  if (activeCategory.value) list = list.filter(r => r.category === activeCategory.value);
  if (activeTag.value) list = list.filter(r => parseTags(r.tags).includes(activeTag.value));
  if (filterKeyword.value) {
    const kw = filterKeyword.value.toLowerCase();
    list = list.filter(r => r.title?.toLowerCase().includes(kw) || r.content?.toLowerCase().includes(kw));
  }
  return list;
});

const parseTags = (tagsStr) => {
  if (!tagsStr) return [];
  return tagsStr.split(',').map(t => t.trim()).filter(Boolean);
};

const setFilter = (f) => { activeFilter.value = f; activeCategory.value = ''; activeTag.value = ''; };
const setCategory = (cat) => { activeCategory.value = activeCategory.value === cat ? '' : cat; activeFilter.value = 'all'; activeTag.value = ''; };
const setTag = (tag) => { activeTag.value = activeTag.value === tag ? '' : tag; activeFilter.value = 'all'; activeCategory.value = ''; };
const applyFilter = () => {}; // computed 自动响应

const addCategory = () => {
  const name = newCategoryName.value.trim();
  if (!name) return;
  if (!categories.value.includes(name)) categories.value.push(name);
  showAddCategory.value = false;
  newCategoryName.value = '';
};

const triggerFileInput = () => { fileInput.value.click(); };
const handleFileChange = (e) => { if (e.target.files.length > 0) selectedFile.value = e.target.files[0]; };

// 项目相关数据
const portfolios = ref([]);
const portfolioDialogVisible = ref(false);
const portfolioDetailVisible = ref(false);
const currentPortfolio = ref(null);
const portfolioForm = ref({
  title: '',
  workType: '',
  description: ''
});
const portfolioFile = ref(null);
const coverFile = ref(null);
const portfolioEditor = ref(null);
const fileTree = ref([]);

// 实习经历相关数据
const practices = ref([]);
const practiceDialogVisible = ref(false);
const practiceForm = ref({
  activityName: '',
  organization: '',
  startDate: '',
  endDate: '',
  content: ''
});
const practiceFile = ref(null);
const practiceEditor = ref(null);

// 计算属性，过滤出有附件的记录作为作品集展示
const portfolioItems = computed(() => {
  return records.value.filter(record => record.attachmentPath);
});

const fetchRecords = async () => {
  if (!userStore.userInfo?.userId) return;
  try {
    const res = await api.get('/diary/list', { params: { studentId: userStore.userInfo.userId } });
    records.value = Array.isArray(res) ? res : (res?.data || []);
    // 同步加载分组和标签
    const [cats, tags] = await Promise.all([
      api.get('/diary/categories', { params: { studentId: userStore.userInfo.userId } }),
      api.get('/diary/tags', { params: { studentId: userStore.userInfo.userId } })
    ]);
    categories.value = cats || [];
    allTags.value = tags || [];
  } catch {
    ElMessage.error('获取日志失败');
  }
};

const resetFilter = () => {
  activeFilter.value = 'all';
  activeCategory.value = '';
  activeTag.value = '';
  filterKeyword.value = '';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toISOString().split('T')[0];
};

const getAttachmentUrl = (path) => {
  if (!path) return '';
  if (path.startsWith('http')) return path;
  if (path.length > 1000) return 'data:image/jpeg;base64,' + path;
  return `http://localhost:8083${path}`;
};

const handleImageError = (e) => { e.target.style.display = 'none'; };

const handlePaste = async (e) => {
  // wangEditor 已内置图片粘贴上传，此函数保留兼容旧 contenteditable 区域
};

const submitRecord = async () => {
  if (!form.value.title) { ElMessage.warning('请填写日志标题'); return; }
  const content = wangEditorRef.value ? wangEditorRef.value.getHtml() : form.value.htmlContent;
  const tagsStr = (form.value.tagList || []).join(',');

  if (editingId.value && editingId.value !== 0) {
    try {
      await api.put(`/diary/${editingId.value}`, {
        title: form.value.title, content,
        mood: form.value.mood,
        visibility: form.value.visibility,
        isStarred: form.value.isStarred,
        category: form.value.category || '',
        tags: tagsStr
      });
      ElMessage.success('修改成功');
      await fetchRecords();
      currentRecord.value = records.value.find(r => r.id === editingId.value) || null;
      editingId.value = null;
    } catch { ElMessage.error('修改失败'); }
    return;
  }

  const formData = new FormData();
  formData.append('studentId', userStore.userInfo.userId);
  formData.append('title', form.value.title);
  formData.append('content', content);
  formData.append('visibility', form.value.visibility);
  formData.append('isStarred', form.value.isStarred);
  formData.append('category', form.value.category || '');
  formData.append('tags', tagsStr);
  formData.append('recordDate', new Date().toISOString().split('T')[0]);
  if (form.value.mood) formData.append('mood', form.value.mood);
  if (selectedFile.value) formData.append('file', selectedFile.value);

  try {
    await createDiary(formData);
    ElMessage.success('发布成功');
    editingId.value = null;
    selectedFile.value = null;
    await fetchRecords();
  } catch { ElMessage.error('发布失败'); }
};

const clearForm = () => {
  form.value = { title: '', htmlContent: '', mood: '', visibility: 1, isStarred: 0, category: '', tagList: [], recordDate: new Date().toISOString().split('T')[0] };
  editingId.value = null;
  selectedFile.value = null;
  if (fileInput.value) fileInput.value.value = '';
};

const openDetail = (record) => { currentRecord.value = record; editingId.value = null; };

// 新建：editingId=0 表示新建模式
const startNew = () => {
  currentRecord.value = null;
  editingId.value = 0;
  form.value = { title: '', htmlContent: '', mood: '', visibility: 1, isStarred: 0, category: '', tagList: [], recordDate: new Date().toISOString().split('T')[0] };
  selectedFile.value = null;
  nextTick(() => { if (wangEditorRef.value) wangEditorRef.value.setHtml(''); });
};

const openEdit = (record) => {
  editingId.value = record.id;
  form.value.title = record.title;
  form.value.mood = record.mood || '';
  form.value.visibility = record.visibility ?? 1;
  form.value.isStarred = record.isStarred ?? 0;
  form.value.category = record.category || '';
  form.value.tagList = parseTags(record.tags);
  form.value.htmlContent = record.content || '';
  nextTick(() => { if (wangEditorRef.value) wangEditorRef.value.setHtml(record.content || ''); });
};

const cancelEdit = () => {
  editingId.value = null;
  selectedFile.value = null;
};

const deleteDiary = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条日志吗？', '警告', { type: 'warning' });
    await api.delete(`/diary/${id}`);
    ElMessage.success('删除成功');
    if (currentRecord.value?.id === id) currentRecord.value = null;
    await fetchRecords();
  } catch { /* cancelled */ }
};

const toggleStar = async (record) => {
  const newVal = record.isStarred === 1 ? 0 : 1;
  try {
    await api.put(`/diary/${record.id}/star`, null, { params: { starred: newVal === 1 } });
    record.isStarred = newVal;
    if (currentRecord.value?.id === record.id) currentRecord.value.isStarred = newVal;
    ElMessage.success(newVal === 1 ? '已加星标' : '已取消星标');
  } catch { ElMessage.error('操作失败'); }
};

// 项目相关方法
const fetchPortfolios = async () => {
  if (!userStore.userInfo?.userId) return;
  try {
    const res = await getPortfolios(userStore.userInfo.userId);
    portfolios.value = res.data || res || [];
  } catch (error) {
    console.error('Failed to fetch portfolios:', error);
    ElMessage.error('获取作品集失败');
  }
};

const handlePortfolioFileChange = (file) => {
  portfolioFile.value = file.raw;
};

const handleCoverFileChange = (file) => {
  coverFile.value = file.raw;
};

const handlePortfolioPaste = async (e) => {
  const items = e.clipboardData.items;
  for (let i = 0; i< items.length; i++) {
    const item = items[i];
    if (item.type.indexOf('image') !== -1) {
      e.preventDefault();
      const file = item.getAsFile();
      if (file) {
        ElMessage.info('正在上传图片...');
        
        const formData = new FormData();
        formData.append('file', file);
        formData.append('studentId', userStore.userInfo.userId);
        
        try {
          const response = await fetch('/api/diary/upload-image', {
            method: 'POST',
            body: formData
          });
          
          if (response.ok) {
            const data = await response.text();
            let imageUrl = data;
            if (!imageUrl.startsWith('http')) {
              imageUrl = `http://localhost:8083${imageUrl}`;
            }
            
            const img = document.createElement('img');
            img.src = imageUrl;
            img.style.maxWidth = '100%';
            img.style.margin = '10px 0';
            
            const range = window.getSelection().getRangeAt(0);
            range.deleteContents();
            range.insertNode(img);
            range.collapse(false);
            
            const selection = window.getSelection();
            selection.removeAllRanges();
            selection.addRange(range);
            
            ElMessage.success('图片上传成功');
          } else {
            const errorText = await response.text();
            ElMessage.error('图片上传失败: ' + errorText);
          }
        } catch (error) {
          console.error('图片上传错误:', error);
          ElMessage.error('图片上传失败: ' + error.message);
        }
      }
      break;
    }
  }
};

const generateFileTree = (portfolio) => {
  const fileTreeData = [];
  
  // 如果有后端生成的文件结构，使用真实结构
  if (portfolio.fileStructure) {
    try {
      const treeData = JSON.parse(portfolio.fileStructure);
      fileTreeData.push(treeData);
      return fileTreeData;
    } catch (error) {
      console.error('解析文件结构失败:', error);
    }
  }
  
  // 备用逻辑：如果没有文件结构，根据文件类型生成默认结构
  if (portfolio.fileUrl && (portfolio.fileUrl.includes('.zip') || portfolio.fileUrl.includes('.rar'))) {
    fileTreeData.push({
      name: portfolio.title,
      type: 'folder',
      expanded: true,
      children: [
        { name: '项目文件', type: 'file', extension: 'zip' }
      ]
    });
  } else if (portfolio.fileUrl && (portfolio.fileUrl.includes('.jpg') || portfolio.fileUrl.includes('.png') || portfolio.fileUrl.includes('.jpeg'))) {
    fileTreeData.push({
      name: portfolio.title,
      type: 'file',
      extension: portfolio.fileUrl.split('.').pop()
    });
  } else if (portfolio.fileUrl && portfolio.fileUrl.includes('.pdf')) {
    fileTreeData.push({
      name: portfolio.title,
      type: 'file',
      extension: 'pdf'
    });
  } else {
    fileTreeData.push({
      name: portfolio.title,
      type: 'file',
      extension: portfolio.fileUrl ? portfolio.fileUrl.split('.').pop() : 'file'
    });
  }
  
  return fileTreeData;
};

const toggleExpand = (node) => {
  if (node.type === 'folder') {
    node.expanded = !node.expanded;
  }
};

// 递归渲染文件树
const renderFileTree = (node, level) => {
  return h('div', { style: { paddingLeft: `${level * 20}px` } }, [
    h('div', { class: 'file-tree-item', onClick: () => toggleExpand(node) }, [
      h('span', { class: 'tree-icon' }, node.type === 'folder' ? (node.expanded ? '▼' : '▶') : ''),
      h('span', { class: 'file-icon' }, node.type === 'folder' ? '📁' : getFileIcon(node.extension)),
      h('span', { class: 'file-name' }, node.name)
    ]),
    node.type === 'folder' && node.expanded && node.children ? h('div', { class: 'file-tree-children' }, [
      node.children.map(child => renderFileTree(child, level + 1))
    ]) : null
  ]);
};

const getFileIcon = (extension) => {
  const icons = {
    'js': '📄', 'vue': '📄', 'html': '🌐', 'css': '🎨', 'scss': '🎨',
    'md': '📝', 'json': '📋', 'xml': '📋', 'txt': '📄',
    'jpg': '🖼️', 'png': '🖼️', 'jpeg': '🖼️', 'gif': '🎞️',
    'pdf': '📕', 'doc': '📘', 'docx': '📘', 'xlsx': '📗',
    'zip': '📦', 'rar': '📦', '7z': '📦',
    'exe': '⚙️', 'dll': '⚙️', 'bat': '⚙️', 'sh': '⚙️',
    'py': '🐍', 'java': '☕', 'cpp': '💻', 'c': '💻', 'h': '💻',
    'sql': '🗄️', 'db': '🗄️', 'sqlite': '🗄️',
    'default': '📄'
  };
  return icons[extension] || icons.default;
};

const openPortfolioDetail = (portfolio) => {
  currentPortfolio.value = portfolio;
  fileTree.value = generateFileTree(portfolio);
  portfolioDetailVisible.value = true;
};



const submitPortfolio = async () => {
  if (!portfolioForm.value.title || !portfolioForm.value.workType || !portfolioEditor.value) {
    ElMessage.warning('请填写作品标题、类型和简介');
    return;
  }

  const description = portfolioEditor.value.innerHTML;
  const formData = new FormData();
  formData.append('studentId', userStore.userInfo.userId);
  formData.append('title', portfolioForm.value.title);
  formData.append('workType', portfolioForm.value.workType);
  formData.append('description', description);
  if (portfolioFile.value) {
    formData.append('file', portfolioFile.value);
  }
  if (coverFile.value) {
    formData.append('cover', coverFile.value);
  }

  try {
    await addPortfolio(formData);
    ElMessage.success('上传成功');
    portfolioDialogVisible.value = false;
    portfolioForm.value = { title: '', workType: '', description: '' };
    portfolioFile.value = null;
    coverFile.value = null;
    if (portfolioEditor.value) portfolioEditor.value.innerHTML = '';
    fetchPortfolios();
  } catch (error) {
    console.error(error);
    ElMessage.error('上传失败');
  }
};

const deletePortfolio = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个作品吗？', '警告', { type: 'warning' });
    await deletePortfolioApi(id);
    ElMessage.success('删除成功');
    fetchPortfolios();
  } catch (e) {
    // cancelled
  }
};



// 实习经历相关方法
const fetchPractices = async () => {
  if (!userStore.userInfo?.userId) return;
  try {
    practices.value = await api.get(`/practice/list/${userStore.userInfo.userId}`) || [];
  } catch {
    ElMessage.error('获取实习经历失败');
  }
};

const handlePracticeFileChange = (file) => {
  practiceFile.value = file.raw;
};

const handlePracticePaste = async (e) => {
  const items = e.clipboardData.items;
  for (let i = 0; i< items.length; i++) {
    const item = items[i];
    if (item.type.indexOf('image') !== -1) {
      e.preventDefault();
      const file = item.getAsFile();
      if (file) {
        ElMessage.info('正在上传图片...');
        
        const formData = new FormData();
        formData.append('file', file);
        formData.append('studentId', userStore.userInfo.userId);
        
        try {
          const response = await fetch('/api/diary/upload-image', {
            method: 'POST',
            body: formData
          });
          
          if (response.ok) {
            const data = await response.text();
            let imageUrl = data;
            if (!imageUrl.startsWith('http')) {
              imageUrl = `http://localhost:8083${imageUrl}`;
            }
            
            const img = document.createElement('img');
            img.src = imageUrl;
            img.style.maxWidth = '100%';
            img.style.margin = '10px 0';
            
            const range = window.getSelection().getRangeAt(0);
            range.deleteContents();
            range.insertNode(img);
            range.collapse(false);
            
            const selection = window.getSelection();
            selection.removeAllRanges();
            selection.addRange(range);
            
            ElMessage.success('图片上传成功');
          } else {
            const errorText = await response.text();
            ElMessage.error('图片上传失败: ' + errorText);
          }
        } catch (error) {
          console.error('图片上传错误:', error);
          ElMessage.error('图片上传失败: ' + error.message);
        }
      }
      break;
    }
  }
};

const submitPractice = async () => {
  if (!practiceForm.value.activityName || !practiceForm.value.organization ||
      !practiceForm.value.startDate || !practiceForm.value.endDate) {
    ElMessage.warning('请填写完整的实习经历信息');
    return;
  }

  const content = practiceEditor.value ? practiceEditor.value.innerHTML : '';
  const formData = new FormData();
  formData.append('studentId', userStore.userInfo.userId);
  formData.append('activityName', practiceForm.value.activityName);
  formData.append('organization', practiceForm.value.organization);
  formData.append('startDate', practiceForm.value.startDate);
  formData.append('endDate', practiceForm.value.endDate);
  formData.append('content', content);
  if (practiceFile.value) {
    formData.append('file', practiceFile.value);
  }

  try {
    await api.post('/practice/upload', formData);
    ElMessage.success('实习经历上传成功');
    practiceDialogVisible.value = false;
    practiceForm.value = { activityName: '', organization: '', startDate: '', endDate: '', content: '' };
    practiceFile.value = null;
    if (practiceEditor.value) practiceEditor.value.innerHTML = '';
    fetchPractices();
  } catch (error) {
    ElMessage.error('上传失败');
  }
};

const deletePractice = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个实习经历吗？', '警告', { type: 'warning' });
    const response = await fetch(`/api/practice/delete/${id}`, {
      method: 'DELETE',
      headers: (() => { const t = sessionStorage.getItem('token'); return t ? { Authorization: `Bearer ${t}` } : {}; })()
    });
    
    if (response.ok) {
      ElMessage.success('删除成功');
      fetchPractices();
    } else {
      ElMessage.error('删除失败');
    }
  } catch (e) {
    // cancelled
  }
};

// 监听标签页变化，加载对应数据
watch(activeTab, (newTab) => {
  if (newTab === 'portfolio') fetchPortfolios();
  else if (newTab === 'practice') fetchPractices();
});

onMounted(() => { fetchRecords(); });

onBeforeUnmount(() => {
  // WangEditor 组件内部自行销毁
});
</script>

<style scoped>
.student-growth {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.content-area {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.sidebar {
  width: 350px;
  flex-shrink: 0;
}

.main-content {
  flex: 1;
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

.filter-card {
  padding: 15px 25px;
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

.clickable-title {
  color: #3498db !important;
  cursor: pointer;
  transition: color 0.2s;
  display: inline-block;
}

.clickable-title:hover {
  text-decoration: underline;
  color: #2980b9 !important;
}

.list-content {
  padding-bottom: 5px;
}

.record-detail {
  padding: 10px;
}

.detail-meta {
  color: #7f8c8d;
  font-size: 0.9em;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.detail-mood {
  margin-left: 15px;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.detail-body {
  line-height: 1.6;
  white-space: pre-wrap;
  color: #2c3e50;
  font-size: 1.05em;
  margin-bottom: 30px;
}

.detail-attachment {
  border-top: 1px dashed #eee;
  padding-top: 15px;
}

.detail-attachment a {
  color: #e67e22;
  text-decoration: none;
  font-weight: bold;
}

.editor-toolbar {
  background: #f8f9fa;
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.editor-toolbar button {
  background: white;
  border: 1px solid #ddd;
  padding: 4px 10px;
  cursor: pointer;
  border-radius: 3px;
}

.rich-editor {
  min-height: 200px;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-top: 10px;
  outline: none;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-y: auto;
}

.rich-editor:empty:before {
  content: attr(placeholder);
  color: #999;
  pointer-events: none;
}

.rich-editor img {
  max-width: 100%;
  margin: 10px 0;
  border-radius: 4px;
}

.rich-editor p {
  margin: 10px 0;
}

.journal-content {
  height: 150px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  margin-top: 10px;
}

.journal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 10px;
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
  margin-top: 20px;
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
  white-space: pre-wrap;
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

/* 卡片风格的作品集样式 */
.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.portfolio-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  overflow: hidden;
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.portfolio-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.portfolio-card-cover {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f8f9fa;
}

.portfolio-card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.portfolio-card:hover .portfolio-card-cover img {
  transform: scale(1.05);
}

.portfolio-card-cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
}

.placeholder-icon {
  font-size: 48px;
  color: #ccc;
}

.portfolio-card-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.portfolio-card-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #2c3e50;
  font-weight: 600;
  line-height: 1.3;
}

.portfolio-card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.portfolio-card-date {
  font-size: 12px;
  color: #7f8c8d;
}

.portfolio-card-preview {
  flex: 1;
  line-height: 1.5;
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.portfolio-card-actions {
  display: flex;
  gap: 8px;
}

.download-btn {
  background: #3498db;
  color: white;
  padding: 6px 12px;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.2s;
}

.download-btn:hover {
  background: #2980b9;
}

/* 作品集详情样式 */
.portfolio-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e9ecef;
}

.detail-cover {
  width: 120px;
  height: 90px;
  margin-right: 20px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-title-area {
  flex: 1;
}

.detail-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
}

.detail-meta {
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 20px;
}

/* 项目结构展示样式 */
.detail-file-tree {
  margin-bottom: 30px;
}

.detail-file-tree h5 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
}

.file-tree-container {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 15px;
  max-height: 400px;
  overflow-y: auto;
}

.project-structure, .single-file {
  font-family: 'Courier New', monospace;
}

.structure-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.structure-item:hover {
  background: #f8f9fa;
}

.structure-icon {
  font-size: 18px;
  margin-right: 10px;
  width: 20px;
  text-align: center;
}

.structure-name {
  font-size: 14px;
  color: #2c3e50;
}

/* 项目简介样式 */
.detail-description {
  margin-bottom: 30px;
}

.detail-description h5 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
}

.description-content {
  line-height: 1.6;
  color: #2c3e50;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.description-content img {
  max-width: 100%;
  margin: 15px 0;
  border-radius: 6px;
}

.description-content p {
  margin: 12px 0;
}

/* 下载按钮样式 */
.detail-download {
  text-align: center;
  margin-top: 20px;
}

.detail-download .download-btn {
  background: #28a745;
  color: white;
  padding: 10px 20px;
  border-radius: 6px;
  text-decoration: none;
  font-size: 16px;
  font-weight: 500;
  transition: background 0.2s;
}

.detail-download .download-btn:hover {
  background: #218838;
}

/* 实习经历样式 */
.practice-list {
  margin-top: 20px;
}

.practice-item {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
}

.practice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.practice-header h5 {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
}

.practice-organization {
  background: #e3f2fd;
  color: #1976d2;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.practice-meta {
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
}

.practice-content {
  line-height: 1.6;
  color: #2c3e50;
  margin-bottom: 15px;
  white-space: pre-wrap;
}

.practice-content img {
  max-width: 100%;
  margin: 10px 0;
  border-radius: 4px;
}

.practice-attachment {
  margin-bottom: 15px;
  padding-top: 10px;
  border-top: 1px dashed #eee;
}

.practice-attachment a {
  color: #e67e22;
  text-decoration: none;
  font-weight: bold;
}

.filter-section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}
.filter-group { display: flex; flex-direction: column; gap: 2px; }
.filter-item {
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.15s;
}
.filter-item:hover { background: #f0f2f5; }
.filter-item.active { background: #ecf5ff; color: #409EFF; font-weight: 600; }
.filter-item .count {
  background: #f0f2f5;
  border-radius: 10px;
  padding: 1px 7px;
  font-size: 11px;
  color: #909399;
}
.tag-cloud { display: flex; flex-wrap: wrap; gap: 4px; }
.timeline-active { background: #ecf5ff; border-radius: 6px; }
.diary-detail-card { padding: 28px; }
.diary-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
.diary-body {
  line-height: 1.8;
  color: #303133;
  font-size: 15px;
  min-height: 200px;
}
.diary-body img { max-width: 100%; border-radius: 6px; margin: 10px 0; }
</style>

