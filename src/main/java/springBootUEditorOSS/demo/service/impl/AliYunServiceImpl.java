package springBootUEditorOSS.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PolicyConditions;
import com.aliyun.oss.model.PutObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springBootUEditorOSS.demo.config.AliyunProperties;
import springBootUEditorOSS.demo.service.IAliYunService;
import springBootUEditorOSS.demo.utils.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service("IAliYunService")
public class AliYunServiceImpl implements IAliYunService {
    private Logger logger = LoggerFactory.getLogger(AliYunServiceImpl.class);

    @Autowired
    public AliyunProperties aliyuncsProperties;

    @Override
    public Map<String, String> getToken(String dir) {
        String endpoint = aliyuncsProperties.getOssEndpoint();
        String accessId = aliyuncsProperties.getAccessId();
        String accessKey = aliyuncsProperties.getAccessKey();
        String bucket = aliyuncsProperties.getOssBucket();
        String host = aliyuncsProperties.getOssHost();
        OSSClient client = new OSSClient(endpoint, accessId, accessKey);
        try {
            long expireTime = 5 * 1000;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);//1GB
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            respMap.put("requestUrl", aliyuncsProperties.getHost());

            return respMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * OSS servlet 上传文件
     *
     * @param file
     * @param dir
     * @return
     * @throws Exception
     */
    @Override
    public String upload(MultipartFile file, String dir) throws Exception {
        String fileName = file.getOriginalFilename();
        logger.info("开始上传" + fileName);
        fileName = dir + "/" + fileName;
        Long fileSize = file.getSize();
        //创建上传object的metadata
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileSize);
        metadata.setCacheControl("no-cache");
        metadata.setHeader("Pragma", "no-cache");
        metadata.setContentEncoding("utf-8");
        metadata.setContentType(file.getContentType());
        metadata.setContentDisposition("filename/filesize=" + fileName + "/" + fileSize + "Byte.");
        try {
            //上传文件
            OSSClient client = new OSSClient(aliyuncsProperties.getOssEndpoint(), aliyuncsProperties.getAccessId(), aliyuncsProperties.getAccessKey());
            PutObjectResult putresult = client.putObject(aliyuncsProperties.getOssBucket(), fileName, file.getInputStream(), metadata);
            logger.info(putresult.getETag());
            logger.info(JSON.toJSONString(putresult));
            String url = aliyuncsProperties.getHost() + "/" + fileName;
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    public String upload(File file, String dir) {
        String fileName = file.getName();
        String endpoint = aliyuncsProperties.getOssEndpoint();
        String accessId = aliyuncsProperties.getAccessId();
        String accessKey = aliyuncsProperties.getAccessKey();
        String bucket = aliyuncsProperties.getOssBucket();
        OSSClient client = new OSSClient("http://" + endpoint, accessId, accessKey);
        logger.info(dir +"/"+ fileName);
        client.putObject(bucket, dir +"/"+ fileName, file);
        client.shutdown();
        return aliyuncsProperties.getHost() + "/" + dir +"/"+ fileName;
    }

    @Override
    public boolean deleteOSS(String fileName) {
        // 创建OSSClient实例。
        OSSClient ossClient = new OSSClient(aliyuncsProperties.getOssEndpoint(), aliyuncsProperties.getAccessId(), aliyuncsProperties.getAccessKey());
        // 删除文件。
        ossClient.deleteObject(aliyuncsProperties.getOssBucket(), fileName);
        // 关闭OSSClient。
        ossClient.shutdown();
        return true;
    }

    @Override
    public String getFileName(String url) {
        if (StringUtils.isNotBlank(url)) {
            String[] strArry = url.split(aliyuncsProperties.getHost() + "/");
            if (strArry.length > 1) {
                return strArry[1];
            }
        }
        return null;
    }

    @Override
    public void createTempFile(String path, InputStream inputStream) {
        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(path));
            int byteCount = 0;
            //1M逐个读取
            byte[] bytes = new byte[1024 * 1024];
            while ((byteCount = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, byteCount);
            }
            inputStream.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    @Override
    public void removeTempFile(String path) {
        File file = new File(path);
        file.delete();
    }

    @Override
    public Map<String, String> download(String urlString, String savePath) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        String filename = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String suffix = urlString.substring(urlString.lastIndexOf("."), urlString.length());
        // 构造URL
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        //设置请求超时为5s
        con.setConnectTimeout(5 * 1000);
        // 输入流
        InputStream is = con.getInputStream();

        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;
        // 输出的文件流
        File sf = new File(savePath);
        if (!sf.exists()) {
            sf.mkdirs();
        }
        OutputStream os = new FileOutputStream(sf.getPath() + "\\" + filename + suffix);
        // 开始读取
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();
        resultMap.put("suffix", suffix);
        resultMap.put("filename", filename);
        resultMap.put("localPath", savePath + filename + suffix);
        return resultMap;
    }

    /*public static void main(String[] args) throws Exception {
        String url = download("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1544438917172&di=038ee0f48a111c4270c779103d497642&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2F8%2F5121d1c073778.jpg", "1.jpg","d:\\image\\");
        System.out.println(url);
    }*/

}
