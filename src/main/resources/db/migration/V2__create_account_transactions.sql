START TRANSACTION;
SET lock_timeout=3000;

CREATE TABLE bank_transaction (
                              id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                              amount DECIMAL(20,2),
                              timestamp BIGINT,
                              last_name VARCHAR(255),
                              flow varchar(255),
                              bank_account_id UUID REFERENCES bank_account(id)
);

CREATE TABLE  deposit_transaction (
   id UUID DEFAULT gen_random_uuid() PRIMARY KEY REFERENCES bank_transaction(id)
);

CREATE TABLE withdraw_transaction (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY REFERENCES bank_transaction(id)
);

CREATE TABLE transfer_transaction (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY REFERENCES bank_transaction(id),
    visavis_id UUID REFERENCES bank_account(id)
);

COMMIT;

