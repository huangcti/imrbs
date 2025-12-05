package tw.huangcti.imrbs.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tw.huangcti.imrbs.domain.exception.ValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * T122 [P] [US4] 檔案上傳服務
 * 職責: 處理會議室照片上傳、驗證、儲存
 * 
 * 實作策略:
 * - MVP: 本地檔案系統儲存 (適用於單機部署)
 * - Production: 可擴展至 Azure Blob Storage, AWS S3, MinIO (物件儲存)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    // 允許的圖片類型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    // 最大檔案大小: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Value("${app.file-upload.base-path:./uploads/rooms}")
    private String uploadBasePath;

    @Value("${app.file-upload.base-url:http://localhost:8080/uploads/rooms}")
    private String uploadBaseUrl;

    /**
     * 上傳會議室照片
     * 
     * @param file 上傳的檔案
     * @param roomId 會議室 ID
     * @return 檔案存取 URL
     * @throws ValidationException 檔案驗證失敗
     * @throws IOException 檔案儲存失敗
     */
    public String uploadRoomPhoto(MultipartFile file, Long roomId) throws IOException {
        // 1. 驗證檔案
        validateFile(file);

        // 2. 生成唯一檔案名稱
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(roomId, fileExtension);

        // 3. 建立目錄結構: uploads/rooms/{roomId}/
        Path roomDirectory = Paths.get(uploadBasePath, String.valueOf(roomId));
        Files.createDirectories(roomDirectory);

        // 4. 儲存檔案
        Path targetPath = roomDirectory.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("檔案上傳成功: roomId={}, filename={}, size={} bytes", 
                roomId, uniqueFilename, file.getSize());

        // 5. 回傳存取 URL
        return String.format("%s/%s/%s", uploadBaseUrl, roomId, uniqueFilename);
    }

    /**
     * 刪除會議室照片
     * 
     * @param photoUrl 照片 URL
     * @throws IOException 檔案刪除失敗
     */
    public void deleteRoomPhoto(String photoUrl) throws IOException {
        // 從 URL 解析檔案路徑
        // 範例: http://localhost:8080/uploads/rooms/123/photo.jpg -> uploads/rooms/123/photo.jpg
        String relativePath = photoUrl.replace(uploadBaseUrl + "/", "");
        Path filePath = Paths.get(uploadBasePath, relativePath);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("檔案刪除成功: path={}", filePath);
        } else {
            log.warn("檔案不存在，無需刪除: path={}", filePath);
        }
    }

    /**
     * 驗證上傳檔案
     */
    private void validateFile(MultipartFile file) {
        // 檢查檔案是否為空
        if (file == null || file.isEmpty()) {
            throw new ValidationException("上傳檔案不能為空");
        }

        // 檢查檔案大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException(
                String.format("檔案大小超過限制 (最大 %d MB)", MAX_FILE_SIZE / 1024 / 1024)
            );
        }

        // 檢查檔案類型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new ValidationException(
                "不支援的檔案類型，僅允許: " + String.join(", ", ALLOWED_IMAGE_TYPES)
            );
        }
    }

    /**
     * 取得檔案副檔名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 生成唯一檔案名稱
     * 格式: {timestamp}_{uuid}{extension}
     * 範例: 20250120_123456_a1b2c3d4.jpg
     */
    private String generateUniqueFilename(Long roomId, String extension) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s%s", timestamp, uuid, extension);
    }
}
