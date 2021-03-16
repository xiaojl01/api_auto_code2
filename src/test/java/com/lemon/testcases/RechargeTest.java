package com.lemon.testcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.pojo.CaseInfo;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

public class RechargeTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup(){
        //从excel读取登录接口模块所需的用例数据
        caseInfoList = getCaseDataFromExcel(3);
        //参数化替换
        caseInfoList = paramsReplace(caseInfoList);

    }

    @Test(dataProvider = "getRechargeDatas")
    public void testRecharge(CaseInfo caseInfo){
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
        //数据库断言
//        assertSQL(caseInfo);




        }

    @DataProvider
    public Object[] getRechargeDatas() {
        return caseInfoList.toArray();

    }

    public static void main(String[] args) {
        Double a = 0.01;
        Float b = 0.01f;
        //类型不一致，会导致断言失败
        //BigDecimal -->大的小数，用它来进行运算可以避免精度丢失
        //把原始的类型Double/Float转化为BigDecimal
        //res-assured如果接口响应结果返回时json，并且json里面有小数，你用Gpath表达式获取结果的时候用Float来存储
        //1、解决方案：Gpath表达式获取结果饿时候用BigDecimal来存储（实际值）
        //
        BigDecimal bigDecimala = new BigDecimal(a.toString());
        BigDecimal bigDecimalb = new BigDecimal(b.toString());
        Assert.assertEquals(bigDecimala,bigDecimalb);
    }
}
