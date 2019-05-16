/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 50553
 Source Host           : localhost:3306
 Source Schema         : angli_db

 Target Server Type    : MySQL
 Target Server Version : 50553
 File Encoding         : 65001

 Date: 16/05/2019 21:59:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for record
-- ----------------------------
DROP TABLE IF EXISTS `record`;
CREATE TABLE `record`  (
  `face_record_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'UUID',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '姓名',
  `student_num` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '学号',
  `face_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '人脸平台对应的staff_id',
  `branch` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '分支机构:{\"id\":\"12345\",\"name\":\"智立方浦东花木分校\"}',
  `status` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息是否推送成功',
  `attendance_time` datetime NOT NULL COMMENT '考勤时间',
  `push_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL COMMENT '插入数据库时间',
  `role` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '1代表学生, 2代表老师',
  PRIMARY KEY (`face_record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of record
-- ----------------------------
INSERT INTO `record` VALUES ('4510c1ce876a4772b9006a3a49a7c814', 'Hu Tong', 'qwcd38', '6187', '{\"name\":\"智立方浦东花木分校\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-03-16 12:21:14', NULL, '2019-03-16 14:42:16', NULL);
INSERT INTO `record` VALUES ('4aff8977060b40ed8dfaba2629e0915c', '王嘉乐', '_Lead418771_ZLF', '6333', '{\"name\":\"智立方浦东花木分校\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-03-16 20:10:18', NULL, '2019-03-16 20:10:23', NULL);
INSERT INTO `record` VALUES ('5824564241794e95ab42010b24ba193b', 'Hu Tong', 'qr0574th', '26176', '{\"name\":\"Test\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-05-14 23:36:20', NULL, '2019-05-14 23:44:49', '1');
INSERT INTO `record` VALUES ('67dc93cf47534f588199ed019912ed41', '王嘉乐', '_Lead418771_ZLF', '6333', '{\"name\":\"智立方浦东花木分校\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-03-16 21:39:21', NULL, '2019-03-16 23:47:06', NULL);
INSERT INTO `record` VALUES ('6a7699d218a84aabae447045c1256660', 'Hu Tong', 'qwcd38', '6187', '{\"name\":\"智立方浦东花木分校\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-03-16 13:03:28', NULL, '2019-03-16 14:42:20', NULL);
INSERT INTO `record` VALUES ('6c48090df28a40c7a7c5a94c272d7c68', '王嘉乐', '_Lead418771_ZLF', '6333', '{\"name\":\"智立方浦东花木分校\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-03-17 01:25:39', NULL, '2019-03-17 01:25:43', NULL);
INSERT INTO `record` VALUES ('9ddc7e6621fc43c2a6e88f68c1417d1b', '王嘉乐', '_Lead418771_ZLF', '6333', '{\"name\":\"智立方浦东花木分校\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-03-17 01:27:44', NULL, '2019-03-17 01:27:47', NULL);
INSERT INTO `record` VALUES ('e45da06ff83c41e896cf8fc89c79ccde', 'Hu Tong', 'qwcd38', '6187', '{\"name\":\"智立方浦东花木分校\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-03-16 15:37:16', NULL, '2019-03-16 15:39:25', NULL);
INSERT INTO `record` VALUES ('e68aafdedf334d9d9be3dcf4c4420349', 'Hu Tong', 'qr0574th', '26176', '{\"name\":\"Test\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-05-15 00:26:34', NULL, '2019-05-15 00:26:39', '1');
INSERT INTO `record` VALUES ('eb8423b752754a048c5eb8c769284713', 'Hu Tong', 'qwcd38', '6187', '{\"name\":\"智立方浦东花木分校\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-03-16 12:21:16', NULL, '2019-03-16 14:42:18', NULL);
INSERT INTO `record` VALUES ('efbe8774fb5c4b55a1cc3fafa0db3280', '王嘉乐', '_Lead418771_ZLF', '6333', '{\"name\":\"智立方浦东花木分校\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-03-16 23:51:27', NULL, '2019-03-16 23:51:32', NULL);
INSERT INTO `record` VALUES ('f616212395a542a7b5dda3732088e1b4', 'Hu Tong', 'qwcd38', '6187', '{\"name\":\"智立方浦东花木分校\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-03-16 14:16:51', NULL, '2019-03-16 14:42:22', NULL);
INSERT INTO `record` VALUES ('fb06b048fdc142aaba81ecc2c4ac5f81', 'Hu Tong', 'qr0574th', '26176', '{\"name\":\"Test\",\"id\":\"Campu_HuaMu__ZLF\"}', 0, '2019-05-14 23:36:19', NULL, '2019-05-14 23:44:43', '1');

SET FOREIGN_KEY_CHECKS = 1;
