package springBootUEditorOSS.demo.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface ActionEnterService {
    void init(HttpServletRequest request, String rootPath, String configJsonPath);

    void init(HttpServletRequest request, MultipartFile uploadfile, String rootPath, String configJsonPath);

    void init(HttpServletRequest request, String source, String rootPath, String configJsonPath);

    String exec();
}
