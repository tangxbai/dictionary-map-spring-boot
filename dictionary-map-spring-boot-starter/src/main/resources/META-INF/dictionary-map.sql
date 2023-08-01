/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50728
Source Host           : 127.0.0.1:3306
Source Database       : tester

Target Server Type    : MYSQL
Target Server Version : 50728
File Encoding         : 65001

Date: 2023-07-24 17:26:05
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for global_dictionary
-- ----------------------------
DROP TABLE IF EXISTS `global_dictionary`;
CREATE TABLE `global_dictionary` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `type` enum('ENUM','TEXT') NOT NULL,
  `key` varchar(64) NOT NULL COMMENT '字典键',
  `code` tinyint(5) NOT NULL DEFAULT '0' COMMENT '字典真实值',
  `alias` varchar(32) DEFAULT NULL COMMENT '键别名',
  `text` varchar(128) DEFAULT NULL COMMENT '字典文本',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for global_dictionary_lang
-- ----------------------------
DROP TABLE IF EXISTS `global_dictionary_lang`;
CREATE TABLE `global_dictionary_lang` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `lang` varchar(64) DEFAULT NULL COMMENT '字典键',
  `label` varchar(128) NOT NULL COMMENT '字典文本',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
