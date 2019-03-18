/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50553
Source Host           : localhost:3306
Source Database       : angli_db

Target Server Type    : MYSQL
Target Server Version : 50553
File Encoding         : 65001

Date: 2019-03-16 02:08:59
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `record`
-- ----------------------------
DROP TABLE IF EXISTS `record`;
CREATE TABLE `record` (
  `face_record_id` varchar(32) NOT NULL COMMENT 'UUID',
  `name` varchar(255) NOT NULL COMMENT '姓名',
  `student_num` varchar(255) NOT NULL COMMENT '学号',
  `face_id` varchar(255) NOT NULL COMMENT '人脸平台对应的staff_id',
  `branch` varchar(1000) NOT NULL COMMENT '分支机构:{"id":"12345","name":"智立方浦东花木分校"}',
  `status` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '消息是否推送成功',
  `attendance_time` datetime NOT NULL COMMENT '考勤时间',
  `push_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL COMMENT '插入数据库时间',
  PRIMARY KEY (`face_record_id`),
  UNIQUE KEY `UK_Record` (`student_num`,`attendance_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of record
-- ----------------------------
