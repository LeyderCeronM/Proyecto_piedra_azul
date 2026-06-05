-- Datos de prueba para audits-service
-- Insertar algunos logs de usuario y eventos de transferencia

INSERT INTO audit_logs (id, servicio_origen, tipo_evento, operacion, usuario_operador, detalle_payload, fecha) VALUES
  ('550e8400-e29b-41d4-a716-446655440000', 'users-service', 'LOGIN', 'User login', 'juan.perez', '{"ip":"192.168.1.10"}', '2026-06-01 09:00:00'),
  ('550e8400-e29b-41d4-a716-446655440001', 'appointments-service', 'CREATE_APPOINTMENT', 'Create appointment', 'maria.gomez', '{"appointmentId":123}', '2026-06-02 10:30:00'),
  ('550e8400-e29b-41d4-a716-446655440002', 'users-service', 'UPDATE_PROFILE', 'Update profile', 'juan.perez', '{"fields":"email"}', '2026-06-03 11:00:00'),
  ('550e8400-e29b-41d4-a716-446655440003', 'reports-service', 'GENERATE_REPORT', 'Generate report', 'ana.lopez', '{"report":"monthly"}', '2026-06-04 12:00:00'),
  -- Eventos de transferencia (TRANSFER)
  ('550e8400-e29b-41d4-a716-446655440010', 'clinical-records-service', 'TRANSFER', 'Transfer patient', 'dr.carlos', '{"patientId":555,"from":"Clinic A","to":"Clinic B"}', '2026-06-05 14:00:00'),
  ('550e8400-e29b-41d4-a716-446655440011', 'clinical-records-service', 'TRANSFER', 'Transfer patient', 'dr.martinez', '{"patientId":556,"from":"Clinic B","to":"Clinic C"}', '2026-06-05 15:30:00');
