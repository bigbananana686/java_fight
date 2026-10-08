-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: java_fight
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `tb_user`
--

DROP TABLE IF EXISTS `tb_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `age` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `password` varchar(100) NOT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'USER',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_user`
--

LOCK TABLES `tb_user` WRITE;
/*!40000 ALTER TABLE `tb_user` DISABLE KEYS */;
INSERT INTO `tb_user` VALUES (21,'test-bcrypt-01',20,'2026-09-26 18:32:20','$2a$10$rGY0WZUPbKRrjcZP94FJKuTIrkkngeXSEcubZsch24UsNZxZiVNGS','ADMIN'),(27,'test-bcrypt-02',21,'2026-09-26 19:01:16','$2a$10$32RForOOkYTDmaC1lkP7sOUp0t/BF4m2j.ny3gWEStRUexbCwuHtK','USER'),(37,'test-user-01',22,'2026-09-27 16:38:11','$2a$10$fTA3yaCUJBW00nvMSuto6uBCrOLXTgc/ksNXtF/XFkgJNW4YMUrve','USER');
/*!40000 ALTER TABLE `tb_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-28 20:48:36

-- ===== Stage 7: tb_conversation / tb_message / tb_doc_chunk =====
CREATE TABLE `tb_conversation` (
  `id`         bigint       NOT NULL AUTO_INCREMENT,
  `user_id`    bigint       NOT NULL,
  `title`      varchar(100) DEFAULT NULL,
  `created_at` datetime     NOT NULL,
  `updated_at` datetime     NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id_updated` (`user_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ===== tb_message =====
CREATE TABLE `tb_message` (
  `id`              bigint      NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint      NOT NULL,
  `role`            varchar(16) NOT NULL,
  `content`         text        NOT NULL,
  `created_at`      datetime    NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id_id` (`conversation_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ===== tb_doc_chunk =====
CREATE TABLE `tb_doc_chunk` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `user_id`     bigint       NOT NULL,
  `doc_name`    varchar(255) NOT NULL,
  `chunk_index` int          NOT NULL,
  `content`     text         NOT NULL,
  `embedding`   mediumtext   NOT NULL,
  `created_at`  datetime     NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
