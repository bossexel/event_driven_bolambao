-- event_driven_bolambao: PostgreSQL Insert API with Supabase
-- Run once in the SQL Editor of a new Supabase project.
begin;

create table public.customer (
    customerid integer primary key
        constraint customer_customerid_positive check (customerid > 0),
    lastname text not null
        constraint customer_lastname_required check (btrim(lastname) <> ''),
    firstname text not null
        constraint customer_firstname_required check (btrim(firstname) <> ''),
    mobileno text not null
        constraint customer_mobileno_required check (btrim(mobileno) <> ''),
    address text not null
        constraint customer_address_required check (btrim(address) <> '')
);

alter table public.customer enable row level security;

-- This lab accepts anonymous inserts of dummy customer data only.
-- API clients cannot read, edit, or delete customer records.
revoke all privileges on table public.customer from public, anon, authenticated;
grant usage on schema public to anon;
grant insert on table public.customer to anon;

create policy customer_lab_insert
    on public.customer
    for insert
    to anon
    with check (true);

-- Refresh the REST API's schema metadata after the transaction commits.
notify pgrst, 'reload schema';
commit;
