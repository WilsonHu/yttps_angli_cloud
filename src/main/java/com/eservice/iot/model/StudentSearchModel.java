package com.eservice.iot.model;

import java.util.List;

public class StudentSearchModel {
    /**
     * 对应人脸平台的staff id
     */
    private String faceId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 学号
     */
    private String studentNum;
    /**
     * 分支机构
     */
    private List<Branch> branches;
    /**
     * 相似度分数
     */
    private Double score;
    /**
     * 照片地址
     */
    private String photoUrl;
    /**
     * 创建时间：eg. 2019-03-10 23:42:00
     */
    private String createTime;

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentNum() {
        return studentNum;
    }

    public void setStudentNum(String studentNum) {
        this.studentNum = studentNum;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
