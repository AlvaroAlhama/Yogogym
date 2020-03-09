/*
-- One admin user, named admin1 with passwor 4dm1n and authority admin
INSERT INTO users(username,password,enabled) VALUES ('admin1','4dm1n',TRUE);
INSERT INTO authorities VALUES ('admin1','admin');
-- One owner user, named owner1 with passwor 0wn3r
INSERT INTO users(username,password,enabled) VALUES ('owner1','0wn3r',TRUE);
INSERT INTO authorities VALUES ('owner1','owner');
-- One vet user, named vet1 with passwor v3t
INSERT INTO users(username,password,enabled) VALUES ('vet1','v3t',TRUE);
INSERT INTO authorities VALUES ('vet1','veterinarian');

INSERT INTO vets VALUES (1, 'James', 'Carter');
INSERT INTO vets VALUES (2, 'Helen', 'Leary');
INSERT INTO vets VALUES (3, 'Linda', 'Douglas');
INSERT INTO vets VALUES (4, 'Rafael', 'Ortega');
INSERT INTO vets VALUES (5, 'Henry', 'Stevens');
INSERT INTO vets VALUES (6, 'Sharon', 'Jenkins');

INSERT INTO specialties VALUES (1, 'radiology');
INSERT INTO specialties VALUES (2, 'surgery');
INSERT INTO specialties VALUES (3, 'dentistry');

INSERT INTO vet_specialties VALUES (2, 1);
INSERT INTO vet_specialties VALUES (3, 2);
INSERT INTO vet_specialties VALUES (3, 3);
INSERT INTO vet_specialties VALUES (4, 2);
INSERT INTO vet_specialties VALUES (5, 1);

INSERT INTO types VALUES (1, 'cat');
INSERT INTO types VALUES (2, 'dog');
INSERT INTO types VALUES (3, 'lizard');
INSERT INTO types VALUES (4, 'snake');
INSERT INTO types VALUES (5, 'bird');
INSERT INTO types VALUES (6, 'hamster');

INSERT INTO owners VALUES (1, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023', 'owner1');
INSERT INTO owners VALUES (2, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749', 'owner1');
INSERT INTO owners VALUES (3, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763', 'owner1');
INSERT INTO owners VALUES (4, 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198', 'owner1');
INSERT INTO owners VALUES (5, 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765', 'owner1');
INSERT INTO owners VALUES (6, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654', 'owner1');
INSERT INTO owners VALUES (7, 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387', 'owner1');
INSERT INTO owners VALUES (8, 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683', 'owner1');
INSERT INTO owners VALUES (9, 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435', 'owner1');
INSERT INTO owners VALUES (10, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487', 'owner1');

INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (1, 'Leo', '2010-09-07', 1, 1);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (2, 'Basil', '2012-08-06', 6, 2);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (3, 'Rosy', '2011-04-17', 2, 3);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (4, 'Jewel', '2010-03-07', 2, 3);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (5, 'Iggy', '2010-11-30', 3, 4);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (6, 'George', '2010-01-20', 4, 5);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (7, 'Samantha', '2012-09-04', 1, 6);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (8, 'Max', '2012-09-04', 1, 6);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (9, 'Lucky', '2011-08-06', 5, 7);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (10, 'Mulligan', '2007-02-24', 2, 8);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (11, 'Freddy', '2010-03-09', 5, 9);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (12, 'Lucky', '2010-06-24', 2, 10);
INSERT INTO pets(id,name,birth_date,type_id,owner_id) VALUES (13, 'Sly', '2012-06-08', 1, 10);

INSERT INTO visits(id,pet_id,visit_date,description) VALUES (1, 7, '2013-01-01', 'rabies shot');
INSERT INTO visits(id,pet_id,visit_date,description) VALUES (2, 8, '2013-01-02', 'rabies shot');
INSERT INTO visits(id,pet_id,visit_date,description) VALUES (3, 8, '2013-01-03', 'neutered');
INSERT INTO visits(id,pet_id,visit_date,description) VALUES (4, 7, '2013-01-04', 'spayed');
*/

