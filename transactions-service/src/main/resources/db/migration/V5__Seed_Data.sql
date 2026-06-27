-- ============================================================
-- GDB Transactions Seed Data
-- V5: Realistic transaction history for seeded accounts
-- Account numbers match accounts-service seed: 10001-10008, 20001-20004
-- ============================================================

INSERT INTO transaction_logging (account_number, amount, transaction_type, description, mode, status, created_at, updated_at)
VALUES
  -- Account 10001 (Amit Verma - GOLD Savings)
  (10001, 50000.00, 'DEPOSIT',  'Opening deposit',            'NEFT', 'SUCCESS', NOW() - INTERVAL '90 days', NOW() - INTERVAL '90 days'),
  (10001, 25000.00, 'DEPOSIT',  'Salary credit - June',       'NEFT', 'SUCCESS', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),
  (10001, 5000.00,  'WITHDRAW', 'ATM withdrawal',             'UPI',  'SUCCESS', NOW() - INTERVAL '55 days', NOW() - INTERVAL '55 days'),
  (10001, 12000.00, 'TRANSFER', 'Rent payment to landlord',   'IMPS', 'SUCCESS', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days'),
  (10001, 25000.00, 'DEPOSIT',  'Salary credit - July',       'NEFT', 'SUCCESS', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
  (10001, 3500.00,  'WITHDRAW', 'Grocery & utilities',        'UPI',  'SUCCESS', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
  (10001, 8000.00,  'TRANSFER', 'Insurance premium',          'NEFT', 'SUCCESS', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
  (10001, 25000.00, 'DEPOSIT',  'Salary credit - Aug',        'NEFT', 'SUCCESS', NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days'),

  -- Account 10002 (Neha Singh - SILVER Savings)
  (10002, 30000.00, 'DEPOSIT',  'Opening deposit',            'NEFT', 'SUCCESS', NOW() - INTERVAL '80 days', NOW() - INTERVAL '80 days'),
  (10002, 18000.00, 'DEPOSIT',  'Salary credit - June',       'NEFT', 'SUCCESS', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),
  (10002, 2000.00,  'WITHDRAW', 'ATM cash withdrawal',        'UPI',  'SUCCESS', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days'),
  (10002, 5000.00,  'TRANSFER', 'Mobile recharge & bills',    'UPI',  'SUCCESS', NOW() - INTERVAL '40 days', NOW() - INTERVAL '40 days'),
  (10002, 18000.00, 'DEPOSIT',  'Salary credit - July',       'NEFT', 'SUCCESS', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
  (10002, 1500.00,  'WITHDRAW', 'Petrol and maintenance',     'UPI',  'SUCCESS', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),

  -- Account 10003 (Priya Sharma - PREMIUM Savings / Admin)
  (10003, 200000.00,'DEPOSIT',  'Investment transfer in',     'RTGS', 'SUCCESS', NOW() - INTERVAL '120 days',NOW() - INTERVAL '120 days'),
  (10003, 50000.00, 'DEPOSIT',  'Salary credit - May',        'NEFT', 'SUCCESS', NOW() - INTERVAL '90 days', NOW() - INTERVAL '90 days'),
  (10003, 20000.00, 'TRANSFER', 'Mutual fund investment',     'NEFT', 'SUCCESS', NOW() - INTERVAL '75 days', NOW() - INTERVAL '75 days'),
  (10003, 50000.00, 'DEPOSIT',  'Salary credit - June',       'NEFT', 'SUCCESS', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),
  (10003, 35000.00, 'TRANSFER', 'Home loan EMI',              'NEFT', 'SUCCESS', NOW() - INTERVAL '55 days', NOW() - INTERVAL '55 days'),
  (10003, 50000.00, 'DEPOSIT',  'Salary credit - July',       'NEFT', 'SUCCESS', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
  (10003, 10000.00, 'WITHDRAW', 'Foreign travel expense',     'RTGS', 'SUCCESS', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),

  -- Account 10004 (Rajesh Kumar - GOLD Savings / Manager)
  (10004, 60000.00, 'DEPOSIT',  'Opening deposit',            'RTGS', 'SUCCESS', NOW() - INTERVAL '100 days',NOW() - INTERVAL '100 days'),
  (10004, 40000.00, 'DEPOSIT',  'Salary credit - June',       'NEFT', 'SUCCESS', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),
  (10004, 15000.00, 'TRANSFER', 'Children school fees',       'NEFT', 'SUCCESS', NOW() - INTERVAL '58 days', NOW() - INTERVAL '58 days'),
  (10004, 40000.00, 'DEPOSIT',  'Salary credit - July',       'NEFT', 'SUCCESS', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
  (10004, 8000.00,  'WITHDRAW', 'Shopping - festive season',  'UPI',  'SUCCESS', NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),

  -- Account 10006 (Arjun Mehta - PREMIUM Savings)
  (10006, 500000.00,'DEPOSIT',  'FD maturity credited',       'RTGS', 'SUCCESS', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days'),
  (10006, 75000.00, 'DEPOSIT',  'Business income',            'RTGS', 'SUCCESS', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
  (10006, 45000.00, 'TRANSFER', 'Investment - stock market',  'RTGS', 'SUCCESS', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'),
  (10006, 20000.00, 'WITHDRAW', 'Personal expense',           'UPI',  'SUCCESS', NOW() - INTERVAL '5 days',  NOW() - INTERVAL '5 days'),

  -- Account 20001 (TechNova - PREMIUM Current)
  (20001, 1000000.00,'DEPOSIT', 'Investor funding round',     'RTGS', 'SUCCESS', NOW() - INTERVAL '70 days', NOW() - INTERVAL '70 days'),
  (20001, 500000.00, 'DEPOSIT', 'Client payment - Q2',        'RTGS', 'SUCCESS', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days'),
  (20001, 200000.00, 'TRANSFER','Vendor payments - batch',    'NEFT', 'SUCCESS', NOW() - INTERVAL '40 days', NOW() - INTERVAL '40 days'),
  (20001, 150000.00, 'TRANSFER','Salary disbursal - staff',   'NEFT', 'SUCCESS', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
  (20001, 300000.00, 'DEPOSIT', 'Client payment - Q3',        'RTGS', 'SUCCESS', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),

  -- Account 20002 (GreenLeaf Exports - GOLD Current)
  (20002, 800000.00, 'DEPOSIT', 'Export proceeds credited',   'RTGS', 'SUCCESS', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),
  (20002, 250000.00, 'TRANSFER','Supplier payment',           'RTGS', 'SUCCESS', NOW() - INTERVAL '55 days', NOW() - INTERVAL '55 days'),
  (20002, 325000.00, 'DEPOSIT', 'Advance from buyer',         'RTGS', 'SUCCESS', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
  (20002, 200000.00, 'TRANSFER','Customs duty payment',       'NEFT', 'SUCCESS', NOW() - INTERVAL '8 days',  NOW() - INTERVAL '8 days')
ON CONFLICT DO NOTHING;

-- ── Fund Transfers (inter-account) ────────────────────────
INSERT INTO fund_transfers (from_account, to_account, transfer_amount, transfer_mode, created_at, updated_at)
VALUES
  (10001, 10002, 12000.00, 'IMPS', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days'),
  (10003, 10004, 35000.00, 'NEFT', NOW() - INTERVAL '55 days', NOW() - INTERVAL '55 days'),
  (10004, 10001, 8000.00,  'UPI',  NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
  (20001, 20002, 200000.00,'RTGS', NOW() - INTERVAL '40 days', NOW() - INTERVAL '40 days'),
  (10006, 10003, 45000.00, 'RTGS', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days')
ON CONFLICT DO NOTHING;
