package com.ruoyi.common.utils;

import com.alibaba.fastjson.JSONObject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.experimental.var;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author chenxd
 * @create 2019-07-06 9:16
 */
public class HtmlToDocUtil {
    private static Configuration configuration;
    private static String encoding;

    public HtmlToDocUtil(String encoding) {
        this.encoding = encoding;
        configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setDefaultEncoding(encoding);
        configuration.setClassForTemplateLoading(this.getClass(), "/fileTemplates");
    }

    public static Template getTemplate(String name) throws Exception {
        return configuration.getTemplate(name);
    }

    /**
     * 获取图片
     * @param image
     * @return
     * @throws IOException
     */
    public static String getImageStr(String image) throws IOException {
        InputStream is = new FileInputStream(image);
        BASE64Encoder encoder = new BASE64Encoder();
        byte[] data = new byte[is.available()];
        is.read(data); is.close();
        return encoder.encode(data);
    }

    /**
     * 获取数据Map
     * @return
     */
    public static Map<String, Object> getDataMap(JSONObject dataJSON) {
        Map<String, Object> dataMap = new HashMap<>();
        Iterator<String> keys = dataJSON.keySet().iterator();// jsonObject.keys();
        while (keys.hasNext()){
            String key = keys.next();
            Object value = dataJSON.get(key);
            dataMap.put(key,value);
        }
        return dataMap;
    }

    public static void createDoc(String doc, String name,JSONObject dataJSON) throws Exception {
        Writer writer = null;
        try{
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(doc), encoding));
            getTemplate(name).process(getDataMap(dataJSON), writer);
        }finally {
            writer.close();
        }
    }
}
