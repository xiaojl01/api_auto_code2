package com.lemon.testcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.pojo.CaseInfo;
import com.lemon.data.GlobalEnvironment;
import com.lemon.util.JDBCUtils;
import com.lemon.util.PhoneRandom;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;

public class RegisterTest extends BaseCase {
    List<CaseInfo> caseInfoList;
    @BeforeClass
    public void setup(){
        caseInfoList = getCaseDataFromExcel(0);

    }

    @Test(dataProvider = "getRegisterDatas")
    public void testRegister(CaseInfo caseInfo) throws JsonProcessingException {
        if (caseInfo.getCaseId() == 1){
            String mobilePhone1 = PhoneRandom.getRandomPhone();
            GlobalEnvironment.envData.put("mobile_phone1" , mobilePhone1);
        }else if (caseInfo.getCaseId() == 2){
            String mobilePhone2 = PhoneRandom.getRandomPhone();
            GlobalEnvironment.envData.put("mobile_phone2" , mobilePhone2);
        }else if (caseInfo.getCaseId() == 3){
            String mobilePhone3 = PhoneRandom.getRandomPhone();
            GlobalEnvironment.envData.put("mobile_phone3" , mobilePhone3);
        }
        //参数化替换 - 对当前的case
        caseInfo = paramsReplaceCaseInfo(caseInfo);
        Map headersMap = fromJsonToMap(caseInfo.getRequestHeader());

        //日志输出
        String logFilePath = addLogToFile(caseInfo);

        Response response =
                given().log().all().
                        headers(headersMap).
                        body(caseInfo.getInputParams()).
                when().
                        post(caseInfo.getUrl()).
                then().log().all().
                        extract().response();

        //接口请求之后把请求和响应的信息添加到Allure中（附件形式）
        addLogToAllure(logFilePath);

        // 断言
        //断言响应结果
        assertExpected(caseInfo,response);
        //断言数据库
//        assertSQL(caseInfo);
        //取得注册成功的密码
        String inputParams = caseInfo.getInputParams();
        //将取得的字符串转化成map
        ObjectMapper objectMapper2 = new ObjectMapper();
        Map inputParamsMap = objectMapper2.readValue(inputParams,Map.class);
        Object pwd = inputParamsMap.get("pwd");
        if(caseInfo.getCaseId()==1){
            GlobalEnvironment.envData.put("pwd1",pwd+"");
        }else if (caseInfo.getCaseId()==2){
            GlobalEnvironment.envData.put("pwd2",pwd+"");
        }else if (caseInfo.getCaseId()==3){
            GlobalEnvironment.envData.put("pwd3",pwd+"");
        }



    }

    @DataProvider
    public Object[] getRegisterDatas() {
        return caseInfoList.toArray();

    }

    public static void main(String[] args) {
        //创建目录
        String dirPath = "test/log";
        File dirFile = new File(dirPath);
        //判断该目录是否存在，不存在再创建
        //mkdir创建一层目录，mkdirs创建多层级目录
        if (!dirFile.isDirectory()){
            dirFile.mkdirs();

        }
    }
}
