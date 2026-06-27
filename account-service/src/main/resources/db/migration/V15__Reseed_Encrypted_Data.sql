-- ============================================================
-- GDB Accounts Seed Data Update
-- V15: Update savings details with AES-encrypted phone_no & aadhar_number
-- ============================================================

-- Account 10001 (Amit Verma)
UPDATE savings_account_details SET phone_no = '1/AkY2T4Qrw5Z2i2IrG1ng==', aadhar_number = 'M47rp0nW5FXXpsH53JZEzg==' WHERE account_number = 10001;

-- Account 10002 (Neha Singh)
UPDATE savings_account_details SET phone_no = 'VinF01EgQhcmm7zCfBV1cg==', aadhar_number = 'vJ7XO4uNH4YOzpj+4xuf9w==' WHERE account_number = 10002;

-- Account 10003 (Priya Sharma)
UPDATE savings_account_details SET phone_no = 'aF1EqRNBcIG76KLp4syYbQ==', aadhar_number = 'fQYK/XdfxDJwfLzbHVMcyQ==' WHERE account_number = 10003;

-- Account 10004 (Rajesh Kumar)
UPDATE savings_account_details SET phone_no = 'M7JdSfJRbD+sGrAuds6zUg==', aadhar_number = 'zwuU38aeTlIOpFo4oqwVOg==' WHERE account_number = 10004;

-- Account 10005 (Sunita Agarwal)
UPDATE savings_account_details SET phone_no = 'YIyrs4x0T3hfjxKyNfmThA==', aadhar_number = 'unRlGfDOCk4mZupY+AMmQA==' WHERE account_number = 10005;

-- Account 10006 (Arjun Mehta)
UPDATE savings_account_details SET phone_no = 'Nn3Qwys+1VyuJ8xzOalMgA==', aadhar_number = '8z81rZBMo0DtyVrOwS0uQw==' WHERE account_number = 10006;

-- Account 10007 (Divya Kapoor)
UPDATE savings_account_details SET phone_no = 'GhrO4BjGqhcE0SqyBiBqnQ==', aadhar_number = '2GKtPMoCQWaOfP19qL4Zdw==' WHERE account_number = 10007;

-- Account 10008 (Ravi Nair)
UPDATE savings_account_details SET phone_no = 'D8Cxx824bwZMw7xPaGaTVA==', aadhar_number = 'W9D5Xk/srDqA4/K8+HEwOg==' WHERE account_number = 10008;

-- Account 1001 (John Doe)
UPDATE savings_account_details SET phone_no = '1/AkY2T4Qrw5Z2i2IrG1ng==', aadhar_number = 'NMbqeC3r3SWwBSI710vjyw==' WHERE account_number = 1001;

-- Account 1003 (Teller User)
UPDATE savings_account_details SET phone_no = 'VinF01EgQhcmm7zCfBV1cg==', aadhar_number = 'idXddxKi3DzWiq8NzsYNXg==' WHERE account_number = 1003;
