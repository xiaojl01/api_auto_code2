package com.lemon.base;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.data.Constants;
import com.lemon.pojo.CaseInfo;
import com.lemon.data.GlobalEnvironment;
import com.lemon.util.JDBCUtils;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

/**
 * 所有测试用例类的父类，里面放置公用方法
 */
public class BaseCase {
    @BeforeTest
    public void globalSetup() throws FileNotFoundException {
        //整体全局性前置配置/初始化
        //1、设置项目的url
        RestAssured.baseURI = Constants.BASE_URL;
        //2、设置接口响应结果如果是json返回的小数类型，使用BigDecimal来存储，默认是Float存储
        RestAssured.config = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL));
        //3.设置项目的日志存储到本地文件中
        PrintStream fileOutPutStream = new PrintStream(new File("log/test_all.log"));
        RestAssured.filters(new RequestLoggingFilter(fileOutPutStream),new ResponseLoggingFilter(fileOutPutStream));
    }

    /**
     *方法：从excel中读取测试数据
     * @param index sheet的索引，从0开始
     */
    public List<CaseInfo> getCaseDataFromExcel(int index){
        ImportParams importParams = new ImportParams();
        importParams.setStartSheetIndex(index);
        File excelFile = new File(Constants.EXCEL_PATH);
        List<CaseInfo> list = ExcelImportUtil.importExcel(excelFile, CaseInfo.class, importParams);
        return list;
    }

    /**
     * 正则替换
     * @param sourceStr 原字符串
     * @return 返回替换之后的新字符串
     */
    public String regexReplace(String sourceStr) {
        //如果参数化的源字符串为null的话不需要去进行参数化替换过程
        if (sourceStr == null){
            return sourceStr;
        }
        //1.定义正则表达式
        String regex = "\\{\\{(.*?)\\}\\}";
        //2.通过正则表达式编译出来一个匹配器pattern
        Pattern pattern = Pattern.compile(regex);
        //3.开始进行匹配 参数：为你要去在哪一个字符串里面去进行匹配
        Matcher matcher = pattern.matcher(sourceStr);
        //保存匹配到的整个表达式，比如：{{member_id}}
        String findStr = "";
        //保存匹配到的()里面的内容 比如：member_id
        String singleStr = "";
        //4.连续查找，连续匹配
        while (matcher.find()) {
            //输出找到匹配的结果 匹配到整个正则对应的字符串内容
            findStr = matcher.group(0);
            singleStr = matcher.group(1);
            //5.先去找到环境变量里面对应的key值
            Object replaceStr = GlobalEnvironment.envData.get(singleStr);
            //6.替换原始字符串中的内容
            sourceStr = sourceStr.replace(findStr, replaceStr + "");
        }
        //返回替换好的
        return sourceStr;


    }

    /**
     * 对所有参数做参数化替换（替换请求头，接口地址，参数输入，期望返回结果）
     * @param caseInfoList 当前测试类中的所有测试用例数据
     * @return 替换后的所有参数
     */
    public List<CaseInfo> paramsReplace(List<CaseInfo> caseInfoList){
        //替换请求头，接口地址，参数输入，期望返回结果
        for (CaseInfo caseInfo:caseInfoList){
            //如果数据不为空，才进行参数化的处理
            //参数化替换请求头
            String requestHeader = regexReplace(caseInfo.getRequestHeader());
            caseInfo.setRequestHeader(requestHeader);
            //参数化替换请求地址
            String url = regexReplace(caseInfo.getUrl());
            caseInfo.setUrl(url);
            //参数化替换输入参数
            String inputParams = regexReplace(caseInfo.getInputParams());
            caseInfo.setInputParams(inputParams);
            //参数化替换期望值
            String expected = regexReplace(caseInfo.getExpected());
            caseInfo.setExpected(expected);
            //参数化替换数据库校验
            String checkSql = regexReplace(caseInfo.getCheckSQL());
            caseInfo.setCheckSQL(checkSql);

        }
        return caseInfoList;
    }

    /**
     * 对某一条参数做参数化替换（替换请求头，接口地址，参数输入，期望返回结果）
     * @param caseInfo 当前测试类中的所有测试用例数据
     * @return 替换后的所有参数
     */
    public CaseInfo paramsReplaceCaseInfo(CaseInfo caseInfo){
        //替换请求头，接口地址，参数输入，期望返回结果
            //如果数据不为空，才进行参数化的处理
        String requestHeader = regexReplace(caseInfo.getRequestHeader());
        caseInfo.setRequestHeader(requestHeader);
        String url = regexReplace(caseInfo.getUrl());
        caseInfo.setUrl(url);
        String inputParams = regexReplace(caseInfo.getInputParams());
        caseInfo.setInputParams(inputParams);
        String expected = regexReplace(caseInfo.getExpected());
        caseInfo.setExpected(expected);
        //参数化替换数据库校验
        String checkSql = regexReplace(caseInfo.getCheckSQL());
        caseInfo.setCheckSQL(checkSql);
        return caseInfo;
    }

    /**
     * 用例公共的断言方法，断言期望值和实际值
     * @param caseInfo 用例信息
     * @param response 接口的响应结果
     */
    public void assertExpected(CaseInfo caseInfo, Response response){
        // 断言
        //1、获取到断言信息，把获取的json数据转化成map
        ObjectMapper objectMapper1 = new ObjectMapper();
        //获取excel表中的期望值
        Map expectedMap = null;
        try {
            expectedMap = objectMapper1.readValue(caseInfo.getExpected(), Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //2、循环遍历取到map里面的每一组键值对
        Set<Map.Entry<String,Object>> set = expectedMap.entrySet();
        for (Map.Entry<String,Object> map : set){
            //关键点：做断言，通过Gpath获取实际接口对应字段的值
            //我们在Excel里面写用例的期望结果时，期望结果里面键名-->Gpath表达式
            //期望结果里面的键值-->期望值
            //把期望值转换（期望的json结果是小数类型-Float/Double才需要转换）
            //map.getValue() 判断一下，是不是Float/Double类型
            Object expected = map.getValue();
            if ((expected instanceof Float) || (expected instanceof Double)){
                System.out.println("将小数转化为BigDecimal类型");
                BigDecimal bigDecimalData = new BigDecimal(map.getValue().toString());
                Assert.assertEquals(response.path(map.getKey()),bigDecimalData,"接口响应断言失败");
            }else{
//                System.out.println("断言实际值"+response.path(map.getKey()));
//                System.out.println("期望值"+expected);
                Assert.assertEquals(response.path(map.getKey()),expected,"接口响应断言失败");
            }

        }
    }

    /**
     * 断言数据库
     * @param caseInfo 用例信息
     */
    public void assertSQL(CaseInfo caseInfo){
        //断言数据库
        String checkSql = caseInfo.getCheckSQL();
        if (checkSql != null) {
            Map checkSqlMap = fromJsonToMap(checkSql);
            Set<Map.Entry<String,Object>> set = checkSqlMap.entrySet();
            for (Map.Entry<String, Object> mapEntry : set) {
                String sql = mapEntry.getKey();
                //查询数据库
                Object actual = JDBCUtils.querySingle(sql);
                //1.数据库查询的返回结果是Long类型，Excel读取期望值结果是Integer
                if (actual instanceof Long) {
                    //把期望值expected转成Long类型
                    Long expected = new Long(mapEntry.getValue().toString());
                    System.out.println("Long类型和Integer类型断言");
                    Assert.assertEquals(actual, expected,"数据库断言失败");
                }else if (actual instanceof BigDecimal) {
                    //2、数据库查询的返回结果是BigDecimal类型，Excel读取期望值结果是Double类型
                    BigDecimal expected = new BigDecimal(mapEntry.getValue().toString());
                    System.out.println("BigDecimal类型和Double类型断言");
                    Assert.assertEquals(actual, expected,"数据库断言失败");
                }else {
                    System.out.println("字符串类型断言");
                    Assert.assertEquals(actual, mapEntry.getValue(),"数据库断言失败");

                }

            }
        }
    }

    /**
     * 把json字符串转换为Map类型
     * @param jsonStr json字符串
     * @return 转换好的Map类型数据
     */
    public Map fromJsonToMap(String jsonStr){
        //字符串请求头转换成Map
        //实现思路：原始的字符串会比较麻烦，把原始字符串通过json数据类型保存，通过ObjectMapper去转换为Map
        //jackson  json字符串-->Map
        //1、实例化ObjectMapper对象
        ObjectMapper objectMapper = new ObjectMapper();
        //readValue方法参数解释
        //第一个参数:json字符串  第二个参数：转换的类型（Map）
        try {
            return objectMapper.readValue(jsonStr,Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 将日志重定向到单独的文件中
     * @param caseInfo 用例信息
     */
    public String addLogToFile(CaseInfo caseInfo) {
        String logFilePath = "";
        if (!Constants.IS_DEBUG) {
            //日志输出
            //提前创建好目录层级
            String dirPath = "target/log/" + caseInfo.getInterfaceName();
            File dirFile = new File(dirPath);
            //判断该目录是否存在，不存在再创建
            if (!dirFile.isDirectory()) {
                dirFile.mkdirs();
            }
            logFilePath = dirPath + "/" + caseInfo.getInterfaceName() + "_" + caseInfo.getCaseId() + ".log";
            //请求之前对日志做配置，输出到对应的文件中
            PrintStream fileOutPutStream = null;
            try {
                fileOutPutStream = new PrintStream(new File(logFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            RestAssured.config = RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(fileOutPutStream));

        }
        return logFilePath;
    }

    /**
     * 接口请求之后把请求和响应的信息添加到Allure中
     * @param logFilePath 日志文件的路径
     */
    public void addLogToAllure(String logFilePath){
        if (!Constants.IS_DEBUG){
            //将日志作为附件添加到Allure中（附件形式）
            //第一个参数：附件的名字 第二个参数：FileInputStream
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(logFilePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Allure.addAttachment("接口请求响应信息",inputStream);
        }
    }




}
