ALTER TABLE `tb_article`
  ADD COLUMN `article_summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '概要'
  AFTER `article_content`;
