package com.example.archivemanagement.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.dto.StudentProfileDTO;
import com.example.archivemanagement.entity.*;
import com.example.archivemanagement.mapper.*;
import com.example.archivemanagement.service.EvalGrowthScoreService;
import com.example.archivemanagement.service.InfoStudentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI 成长分析接口 — 调用千问 API，结果持久化到 eval_growth_score 表
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private static final String QWEN_API_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    @Value("${qwen.api.key}")
    private String apiKey;

    private final InfoStudentService studentService;
    private final EvalGrowthScoreService growthScoreService;
    private final RecordAcademicMapper academicMapper;
    private final RecordAwardMapper awardMapper;
    private final RecordResearchMapper researchMapper;
    private final RecordPracticeMapper practiceMapper;
    private final BusRecruitmentMapper recruitmentMapper;
    private final InfoStudentMapper infoStudentMapper;

    /**
     * 获取已保存的 AI 分析结果（不重新生成）
     * GET /api/ai/result/{studentId}
     */
    @GetMapping("/result/{studentId}")
    public Result<Map<String, Object>> getResult(@PathVariable Long studentId) {
        QueryWrapper<EvalGrowthScore> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId)
          .isNotNull("ai_analysis")
          .orderByDesc("ai_update_time")
          .last("LIMIT 1");
        EvalGrowthScore score = growthScoreService.getOne(qw);
        Map<String, Object> resp = new HashMap<>();
        if (score != null) {
            resp.put("aiAnalysis",       score.getAiAnalysis());
            resp.put("aiResume",         score.getAiResume());
            resp.put("aiLearningAdvice", score.getAiLearningAdvice());
            resp.put("aiUpdateTime",     score.getAiUpdateTime());
        }
        return Result.ok(resp);
    }

    /**
     * 生成并保存 AI 成长分析报告
     * POST /api/ai/analyze/{studentId}
     */
    @PostMapping("/analyze/{studentId}")
    public Result<Map<String, String>> analyze(@PathVariable Long studentId,
                                               HttpServletRequest request) {
        String prompt = buildAnalysisPrompt(studentId);
        String analysis = callQwen(prompt);
        // 持久化
        saveAiResult(studentId, analysis, null);
        Map<String, String> resp = new HashMap<>();
        resp.put("analysis", analysis);
        return Result.ok(resp);
    }

    /**
     * 生成并保存 AI 简历
     * POST /api/ai/resume/{studentId}  body: { "resumeStyle": "classic|modern|academic|creative" }
     */
    @PostMapping("/resume/{studentId}")
    public Result<Map<String, String>> generateResume(@PathVariable Long studentId,
                                                      @RequestBody(required = false) Map<String, Object> body,
                                                      HttpServletRequest request) {
        String style = (body != null && body.containsKey("resumeStyle"))
                ? body.get("resumeStyle").toString() : "classic";
        String prompt = buildResumePrompt(studentId, style);
        String resume = callQwen(prompt);
        saveAiResult(studentId, null, resume);
        Map<String, String> resp = new HashMap<>();
        resp.put("resume", resume);
        resp.put("style", style);
        return Result.ok(resp);
    }

    /**
     * 生成并保存 AI 个性化学习建议
     * POST /api/ai/learning-advice/{studentId}
     */
    @PostMapping("/learning-advice/{studentId}")
    public Result<Map<String, String>> learningAdvice(@PathVariable Long studentId,
                                                      HttpServletRequest request) {
        String prompt = buildLearningAdvicePrompt(studentId);
        String advice = callQwen(prompt);
        saveAiLearningAdvice(studentId, advice);
        Map<String, String> resp = new HashMap<>();
        resp.put("advice", advice);
        return Result.ok(resp);
    }

    /**
     * AI 职位推荐 + 未来规划
     * POST /api/ai/job-recommend/{studentId}
     */
    @PostMapping("/job-recommend/{studentId}")
    public Result<Map<String, String>> jobRecommend(@PathVariable Long studentId,
                                                    HttpServletRequest request) {
        String prompt = buildJobRecommendPrompt(studentId);
        String result = callQwen(prompt);
        Map<String, String> resp = new HashMap<>();
        resp.put("result", result);
        return Result.ok(resp);
    }

    /**
     * AI 智能学生推荐（企业端，按职位推荐匹配学生）
     * POST /api/ai/student-recommend/{recruitmentId}
     */
    @PostMapping("/student-recommend/{recruitmentId}")
    public Result<Map<String, String>> studentRecommend(@PathVariable Long recruitmentId,
                                                        HttpServletRequest request) {
        String prompt = buildStudentRecommendPrompt(recruitmentId);
        String result = callQwen(prompt);
        Map<String, String> resp = new HashMap<>();
        resp.put("result", result);
        return Result.ok(resp);
    }

    // ===== 持久化 AI 结果到最新成长分记录 =====
    private void saveAiResult(Long studentId, String analysis, String resume) {
        QueryWrapper<EvalGrowthScore> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId).orderByDesc("update_time").last("LIMIT 1");
        EvalGrowthScore score = growthScoreService.getOne(qw);

        if (score == null) {
            score = new EvalGrowthScore();
            score.setStudentId(studentId);
            score.setTermYear("AI分析");
        }
        if (analysis != null) score.setAiAnalysis(analysis);
        if (resume != null) score.setAiResume(resume);
        score.setAiUpdateTime(LocalDateTime.now());

        if (score.getId() == null) {
            growthScoreService.save(score);
        } else {
            growthScoreService.updateById(score);
        }
    }

    private void saveAiLearningAdvice(Long studentId, String advice) {
        QueryWrapper<EvalGrowthScore> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId).orderByDesc("update_time").last("LIMIT 1");
        EvalGrowthScore score = growthScoreService.getOne(qw);

        if (score == null) {
            score = new EvalGrowthScore();
            score.setStudentId(studentId);
            score.setTermYear("AI分析");
        }
        score.setAiLearningAdvice(advice);
        score.setAiUpdateTime(LocalDateTime.now());

        if (score.getId() == null) {
            growthScoreService.save(score);
        } else {
            growthScoreService.updateById(score);
        }
    }

    // ===== 构建分析 Prompt =====
    private String buildAnalysisPrompt(Long studentId) {
        StudentProfileDTO profile = studentService.getStudentProfile(studentId);
        List<RecordAcademic> academics = academicMapper.selectByStudentId(studentId);
        List<RecordAward> awards = awardMapper.selectByStudentId(studentId);
        List<RecordResearch> researches = researchMapper.selectByStudentId(studentId);
        List<RecordPractice> practices = practiceMapper.selectByStudentId(studentId);

        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的高校学业发展顾问。请根据以下学生的成长档案数据，");
        sb.append("从学业能力、科研能力、实践能力、综合素质四个维度进行深度分析，");
        sb.append("并给出具体的未来发展方向建议（包括考研、就业、出国等方向）。\n\n");

        if (profile != null) {
            sb.append("【基本信息】\n");
            sb.append("姓名：").append(profile.getRealName()).append("\n");
            sb.append("专业：").append(profile.getMajor()).append("\n");
            sb.append("学院：").append(profile.getCollege()).append("\n");
            sb.append("入学年份：").append(profile.getEnrollmentYear()).append("\n\n");
        }

        if (!academics.isEmpty()) {
            double totalCredit = 0, totalGpa = 0;
            for (RecordAcademic a : academics) {
                if (a.getCredit() != null && a.getGpa() != null) {
                    totalCredit += a.getCredit().doubleValue();
                    totalGpa += a.getCredit().doubleValue() * a.getGpa().doubleValue();
                }
            }
            double avgGpa = totalCredit > 0 ? totalGpa / totalCredit : 0;
            sb.append("【学业成绩】\n课程总数：").append(academics.size())
              .append("门，加权平均绩点：").append(String.format("%.2f", avgGpa)).append("\n\n");
        }

        if (!awards.isEmpty()) {
            sb.append("【获奖记录】\n");
            awards.stream().filter(a -> a.getAuditStatus() != null && a.getAuditStatus() == 1)
                  .forEach(a -> sb.append("- ").append(a.getAwardName())
                      .append("（").append(a.getAwardLevel()).append("，")
                      .append(a.getIssuingAuthority()).append("）\n"));
            sb.append("\n");
        }

        if (!researches.isEmpty()) {
            sb.append("【科研项目】\n");
            researches.forEach(r -> sb.append("- ").append(r.getProjectName())
                .append("（角色：").append(r.getRole()).append("）\n"));
            sb.append("\n");
        }

        if (!practices.isEmpty()) {
            sb.append("【实践经历】\n");
            practices.forEach(p -> sb.append("- ").append(p.getActivityName())
                .append("（").append(p.getOrganization()).append("）\n"));
            sb.append("\n");
        }

        sb.append("请用中文输出，结构清晰，分点说明，总字数控制在800字以内。");
        return sb.toString();
    }

    // ===== 构建简历 Prompt（支持风格参数）=====
    private String buildResumePrompt(Long studentId, String style) {
        StudentProfileDTO profile = studentService.getStudentProfile(studentId);
        List<RecordAcademic> academics = academicMapper.selectByStudentId(studentId);
        List<RecordAward> awards = awardMapper.selectByStudentId(studentId);
        List<RecordResearch> researches = researchMapper.selectByStudentId(studentId);
        List<RecordPractice> practices = practiceMapper.selectByStudentId(studentId);

        // 风格对应的 Prompt 前缀
        String styleInstruction = switch (style) {
            case "modern"   -> "请生成一份现代简约风格的中文个人简历（Markdown 格式）。排版简洁，突出核心亮点，使用简短有力的描述，适合互联网/科技行业投递。";
            case "academic" -> "请生成一份学术风格的中文个人简历（Markdown 格式）。重点突出科研项目、学术成果和学业成绩，语言严谨正式，适合申请研究生或学术岗位。";
            case "creative" -> "请生成一份创意风格的中文个人简历（Markdown 格式）。语言生动有个性，可适当使用 emoji 装饰，突出个人特色和创新能力，适合设计/创意类岗位。";
            default         -> "请生成一份经典专业风格的中文个人简历（Markdown 格式）。格式规范，内容全面，语言正式，适合大多数企业和岗位投递。";
        };

        StringBuilder sb = new StringBuilder(styleInstruction).append("\n\n");
        sb.append("简历应包含：个人信息、教育背景、获奖荣誉、科研经历、实践经历等模块。\n\n");

        if (profile != null) {
            sb.append("姓名：").append(profile.getRealName()).append("\n");
            sb.append("专业：").append(profile.getMajor()).append(" | 学院：").append(profile.getCollege()).append("\n");
            sb.append("入学年份：").append(profile.getEnrollmentYear()).append("\n");
            if (profile.getPhone() != null) sb.append("联系方式：").append(profile.getPhone()).append("\n");
            if (profile.getEmail() != null) sb.append("邮箱：").append(profile.getEmail()).append("\n");
        }

        if (!academics.isEmpty()) {
            double totalCredit = 0, totalGpa = 0;
            for (RecordAcademic a : academics) {
                if (a.getCredit() != null && a.getGpa() != null) {
                    totalCredit += a.getCredit().doubleValue();
                    totalGpa += a.getCredit().doubleValue() * a.getGpa().doubleValue();
                }
            }
            sb.append("GPA：").append(String.format("%.2f", totalCredit > 0 ? totalGpa / totalCredit : 0)).append("\n");
        }

        if (!awards.isEmpty()) {
            sb.append("\n获奖情况：\n");
            awards.stream().filter(a -> a.getAuditStatus() != null && a.getAuditStatus() == 1)
                  .forEach(a -> sb.append("- ").append(a.getAwardName())
                      .append("（").append(a.getAwardLevel()).append("）\n"));
        }

        if (!researches.isEmpty()) {
            sb.append("\n科研项目：\n");
            researches.forEach(r -> {
                sb.append("- ").append(r.getProjectName()).append("，担任").append(r.getRole()).append("\n");
                if (r.getDescription() != null) sb.append("  ").append(r.getDescription()).append("\n");
            });
        }

        if (!practices.isEmpty()) {
            sb.append("\n实践经历：\n");
            practices.forEach(p -> sb.append("- ").append(p.getActivityName())
                .append("，").append(p.getOrganization()).append("\n"));
        }

        return sb.toString();
    }

    // ===== 构建学习建议 Prompt =====
    private String buildLearningAdvicePrompt(Long studentId) {
        StudentProfileDTO profile = studentService.getStudentProfile(studentId);
        List<RecordAcademic> academics = academicMapper.selectByStudentId(studentId);
        List<RecordAward> awards = awardMapper.selectByStudentId(studentId);
        List<RecordResearch> researches = researchMapper.selectByStudentId(studentId);
        List<RecordPractice> practices = practiceMapper.selectByStudentId(studentId);

        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的高校学业发展顾问。请根据以下学生的成长档案数据，");
        sb.append("识别该学生的薄弱项，并给出具体的个性化学习建议。\n");
        sb.append("建议需包含以下四个部分：\n");
        sb.append("1. 【综合评估】：对学生当前学业、科研、实践、获奖四个维度的简要评价\n");
        sb.append("2. 【薄弱项分析】：指出最需要提升的1-2个维度，说明原因\n");
        sb.append("3. 【短期目标（1学期内）】：给出3-5条具体可执行的提升建议\n");
        sb.append("4. 【长期规划（1-2年）】：结合专业方向，给出考研/就业/出国的路径建议\n\n");

        if (profile != null) {
            sb.append("专业：").append(profile.getMajor())
              .append("，学院：").append(profile.getCollege())
              .append("，入学年份：").append(profile.getEnrollmentYear()).append("\n");
        }

        // 计算各维度得分情况
        if (!academics.isEmpty()) {
            double totalCredit = 0, totalGpa = 0;
            int failCount = 0;
            for (RecordAcademic a : academics) {
                if (a.getCredit() != null && a.getGpa() != null) {
                    totalCredit += a.getCredit().doubleValue();
                    totalGpa += a.getCredit().doubleValue() * a.getGpa().doubleValue();
                }
                if (a.getScore() != null && Double.parseDouble(a.getScore()) < 60) failCount++;
            }
            double avgGpa = totalCredit > 0 ? totalGpa / totalCredit : 0;
            sb.append("学业：共").append(academics.size()).append("门课，加权GPA=")
              .append(String.format("%.2f", avgGpa))
              .append("，不及格").append(failCount).append("门\n");
        } else {
            sb.append("学业：暂无成绩记录\n");
        }

        long passedAwards = awards.stream()
            .filter(a -> a.getAuditStatus() != null && a.getAuditStatus() == 1).count();
        sb.append("获奖：已审核通过").append(passedAwards).append("项\n");
        sb.append("科研：参与").append(researches.size()).append("个项目\n");
        sb.append("实践：参与").append(practices.size()).append("次实践活动\n\n");
        sb.append("请用中文输出，结构清晰，建议具体可操作，总字数控制在600字以内。");
        return sb.toString();
    }

    // ===== 构建学生推荐 Prompt（企业端，按职位推荐学生）=====
    private String buildStudentRecommendPrompt(Long recruitmentId) {
        // 获取职位信息
        BusRecruitment job = recruitmentMapper.selectById(recruitmentId);
        if (job == null) return "职位信息不存在";

        // 批量获取学生摘要
        List<java.util.Map<String, Object>> students = infoStudentMapper.selectAllStudentSummary();

        // 为每个学生补充 GPA 和获奖数
        List<java.util.Map<String, Object>> allGpa = academicMapper.selectAllStudentAvgGpa();
        java.util.Map<Object, Object> gpaMap = new java.util.HashMap<>();
        for (java.util.Map<String, Object> g : allGpa) {
            gpaMap.put(g.get("studentId"), g.get("avgGpa"));
        }

        // 获奖数：按 studentId 统计已审核通过的获奖
        List<com.example.archivemanagement.entity.RecordAward> allAwards = awardMapper.selectAllPassed();
        java.util.Map<Long, Long> awardCountMap = new java.util.HashMap<>();
        for (com.example.archivemanagement.entity.RecordAward a : allAwards) {
            awardCountMap.merge(a.getStudentId(), 1L, Long::sum);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的招聘顾问。请根据以下职位要求，从学生列表中推荐最匹配的5名候选人。\n\n");
        sb.append("【职位信息】\n");
        sb.append("职位：").append(job.getTitle()).append(" - ").append(job.getPosition()).append("\n");
        if (job.getRequirement() != null) {
            sb.append("任职要求：").append(job.getRequirement()).append("\n");
        }
        if (job.getSalaryRange() != null) sb.append("薪资：").append(job.getSalaryRange()).append("\n");
        if (job.getLocation() != null) sb.append("地点：").append(job.getLocation()).append("\n\n");

        sb.append("【学生候选人列表（最多30人）】\n");
        int count = 0;
        for (java.util.Map<String, Object> s : students) {
            if (count++ >= 30) break;
            Object uid = s.get("userId");
            Object gpa = gpaMap.get(uid);
            Long awards = awardCountMap.getOrDefault(uid instanceof Long ? (Long) uid
                    : (uid != null ? Long.valueOf(uid.toString()) : 0L), 0L);
            sb.append("- [").append(uid).append("] ")
              .append(s.get("realName")).append("，")
              .append(s.get("major")).append("（").append(s.get("college")).append("），")
              .append(s.get("enrollmentYear")).append("级，")
              .append("GPA=").append(gpa != null ? String.format("%.2f", Double.parseDouble(gpa.toString())) : "暂无")
              .append("，获奖").append(awards).append("项\n");
        }

        sb.append("\n请按匹配度从高到低推荐5名学生，每人说明：\n");
        sb.append("1. 姓名和学号\n2. 匹配原因（结合专业/GPA/获奖）\n3. 匹配度评分（1-10分）\n");
        sb.append("请用中文输出，结构清晰，总字数控制在600字以内。");
        return sb.toString();
    }

    // ===== 构建职位推荐 Prompt =====
    private String buildJobRecommendPrompt(Long studentId) {
        StudentProfileDTO profile = studentService.getStudentProfile(studentId);
        List<RecordAcademic> academics = academicMapper.selectByStudentId(studentId);
        List<RecordAward> awards = awardMapper.selectByStudentId(studentId);
        List<RecordResearch> researches = researchMapper.selectByStudentId(studentId);
        List<RecordPractice> practices = practiceMapper.selectByStudentId(studentId);

        // 获取当前招聘中的职位（最多20条）
        QueryWrapper<BusRecruitment> qw = new QueryWrapper<>();
        qw.eq("status", 1).orderByDesc("create_time").last("LIMIT 20");
        List<BusRecruitment> jobs = recruitmentMapper.selectList(qw);

        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的职业规划顾问。请根据以下学生档案信息和当前招聘职位列表，");
        sb.append("完成两项任务：\n\n");
        sb.append("【任务一：职位匹配推荐】\n");
        sb.append("从招聘列表中选出最匹配该学生的3-5个职位，每个职位说明：\n");
        sb.append("- 职位名称和公司\n- 匹配原因（结合学生的专业/技能/经历）\n- 匹配度评分（1-10分）\n\n");
        sb.append("【任务二：未来规划建议】\n");
        sb.append("结合学生档案，给出三条发展路径的分析：\n");
        sb.append("- 就业方向：适合的行业/岗位类型，需要补充的技能\n");
        sb.append("- 考研方向：适合的研究方向，备考建议\n");
        sb.append("- 出国方向：适合的国家/专业，申请条件分析\n\n");

        // 学生信息
        if (profile != null) {
            sb.append("【学生档案】\n");
            sb.append("专业：").append(profile.getMajor())
              .append("，学院：").append(profile.getCollege())
              .append("，入学年份：").append(profile.getEnrollmentYear()).append("\n");
        }

        if (!academics.isEmpty()) {
            double totalCredit = 0, totalGpa = 0;
            for (RecordAcademic a : academics) {
                if (a.getCredit() != null && a.getGpa() != null) {
                    totalCredit += a.getCredit().doubleValue();
                    totalGpa += a.getCredit().doubleValue() * a.getGpa().doubleValue();
                }
            }
            sb.append("GPA：").append(String.format("%.2f", totalCredit > 0 ? totalGpa / totalCredit : 0)).append("\n");
        }

        long passedAwards = awards.stream()
            .filter(a -> a.getAuditStatus() != null && a.getAuditStatus() == 1).count();
        if (passedAwards > 0) sb.append("获奖：").append(passedAwards).append("项已审核通过\n");
        if (!researches.isEmpty()) {
            sb.append("科研：");
            researches.forEach(r -> sb.append(r.getProjectName()).append("（").append(r.getRole()).append("）；"));
            sb.append("\n");
        }
        if (!practices.isEmpty()) {
            sb.append("实践：");
            practices.forEach(p -> sb.append(p.getActivityName()).append("；"));
            sb.append("\n");
        }

        // 招聘列表
        if (!jobs.isEmpty()) {
            sb.append("\n【当前招聘职位列表】\n");
            jobs.forEach(j -> sb.append("- [").append(j.getId()).append("] ")
                .append(j.getTitle()).append(" | ").append(j.getPosition())
                .append(" | ").append(j.getSalaryRange() != null ? j.getSalaryRange() : "薪资面议")
                .append(" | ").append(j.getLocation() != null ? j.getLocation() : "")
                .append("\n  要求：").append(j.getRequirement() != null
                    ? j.getRequirement().substring(0, Math.min(80, j.getRequirement().length())) : "")
                .append("\n"));
        } else {
            sb.append("\n【当前暂无招聘职位，请仅完成任务二】\n");
        }

        sb.append("\n请用中文输出，结构清晰，总字数控制在800字以内。");
        return sb.toString();
    }

    // ===== 调用千问 API =====
    private String callQwen(String userPrompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "qwen-plus");
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", userPrompt);
        messages.add(msg);
        body.put("messages", messages);
        body.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(QWEN_API_URL, entity, Map.class);
            Map respBody = response.getBody();
            if (respBody != null && respBody.containsKey("choices")) {
                List choices = (List) respBody.get("choices");
                if (!choices.isEmpty()) {
                    Map choice = (Map) choices.get(0);
                    Map message = (Map) choice.get("message");
                    return (String) message.get("content");
                }
            }
            return "AI 分析暂时不可用，请稍后重试";
        } catch (Exception e) {
            return "AI 服务调用失败：" + e.getMessage();
        }
    }
}
