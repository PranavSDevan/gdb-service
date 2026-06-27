-- ============================================================
-- GDB Accounts Seed Data
-- V14: Realistic synthetic savings + current accounts
-- ============================================================
-- Account Numbers start from 1000 (via sequence).
-- We insert with explicit account_number values far ahead to avoid
-- conflicts with sequence-generated ones.
-- PIN hash below = bcrypt("1234")
-- ============================================================

DO $$
DECLARE
  v_pin_hash TEXT := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lJtm';
BEGIN

-- ── SAVINGS ACCOUNTS ──────────────────────────────────────
INSERT INTO accounts (account_number, account_type, name, pin_hash, balance, privilege, bank_name, bank_branch, ifsc_code, is_active)
VALUES
  (10001, 'SAVINGS', 'Amit Verma',     v_pin_hash, 125400.00, 'GOLD',    'Global Digital Bank', 'Main Branch',    'GDB0000001', TRUE),
  (10002, 'SAVINGS', 'Neha Singh',     v_pin_hash, 48750.50,  'SILVER',  'Global Digital Bank', 'North Branch',   'GDB0000002', TRUE),
  (10003, 'SAVINGS', 'Priya Sharma',   v_pin_hash, 285000.00, 'PREMIUM', 'Global Digital Bank', 'Main Branch',    'GDB0000001', TRUE),
  (10004, 'SAVINGS', 'Rajesh Kumar',   v_pin_hash, 67500.00,  'GOLD',    'Global Digital Bank', 'East Branch',    'GDB0000003', TRUE),
  (10005, 'SAVINGS', 'Sunita Agarwal', v_pin_hash, 32100.75,  'SILVER',  'Global Digital Bank', 'South Branch',   'GDB0000004', TRUE),
  (10006, 'SAVINGS', 'Arjun Mehta',    v_pin_hash, 510000.00, 'PREMIUM', 'Global Digital Bank', 'Main Branch',    'GDB0000001', TRUE),
  (10007, 'SAVINGS', 'Divya Kapoor',   v_pin_hash, 15300.00,  'SILVER',  'Global Digital Bank', 'West Branch',    'GDB0000005', TRUE),
  (10008, 'SAVINGS', 'Ravi Nair',      v_pin_hash, 92400.00,  'GOLD',    'Global Digital Bank', 'Central Branch', 'GDB0000006', FALSE)
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO savings_account_details (account_number, date_of_birth, gender, phone_no, aadhar_number)
VALUES
  (10001, '1990-05-12', 'Male',   '9876543210', '234567890123'),
  (10002, '1995-08-22', 'Female', '9876543211', '345678901234'),
  (10003, '1985-03-15', 'Female', '9876543212', '456789012345'),
  (10004, '1982-11-30', 'Male',   '9876543213', '567890123456'),
  (10005, '1992-07-04', 'Female', '9876543214', '678901234567'),
  (10006, '1978-09-18', 'Male',   '9876543215', '789012345678'),
  (10007, '1998-02-25', 'Female', '9876543216', '890123456789'),
  (10008, '1988-12-01', 'Male',   '9876543217', '901234567890')
ON CONFLICT (account_number) DO NOTHING;

-- ── CURRENT ACCOUNTS ──────────────────────────────────────
INSERT INTO accounts (account_number, account_type, name, pin_hash, balance, privilege, bank_name, bank_branch, ifsc_code, is_active)
VALUES
  (20001, 'CURRENT', 'TechNova Solutions Pvt Ltd',   v_pin_hash, 1250000.00, 'PREMIUM', 'Global Digital Bank', 'Main Branch',  'GDB0000001', TRUE),
  (20002, 'CURRENT', 'GreenLeaf Exports Ltd',        v_pin_hash, 875000.00,  'GOLD',    'Global Digital Bank', 'East Branch',  'GDB0000003', TRUE),
  (20003, 'CURRENT', 'Sunrise Retail Pvt Ltd',       v_pin_hash, 340000.00,  'SILVER',  'Global Digital Bank', 'North Branch', 'GDB0000002', TRUE),
  (20004, 'CURRENT', 'BlueSky Consulting Group',     v_pin_hash, 2100000.00, 'PREMIUM', 'Global Digital Bank', 'Main Branch',  'GDB0000001', FALSE)
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO current_account_details (account_number, company_name, website, registration_no)
VALUES
  (20001, 'TechNova Solutions Pvt Ltd',  'https://technova.in',    'U72900MH2018PTC123456'),
  (20002, 'GreenLeaf Exports Ltd',       'https://greenleaf.co',   'U52100DL2015PLC789012'),
  (20003, 'Sunrise Retail Pvt Ltd',      'https://sunriseretail.in','U51900KA2020PTC345678'),
  (20004, 'BlueSky Consulting Group',    'https://bluesky.com',    'U74140TN2012PLC901234')
ON CONFLICT (account_number) DO NOTHING;

END $$;
