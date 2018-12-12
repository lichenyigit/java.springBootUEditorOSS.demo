package springBootUEditorOSS.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AliyunProperties {

    @Value("${aliyun.access.id}")
    private String accessId;

    @Value("${aliyun.access.key}")
    private String accessKey;

    @Value("${aliyun.oss.endpoint}")
    private String ossEndpoint;

    @Value("${aliyun.oss.bucket}")
    private String ossBucket;

    @Value("${aliyun.oss.host}")
    private String host;

    @Value("${aliyun.oss.dir.ueditor}")
    private String ossUeditorDir;

    public String getAccessId() {
        return accessId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getOssEndpoint() {
        return ossEndpoint;
    }

    public String getOssBucket() {
        return ossBucket;
    }

    public String getOssUeditorDir() {
        return ossUeditorDir;
    }

    /**
     * 获取oss host "https://"+ ossBucket+"."+ossEndpoint;
     * @return
     */
    public String getOssHost(){
        return "https://"+ ossBucket+"."+ossEndpoint;
    }

    /**
     * 获取访问的路径
     * @return
     */
    public String getHost(){
        return host;
    }
}
