package com.lemon.testcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.pojo.CaseInfo;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;

public class GetUserInfoTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup(){
        //从excel读取登录接口模块所需的用例数据
        caseInfoList = getCaseDataFromExcel(2);
        //参数化替换
        caseInfoList = paramsReplace(caseInfoList);

    }

    @Test(dataProvider = "getUserInfoDatas")
    public void testGetUserInfo(CaseInfo caseInfo){

        Map headersMap = fromJsonToMap(caseInfo.getRequestHeader());
        //日志输出
        String logFilePath = addLogToFile(caseInfo);

        Response response =
        given().log().all().
                headers(headersMap).
        when().
                get(caseInfo.getUrl()).
        then().log().all().
                extract().response();

        //接口请求之后把请求和响应的信息添加到Allure中（附件形式）
        addLogToAllure(logFilePath);
        // 断言
        assertExpected(caseInfo,response);

    }

        @DataProvider
        public Object[] getUserInfoDatas() {
            return caseInfoList.toArray();

        }



}