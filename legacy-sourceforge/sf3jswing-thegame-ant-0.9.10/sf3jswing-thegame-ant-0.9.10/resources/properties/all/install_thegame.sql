-- phpMyAdmin SQL Dump
-- version 2.11.9.3
-- http://www.phpmyadmin.net
--
-- Serveur: localhost
-- Généré le : Sam 19 Décembre 2009 à 13:02
-- Version du serveur: 5.0.51
-- Version de PHP: 5.2.9


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de données: `s173003_thegame`
--

-- --------------------------------------------------------

--
-- Structure de la table `game`
--

CREATE TABLE `game` (
  `id` int(11) NOT NULL,
  `host_id` int(11) NOT NULL,
  `players_num` int(11) default NULL,
  `started` timestamp NOT NULL,
  PRIMARY KEY  (`id`)
) TYPE=InnoDB COMMENT='a hosted game is running and may be joined';

-- --------------------------------------------------------

--
-- Structure de la table `host`
--

CREATE TABLE `host` (
  `id` bigint(20) NOT NULL auto_increment,
  `ip` varchar(15) NOT NULL default '127.0.0.0',
  `port` int(11) NOT NULL default '9090',
  `locale` int(11) NOT NULL,
  `api` varchar(10) NOT NULL,
  PRIMARY KEY  (`id`)
) TYPE=InnoDB COMMENT='host of a game' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Structure de la table `locale`
--

CREATE TABLE `locale` (
  `id` bigint(20) NOT NULL auto_increment,
  `language` varchar(2) NOT NULL,
  `country` varchar(2) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `language` (`language`,`country`)
) TYPE=InnoDB  AUTO_INCREMENT=2 ;
