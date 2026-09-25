-- Seed Sample Technician Jobs for technician@home.com
-- This script creates sample Pending (ACCEPTED) and History (COMPLETED) jobs.

DO $$
DECLARE
  tech_user RECORD;
  cust_user RECORD;
  service_clean RECORD;
  service_install RECORD;
  service_ac RECORD;
  cat_general RECORD;
BEGIN
  -- Get technician
  SELECT * INTO tech_user FROM public.users WHERE email = 'technician@home.com' LIMIT 1;
  IF tech_user IS NULL THEN
    SELECT * INTO tech_user FROM public.users WHERE role = 'TECHNICIAN' LIMIT 1;
  END IF;

  -- Get customer
  SELECT * INTO cust_user FROM public.users WHERE role = 'CUSTOMER' OR email != 'technician@home.com' LIMIT 1;
  IF cust_user IS NULL THEN
    cust_user := tech_user;
  END IF;

  -- Get services
  SELECT * INTO service_clean FROM public.services WHERE service_name LIKE '%ล้างแอร์%' LIMIT 1;
  SELECT * INTO service_install FROM public.services WHERE service_name LIKE '%ติดตั้ง%' LIMIT 1;
  SELECT * INTO service_ac FROM public.services LIMIT 1;

  IF service_clean IS NULL THEN service_clean := service_ac; END IF;
  IF service_install IS NULL THEN service_install := service_ac; END IF;

  IF tech_user IS NOT NULL AND service_clean IS NOT NULL THEN
    -- 1. Pending Job 1
    INSERT INTO public.service_jobs (
      customer_id, technician_id, service_id, address, latitude, longitude,
      status, order_code, total_price, scheduled_at, items_description, created_at, updated_at
    ) VALUES (
      cust_user.user_id, tech_user.user_id, service_clean.service_id,
      '444/4 คอนโดศุภาลัย เสนานิคม จตุจักร กรุงเทพฯ', 13.8282, 100.5750,
      'ACCEPTED', 'AD04071205', 1550.00,
      NOW() + INTERVAL '2 days', 'ล้างแอร์ 9,000 - 18,000 BTU, ติดผนัง 2 เครื่อง',
      NOW() - INTERVAL '1 day', NOW()
    );

    -- 2. Pending Job 2
    INSERT INTO public.service_jobs (
      customer_id, technician_id, service_id, address, latitude, longitude,
      status, order_code, total_price, scheduled_at, items_description, created_at, updated_at
    ) VALUES (
      cust_user.user_id, tech_user.user_id, service_install.service_id,
      '123/45 ซอยพหลโยธิน 32 แขวงจันทรเกษม เขตจตุจักร กรุงเทพฯ', 13.8300, 100.5700,
      'ACCEPTED', 'AD04071206', 2200.00,
      NOW() + INTERVAL '4 days', 'ติดตั้งเครื่องทำน้ำอุ่น ขนาด 4,500 วัตต์ 1 เครื่อง',
      NOW() - INTERVAL '2 days', NOW()
    );

    -- 3. History Job 1 (Completed with Rating and Review)
    INSERT INTO public.service_jobs (
      customer_id, technician_id, service_id, address, latitude, longitude,
      status, order_code, total_price, scheduled_at, items_description,
      rating, review_comment, created_at, updated_at
    ) VALUES (
      cust_user.user_id, tech_user.user_id, service_clean.service_id,
      '444/4 คอนโดศุภาลัย เสนานิคม จตุจักร กรุงเทพฯ', 13.8282, 100.5750,
      'COMPLETED', 'AD04071201', 1550.00,
      NOW() - INTERVAL '5 days', 'ล้างแอร์ 9,000 - 18,000 BTU, ติดผนัง 2 เครื่อง',
      4, 'เก็บงานเรียบร้อยมาก เสร็จไว มาตรงตามเวลานัดเลยค่ะ',
      NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days'
    );

    -- 4. History Job 2 (Completed with 5 Stars)
    INSERT INTO public.service_jobs (
      customer_id, technician_id, service_id, address, latitude, longitude,
      status, order_code, total_price, scheduled_at, items_description,
      rating, review_comment, created_at, updated_at
    ) VALUES (
      cust_user.user_id, tech_user.user_id, service_install.service_id,
      '88/9 หมู่บ้านกรีนวิลล์ บางกรวย นนทบุรี', 13.8100, 100.5000,
      'COMPLETED', 'AD04071198', 1890.00,
      NOW() - INTERVAL '10 days', 'ติดตั้งเครื่องดูดควัน แบบติดผนัง 1 เครื่อง',
      5, 'ช่างสุภาพมาก อธิบายการใช้งานละเอียด แนะนำเลยครับ',
      NOW() - INTERVAL '12 days', NOW() - INTERVAL '10 days'
    );
  END IF;
END $$;
