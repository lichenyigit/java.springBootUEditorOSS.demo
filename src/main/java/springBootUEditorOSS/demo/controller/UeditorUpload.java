package springBootUEditorOSS.demo.controller;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springBootUEditorOSS.demo.service.ActionEnterService;
import springBootUEditorOSS.demo.utils.CommonUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

@RestController
public class UeditorUpload {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ActionEnterService actionEnterService;

    @GetMapping("/manager/UeditorUploadImgServlet")
    public String get(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, FileNotFoundException {
        logger.info("GET");
        try {
            logger.info(JSON.toJSONString(CommonUtil.resquestParameter2Map(request)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String rootPath = ResourceUtils.getURL("classpath:").getPath();
        String configJsonPath = rootPath + "/static/UEditor/config.json";//获取文件路径
        request.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "text/html");
        actionEnterService.init(request, rootPath, configJsonPath);
        String result = actionEnterService.exec();
        return result;
    }

    @PostMapping("/manager/UeditorUploadImgServlet")
    public String post(HttpServletRequest request, @RequestParam(value = "upfile", required = false) MultipartFile uploadfile) throws Exception {
        logger.info("POST");
        logger.info(JSON.toJSONString(CommonUtil.resquestParameter2Map(request)));
        String rootPath = ResourceUtils.getURL("classpath:").getPath();
        String configJsonPath = rootPath + "/static/UEditor/config.json";//获取文件路径
        if(uploadfile != null){
            actionEnterService.init(request, uploadfile, rootPath, configJsonPath);
        }else{
            String source = request.getParameter("source[]");
            actionEnterService.init(request, source, rootPath, configJsonPath);
        }
        return actionEnterService.exec();
    }

}