INSERT INTO users(username,password,enabled) VALUES ('admin1','admin1999',TRUE);
INSERT INTO authorities VALUES (1,'admin','admin1');
INSERT INTO authorities VALUES (2,'user','admin1');
INSERT INTO admins VALUES (1, 'proyectowip@vekto.com','Vekto', 'Rino', '12345678A','admin1');

INSERT INTO users(username,password,enabled) VALUES ('user1','user1999',TRUE);
INSERT INTO authorities VALUES (3,'user','user1');
INSERT INTO clients VALUES (2, 'proyectowip@keke.com','Keke', 'Rino', '12345678B','user1');

INSERT INTO users(username,password,enabled) VALUES ('trainer1','trainer1999',TRUE);
INSERT INTO authorities VALUES (4,'trainer','trainer1');
INSERT INTO trainers VALUES (3,  'proyectowip@alvaro.com', 'Alvaro', 'Rino', '12345678C','trainer1');

INSERT INTO trainers_clients(trainer_id,clients_id) VALUES (3,2);

INSERT INTO machines(name, location) VALUES('Gemelos_10000','Musculación');
INSERT INTO machines(name, location) VALUES('Biceps_9000','Musculación');
INSERT INTO machines(name, location) VALUES('Cinta de Correr','Cardio');

INSERT INTO exercises(name, description, kcal, intensity, machine_id) VALUES('Biceps Normales', 'Arriba abajo', 50, 2, 2);
INSERT INTO exercises(name, description, kcal, intensity, machine_id) VALUES('Correr', '20 minutos v=12', 200, 3, 3);

INSERT INTO routines(name, description) VALUES('Piernas', 'Piernas fuertes');
INSERT INTO routines(name, description) VALUES('Brazos', 'Biceps redondos');

INSERT INTO clients_routines(client_id,routines_id) VALUES(2,1);
INSERT INTO clients_routines(client_id,routines_id) VALUES(2,2);

INSERT INTO routines_lines(reps, weight,exercise_id,routine_id) VALUES(10, 25.0, 1, 1);
INSERT INTO routines_lines(reps, weight,exercise_id,routine_id) VALUES(5, 0.0, 2, 1);
INSERT INTO routines_lines(reps, weight,exercise_id,routine_id) VALUES(5, 50.0, 1, 2);

INSERT INTO phrases(text, prob) VALUES('BUENOS DIAS', 0.05);
INSERT INTO phrases(text, prob) VALUES('VAMOS MAQUINA', 0.1);


INSERT INTO trainings(id,name,description) VALUES (1, 'Entrenamiento1', 'Prueba de entrenamiento 1');
INSERT INTO trainings(id,name,description) VALUES (2, 'Entrenamiento2', 'Prueba de entrenamiento 2');
INSERT INTO trainings(id,name,description) VALUES (3, 'Entrenamiento3', 'Prueba de entrenamiento 3');
INSERT INTO trainings(id,name,description) VALUES (4, 'Entrenamiento4', 'Prueba de entrenamiento 4');


INSERT INTO diets(id,type,kcal,protein,fat,carb) VALUES(1, 'Dieta 1', 1, 1, 1, 1);
INSERT INTO diets(id,type,kcal,protein,fat,carb) VALUES(2, 'Dieta 2', 1, 1, 1, 1);
INSERT INTO diets(id,type,kcal,protein,fat,carb) VALUES(3, 'Dieta 3', 1, 1, 1, 1);
INSERT INTO diets(id,type,kcal,protein,fat,carb) VALUES(4, 'Dieta 4', 1, 1, 1, 1);

INSERT INTO challenges(id,description,reward,start,end,status) VALUES(1, 'Challenge1', 'Reward1', '2020-01-01', '2020-01-02', 2);