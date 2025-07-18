-- Script de données de test pour OSHapp

-- Mettre à jour la contrainte de vérification du statut pour accepter nos nouveaux statuts
-- Supprimer l'ancienne contrainte si elle existe
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'appointment_status_check' 
        AND table_name = 'appointment'
    ) THEN
        ALTER TABLE appointment DROP CONSTRAINT appointment_status_check;
    END IF;
END $$;

-- Créer la nouvelle contrainte avec tous nos statuts
ALTER TABLE appointment ADD CONSTRAINT appointment_status_check 
CHECK (status IN ('DEMANDE', 'PROPOSE', 'CONFIRME', 'REPORTE', 'ANNULE', 'TERMINE'));

-- Insérer les utilisateurs avec leurs rôles

-- Utilisateurs RH
INSERT INTO users (username, password, role, active, email, created_at) VALUES
('rh1@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'HR', true, 'rh1@company.com', NOW()),
('rh2@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'HR', true, 'rh2@company.com', NOW());

-- Utilisateurs Infirmiers
INSERT INTO users (username, password, role, active, email, created_at) VALUES
('nurse1@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'NURSE', true, 'nurse1@company.com', NOW()),
('nurse2@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'NURSE', true, 'nurse2@company.com', NOW());

-- Utilisateurs Médecins
INSERT INTO users (username, password, role, active, email, created_at) VALUES
('doctor1@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'DOCTOR', true, 'doctor1@company.com', NOW()),
('doctor2@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'DOCTOR', true, 'doctor2@company.com', NOW());

-- Utilisateurs Employés (N+1 et N+2)
INSERT INTO users (username, password, role, active, email, created_at) VALUES
('manager1@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'EMPLOYEE', true, 'manager1@company.com', NOW()),
('manager2@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'EMPLOYEE', true, 'manager2@company.com', NOW());

-- Utilisateurs Employés normaux
INSERT INTO users (username, password, role, active, email, created_at) VALUES
('employee1@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'EMPLOYEE', true, 'employee1@company.com', NOW()),
('employee2@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'EMPLOYEE', true, 'employee2@company.com', NOW()),
('employee3@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'EMPLOYEE', true, 'employee3@company.com', NOW());

-- Insérer les employés avec la hiérarchie en utilisant les IDs réels
INSERT INTO employee (first_name, last_name, email, phone_number, position, department, employee_id, hire_date, user_id, manager1_id, manager2_id) VALUES
-- Managers (N+1 et N+2) - utiliser les IDs réels des utilisateurs
('Jean', 'Dupont', 'manager1@company.com', '0123456789', 'Chef de Service', 'Production', 'EMP001', '2020-01-15', 
 (SELECT id FROM users WHERE email = 'manager1@company.com'), NULL, NULL),
('Marie', 'Martin', 'manager2@company.com', '0123456790', 'Directeur', 'Production', 'EMP002', '2019-06-01', 
 (SELECT id FROM users WHERE email = 'manager2@company.com'), 
 (SELECT id FROM employee WHERE email = 'manager1@company.com'), NULL),

-- Employés avec hiérarchie
('Pierre', 'Durand', 'employee1@company.com', '0123456791', 'Ouvrier', 'Production', 'EMP003', '2021-03-10', 
 (SELECT id FROM users WHERE email = 'employee1@company.com'), 
 (SELECT id FROM employee WHERE email = 'manager1@company.com'), 
 (SELECT id FROM employee WHERE email = 'manager2@company.com')),
('Sophie', 'Leroy', 'employee2@company.com', '0123456792', 'Technicien', 'Maintenance', 'EMP004', '2021-07-20', 
 (SELECT id FROM users WHERE email = 'employee2@company.com'), 
 (SELECT id FROM employee WHERE email = 'manager1@company.com'), 
 (SELECT id FROM employee WHERE email = 'manager2@company.com')),
('Lucas', 'Moreau', 'employee3@company.com', '0123456793', 'Opérateur', 'Production', 'EMP005', '2022-01-05', 
 (SELECT id FROM users WHERE email = 'employee3@company.com'), 
 (SELECT id FROM employee WHERE email = 'manager1@company.com'), 
 (SELECT id FROM employee WHERE email = 'manager2@company.com'));

-- Insérer quelques rendez-vous de test en utilisant les IDs réels
INSERT INTO appointment (employee_id, nurse_id, doctor_id, type, status, appointment_date, reason, notes, location, created_by, created_at, updated_at) VALUES
((SELECT id FROM employee WHERE email = 'employee1@company.com'), 
 (SELECT id FROM users WHERE email = 'nurse1@company.com'), 
 (SELECT id FROM users WHERE email = 'doctor1@company.com'), 
 'SPONTANEE', 'DEMANDE', NOW() + INTERVAL '2 days', 'Visite médicale spontanée', 'Demande urgente', 'Infirmerie - Bâtiment A', 'employee1@company.com', NOW(), NOW()),
((SELECT id FROM employee WHERE email = 'employee2@company.com'), 
 (SELECT id FROM users WHERE email = 'nurse1@company.com'), 
 (SELECT id FROM users WHERE email = 'doctor1@company.com'), 
 'PERIODIQUE', 'PROPOSE', NOW() + INTERVAL '3 days', 'Visite périodique', 'Créneau proposé par l''infirmier', 'Infirmerie - Bâtiment A', 'nurse1@company.com', NOW(), NOW()),
((SELECT id FROM employee WHERE email = 'employee3@company.com'), 
 (SELECT id FROM users WHERE email = 'nurse2@company.com'), 
 (SELECT id FROM users WHERE email = 'doctor2@company.com'), 
 'EMBAUCHE', 'CONFIRME', NOW() + INTERVAL '5 days', 'Visite d''embauche', 'Rendez-vous confirmé', 'Infirmerie - Bâtiment B', 'employee3@company.com', NOW(), NOW()); 