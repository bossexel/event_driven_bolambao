# PostgreSQL Insert API with Supabase

Project: **event_driven_bolambao**  
Submission deadline: **October 21, 2026**

This lab creates a PostgreSQL customer table and uses Supabase's generated REST API to insert records from Postman.

**Live verification completed:** project `dijglmwapgewnqztbnis` passed all four requests and eight assertions using Newman, the Postman collection runner. See the [live test report](supabase-live-results.md). The table already exists in this project, so skip step 1 and import the collection with the configured `postman/supabase.local.postman_environment.json` environment to repeat the tests. That local file is excluded from Git.

## 1. Create your Supabase project and table

1. Sign in at [Supabase Dashboard](https://supabase.com/dashboard) and create a project named `event_driven_bolambao`. Choose your organization, database password, and region, then wait for provisioning.
2. Open **SQL Editor**, start a new query, paste [schema.sql](../supabase/schema.sql), and run it once. Use a new project or a database without an existing `public.customer` table; the script does not replace existing tables.
3. Open **Table Editor** and confirm `public.customer` has these five columns:

| Column | PostgreSQL type | Rule |
| --- | --- | --- |
| `customerid` | `integer` | Supplied by the caller; positive and unique primary key |
| `lastname` | `text` | Required and nonblank |
| `firstname` | `text` | Required and nonblank |
| `mobileno` | `text` | Required and nonblank; preserves leading zeros and `+` |
| `address` | `text` | Required and nonblank |

Supabase exposes table operations through its [generated Data API](https://supabase.com/docs/guides/api/creating-routes). Keep the Data API enabled and `public` included in its exposed schemas. This lab grants anonymous clients **INSERT only** and enables Row Level Security with an insert policy; it accepts dummy data without requiring a login. Use only fictional customer details for this lab. [Supabase access controls](https://supabase.com/docs/guides/api/securing-your-api)

## 2. Configure Postman

1. From your project's **Connect** dialog, copy the project URL and publishable key. Keys are also available under **Settings > API Keys**. Use a publishable key (`sb_publishable_...`), or a legacy **anon** key; secret and service-role keys are not used by this collection. [Supabase API keys](https://supabase.com/docs/guides/getting-started/api-keys)
2. Import both files into Postman:
   - [customer.postman_collection.json](../postman/customer.postman_collection.json)
   - [supabase.postman_environment.example.json](../postman/supabase.postman_environment.example.json)
3. Select the imported environment and fill in:

| Environment variable | Value |
| --- | --- |
| `supabase_url` | Your project URL, such as `https://YOUR_PROJECT_REF.supabase.co`, without a trailing slash |
| `supabase_key` | Your project's publishable or legacy anon key |

4. Open **01 - Insert customer**, then click **Send**. The pre-request script generates a positive customer ID, sets the JSON body, and logs the ID and generated JSON in the Postman Console. The Body tab uses `{{request_body}}`; view the resolved values in the Console. Run this request again whenever you want another customer.
5. Expect **201 Created** with an empty response body. The request uses `Prefer: return=minimal`, so it does not require read access. See [PostgREST inserts](https://docs.postgrest.org/en/stable/references/api/tables_views.html#insert).

The request is:

```http
POST {{supabase_url}}/rest/v1/customer
apikey: {{supabase_key}}
Content-Type: application/json
Prefer: return=minimal
```

Example JSON body (the collection generates its own ID):

```json
{
  "customerid": 1001,
  "lastname": "Bolambao",
  "firstname": "Juan",
  "mobileno": "09123456789",
  "address": "Cebu City, Philippines"
}
```

For legacy anon JWT keys, the collection also adds `Authorization: Bearer <anon-key>`. Publishable keys belong in `apikey`, not in a Bearer header. The collection sets authentication to **No Auth** and manages the headers itself.

## 3. Run the checks and verify the row

Run the collection in numbered order using Postman's Collection Runner, or send each request in that order. The first request creates the shared test ID used by the error checks.

| Request | Expected HTTP status | Expected outcome |
| --- | --- | --- |
| 01 - Insert customer | `201` | One row inserted; empty response body |
| 02 - Reject duplicate customerid | `409` | PostgreSQL error `23505` |
| 03 - Reject missing firstname | `400` | PostgreSQL error `23502` |
| 04 - Reject blank mobileno | `400` | PostgreSQL error `23514` |

Every request includes automated Postman assertions. Each collection run adds one valid customer; the other three requests must fail. Error codes and statuses follow [PostgREST's PostgreSQL error mapping](https://docs.postgrest.org/en/stable/references/errors.html).

To confirm persistence, run [verify.sql](../supabase/verify.sql) in Supabase SQL Editor, or refresh the customer table in Table Editor. Compare all five values, including the generated `customerid`, with the insert request. Anonymous `GET` requests are intentionally unavailable because this lab grants only INSERT.

If a request returns a permissions error (`42501`), confirm that all of `schema.sql` ran successfully and that Postman uses the public key from the same project. If the table is not found, check that the Data API is enabled, `public` is exposed, and the URL ends with `/rest/v1/customer`. If a manually entered ID returns `409`, choose an unused positive ID.

## 4. Prepare your submission

- Screenshot of the customer table definition with all five columns.
- Screenshot of the Postman insert URL, resolved JSON body in the Console, **201 Created**, and passing assertions. Hide the API key in screenshots.
- Screenshot of the inserted row in Supabase with the matching customer ID.
- Screenshot of Collection Runner results, including the three expected error cases.
- Include `supabase/schema.sql` and the Postman collection if source files are requested.

Capture these from your own hosted project after running the lab.
