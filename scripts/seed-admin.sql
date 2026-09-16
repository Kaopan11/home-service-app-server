-- Seed ADMIN into public.users from Supabase Auth.
-- 1. Authentication → Users → Add user (same email, Auto Confirm).
-- 2. Replace the email below if needed.
-- 3. Run in Supabase SQL Editor.

INSERT INTO public.users (id, email, full_name, role)
SELECT
  id,
  email,
  'Admin Master',
  'ADMIN'
FROM auth.users
WHERE email = 'admin@admin.com'
ON CONFLICT (email) DO UPDATE
SET
  id = EXCLUDED.id,
  full_name = EXCLUDED.full_name,
  role = 'ADMIN',
  updated_at = CURRENT_TIMESTAMP;

-- Expect one row: same uuid as Auth, role ADMIN
SELECT u.user_id, u.id, u.email, u.role, a.id AS auth_id
FROM public.users u
JOIN auth.users a ON a.id = u.id
WHERE u.email = 'admin@admin.com';
