package com.hanafn.openapi.portal.file;

import com.hanafn.openapi.portal.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    @Autowired
    MessageSourceAccessor messageSource;

    private final Path fileLocation;
    private static final String FILE_DIR = "/openapi/upload";	// 파일저장경로
    private static final String CRYPTO_FILE_DIR = "/openapi/enc";
//    private static final String CRYPTO_FILE_DIR = "C:\\openapi\\enc";
//    private static final String FILE_DIR = "C:\\openapi\\upload";	// 파일저장경로

    @Autowired
    public FileService(FileUploadProperties prop) {
        this.fileLocation = Paths.get(prop.getUploadDir())
                .toAbsolutePath().normalize();
        log.debug("★"+fileLocation.toString());
        try {
            Files.createDirectories(this.fileLocation);
        }catch(Exception e) {
            log.error("FileService 업로드 에러 [ " + this.fileLocation +  " ] error:" + e.toString());
            throw new FileUploadException(messageSource.getMessage("F001"), e);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // 파일명에 부적합 문자가 있는지 확인한다.
            if(fileName.contains(".."))
                throw new FileUploadException("파일명에 부적합 문자가 포함되어 있습니다. " + fileName);

            Path targetLocation = this.fileLocation.resolve(fileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        }catch(Exception e) {
            throw new FileUploadException("["+fileName+"] 파일 업로드에 실패하였습니다. 다시 시도하십시오.",e);
        }
    }

    public String fileSave(MultipartFile multipartfile) throws IOException {

        String path = "";
        if (multipartfile != null && !multipartfile.getOriginalFilename().isEmpty()) {
            String fileName = multipartfile.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String subPath = String.format("%s", uuid);
            File tmpDir = new File(getDir(), subPath);
            if(!tmpDir.isDirectory()) {
                tmpDir.mkdirs();
            }

            File target = new File(tmpDir, fileName);

            if (target.exists()) {
                fileName = System.currentTimeMillis() + "_" + fileName;
                target = new File (FILE_DIR + '\\' + fileName);
            }

            byte[] bytes = multipartfile.getBytes();
            FileOutputStream fos = new FileOutputStream(target);
            fos.write(bytes);
            fos.close();

            path = target.getAbsolutePath();
        }

        return path;
    }

    public String cryptoFileSave(MultipartFile multipartfile) throws IOException {

        String path = "";
        if (multipartfile != null && !multipartfile.getOriginalFilename().isEmpty()) {
            String fileName = multipartfile.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String subPath = String.format("%s", uuid);
            File tmpDir = new File(getCryptoFileDir(), subPath);
            if(!tmpDir.isDirectory()) {
                tmpDir.mkdirs();
            }

            File target = new File(tmpDir, fileName);

            if (target.exists()) {
                fileName = System.currentTimeMillis() + "_" + fileName;
                target = new File (CRYPTO_FILE_DIR + '\\' + fileName);
            }

            byte[] bytes = multipartfile.getBytes();
            FileOutputStream fos = new FileOutputStream(target);
            fos.write(bytes);
            fos.close();

            path = target.getAbsolutePath();
        }

        return path;
    }

    // 파일 디렉토리 생성
    public File getDir() {
        File f = new File(FILE_DIR);
        if(!f.isDirectory()) {
            f.mkdirs();
        }
        return f;
    }

    // 파일 디렉토리 생성
    public File getCryptoFileDir() {
        File f = new File(CRYPTO_FILE_DIR);
        if(!f.isDirectory()) {
            f.mkdirs();
        }
        return f;
    }


    public Resource loadFileAsResource(String fileName) {
        if(fileName.length() == 0){
            log.error("파일 로딩 에러 " + fileName);
            throw new BusinessException("E090",messageSource.getMessage("E090"));
        }
        try {
            Path filePath = this.fileLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if(resource.exists()) {
                log.debug("★resource가 존재합니다.");
                return resource;
            }else {
                log.error("파일 resource 존재 하지않습니다." + filePath.toUri());
                throw new BusinessException("E091",messageSource.getMessage("E091"));
            }
        }catch(MalformedURLException e) {
            log.error(e.toString());
            throw new BusinessException("E091",messageSource.getMessage("E091"));
        }
    }

    public int fileDelete(String filePath) throws IOException {

        File file = new File(filePath);

        if(file.exists()) {
            if(file.delete()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public int dirDelete(String filePath) {
        File f = new File(filePath);
        String dirPath = f.getParent();

        File parentDir = new File(dirPath);

        if(parentDir.isDirectory()) {
            if(parentDir.delete()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    /** 사업자 등록증 다운로드 서비스 **/
    public void UseorgUploadDownloadService() {
    }
}
