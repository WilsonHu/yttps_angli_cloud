package com.eservice.iot.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.*;
import com.eservice.iot.service.ResponseCode;
import com.eservice.iot.service.StaffService;
import com.eservice.iot.service.TagService;
import com.eservice.iot.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* Class Description: xxx
* @author Wilson Hu
* @date 2018/08/21.
*/
@RestController
@RequestMapping("/student")
public class StudentController {

    private final static Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Value("${staff_image_url}")
    private String STAFF_IMAGE_URL;

    @Value("${search_person_number}")
    private Integer SEARCH_PERSON_NUMBER;
    @Resource
    private StaffService staffService;

    @Autowired
    private RestTemplate restTemplate;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * Token
     */
    private String token;

    @Autowired
    private TokenService tokenService;

    @Resource
    private TagService tagService;

    @PostMapping("/add")
    public Result add(@RequestParam String photoData, @RequestParam String name, @RequestParam String studentNum,
                      @RequestParam String branches,@RequestParam(defaultValue = "1") String role) {
        if(photoData == null || "".equals(photoData)) {
            return  ResultGenerator.genFailResult("照片不能为空！");
        }
        if(name == null || "".equals(name)) {
            return  ResultGenerator.genFailResult("姓名不能为空！");
        }
        if(studentNum == null || "".equals(studentNum)) {
            return  ResultGenerator.genFailResult("学号不能为空！");
        }
        ArrayList<String> tagIdList = new ArrayList<>();
        if(branches == null || "".equals(branches)) {
            return  ResultGenerator.genFailResult("分支机构不能为空！");
        } else {
            List<Branch> branchesList = null;
            try {
                branchesList = JSON.parseArray(branches, Branch.class);
            } catch (Exception e){
                logger.error("解析分支机构出错！");
            }
            if(branchesList == null) {
                return ResultGenerator.genFailResult("解析分支机构出错，请传入分支机构列表的json string");
            } else {
                if(branchesList.size() > 0) {
                    for (Branch item: branchesList) {
                        Tag tag = tagService.getTagByBranchId(item.getId());
                        //找不到分支机构
                        if(tag == null) {
                            //创建分支机构
                            Map<String, String> meta = new HashMap<>();
                            meta.put("id",item.getId());
                            meta.put("name",item.getName());
                            tag = tagService.createTag(item.getName(), "STAFF", meta);
                            if(tag == null) {
                                return ResultGenerator.genFailResult("创建分支机构失败，分支机构名可能已存在！");
                            }
                            tagIdList.add(tag.getTag_id());
                        } else {
                            //如果标签名字有改动，进行更新
                            if(!tag.getTag_name().equals(item.getName())) {
                                tag.setTag_name(item.getName());
                                HashMap<String, String> meta = new HashMap<>();
                                meta.put("id", item.getId());
                                meta.put("name", item.getName());
                                tag.setMeta(meta);
                                Tag tmp = tagService.updateTag(tag);
                                if(tmp == null) {
                                    return ResultGenerator.genFailResult("更新学员分支机构出错！");
                                } else {
                                    tag = tmp;
                                }
                                tagIdList.add(tag.getTag_id());
                            } else {
                                tagIdList.add(tag.getTag_id());
                            }
                        }
                    }
                }
                if(role.equals("1") && !"".equals(tagService.getTagIdByName("学生"))) {
                    tagIdList.add(tagService.getTagIdByName("学生"));
                }else if(role.equals("2") && !"".equals(tagService.getTagIdByName("老师"))) {
                    tagIdList.add(tagService.getTagIdByName("老师"));
                }else {
                    return ResultGenerator.genFailResult("角色值(role)不正确，必须是0或者1.");
                }
            }
        }
        Staff staff = new Staff();
        staff.setTag_id_list(tagIdList);
        ArrayList<String>  faceList = new ArrayList<>();
        faceList.add(photoData);
        staff.setFace_image_content_list(faceList);
        PersonInformation personInformation = new PersonInformation();
        personInformation.setId(studentNum);
        personInformation.setName(name);
        //增加学生和老师的区别，填写在备注中
        personInformation.setRemark(role.equals("1") ? "学生" : "老师");
        staff.setPerson_information(personInformation);
        Staff newStaff = createStaff(staff);
        if(newStaff != null) {
            StudentSearchModel studentSearchModel = new StudentSearchModel();
            studentSearchModel.setBranches(JSON.parseArray(branches, Branch.class));
            studentSearchModel.setName(name);
            studentSearchModel.setStudentNum(studentNum);
            studentSearchModel.setCreateTime(formatter.format(new Date(newStaff.getUploadTime() * (long)1000)));
            studentSearchModel.setFaceId(newStaff.getStaffId());
            studentSearchModel.setPhotoUrl(STAFF_IMAGE_URL + "image/" + newStaff.getFace_list().get(0).getFace_image_id());
            return ResultGenerator.genSuccessResult(studentSearchModel);
        } else {
            return ResultGenerator.genFailResult("创建学生失败,可能存在相同学号!");
        }
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam String faceId) {
        if(staffService.deleteStaff(faceId)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("删除失败");
        }
    }

