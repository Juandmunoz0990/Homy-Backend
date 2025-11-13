INSERT INTO users (id, name, email, password, phone_number, role, birth_date) VALUES
(1, 'Carlos Gómez', 'carlos@gmail.com', 'Pass1234', '3001234567', 'HOST', '1990-05-14'),
(2, 'María López', 'maria@gmail.com', 'Pass4567', '3109876543', 'GUEST', '1995-07-20'),
(3, 'Andrés Torres', 'andres@gmail.com', 'Pass7890', '3015558899', 'ADMIN', '1988-03-02'),
(4, 'Laura Martínez', 'laura@gmail.com', 'Pass1112', '3207774444', 'HOST', '1993-09-18'),
(5, 'Julián Pérez', 'julian@gmail.com', 'Pass2223', '3156667777', 'GUEST', '1998-12-30');

INSERT INTO housings (id, title, description, city, address, latitude, length, night_price, max_capacity, state, average_rating, host_id) VALUES
(1, 'Casa Colonial en Cartagena', 'Hermosa casa cerca del mar con piscina y WiFi.', 'Cartagena', 'Calle 10 #5-30', 10.4236, -75.5253, 250000, 6, 'active', 4.7, 1),
(2, 'Apartamento Moderno en Bogotá', 'Apartamento moderno con vista a la ciudad.', 'Bogotá', 'Cra 45 #26-50', 4.6097, -74.0817, 180000, 4, 'active', 4.5, 1),
(3, 'Cabaña en el Eje Cafetero', 'Cabaña acogedora rodeada de naturaleza.', 'Armenia', 'Km 5 vía Montenegro', 4.5173, -75.7030, 150000, 5, 'active', 4.8, 4),
(4, 'Hostal en Medellín', 'Habitaciones privadas y compartidas en zona céntrica.', 'Medellín', 'Carrera 70 #44-10', 6.2442, -75.5812, 90000, 10, 'active', 4.2, 4),
(5, 'Casa de Playa en Santa Marta', 'Casa frente al mar con piscina y desayuno incluido.', 'Santa Marta', 'Calle 1 #12-20', 11.2408, -74.1990, 300000, 8, 'active', 4.9, 1);

INSERT INTO housing_services (housing_id, services)
VALUES 
(1, 'WIFI'),
(1, 'POOL'),
(1, 'AIR_CONDITIONING'),
(2, 'WIFI'),
(2, 'PARKING'),
(2, 'GYM');

INSERT INTO bookings (id, housing_id, guest_id, check_in, check_out, guests_number, status, total_price, created_at) VALUES
(1, 1, 2, '2025-10-20', '2025-10-25', 4, 'CONFIRMED', 1250000, NOW()),
(2, 2, 5, '2025-11-01', '2025-11-03', 2, 'CONFIRMED', 360000, NOW()),
(3, 3, 2, '2025-09-15', '2025-09-20', 3, 'COMPLETED', 750000, NOW()),
(4, 4, 5, '2025-08-10', '2025-08-15', 1, 'CANCELED', 0, NOW()),
(5, 5, 2, '2025-12-05', '2025-12-10', 5, 'CONFIRMED', 1500000, NOW());

INSERT INTO host_details (user_id, description, legal_documents_url)
VALUES
(1, 'Host con experiencia en turismo ecológico. Ofrece casas rurales en zonas de montaña.', 'docs/host1_licencia.pdf'),
(2, 'Anfitriona especializada en alojamientos familiares cerca de la playa.', 'docs/host2_registro.pdf'),
(3, 'Propietario con múltiples apartamentos amoblados en el centro de la ciudad.', 'docs/host3_doc.pdf'),
(4, 'Anfitriona con enfoque en turismo sostenible y experiencias gastronómicas locales.', 'docs/host4_certificado.pdf'),
(5, 'Host que ofrece alojamiento en fincas con actividades al aire libre.', 'docs/host5_licencia.pdf');