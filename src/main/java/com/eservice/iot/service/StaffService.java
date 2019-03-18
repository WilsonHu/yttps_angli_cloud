package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.core.Result;
import com.eservice.iot.model.Branch;
import com.eservice.iot.model.ResponseModel;
import com.eservice.iot.model.Staff;
import com.eservice.iot.model.record.Record;
import com.eservice.iot.model.record.RecordWrapper;
import com.eservice.iot.service.impl.RecordServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author HT
 */
@Component
public class StaffService {

    private final static Logger logger = LoggerFactory.getLogger(StaffService.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Value("${record_push_url}")
    private String RECORD_PUSH_URL;

    @Autowired
    private RestTemplate restTemplate;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 发送失败
     */
    public static final int RECORD_PUSH_FAILD = 0;
    /**
     * 发送成功
     */
    public static final int RECORD_PUSH_SUCCESS = 1;
    /**
     * 记录异常
     */
    public static final int RECORD_PUSH_ABNORMAL = 2;


    /**
     * Token
     */
    private String token;
    /**
     * 员工列表
     */
    private ArrayList<Staff> staffList = new ArrayList<>();

    @Autowired
    private TokenService tokenService;

    @Resource
    private RecordServiceImpl recordService;

    private static final ArrayList<String> PUSH_RECORD_LIST = new ArrayList<>();

    /**
     * 查询开始时间,单位为秒
     */
    private Long queryStartTime = 0L;


    public StaffService() {
        //准备初始数据，此时获取到考勤列表后不去通知钉钉，初始化开始查询时间
    }

    /**
     * 凌晨1点清除推送记录
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void resetStaffDataScheduled() {
        logger.info("清除推送记录列表，size：{}", PUSH_RECORD_LIST.size());
        PUSH_RECORD_LIST.clear();
    }

    /**
     * 每分钟获取一次员工信息
     */
    @Scheduled(initialDelay = 3000, fixedRate = 1000 * 60)
    public void fetchStaffScheduled() {
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            try {
                String url = PARK_BASE_URL + "/staffs?";
                url += "page=0&size=0";
                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        processStaffResponse(body);
                    } else {
                        fetchStaffScheduled();
                    }
                }
            } catch (HttpClientErrorException exception) {
                if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    if (token != null) {
                        fetchStaffScheduled();
                    }
                }
            }
        }
    }

    /**
     *
     * 每十分钟检查推送失败的消息，进行重新推送
     * @param
     */
    @Scheduled(initialDelay = 5000, fixedRate = 1000 * 60 * 10)
    private void reSendMessage() {
        List<Record> records = recordService.getPushedMsg(0, null, null);
        for (int i = 0; i < records.size(); i++) {
            pushRecordToServer(JSON.toJSONString(records.get(i)));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void processStaffResponse(String body) {
        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
        if (responseModel != null && responseModel.getResult() != null) {
            ArrayList<Staff> tmpList = (ArrayList<Staff>) JSONArray.parseArray(responseModel.getResult(), Staff.class);
            if (tmpList != null) {
//                if (!staffList.equals(tmpList)) {
//                    logger.info("The number of staff：{} ==> {}", staffList.size(), tmpList.size());
//                    staffList = tmpList;
//                }
                logger.info("The number of staff：{} ==> {}", staffList.size(), tmpList.size());
                staffList = tmpList;
            }
        }
    }

    public boolean deleteStaff(String id) {
        boolean success = false;
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/staffs/" + id, HttpMethod.DELETE, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                        if(responseModel != null && responseModel.getRtn() == 0) {
                            success = true;
                        }
                    }
                }
            } catch (HttpClientErrorException exception) {
                if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    if (token != null) {
                        deleteStaff(id);
                    }
                }
            }
        }
        return success;
    }

    public void pushRecordToServer(String record) {
        Record recordEntity =  JSON.parseObject(record, Record.class);
        if(recordEntity == null) {
            logger.error("Record data format error!");
        } else {
            if(PUSH_RECORD_LIST.contains(recordEntity.getFaceRecordId())) {
                logger.info("Duplicate record: name: {}, studentNum: {}, branch:{}",recordEntity.getName(), recordEntity.getStudentNum(), recordEntity.getBranch());
                return;
            }
            RecordWrapper recordWrapper = new RecordWrapper();
            String faceIdInCloud = getFaceIdByNum(recordEntity.getStudentNum());
            if(faceIdInCloud == null) {
                logger.error("Can not find staff id by student number! name: {}, studentNum: {}, branch:{}",recordEntity.getName(), recordEntity.getStudentNum(), recordEntity.getBranch());
                recordEntity.setStatus(RECORD_PUSH_ABNORMAL);
            } else {
                PUSH_RECORD_LIST.add(recordEntity.getFaceRecordId());
                recordEntity.setFaceId(faceIdInCloud);
                recordEntity.setPushTime(new Date());

                //发送
                recordWrapper.setFaceId(faceIdInCloud);
                recordWrapper.setFaceRecordId(recordEntity.getFaceRecordId());
                recordWrapper.setName(recordEntity.getName());
                recordWrapper.setStudentNum(recordEntity.getStudentNum());
                recordWrapper.setAttendanceTime(formatter.format(recordEntity.getAttendanceTime()));
                recordWrapper.setBranch(JSON.parseObject(recordEntity.getBranch(), Branch.class));
                recordWrapper.setPushTime(formatter.format(recordEntity.getPushTime()));

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.ACCEPT, "application/json");
                headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
                HttpEntity entity = new HttpEntity(JSON.toJSONString(recordWrapper),headers);
                try {
                    ResponseEntity<String> responseEntity = restTemplate.exchange(RECORD_PUSH_URL, HttpMethod.POST, entity, String.class);
                    if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                        String body = responseEntity.getBody();
                        if (body != null) {
                            Result responseModel = JSONObject.parseObject(body, Result.class);
                            if(responseModel != null && responseModel.getCode() == 200) {
                                recordEntity.setStatus(RECORD_PUSH_SUCCESS);
                                logger.info("Face record push success! name:{}, studentNum: {}, branch: {}",recordWrapper.getName(), recordWrapper.getStudentNum(), recordWrapper.getBranch().getName());

                            } else {
                                logger.info("Face record push failed, maybe internal server error! name:{}, studentNum: {}, branch: {}",recordWrapper.getName(), recordWrapper.getStudentNum(), recordWrapper.getBranch().getName());
                                //参数错误或昂立内部处理出错,以后不再推送范围内，需要人工处理
                                recordEntity.setStatus(3);
                            }
                        } else {
                            logger.info("Face record push failed, push response body is null! name:{}, studentNum: {}, branch: {}",recordWrapper.getName(), recordWrapper.getStudentNum(), recordWrapper.getBranch().getName());
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    logger.info("Face record push failed, network is error! name:{}, studentNum: {}, branch: {}",recordWrapper.getName(), recordWrapper.getStudentNum(), recordWrapper.getBranch().getName());
                }
            }
            recordService.save(recordEntity);
        }
    }

    public String getFaceIdByNum(String num) {
        String faceId = null;
        for (Staff staff: staffList) {
            if(staff.getPerson_information().getId().equals(num)) {
                faceId = staff.getStaffId();
            }
        }
        return faceId;
    }


    public ArrayList<Staff> getStaffList() {
        return staffList;
    }
}
