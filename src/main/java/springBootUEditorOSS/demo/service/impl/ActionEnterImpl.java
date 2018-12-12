package springBootUEditorOSS.demo.service.impl;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springBootUEditorOSS.demo.config.AliyunProperties;
import springBootUEditorOSS.demo.service.ActionEnterService;
import springBootUEditorOSS.demo.service.IAliYunService;
import springBootUEditorOSS.demo.ueditor.ConfigManager;
import springBootUEditorOSS.demo.ueditor.define.ActionMap;
import springBootUEditorOSS.demo.ueditor.define.AppInfo;
import springBootUEditorOSS.demo.ueditor.define.BaseState;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("ActionEnterService")
public class ActionEnterImpl implements ActionEnterService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IAliYunService aliYunService;

    @Autowired
    private AliyunProperties aliyunProperties;

    private HttpServletRequest request = null;
    private String source = null;//截图上传
    private MultipartFile uploadfile = null;
    private String rootPath = null;
    private String contextPath = null;
    private String actionType = null;
    private ConfigManager configManager = null;

    @Override
    public void init(HttpServletRequest request, String rootPath, String configJsonPath) {
        this.request = request;
        this.rootPath = rootPath;
        this.actionType = request.getParameter("action");
        this.contextPath = request.getContextPath();
        this.configManager = ConfigManager.getInstance(this.rootPath, this.contextPath, configJsonPath);
    }

    @Override
    public void init(HttpServletRequest request, MultipartFile uploadfile, String rootPath, String configJsonPath) {
        this.request = request;
        this.uploadfile = uploadfile;
        this.rootPath = rootPath;
        this.actionType = request.getParameter("action");
        this.contextPath = request.getContextPath();
        this.configManager = ConfigManager.getInstance(this.rootPath, this.contextPath, configJsonPath);
    }

    @Override
    public void init(HttpServletRequest request, String source, String rootPath, String configJsonPath) {
        this.request = request;
        this.source = source;
        this.rootPath = rootPath;
        this.actionType = request.getParameter("action");
        this.contextPath = request.getContextPath();
        this.configManager = ConfigManager.getInstance(this.rootPath, this.contextPath, configJsonPath);
    }

    @Override
    public String exec() {

        try {
            return this.invoke();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String invoke() throws Exception {

        if (actionType == null || !ActionMap.mapping.containsKey(actionType)) {
            return new BaseState(false, AppInfo.INVALID_ACTION).toJSONString();
        }

        if (this.configManager == null || !this.configManager.valid()) {
            return new BaseState(false, AppInfo.CONFIG_ERROR).toJSONString();
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        String url;
        int actionCode = ActionMap.getType(this.actionType);
        switch (actionCode) {

            case ActionMap.CONFIG:
                return this.configManager.getAllConfig().toString();

            case ActionMap.UPLOAD_IMAGE:
            case ActionMap.UPLOAD_SCRAWL:
            case ActionMap.UPLOAD_VIDEO:
            case ActionMap.UPLOAD_FILE:
                url = aliYunService.upload(uploadfile, aliyunProperties.getOssUeditorDir());
                resultMap.put("type", uploadfile.getContentType());//文件类型
                resultMap.put("original", uploadfile.getOriginalFilename());//文件名称
                resultMap.put("url", url);//oss访问连接
                break;

            case ActionMap.CATCH_IMAGE:
                /* 此处代码根据文档写是错误的，所以自己手动解析了js，重新按照js解析的格式进行拼装 */
                List<Map<String, String>> list = new ArrayList<>();
                Map<String, String> itemMap = new HashMap<>();

                Map<String, String> map = aliYunService.download(source, rootPath);//将网络端文件下载到本地
                String filename = map.get("filename");//文件名
                String localPath = map.get("localPath");//本地路径
                String suffix = map.get("suffix");//文件后缀
                File file = new File(localPath);
                url = aliYunService.upload(file, aliyunProperties.getOssUeditorDir());//本地文件上传到阿里云，并返回oss连接
                aliYunService.removeTempFile(localPath);
                logger.info(url);
                itemMap.put("url", url);//阿里云上传路径
                itemMap.put("source", source);//截图原路径
                itemMap.put("state", "SUCCESS");
                list.add(itemMap);
                resultMap.put("list", list);
                break;

            case ActionMap.LIST_IMAGE:
            case ActionMap.LIST_FILE:
                break;

        }

        /**
         * add by lichenyi
         */
        resultMap.put("state", "SUCCESS");
        return JSON.toJSONString(resultMap);

    }

    public int getStartIndex() {

        String start = this.request.getParameter("start");

        try {
            return Integer.parseInt(start);
        } catch (Exception e) {
            return 0;
        }

    }

}
