-- Seed TECHNICIAN into public.users from Supabase Auth.
-- 1. Authentication → Users → Add user (same email, Auto Confirm).
-- 2. Replace the email below if needed.
-- 3. Run in Supabase SQL Editor.

INSERT INTO public.users (id, email, full_name, first_name, last_name, phone, role)
SELECT
  id,
  email,
  'สแน เขียยอด',
  'สแน',
  'เขียยอด',
  '0890002345',
  'TECHNICIAN'
FROM auth.users
WHERE email = 'technician@home.com'
ON CONFLICT (email) DO UPDATE
SET
  id = EXCLUDED.id,
  full_name = EXCLUDED.full_name,
  first_name = EXCLUDED.first_name,
  last_name = EXCLUDED.last_name,
  phone = EXCLUDED.phone,
  role = 'TECHNICIAN',
  updated_at = CURRENT_TIMESTAMP;

SELECT u.user_id, u.id, u.email, u.role, a.id AS auth_id
FROM public.users u
JOIN auth.users a ON a.id = u.id
WHERE u.email = 'technician@home.com';
