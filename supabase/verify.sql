-- Run in Supabase SQL Editor after sending the Postman insert request.
-- Compare customerid with the value shown in the Postman request/console.
select customerid, lastname, firstname, mobileno, address
from public.customer
order by customerid desc;
