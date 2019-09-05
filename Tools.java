package com.ruoyi.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by huming on 2018/6/7.
 * Http请求数据
 *requestBody 主体数据
 */
public class Tools {
  public   static String getHttpRequestData(HttpServletRequest request) {
      BufferedReader reader = null;
      String input = null;
      StringBuffer requestBody = new StringBuffer();
      try {
          reader = request.getReader();
          while((input = reader.readLine()) != null) {
              requestBody.append(input);
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
      return requestBody.toString();
}

    /**
     *getCurrentDatetimeStr 获取当前日期时间
     */
    public static String getCurrentDatetimeStr(){
        return  getDatetimeStrByDate("yyyy-MM-dd HH:mm:ss", new Date());
    }
    public static String createWarterId(){
        return  getDatetimeStrByDate("yyyyMMddHHmmss",new Date())+System.currentTimeMillis();
    }
    public static String getDatetimeStrByDate(String format,Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return  sdf.format(date);
    }
    public static String getDatetimeStrByFormat(String format,String dataStr){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date;
        try {
            date = sdf.parse(dataStr);
            return  getDatetimeStrByDate("yyyy-MM-dd HH:mm:ss", date);
        } catch (ParseException e) {
            e.printStackTrace();
            return  getDatetimeStrByDate("yyyy-MM-dd HH:mm:ss", new Date());
        }

    }
    public static String convertDatetimeStrByFormat(String format,String dataStr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = sdf.parse(dataStr);
            return  getDatetimeStrByDate(format, date);
        } catch (ParseException e) {
            e.printStackTrace();
            return  getDatetimeStrByDate(format, new Date());
        }

    }
    public  static  String addMonthDatetimeStr(String datetimeStr,int month){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date gaveDate=sdf.parse(datetimeStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(gaveDate);
            calendar.add(Calendar.MONTH,month);
            Date toDate=calendar.getTime();
            return  sdf.format(toDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return  datetimeStr;
    }
    }

    /**
     *formatTimestamp 时间戳格式
     */
    public  static  String formatTimestamp(String str){
        //String timeStr = "2016-12-18 11:16:33.706";
        Timestamp ts = Timestamp.valueOf(str);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        return  sdf.format(ts);
    }
    public  static  String getRandomStr(){
        UUID uuid = UUID.randomUUID();
        String string = uuid.toString();
        String  wString =  string.replaceAll("\\-", "");
        return  wString;
    }

    public static int getDatePoor(String startDate, String endDate)  {
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date begin= null;
        long min=0;
        try {
            begin = dfs.parse(startDate);
            Date end = dfs.parse(endDate);
            long between=(end.getTime()-begin.getTime())/1000;//除以1000是为了转换成秒
            System.out.println(between);
            min=between;
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return (int) min;
    }
    public static String getDatePoors(String endDate) throws ParseException {
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date begin=dfs.parse(endDate);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        String str=sdf.format(begin);
        return  str;
    }

    //获取解密后数据
    public static String exeDesDecrypt(String data, String key) {
        String decryptedData = des_decrypt(data,key);
        return decryptedData;
    }

    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secureKey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(2, secureKey, sr);
        return cipher.doFinal(data);
    }

    public static String des_decrypt(String data, String key) {
        try {
            if (StringUtils.isBlank(data)) {
                return null;
            } else {
                if (StringUtils.isBlank(key)) {
                    key = "09776@!@#$%^^%$";
                }

                byte[] buf = Base64.decodeBase64(data);
                byte[] bt = decrypt(buf, key.getBytes("UTF-8"));
                return new String(bt);
            }
        } catch (Exception var4) {
            return null;
        }
    }

    /**
     * 生成图像
     * @param imgStr
     * @param imgFilePath
     * @return
     */
    public static boolean GenerateImage(String imgStr, String imgFilePath) {
        if (imgStr == null) {
            return false;
        } else {
            try {
                byte[] bytes = Base64.decodeBase64(imgStr);

                for(int i = 0; i < bytes.length; ++i) {
                    if (bytes[i] < 0) {
                        bytes[i] = (byte)(bytes[i] + 256);
                    }
                }

                OutputStream out = new FileOutputStream(imgFilePath);
                out.write(bytes);
                out.flush();
                out.close();
                return true;
            } catch (Exception var4) {
                return false;
            }
        }
    }

    //生成uuid方法
    public static String createUUID(){
        return UUID.randomUUID().toString();
    }

    /**
     * MD5+盐加密
     * @param username
     * @param password
     * @param salt
     * @return
     */
    public static String encryptPassword(String username, String password, String salt)
    {
        return new Md5Hash(username + password + salt).toHex().toString();
    }

    public static void main(String[] args) {
        System.out.println(createUUID());
    }
}
