CREATE TABLE `bankaccounts` (
  `id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY ,
  `accountname` VARCHAR( 255 ) NOT NULL ,
  `owners` LONGTEXT NOT NULL,
  `users` LONGTEXT NOT NULL,
  `password` VARCHAR( 255 ) NULL DEFAULT '',
  `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0'
)
CREATE TABLE `bankareas` (
  `id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY ,
  `world` VARCHAR( 255 ) NOT NULL,
  `areaname` VARCHAR( 255 ) NOT NULL ,
  `x1` INT( 255 ) NOT NULL ,
  `y1` INT( 255 ) NOT NULL ,
  `z1` INT( 255 ) NOT NULL ,
  `x2` INT( 255 ) NOT NULL ,
  `y2` INT( 255 ) NOT NULL ,
  `z2` INT( 255 ) NOT NULL
)
CREATE TABLE `bankloans` (
  `id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY ,
  `player` VARCHAR( 255 ) NOT NULL,
  `totalamount` DOUBLE( 255,2 ) NOT NULL,
  `remaining` DOUBLE( 255,2 ) NOT NULL,
  `timepayment` INT( 255 ) NOT NULL,
  `timeleft` INT( 255 ) NOT NULL,
  `part` INT( 255 ) NOT NULL,
  `parts` INT( 255 ) NOT NULL
)
CREATE TABLE `banktransactions` (
  `id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY ,
  `player` VARCHAR( 255 ) NOT NULL,
  `account` VARCHAR( 255 ) NULL,
  `type` INT( 255 ) NOT NULL,
  `amount` DOUBLE( 255,2 ) NULL,
  `time` INT( 255 ) NOT NULL
)