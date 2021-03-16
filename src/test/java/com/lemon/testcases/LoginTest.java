package com.lemon.testcases;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.pojo.CaseInfo;
import com.lemon.data.GlobalEnvironment;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;

public class LoginTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup(){
        //从excel读取登录接口模块所需的用例数据
        caseInfoList = getCaseDataFromExcel(1);
        //参数化替换
        caseInfoList = paramsReplace(caseInfoList);

    }

    @Test(dataProvider = "getLoginDatas")
    public void testLogin02(CaseInfo caseInfo){
        //字符串请求头转换成Map
        //实现思路：原始的字符串会比较麻烦，把原始字符串通过json数据类型保存，通过ObjectMapper去转换为Map
        Map headersMap = fromJsonToMap(caseInfo.getRequestHeader());
        //日志输出
        String logFilePath = addLogToFile(caseInfo);

        Response response =
        given().log().all().
                headers(headersMap).
                body(caseInfo.getInputParams()).
        when().
                post( caseInfo.getUrl()).
        then().log().all().
                extract().response();

        //接口请求之后把请求和响应的信息添加到Allure中（附件形式）
        addLogToAllure(logFilePath);

        // 断言
        assertExpected(caseInfo,response);

        //在登录模块用例执行结束完成之后，将memberId保存到环境变量中
        //1、拿到正常用例返回的响应信息的memberId
        Integer memberId = response.path("data.id");
        if(caseInfo.getCaseId() == 1){
            //2、保存到环境变量中
//            GlobalEnvironment.memberId = memberId;
            GlobalEnvironment.envData.put("member_id1",memberId);
            //2、拿到正常用例返回的响应信息的token
            String token = response.path("data.token_info.token");
            GlobalEnvironment.envData.put("token1",token);
        }else if (caseInfo.getCaseId() == 2){
            GlobalEnvironment.envData.put("member_id2",memberId);
            String token = response.path("data.token_info.token");
            GlobalEnvironment.envData.put("token2",token);
        }else if (caseInfo.getCaseId() == 3){
            GlobalEnvironment.envData.put("member_id3",memberId);
            String token = response.path("data.token_info.token");
            GlobalEnvironment.envData.put("token3",token);
        }





    }

        @DataProvider
        public Object[] getLoginDatas() {
            return caseInfoList.toArray();

        }





}