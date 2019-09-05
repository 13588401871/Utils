package com.ruoyi.common.utils;

import com.ruoyi.project.prodectManage.sgrjhManage.domain.Sgrjh;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by x1c on 2018/11/16.
 */
public class ObjectTool {
    /**
     * 获取字节码文件所有属性
     * @param clazz
     * @return List<String>
     */
    static public List<String> getClassFieldNames(Class clazz){
        Field[] fields = clazz.getDeclaredFields();//获得所有属性
        List<String> result=new ArrayList<>();
        //获得Object对象中的所有方法
        for(Field field:fields){
            PropertyDescriptor pd = null;
            try {
                pd = new PropertyDescriptor(field.getName(), clazz);
                result.add(field.getName());
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    /**
     * 获取字节码文件所有属性和类型
     * @param clazz
     * @return Map<String,String>
     *  (field.getName()=field.getType().toString())
     */
    static public Map<String,String> getClassFieldNameTypes(Class clazz){
        Field[] fields = clazz.getDeclaredFields();//获得属性
        Map<String,String >result=new HashMap<>();
        //获得Object对象中的所有方法
        for(Field field:fields){
            PropertyDescriptor pd = null;
            try {
                pd = new PropertyDescriptor(field.getName(), clazz);
                result.put(field.getName(),field.getType().toString());
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取对象的属性名和GetMethods
     * @param obj
     * @return Map<String,Method>
     * (field.getName(),getMethod)
     */
    static public Map<String,Method> getGetMethods(Object obj){
        return getClassGetMethods(obj.getClass());
    }
    /**
     * 获取字节码文件的属性名和GetMethods
     * @param clazz
     * @return Map<String,Method>
     * (field.getName(),getMethod)
     */
    static public Map<String,Method> getClassGetMethods(Class clazz){
        Field[] fields = clazz.getDeclaredFields();//获得属性
        Map<String,Method> result=new HashMap<>();
        //获得Object对象中的所有方法
        for(Field field:fields){
            PropertyDescriptor pd = null;
            try {
                pd = new PropertyDescriptor(field.getName(), clazz);
                Method getMethod = pd.getReadMethod();//获得get方法
                result.put(field.getName(),getMethod);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 字节码文件转map
     * @param mapValue
     * @param clazz
     * @return
     */
    static public Object invokeObjectFromMap(Map<String,Object> mapValue,Class clazz){
        Map<String,Method> setMethods=getClassSetMethods(clazz);
        Object rstObj=null;
        try {
            rstObj=clazz.newInstance();
            for (Map.Entry<String , Method> entry : setMethods.entrySet()) {
                Object value=mapValue.get(entry.getKey());
                if (value!=null){
                    Method setM=entry.getValue();
                    setM.invoke(rstObj,value);
                }
               // System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rstObj;
    }

    /**
     * 获取对象的属性名和SetMethods
     * @param obj
     * @return Map<String,Method>
     * (field.getName(),getMethod)
     */
    static public Map<String,Method> getSetMethods(Object obj){
        return getClassSetMethods(obj.getClass());
    }
    /**
     * 获取字节码文件的属性名和SetMethods
     * @param clazz
     * @return Map<String,Method>
     * (field.getName(),getMethod)
     */
    static public Map<String,Method> getClassSetMethods(Class clazz){
        Field[] fields = clazz.getDeclaredFields();//获得属性
        Map<String,Method> result=new HashMap<>();
        //获得Object对象中的所有方法
        for(Field field:fields){
            PropertyDescriptor pd = null;
            try {
                pd = new PropertyDescriptor(field.getName(), clazz);
                Method setMethod = pd.getWriteMethod();//获得set方法
                result.put(field.getName(),setMethod);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
