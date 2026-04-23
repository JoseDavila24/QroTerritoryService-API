-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;

-- Delegaciones (Las que ya tenías)
INSERT INTO delegaciones (id, nombre, sede) VALUES (1, 'Centro Histórico', 'Madero 81, Centro');
INSERT INTO delegaciones (id, nombre, sede) VALUES (2, 'Santa Rosa Jáuregui', 'Agripín García Estrada s/n');
INSERT INTO delegaciones (id, nombre, sede) VALUES (3, 'Felipe Carrillo Puerto', 'Calzada de Guadalupe 103');

-- Colonias (Nuevas)
-- Pertenecen a la Delegación 1 (Centro Histórico)
INSERT INTO colonias (id, nombre, codigoPostal, delegacion_id) VALUES (1, 'Centro', '76000', 1);
INSERT INTO colonias (id, nombre, codigoPostal, delegacion_id) VALUES (2, 'Barrio de La Cruz', '76020', 1);

-- Pertenecen a la Delegación 2 (Santa Rosa Jáuregui)
INSERT INTO colonias (id, nombre, codigoPostal, delegacion_id) VALUES (3, 'Juriquilla', '76230', 2);