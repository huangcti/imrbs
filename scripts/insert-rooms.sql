INSERT INTO rooms (name, floor, building, location_description, capacity, equipment, photos, status, features, created_at, updated_at)
VALUES 
  ('會議室 A (小型)', '3F', '總部大樓', '電梯旁', 6, 
   '[{"name":"投影機","quantity":1},{"name":"白板","quantity":1},{"name":"視訊設備","quantity":1}]'::jsonb, 
   '[]'::jsonb, 'AVAILABLE', 
   '["video_conferencing","whiteboard"]'::jsonb, NOW(), NOW()),
   
  ('會議室 B (中型)', '3F', '總部大樓', '茶水間對面', 12, 
   '[{"name":"投影機","quantity":1},{"name":"白板","quantity":2},{"name":"視訊設備","quantity":1},{"name":"電視螢幕","quantity":1}]'::jsonb, 
   '[]'::jsonb, 'AVAILABLE', 
   '["video_conferencing","whiteboard","tv_display"]'::jsonb, NOW(), NOW()),
   
  ('大會議室 (多功能廳)', '5F', '總部大樓', 'VIP 區', 30, 
   '[{"name":"投影機","quantity":2},{"name":"白板","quantity":2},{"name":"視訊設備","quantity":1},{"name":"音響系統","quantity":1},{"name":"麥克風","quantity":4}]'::jsonb, 
   '[]'::jsonb, 'AVAILABLE', 
   '["video_conferencing","whiteboard","audio_system","microphone"]'::jsonb, NOW(), NOW()),
   
  ('訓練室', '4F', '總部大樓', '人資部旁', 20, 
   '[{"name":"投影機","quantity":1},{"name":"白板","quantity":3},{"name":"電腦設備","quantity":20}]'::jsonb, 
   '[]'::jsonb, 'AVAILABLE', 
   '["training","whiteboard","computers"]'::jsonb, NOW(), NOW()),
   
  ('會議室 C (維護中)', '2F', '總部大樓', '接待區旁', 8, 
   '[{"name":"投影機","quantity":1},{"name":"白板","quantity":1}]'::jsonb, 
   '[]'::jsonb, 'MAINTENANCE', 
   '["whiteboard"]'::jsonb, NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET
  floor = EXCLUDED.floor,
  building = EXCLUDED.building,
  location_description = EXCLUDED.location_description,
  capacity = EXCLUDED.capacity,
  equipment = EXCLUDED.equipment,
  status = EXCLUDED.status,
  features = EXCLUDED.features,
  updated_at = NOW();
