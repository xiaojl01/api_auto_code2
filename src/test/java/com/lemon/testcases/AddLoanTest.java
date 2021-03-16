package com.lemon.testcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.data.GlobalEnvironment;
import com.lemon.pojo.CaseInfo;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

public class AddLoanTest extends BaseCase {
    List<CaseInfo> caseInfoList;
    @BeforeClass
    public void setUp(){
        caseInfoList = getCaseDataFromExcel(4);
        caseInfoList = paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getAddLoanDatas")
    public void testAddLoan(CaseInfo caseInfo){
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

        //断言
        assertExpected(caseInfo,response);
        if (response.path("data.id") != null){
            GlobalEnvironment.envData.put("loan_id",response.path("data.id"));

        }

    }

    @DataProvider
    public Object[] getAddLoanDatas() {
        return caseInfoList.toArray();

    }


}
