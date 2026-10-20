# Supabase customer insert: live test results

Project: **event_driven_bolambao**  
Executed: **October 21, 2026 at 07:09:51 (Asia/Manila)**  
Runner: **Newman (Postman collection runner)**

Endpoint:

```text
POST https://dijglmwapgewnqztbnis.supabase.co/rest/v1/customer
```

**Result: 4 requests completed, 8 assertions passed, 0 failures.**

The customer table was already present in the hosted project. This run tested the deployed API using the project publishable key and the repository Postman collection.

| Test | HTTP status | PostgreSQL code | Assertions |
| --- | --- | --- | --- |
| 01 - Insert customer | 201 | none | 2/2 passed |
| 02 - Reject duplicate customerid | 409 | 23505 | 2/2 passed |
| 03 - Reject missing firstname | 400 | 23502 | 2/2 passed |
| 04 - Reject blank mobileno | 400 | 23514 | 2/2 passed |

## Inserted customer

```json
{
  "customerid": 1168554867,
  "lastname": "Bolambao",
  "firstname": "Juan",
  "mobileno": "09123456789",
  "address": "Cebu City, Philippines"
}
```

The successful insert returned **201 Created** with an empty body because the request specified `Prefer: return=minimal`. Resubmitting the same customer immediately returned **409 Conflict**, PostgreSQL code **23505**, confirming that the primary key was retained in the database.

Anonymous reads are blocked: a separate request for `customer?select=customerid&limit=0` returned HTTP 401 with PostgreSQL code `42501` (permission denied). Use the Supabase dashboard to view the row directly.

## Reproduce the run

Import [the collection](../postman/customer.postman_collection.json) and the configured `postman/supabase.local.postman_environment.json` file into Postman. Select that environment and run the four requests in order. Each run adds one fictional customer with a new generated ID.

The configured environment is saved locally and excluded from Git. The API key has been omitted from this report.

Command-line equivalent:

```powershell
npx --yes --package=newman@6 newman run postman/customer.postman_collection.json --environment postman/supabase.local.postman_environment.json --timeout-request 20000
```

Newman runs exported Postman collections from the command line. [Postman documentation](https://learning.postman.com/docs/reference/newman-cli/installing-running-newman/)

The machine-readable [live results](supabase-live-results.json) contain the actual request bodies, response statuses, error responses, timings, and assertion outcomes from this run.
