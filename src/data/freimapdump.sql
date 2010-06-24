# Sequel Pro dump
# Version 1630
# http://code.google.com/p/sequel-pro
#
# Host: 127.0.0.1 (MySQL 5.1.44)
# Database: pinco
# Generation Time: 2010-06-18 22:56:25 +0100
# ************************************************************

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table FLOWS
# ------------------------------------------------------------

DROP TABLE IF EXISTS `FLOWS`;

CREATE TABLE `FLOWS` (
  `RECNUM` bigint(20) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `PROBE` varchar(16) NOT NULL DEFAULT '',
  `TIME_RECEIVED` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `FLOW_VERSION` tinyint(4) NOT NULL DEFAULT '0',
  `SEQUENCE` bigint(20) unsigned zerofill NOT NULL DEFAULT '00000000000000000000',
  `SOURCE_IP` varchar(16) NOT NULL DEFAULT '',
  `SOURCE_PORT` int(11) NOT NULL DEFAULT '0',
  `DEST_IP` varchar(16) NOT NULL DEFAULT '',
  `DEST_PORT` int(11) NOT NULL DEFAULT '0',
  `NEXT_HOP` varchar(16) NOT NULL DEFAULT '',
  `PROTOCOL` int(11) NOT NULL DEFAULT '0',
  `TCP_FLAGS` int(11) NOT NULL DEFAULT '0',
  `TOS` int(11) NOT NULL DEFAULT '0',
  `BYTES` bigint(20) unsigned NOT NULL DEFAULT '0',
  `PACKETS` bigint(20) unsigned NOT NULL DEFAULT '0',
  `FLOW_BEGIN` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `FLOW_END` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `SOURCE_AS` int(11) NOT NULL DEFAULT '0',
  `DEST_AS` int(11) NOT NULL DEFAULT '0',
  `SOURCE_MASK` smallint(6) NOT NULL DEFAULT '0',
  `DEST_MASK` smallint(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`RECNUM`),
  KEY `k_begin` (`FLOW_BEGIN`),
  KEY `k_end` (`FLOW_END`),
  KEY `k_time` (`TIME_RECEIVED`),
  KEY `k_src` (`SOURCE_IP`),
  KEY `k_dst` (`DEST_IP`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table Layer
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Layer`;

CREATE TABLE `Layer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `srcnode` varchar(32) NOT NULL,
  `clock` timestamp NULL DEFAULT NULL,
  `destnode` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `srcnode` (`srcnode`),
  KEY `destnode` (`destnode`),
  CONSTRAINT `layer_ibfk_1` FOREIGN KEY (`srcnode`) REFERENCES `links` (`src`),
  CONSTRAINT `layer_ibfk_2` FOREIGN KEY (`destnode`) REFERENCES `links` (`dest`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table links
# ------------------------------------------------------------

DROP TABLE IF EXISTS `links`;

CREATE TABLE `links` (
  `clock` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `src` varchar(32) NOT NULL DEFAULT '',
  `dest` varchar(32) NOT NULL DEFAULT '',
  `lq` float DEFAULT NULL,
  `nlq` float DEFAULT NULL,
  `etx` float DEFAULT NULL,
  PRIMARY KEY (`clock`,`src`,`dest`),
  KEY `src` (`src`),
  KEY `dest` (`dest`),
  CONSTRAINT `links_ibfk_2` FOREIGN KEY (`dest`) REFERENCES `nodes` (`ip`),
  CONSTRAINT `links_ibfk_1` FOREIGN KEY (`src`) REFERENCES `nodes` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table nodes
# ------------------------------------------------------------

DROP TABLE IF EXISTS `nodes`;

CREATE TABLE `nodes` (
  `lon` float DEFAULT '12.5535',
  `lat` float DEFAULT '41.8638',
  `ip` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `isGateway` binary(1) NOT NULL,
  `gatewayIp` varchar(32) NOT NULL,
  `uptime` time DEFAULT NULL,
  `interfaces` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`name`),
  KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
