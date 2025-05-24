package com.buguagaoshu.tiktube.file;

import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.repository.FileRepository;
import com.buguagaoshu.tiktube.repository.impl.FileRepositoryInOSS;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.FileTableService;
import com.buguagaoshu.tiktube.utils.AesUtil;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import com.buguagaoshu.tiktube.vo.VditorFiles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-24
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {
    
    private final FileRepository fileRepository;

    private final ArticleService articleService;

    private final FileTableService fileTableService;

    private final ResourceLoader resourceLoader;

    private final FileRepositoryInOSS fileRepositoryInOSS;

    @Autowired
    public FileController(FileRepository fileRepository,
                          ArticleService articleService,
                          FileTableService fileTableService,
                          ResourceLoader resourceLoader,
                          FileRepositoryInOSS fileRepositoryInOSS) {
        this.fileRepository = fileRepository;
        this.articleService = articleService;
        this.fileTableService = fileTableService;
        this.resourceLoader = resourceLoader;
        this.fileRepositoryInOSS = fileRepositoryInOSS;
    }

    @PostMapping("/upload")
    public ResponseDetails uploadFiles(@RequestParam("files") MultipartFile[] files,
                                     @RequestParam("type") Integer type,
                                     @RequestParam("userId") Long userId) {
        try {
            List<FileTableEntity> result = fileRepository.videoAndPhotoSave(files, type, userId);
            return ResponseDetails.ok().put("data", result);
        } catch (FileNotFoundException e) {
            log.error("文件上传失败", e);
            return ResponseDetails.ok(500, e.getMessage());
        }
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            File file = fileRepository.load(filename).toFile();
            return ResponseEntity.ok()
                    .body(new FileSystemResource(file));
        } catch (FileNotFoundException e) {
            log.error("文件未找到: {}", filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api/upload/article")
    @ResponseBody
    public VditorFiles save(@RequestParam(value = "file[]") MultipartFile[] files,
                           HttpServletRequest request) {
        return fileRepository.vditorFileSave(files, null);
    }

    @PostMapping("/api/upload/video")
    @ResponseBody
    public ResponseDetails saveVideo(@RequestParam(value = "file[]") MultipartFile[] files,
                                HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        try {
            List<FileTableEntity> list = fileRepository.videoAndPhotoSave(files, FileTypeEnum.VIDEO.getCode(), userId);
            return ResponseDetails.ok().put("data", list);
        } catch (FileNotFoundException e) {
            return ResponseDetails.ok(ReturnCodeEnum.DATA_VALID_EXCEPTION).put("data", e.getMessage());
        }
    }

    @PostMapping("/api/upload/photo")
    @ResponseBody
    public ResponseDetails savePhoto(@RequestParam(value = "file[]") MultipartFile[] files,
                                     HttpServletRequest request) throws FileNotFoundException {
        long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok()
                .put("data",
                        fileRepository.videoAndPhotoSave(files, FileTypeEnum.PHOTO.getCode(), userId));
    }


    @PostMapping("/api/upload/avatar")
    @ResponseBody
    public ResponseDetails saveAvatar(@RequestParam(value = "file[]") MultipartFile[] files,
                                 HttpServletRequest request) throws FileNotFoundException {
        long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok()
                .put("data",
                        fileRepository.videoAndPhotoSave(files, FileTypeEnum.AVATAR.getCode(), userId));
    }


    @PostMapping("/api/upload/top")
    @ResponseBody
    public ResponseDetails saveTop(@RequestParam(value = "file[]") MultipartFile[] files,
                                      HttpServletRequest request) throws FileNotFoundException {
        long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok()
                .put("data",
                        fileRepository.videoAndPhotoSave(files, FileTypeEnum.TOP_IMAGE.getCode(),  userId));
    }

    @GetMapping("/api/upload/{code}/oss/{year}/{month}/{day}/{filename:.+}")
    public ResponseEntity<Void> getOSS(@PathVariable(value = "code") Integer code,
                                       @PathVariable(value = "year") String year,
                                       @PathVariable(value = "month") String month,
                                       @PathVariable(value = "day") String day,
                                       @PathVariable(value = "filename") String filename,
                                       @RequestParam(value = "key", required = false) String key,
                                       HttpServletRequest request) {
        // 创建HttpHeaders对象
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "max-age=86400");
        String date = year + "/" + month + "/" + day;
        if (FileTypeEnum.getFileType(FileTypeEnum.getFileSuffix(filename)).equals(FileTypeEnum.VIDEO)) {
            FileTableEntity fileTableEntity = fileTableService.findFileByFilename(filename);
            if (fileTableEntity == null) {
                return null;
            }
            // 非视频区投稿视频直接放行
            if (fileTableEntity.getArticleId() == null) {
                headers.add(HttpHeaders.LOCATION, fileRepositoryInOSS.getFileUrl(date + "/" + filename, code));
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            }
            // 视频文件 key 错误直接返回 null
            if (key == null) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            String originalText = AesUtil.getInstance().decrypt(key);
            if (originalText == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            // 第一个值是用户ID，如果访问时没有登录，则这个值是 -1
            // 第二个值是文件ID，进行文件查找
            // 第三个值是过期时间
            // 第四个是文件名
            String[] msg = originalText.split("#");
            Long userId = Long.parseLong(msg[0]);
            Long fileId = Long.parseLong(msg[1]);
            Long expire = Long.parseLong(msg[2]);
            // 先进行过期时间的判断
            if (System.currentTimeMillis() >expire) {
                log.warn("用户 {} 访问文件id为 {} 的的文件时，使用的 key {} 已过期!", msg[0], msg[1], key);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            // 解析出的文件ID不同，则返回null
            if (!fileTableEntity.getId().equals(fileId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (articleService.hasThisVideoPlayPower(fileTableEntity, userId, request)) {
                headers.add(HttpHeaders.LOCATION, fileRepositoryInOSS.getFileUrl(date + "/" + filename, code));
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            headers.add(HttpHeaders.LOCATION, fileRepositoryInOSS.getFileUrl(date + "/" + filename, code));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    @GetMapping("/api/upload/file/{year}/{month}/{day}/{filename:.+}")
    public ResponseEntity get(@PathVariable(value = "year") String year,
                              @PathVariable(value = "month") String month,
                              @PathVariable(value = "day") String day,
                              @PathVariable(value = "filename") String filename,
                              @RequestParam(value = "key", required = false) String key,
                              HttpServletRequest request) {
        String date = year + "/" + month + "/" + day;
        return getFile(date, filename, key, request);
    }


    @GetMapping("/api/upload/file/{date}/{filename:.+}")
    public ResponseEntity get(@PathVariable(value = "date") String date,
                                      @PathVariable(value = "filename") String filename,
                                      @RequestParam(value = "key", required = false) String key,
                                      HttpServletRequest request) {
        return getFile(date, filename, key, request);

    }

    public ResponseEntity getFile(String date, String filename, String key, HttpServletRequest request) {
        try {
            Path path = fileRepository.load(date + "/" + filename);
            Resource resource = new UrlResource(path.toUri());
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
            httpHeaders.add("Cache-Control", "max-age=86400");
            // TODO 优化在使用安卓版 Edge 在播放视频时，多次循环请求接口，导致多次查询数据库的的问题
            // TODO 优化判断逻辑
            // TODO 拆分函数
            // 判断是否是视频
            if (FileTypeEnum.getFileType(FileTypeEnum.getFileSuffix(filename)).equals(FileTypeEnum.VIDEO)) {
                FileTableEntity fileTableEntity = fileTableService.findFileByFilename(filename);
                if (fileTableEntity == null) {
                    return null;
                }
                // 非视频区投稿视频直接放行
                if (fileTableEntity.getArticleId() == null) {
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .headers(httpHeaders)
                            .body(resource);
                }
                // 视频文件 key 错误直接返回 null
                if (key == null) {
                    return null;
                }
                String originalText = AesUtil.getInstance().decrypt(key);
                if (originalText == null) {
                    return null;
                }
                // 第一个值是用户ID，如果访问时没有登录，则这个值是 -1
                // 第二个值是文件ID，进行文件查找
                // 第三个值是过期时间
                // 第四个是文件名
                String[] msg = originalText.split("#");
                Long userId = Long.parseLong(msg[0]);
                Long fileId = Long.parseLong(msg[1]);
                Long expire = Long.parseLong(msg[2]);
                // 先进行过期时间的判断
                if (System.currentTimeMillis() >expire) {
                    log.warn("用户 {} 访问文件id为 {} 的的文件时，使用的 key {} 已过期!", msg[0], msg[1], key);
                    return null;
                }
                // 解析出的文件ID不同，则返回null
                if (!fileTableEntity.getId().equals(fileId)) {
                    return null;
                }
                if (articleService.hasThisVideoPlayPower(fileTableEntity, userId, request)) {
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .headers(httpHeaders)
                            .body(resource);
                } else {
                    return null;
                }
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(httpHeaders)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
