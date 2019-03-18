package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.ResponseModel;
import com.eservice.iot.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author HT
 */
@Component
public class TagService {

    private final static Logger logger = LoggerFactory.getLogger(TagService.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenService tokenService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ThreadPoolTaskExecutor mExecutor;

    private static boolean TAG_INITIAL_FINISHED = false;

    /**
     * Token
     */
    private String token;

    /**
     * 全部tag列表
     */
    private ArrayList<Tag> mAllTagList = new ArrayList<>();

    /**
     * 需要考勤标签名称列表
     */
    private ArrayList<String> SIGNIN_TAG_NAME_LIST;

    /**
     * 需要考勤标签ID列表
     */
    private ArrayList<String> mSignInTagIdList = new ArrayList<>();

    /**
     * VIP标签名称列表，包含了员工和访客
     */
    private ArrayList<String> VIP_TAG_NAME_LIST;

    /**
     * VIP标签ID列表,包含了员工和访客
     */
    private ArrayList<String> mVIPTagIdList = new ArrayList<>();


    public TagService() {
        /**
         * 考勤
         */
        SIGNIN_TAG_NAME_LIST = new ArrayList<>();
        SIGNIN_TAG_NAME_LIST.add("员工");
        /**
         * VIP
         */
        VIP_TAG_NAME_LIST = new ArrayList<>();
        VIP_TAG_NAME_LIST.add("VIP");
        fetchTags();
    }

    /**
     * 一分钟更新一次TAG
     */
    @Scheduled(fixedRate = 1000*60)
    public void fetchTags() {
        if (tokenService != null) {
            if(token == null) {
                token = tokenService.getToken();
            }
            if(token != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.ACCEPT, "application/json");
                headers.add("Authorization", token);
                HttpEntity entity = new HttpEntity(headers);
                try {
                    ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/tags?page=0&size=0", HttpMethod.GET, entity, String.class);
                    if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                        String body = responseEntity.getBody();
                        if (body != null) {
                            processTagResponse(body);
                        } else {
                            fetchTags();
                        }
                    }
                } catch (HttpClientErrorException errorException) {
                    if(errorException.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                        //token失效,重新获取token后再进行数据请求
                        token = tokenService.getToken();
                        if (token != null) {
                            fetchTags();
                        }
                    }
                }
            }

            if (TAG_INITIAL_FINISHED && mExecutor != null) {
                mExecutor.shutdown();
                mExecutor = null;
            }
        } else {

            ///等待tokenService初始化完成，TAG标签被其他很多service依赖，所以需要其先初始化完毕后
            if (mExecutor == null) {
                mExecutor = new ThreadPoolTaskExecutor();
                mExecutor.setCorePoolSize(1);
                mExecutor.setMaxPoolSize(2);
                mExecutor.setThreadNamePrefix("YTTPS-");
                mExecutor.initialize();
            }
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        fetchTags();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void processTagResponse(String body) {
        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
        if (responseModel != null && responseModel.getResult() != null) {
            ArrayList<Tag> tmpList = (ArrayList<Tag>) JSONArray.parseArray(responseModel.getResult(), Tag.class);
            if (tmpList != null && tmpList.size() > 0) {
                logger.info("The number of tag：{} ==> {}", mAllTagList.size(), tmpList.size());
                mAllTagList = tmpList;
                TAG_INITIAL_FINISHED = true;
            }
        }
    }

    public Tag createTag(String name, String identity, Map meta) {
        Tag resultTag = null;
        HashMap<String, Object> postParameters = new HashMap<>();
        ArrayList<Tag> tagList = new ArrayList<>();
        Tag tag = new Tag();
        tag.setTag_name(name);
        ArrayList<String> identityList = new ArrayList<>();
        identityList.add(identity);
        tag.setVisible_identity(identityList);
        if(meta != null) {
            tag.setMeta(meta);
        }
        tagList.add(tag);
        postParameters.put("tag_list", tagList);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, tokenService.getToken());
        HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/tags", httpEntity, String.class);
        if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
            ResponseModel responseModel = JSON.parseObject(responseEntity.getBody(),ResponseModel.class);
            if(responseModel.getRtn() == 0) {
                List<ResponseModel> tmp = JSON.parseArray(responseModel.getResult(), ResponseModel.class);
                if(tmp.size() > 0) {
                    resultTag = JSON.parseObject(tmp.get(0).getResult(), Tag.class);
                    fetchTags();
                }
            }
        }
        return resultTag;
    }

    public Tag updateTag(Tag tag) {
        Tag resultTag = null;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, tokenService.getToken());
        HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(tag), headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/tags/" + tag.getTag_id(), HttpMethod.PUT, httpEntity, String.class);
        if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
            ResponseModel responseModel = JSON.parseObject(responseEntity.getBody(),ResponseModel.class);
            if(responseModel.getRtn() == 0) {
                resultTag = JSON.parseObject(responseModel.getResult(), Tag.class);
                fetchTags();
            }
        }
        return resultTag;
    }

    public String getTagNameById(String tagId) {
        String name = "";
        for (int i = 0; i < mAllTagList.size(); i++) {
            if(mAllTagList.get(i).getTag_id().equals(tagId)) {
                name = mAllTagList.get(i).getTag_name();
                break;
            }
        }
        return name;
    }

    public String getTagIdByName(String name) {
        String id = "";
        for (int i = 0; i < mAllTagList.size(); i++) {
            if(mAllTagList.get(i).getTag_name().equals(name)) {
                id = mAllTagList.get(i).getTag_id();
                break;
            }
        }
        return id;
    }

    public Tag getTagById(String tagId) {
        Tag tag = null;
        for (int i = 0; i < mAllTagList.size(); i++) {
            if(mAllTagList.get(i).getTag_id().equals(tagId)) {
                tag = mAllTagList.get(i);
                break;
            }
        }
        return tag;
    }

    public Tag getTagByBranchId(String branchId) {
        Tag targetTag = null;
        for (Tag tag: mAllTagList) {
            if(tag.getMeta().get("id") != null && tag.getMeta().get("id").equals(branchId)) {
                targetTag = tag;
                break;
            }
        }
        return targetTag;
    }

    public String tagIdToName(List<String> ids) {
        String result = "";
        for (int i = 0; i < ids.size(); i++) {
            if(i != ids.size() -1) {
                result += getTagNameById(ids.get(i)) + "/";
            } else {
                result += getTagNameById(ids.get(i));
            }
        }
        return result;
    }

    public ArrayList<String> getSignInTagIdList() {
        return mSignInTagIdList;
    }

    public ArrayList<String> getVIPTagIdList() {
        return mVIPTagIdList;
    }

    public ArrayList<Tag> getmAllTagList() {
        return mAllTagList;
    }

    public boolean isTagInitialFinished() {
        return TAG_INITIAL_FINISHED;
    }
}
