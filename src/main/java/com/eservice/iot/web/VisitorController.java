package com.eservice.iot.web;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.ResponseModel;
import com.eservice.iot.model.Visitor;
import com.eservice.iot.service.ResponseCode;
import com.eservice.iot.service.TokenService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
* Class Description: xxx
* @author Wilson Hu
* @date 2018/08/21.
*/
@RestController
@RequestMapping("/visitors")
public class VisitorController {
    @Resource
    private TokenService tokenService;
    @Resource
    private RestTemplate restTemplate;

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @PostMapping("/add")
    public Result add(@RequestParam String jsonData) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/json");
        headers.add("Authorization", tokenService.getToken());
        HttpEntity httpEntity = new HttpEntity<>(jsonData, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/visitors", httpEntity, String.class);
        if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
            String body = responseEntity.getBody();
            if (body != null) {
                ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                if (responseModel != null && responseModel.getResult() != null) {
                    List<Visitor> tmpList = JSONArray.parseArray(responseModel.getResult(), Visitor.class);
                    if (tmpList != null && tmpList.size() > 0) {
                        //TODO:发送钉钉

                    }
                }
            }
        }
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/getVisitor")
    public Result getVisitor(@RequestParam String visitorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/json");
        headers.add("Authorization", tokenService.getToken());
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/visitors?visitor_id_list=" + visitorId, HttpMethod.GET, entity, String.class);
        if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
            String body = responseEntity.getBody();
            if (body != null) {
                ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                if (responseModel != null && responseModel.getResult() != null) {
                    List<Visitor> tmpList = JSONArray.parseArray(responseModel.getResult(), Visitor.class);
                    if (tmpList != null && tmpList.size() > 0) {
                        PageInfo pageInfo = new PageInfo(tmpList);
                        return ResultGenerator.genSuccessResult(pageInfo);
                    }
                }
            }
        }
        return ResultGenerator.genFailResult("获取访客信息失败！");
    }
}
