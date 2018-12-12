package springBootUEditorOSS.demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public interface IAliYunService {
    Map<String, String> getToken(String dir);

    String upload(MultipartFile file, String dir) throws Exception;

    String upload(File file, String dir);

    boolean deleteOSS(String fileName);

    String getFileName(String url);

    void createTempFile(String path, InputStream inputStream);

    void removeTempFile(String path);

    Map<String, String> download(String urlString, String savePath) throws Exception;
}
