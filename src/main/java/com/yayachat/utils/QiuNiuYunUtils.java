package com.yayachat.utils;




import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.UrlSafeBase64;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import com.qiniu.util.*;
import okhttp3.*;

import java.util.Calendar;


public class QiuNiuYunUtils {
    private static  String accessKey = "tDPvHwBhKRxNScBAr4l7Hfa7kwpE4eKxsgFX2H6A";      //AccessKey的值
    private static   String secretKey = "QFezuLMw_rQANhska7DwcMQ_ebDM8ug4IvuseFe2";      //SecretKey的值
    private static  String bucket = "yaya-chatimg";                                          //存储空间名
    // 密钥配置
    private static Auth auth = Auth.create(accessKey, secretKey);
    // 创建上传对象
    private static UploadManager uploadManager = new UploadManager(new Configuration(Zone.zone0()));

    public static  String uploadImg(String imgStr,Integer length,String bucketName) {

        Calendar Cld = Calendar.getInstance();
        int YY = Cld.get(Calendar.YEAR) ;//年
        int MM = Cld.get(Calendar.MONTH)+1;//月
        int DD = Cld.get(Calendar.DATE);//日
        int HH = Cld.get(Calendar.HOUR_OF_DAY);//时
        int mm = Cld.get(Calendar.MINUTE);//分
        int SS = Cld.get(Calendar.SECOND);//秒
        int MI = Cld.get(Calendar.MILLISECOND);//毫秒
        Integer rendom = (int) (Math.random() * 100000);
        String imgName = "yaya" + YY+MM+DD+HH+mm+SS+MI + rendom;
        String key = imgName;
        key = key.trim();


        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            String file64 = imgStr.substring(23,(int)imgStr.length());

            String url = "http://upload-z2.qiniup.com/putb64/" +length+"/key/"+ UrlSafeBase64.encodeToString(key);
            //非华东空间需要根据注意事项 1 修改上传域名
            RequestBody rb = RequestBody.create(null, file64);

            Request request = new Request.Builder().
                    url(url).
                    addHeader("Content-Type", "application/octet-stream")
                    .addHeader("Authorization", "UpToken " + getUpToken(key,bucketName))
                    .post(rb).build();
            OkHttpClient client = new OkHttpClient();
            okhttp3.Response response = client.newCall(request).execute();

        }catch (Exception ex) {
             ex.printStackTrace();
        }
        return "http://chatimg.wlkqzs.com/" +imgName;
    }

    // 覆盖上传
    private static String getUpToken(String key,String bucket) {
        // <bucket>:<key>，表示只允许用户上传指定key的文件。在这种格式下文件默认允许“修改”，已存在同名资源则会被本次覆盖。
        // 如果希望只能上传指定key的文件，并且不允许修改，那么可以将下面的 insertOnly 属性值设为 1。
        // 第三个参数是token的过期时间
        return auth.uploadToken(bucket, null, 3600, new StringMap().put("insertOnly", 1));
    }


    public static byte[] getBytesWithMultipartFile(MultipartFile multipartFile) {
        try {
            return multipartFile.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //获取base64文件大小
    public static Integer getBase64FileSize(String base64File){
        //去掉data:image/png;base64,
        String file64 = base64File.substring(23,(int)base64File.length());
        //去掉等号
        int i = file64.indexOf("=");//首先获取字符的位置
        if(file64.indexOf("=")>0)
        {
            file64=file64.substring(0, i);

        }
        file64 = file64.replace("=",
                "").trim();;

        //原来的字符流大小，单位为字节
        Integer strLength=file64.length();
        //计算后得到的文件流大小，单位为字节
        return (int)(strLength-(Math.ceil(strLength/8))*2);
    }


}
