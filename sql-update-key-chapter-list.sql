ALTER TABLE `tb_article`
  ADD COLUMN `key_chapter_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '重点章节'
  AFTER `article_summary`;

