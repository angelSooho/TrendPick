ALTER TABLE product DROP INDEX `title`;
ALTER TABLE brand DROP INDEX `name`;
ALTER TABLE main_category DROP INDEX `name`;
ALTER TABLE sub_category DROP INDEX `name`;

ALTER TABLE product ADD FULLTEXT INDEX `title` (`title`) VISIBLE;
ALTER TABLE brand ADD FULLTEXT INDEX `name` (`name`) VISIBLE;
ALTER TABLE main_category ADD FULLTEXT INDEX `name` (`name`) VISIBLE;
ALTER TABLE sub_category ADD FULLTEXT INDEX `name` (`name`) VISIBLE;