package com.eservice.iot.model.record;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

public class Record {
    /**
     * UUID
     */
    @Id
    @Column(name = "face_record_id")
    private String faceRecordId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 学号
     */
    @Column(name = "student_num")
    private String studentNum;

    /**
     * 人脸平台对应的staff_id
     */
    @Column(name = "face_id")
    private String faceId;

    /**
     * 分支机构:{"id":"12345","name":"智立方浦东花木分校"}
     */
    private String branch;

    /**
     * 消息是否推送成功
     */
    private Integer status;

    /**
     * 考勤时间
     */
    @Column(name = "attendance_time")
    private Date attendanceTime;

    @Column(name = "push_time")
    private Date pushTime;

    /**
     * 插入数据库时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 角色（学生）
     */
    @Column(name = "role")
    private String role = "1";

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 获取UUID
     *
     * @return face_record_id - UUID
     */
    public String getFaceRecordId() {
        return faceRecordId;
    }

    /**
     * 设置UUID
     *
     * @param faceRecordId UUID
     */
    public void setFaceRecordId(String faceRecordId) {
        this.faceRecordId = faceRecordId;
    }

    /**
     * 获取姓名
     *
     * @return name - 姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置姓名
     *
     * @param name 姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取学号
     *
     * @return student_num - 学号
     */
    public String getStudentNum() {
        return studentNum;
    }

    /**
     * 设置学号
     *
     * @param studentNum 学号
     */
    public void setStudentNum(String studentNum) {
        this.studentNum = studentNum;
    }

    /**
     * 获取人脸平台对应的staff_id
     *
     * @return face_id - 人脸平台对应的staff_id
     */
    public String getFaceId() {
        return faceId;
    }

    /**
     * 设置人脸平台对应的staff_id
     *
     * @param faceId 人脸平台对应的staff_id
     */
    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    /**
     * 获取分支机构:{"id":"12345","name":"智立方浦东花木分校"}
     *
     * @return branch - 分支机构:{"id":"12345","name":"智立方浦东花木分校"}
     */
    public String getBranch() {
        return branch;
    }

    /**
     * 设置分支机构:{"id":"12345","name":"智立方浦东花木分校"}
     *
     * @param branch 分支机构:{"id":"12345","name":"智立方浦东花木分校"}
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * 获取消息是否推送成功
     *
     * @return status - 消息是否推送成功
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置消息是否推送成功
     *
     * @param status 消息是否推送成功
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取考勤时间
     *
     * @return attendance_time - 考勤时间
     */
    public Date getAttendanceTime() {
        return attendanceTime;
    }

    /**
     * 设置考勤时间
     *
     * @param attendanceTime 考勤时间
     */
    public void setAttendanceTime(Date attendanceTime) {
        this.attendanceTime = attendanceTime;
    }

    /**
     * @return push_time
     */
    public Date getPushTime() {
        return pushTime;
    }

    /**
     * @param pushTime
     */
    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    /**
     * 获取插入数据库时间
     *
     * @return create_time - 插入数据库时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置插入数据库时间
     *
     * @param createTime 插入数据库时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}