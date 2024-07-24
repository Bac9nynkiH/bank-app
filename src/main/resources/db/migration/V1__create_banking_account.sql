CREATE TABLE bank_account (
                            id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                            balance DECIMAL(20,2),
                            account_number varchar(16) NOT NULL UNIQUE

);