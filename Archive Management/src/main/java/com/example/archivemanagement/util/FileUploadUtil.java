package com.example.archivemanagement.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUploadUtil {
    private static final String BASE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /**
     * 上传文件到用户专属目录（无类型限制版本，用于作品集等场景）
     */
    public static String uploadFileAny(MultipartFile file, Long userId, String subDir) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("文件名不合法");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        String userDirPath = BASE_UPLOAD_DIR + userId + "/" + subDir;
        File userDir = new File(userDirPath);
        if (!userDir.exists()) userDir.mkdirs();
        String filename = UUID.randomUUID().toString() + ext;
        file.transferTo(new File(userDir, filename));
        return "/uploads/" + userId + "/" + subDir + "/" + filename;
    }

    /**
     * 上传文件到用户专属目录
     * @param file 上传的文件
     * @param userId 用户ID
     * @param subDir 子目录（如"portfolio", "award"等）
     * @return 相对路径，用于存储到数据库
     * @throws IOException 上传失败时抛出异常
     * @throws IllegalArgumentException 文件类型不符合要求时抛出异常
     */
    public static String uploadFile(MultipartFile file, Long userId, String subDir) throws IOException, IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();

        // portfolio/diary/research/cover/forum 子目录支持所有文件类型（包括视频）
        if (!"portfolio".equals(subDir) && !"diary".equals(subDir)
                && !"research".equals(subDir) && !"cover".equals(subDir)
                && !"forum".equals(subDir) && !"apply".equals(subDir)) {
            List<String> allowedExtensions = Arrays.asList(
                ".png", ".jpg", ".jpeg", ".gif",
                ".pdf", ".doc", ".docx", ".xls", ".xlsx",
                ".zip", ".rar", ".7z",
                ".txt", ".md",
                ".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm",
                ".mp3", ".wav", ".flac", ".aac", ".ogg"
            );
            if (!allowedExtensions.contains(fileExtension)) {
                throw new IllegalArgumentException("文件类型不符合要求，支持图片、文档、压缩包、视频、音频等常见格式");
            }
        }

        // 创建用户专属目录结构：uploads/{userId}/{subDir}
        String userDirPath = BASE_UPLOAD_DIR + userId + "/" + subDir;
        File userDir = new File(userDirPath);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        // 生成唯一文件名
        String filename = UUID.randomUUID().toString() + fileExtension;

        // 保存文件
        File dest = new File(userDir, filename);
        file.transferTo(dest);

        // 返回相对路径，用于存储到数据库
        return "/uploads/" + userId + "/" + subDir + "/" + filename;
    }

    /**
     * 将文件转换为base64字符串
     * @param file 上传的文件
     * @return base64字符串
     * @throws IOException 转换失败时抛出异常
     */
    public static String fileToBase64(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        byte[] bytes = file.getBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 解析压缩包文件结构
     * @param file 上传的压缩包文件
     * @return 文件结构JSON字符串
     * @throws IOException 解析失败时抛出异常
     */
    public static String parseZipFileStructure(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".zip") && !originalFilename.endsWith(".rar"))) {
            return null;
        }

        Map<String, Object> root = new HashMap<>();
        root.put("name", originalFilename.substring(0, originalFilename.lastIndexOf('.')));
        root.put("type", "folder");
        root.put("expanded", true);
        List<Map<String, Object>> children = new ArrayList<>();
        root.put("children", children);

        try (InputStream inputStream = file.getInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String filePath = entry.getName();
                    addFileToTree(children, filePath);
                }
                zipInputStream.closeEntry();
            }
        }

        return buildTreeJson(root);
    }

    /**
     * 将文件路径添加到树结构中
     * @param parentChildren 父节点的子节点列表
     * @param filePath 文件路径
     */
    public static void addFileToTree(List<Map<String, Object>> parentChildren, String filePath) {
        String[] pathParts = filePath.split("/");
        List<Map<String, Object>> currentLevel = parentChildren;

        for (int i = 0; i< pathParts.length; i++) {
            String part = pathParts[i];
            boolean isFile = (i == pathParts.length - 1);

            Map<String, Object> existingNode = findNode(currentLevel, part);
            if (existingNode == null) {
                Map<String, Object> newNode = new HashMap<>();
                newNode.put("name", part);
                newNode.put("type", isFile ? "file" : "folder");
                newNode.put("expanded", !isFile);

                if (!isFile) {
                    newNode.put("children", new ArrayList<Map<String, Object>>());
                } else {
                    String extension = part.contains(".") ? part.substring(part.lastIndexOf('.') + 1) : "file";
                    newNode.put("extension", extension);
                }

                currentLevel.add(newNode);
                currentLevel = isFile ? null : (List<Map<String, Object>>) newNode.get("children");
            } else {
                currentLevel = isFile ? null : (List<Map<String, Object>>) existingNode.get("children");
            }
        }
    }

    /**
     * 在节点列表中查找指定名称的节点
     * @param nodes 节点列表
     * @param name 节点名称
     * @return 找到的节点，未找到则返回null
     */
    private static Map<String, Object> findNode(List<Map<String, Object>> nodes, String name) {
        for (Map<String, Object> node : nodes) {
            if (name.equals(node.get("name"))) {
                return node;
            }
        }
        return null;
    }

    /**
     * 将树结构转换为JSON字符串
     * @param node 树节点
     * @return JSON字符串
     */
    public static String buildTreeJson(Map<String, Object> node) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        sb.append("\"name\": \"").append(escapeJson((String) node.get("name"))).append("\",");
        sb.append("\"type\": \"").append(node.get("type")).append("\",");
        sb.append("\"expanded\": ").append(node.get("expanded"));
        
        if ("folder".equals(node.get("type"))) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            sb.append(",\"children\": [");
            for (int i = 0; i< children.size(); i++) {
                sb.append(buildTreeJson(children.get(i)));
                if (i < children.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
        } else if ("file".equals(node.get("type"))) {
            sb.append(",\"extension\": \"").append(node.get("extension")).append("\"");
        }
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * 转义JSON字符串
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 获取基础上传目录
     * @return 基础上传目录路径
     */
    public static String getBaseUploadDir() {
        return BASE_UPLOAD_DIR;
    }

    /**
     * 确保上传目录存在
     */
    public static void ensureUploadDirExists() {
        File baseDir = new File(BASE_UPLOAD_DIR);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }
}
