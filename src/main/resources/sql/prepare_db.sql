CREATE DATABASE IF NOT EXISTS `tal` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */;

USE `tal`;

CREATE TABLE IF NOT EXISTS `acts` (
  `episode_number` int(11) NOT NULL,
  `act_number` int(11) NOT NULL,
  `name` varchar(150) NOT NULL,
  PRIMARY KEY (`episode_number`,`act_number`),
  KEY `episode_number_idx` (`episode_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `episodes` (
  `number` int(11) NOT NULL,
  `title` varchar(150) NOT NULL,
  PRIMARY KEY (`number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `people` (
  `name` varchar(150) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS`records` (
  `episode_number` int(11) NOT NULL,
  `act_number` int(11) NOT NULL,
  `speaker_role` varchar(50) NOT NULL,
  `speaker_name` varchar(150) NOT NULL,
  `timestamp` varchar(50) NOT NULL,
  `text` text NOT NULL,
  PRIMARY KEY (`episode_number`,`timestamp`),
  KEY `speaker_id_idx` (`speaker_name`),
  KEY `act_number_idx` (`act_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `tal`.`full_view` AS select `tal`.`acts`.`episode_number` AS `episode_number`,`tal`.`acts`.`act_number` AS `act_number`,`tal`.`acts`.`name` AS `act_name`,`tal`.`episodes`.`title` AS `episode_title`,`tal`.`records`.`speaker_role` AS `speaker_role`,`tal`.`records`.`speaker_name` AS `speaker_name`,`tal`.`records`.`timestamp` AS `timestamp`,`tal`.`records`.`text` AS `text` from ((`tal`.`acts` join `tal`.`episodes` on((`tal`.`acts`.`episode_number` = `tal`.`episodes`.`number`))) join `tal`.`records` on(((`tal`.`acts`.`episode_number` = `tal`.`records`.`episode_number`) and (`tal`.`acts`.`act_number` = `tal`.`records`.`act_number`))));
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `tal`.`staff_view` AS select `tal`.`records`.`episode_number` AS `episode_number`,`tal`.`records`.`act_number` AS `act_number`,`tal`.`records`.`speaker_role` AS `speaker_role`,`tal`.`records`.`speaker_name` AS `speaker_name`,`tal`.`records`.`timestamp` AS `timestamp`,`tal`.`records`.`text` AS `text` from `tal`.`records` where ((`tal`.`records`.`speaker_role` = 'interviewer') or (`tal`.`records`.`speaker_role` = 'host'));