    @PostMapping("/update")
    public Result update(@RequestParam String faceId,String photoData, @RequestParam String name, @RequestParam String studentNum,
                         @RequestParam String branches,@RequestParam(defaultValue = "1") String role) {
        ArrayList<Staff> staffArrayList = staffService.getStaffList();
        if(faceId == null || "".equals(faceId)) {
            return  ResultGenerator.genFailResult("FaceId不能为空！");
        }
        if(name == null || "".equals(name)) {
            return  ResultGenerator.genFailResult("姓名不能为空！");
        }
        if(studentNum == null || "".equals(studentNum)) {
            return  ResultGenerator.genFailResult("学号不能为空！");
        }
        ArrayList<String> tagIdList = new ArrayList<>();
        if(branches == null || "".equals(branches)) {
            return  ResultGenerator.genFailResult("分支机构不能为空！");
        } else {
            List<Branch> branchesList = JSON.parseArray(branches, Branch.class);
            if(branchesList == null || branchesList.size() == 0) {
                return ResultGenerator.genFailResult("解析分支机构出错，请传入分支机构列表的json string");
            } else {
                for (Branch item: branchesList) {
                    Tag tag = tagService.getTagByBranchId(item.getId());
                    //找不到分支机构
                    if(tag == null) {
                        //创建分支机构
                        Map<String, String> meta = new HashMap<>();
                        meta.put("id",item.getId());
                        meta.put("name",item.getName());
                        tag = tagService.createTag(item.getName(), "STAFF", meta);
                        if(tag == null) {
                            return ResultGenerator.genFailResult("创建分支机构失败，分支机构名可能已存在！");
                        }
                        tagIdList.add(tag.getTag_id());
                    } else {
                        //如果标签名字有改动，进行更新
                        if(!tag.getTag_name().equals(item.getName())) {
                            tag.setTag_name(item.getName());
                            HashMap<String, String> meta = new HashMap<>();
                            meta.put("id", item.getId());
                            meta.put("name", item.getName());
                            tag.setMeta(meta);
                            Tag tmp = tagService.updateTag(tag);
                            if(tmp == null) {
                                return ResultGenerator.genFailResult("更新学员分支机构出错！");
                            } else {
                                tag = tmp;
                            }
                            tagIdList.add(tag.getTag_id());
                        } else {
                            tagIdList.add(tag.getTag_id());
                        }
                    }
                }
                if(role.equals("1") && !"".equals(tagService.getTagIdByName("学生"))) {
                    tagIdList.add(tagService.getTagIdByName("学生"));
                }else if(role.equals("2") && !"".equals(tagService.getTagIdByName("老师"))) {
                    tagIdList.add(tagService.getTagIdByName("老师"));
                } else {
                    return ResultGenerator.genFailResult("角色值(role)不正确，必须是0或者1.");
                }
            }
        }
        Staff originalStaff = null;
        for (Staff item: staffArrayList) {
            if(item.getStaffId().equals(faceId)) {
                originalStaff = item;
                break;
            }
        }
        if(originalStaff == null) {
            return ResultGenerator.genFailResult("没有faceId对应的员工！");
        } else {

            UpdateStaffDTO updateStaffDTO = new UpdateStaffDTO();
            //卡号
            updateStaffDTO.setCard_numbers(originalStaff.getCard_numbers());
            //标签
            updateStaffDTO.setTag_id_list(tagIdList);
            UpdateStaffDTO.Update_faceEntity update_faceEntity = updateStaffDTO.new Update_faceEntity();
            if(photoData == null || "".equals(photoData)){
                update_faceEntity.setInsert_face_image_content_list(new ArrayList<>());
                update_faceEntity.setDelete_face_id_list(new ArrayList<>());
                update_faceEntity.setEnforce(false);
            } else {
                //删除的照片
                ArrayList<String> faceIdList = new ArrayList<>();
                for (FaceListBean item: originalStaff.getFace_list()) {
                    faceIdList.add(item.getFace_id());
                }
                update_faceEntity.setDelete_face_id_list(faceIdList);
                //强制删除
                update_faceEntity.setEnforce(true);
                //新增照片
                ArrayList<String> faceBase64List = new ArrayList<>();
                faceBase64List.add(photoData);
                update_faceEntity.setInsert_face_image_content_list(faceBase64List);
            }
            updateStaffDTO.setUpdate_face(update_faceEntity);
            //人员信息
            PersonInformation personInformation = new PersonInformation();
            personInformation.setId(studentNum);
            personInformation.setName(name);
            updateStaffDTO.setPerson_information(personInformation);
            //meta
            updateStaffDTO.setMeta(originalStaff.getMeta());
            if (token == null && tokenService != null) {
                token = tokenService.getToken();
            }
            if (token != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.ACCEPT, "application/json");
                headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
                headers.add("Authorization", token);
                HttpEntity entity = new HttpEntity(JSON.toJSONString(updateStaffDTO),headers);
                try {
                    ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/staffs/" + originalStaff.getStaffId(), HttpMethod.PUT, entity, String.class);
                    if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                        String body = responseEntity.getBody();
                        if (body != null) {
                            ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                            if(responseModel != null && responseModel.getRtn() == 0) {
                                Staff newStaff = JSON.parseObject(responseModel.getResult(), Staff.class);
                                if(newStaff != null) {
                                    logger.info("Update student success! ");
                                    if(photoData != null && photoData != "") {
                                        logger.info("Delete photo id ");
                                    }
                                    StudentSearchModel studentSearchModel = new StudentSearchModel();
                                    studentSearchModel.setBranches(JSON.parseArray(branches, Branch.class));
                                    studentSearchModel.setName(name);
                                    studentSearchModel.setStudentNum(studentNum);
                                    studentSearchModel.setCreateTime(formatter.format(new Date(newStaff.getUploadTime() * (long)1000)));
                                    studentSearchModel.setFaceId(newStaff.getStaffId());
                                    studentSearchModel.setPhotoUrl(STAFF_IMAGE_URL + "image/" + newStaff.getFace_list().get(0).getFace_image_id());
                                    return ResultGenerator.genSuccessResult(studentSearchModel);
                                } else {
                                    return ResultGenerator.genFailResult("更新学生失败!");
                                }

                            }
                        }
                    }
                } catch (HttpClientErrorException exception) {
                    if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                        token = tokenService.getToken();
                        if (token != null) {
                            update(faceId, photoData, name, studentNum, branches, role);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("更新学生信息失败");

                }
            }
            return ResultGenerator.genFailResult("更新学生失信息败!");
        }
    }


    @PostMapping("/search")
    public Result search(@RequestParam String photoData, @RequestParam(defaultValue = "85") Long score) {
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (photoData != null) {
            ArrayList<StudentSearchModel> searchResultList = new ArrayList<>();
            HashMap<String, Object> postParameters = new HashMap<>();
            postParameters.put("image_content_base64", photoData);
            //只查85分以上的，至多3人
            postParameters.put("threshold", score < 85 ? 85 : score);
            postParameters.put("topk", SEARCH_PERSON_NUMBER);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/persons/retrieval", entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                        if (responseModel != null && responseModel.getResult() != null && responseModel.getRtn() == 0) {
                            ArrayList<PersonRetrievalResultDTO> tempList = (ArrayList<PersonRetrievalResultDTO>) JSONArray.parseArray(responseModel.getResult(), PersonRetrievalResultDTO.class);
                            if (tempList != null && tempList.size() > 0) {
                                for (PersonRetrievalResultDTO person : tempList) {
                                    StudentSearchModel studentSearchModel = new StudentSearchModel();
                                    studentSearchModel.setFaceId(person.getPerson().getPerson_id());
                                    studentSearchModel.setName(person.getPerson().getPerson_information().getName());
                                    studentSearchModel.setStudentNum(person.getPerson().getPerson_information().getId());
                                    studentSearchModel.setPhotoUrl(PARK_BASE_URL + "image/" + person.getPerson().getFace_list().get(0).getFace_image_id());
                                    studentSearchModel.setScore(person.getScore());
                                    studentSearchModel.setCreateTime(formatter.format(new Date(person.getPerson().getUpload_time()* (long)1000)));
                                    ArrayList<Branch> branches = new ArrayList<>();
                                    for (int i = 0; i < person.getPerson().getTag_id_list().size(); i++) {
                                        Tag tag = tagService.getTagById(person.getPerson().getTag_id_list().get(i));
                                        if(tag != null && tag.getMeta() != null) {
                                            Branch branch = new Branch();
                                            branch.setId((String)tag.getMeta().get("id"));
                                            branch.setName((String)tag.getMeta().get("name"));
                                            branches.add(branch);
                                        } else {
                                            logger.error("Can not find tag by tag ID! ID:{}",person.getPerson().getTag_id_list().get(i));
                                        }
                                    }
                                    studentSearchModel.setBranches(branches);
                                    searchResultList.add(studentSearchModel);
                                }
                            }
                        } else if(responseModel.getRtn() != 0) {
                            logger.warn("Search student failed! message:{}",responseModel.getMessage());
                            ResultGenerator.genFailResult(responseModel.getMessage());
                        }
                    }
                }
            } catch (HttpClientErrorException errorException) {
                if (errorException.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    if (token != null) {
                        search(photoData,score);
                    }
                }
            }
            return ResultGenerator.genSuccessResult(searchResultList);
        } else {
            return ResultGenerator.genFailResult("Cannot get staff's base64 image!");
        }
    }

    private Staff createStaff(Staff staff) {
        Staff resultStaff = null;

        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            HashMap<String, Object> postParameters = new HashMap<>();
            //只获取员工数据
            ArrayList<Staff> staffArrayList = new ArrayList<>();
            staffArrayList.add(staff);
            postParameters.put("staff_list", staffArrayList);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(JSON.toJSONString(postParameters),headers);
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/staffs/", HttpMethod.POST, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                        if(responseModel != null && responseModel.getRtn() == 0) {
                            List<ResponseModel> model = JSONArray.parseArray(responseModel.getResult(),ResponseModel.class);
                            resultStaff = JSON.parseObject(model.get(0).getResult(), Staff.class);
                        }
                    }
                }
            } catch (HttpClientErrorException exception) {
                if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    if (token != null) {
                        createStaff(staff);
                    }
                }
            }
        }
        return resultStaff;
    }
}
