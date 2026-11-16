# REST API

All endpoints are under `/api` and use JSON.

- **Session:** the API uses a session cookie (`JSESSIONID`).
- **CSRF:** every write must send the `XSRF-TOKEN` cookie value back in the `X-XSRF-TOKEN` header. `frontend/src/api/client.ts` does this.
- **Errors:** errors come back as `{status, message, fields}`. `fields` holds per-field validation messages.

Roles are cumulative: an Admin also has Manager, Curator and User rights.

| Method | Path | Role | Replaces |
|---|---|---|---|
| GET | `/api/public/config` | public | reCAPTCHA site key / Maps key `<script>` tags |
| GET | `/api/auth/me` | public | `userCredential` session attribute, keep-alive timer |
| POST | `/api/auth/login` | public | `LoginController` / `AuthenticationService` |
| POST | `/api/auth/logout` | public | `BannerController` logout |
| POST | `/api/auth/register` | public | `RegisterController` |
| GET | `/api/auth/verify?token=` | public | `ValidateController` |
| POST | `/api/auth/forgot` | public | `forgotPasswordController` |
| GET, POST | `/api/auth/reset` | public | `ResetPasswordController` |
| GET | `/api/auth/invitation?token=` | public | `ValidateEmailController` |
| POST | `/api/contact` | public | `SendMailController` |
| GET | `/api/search/filters` | User | `SearchController.doAfterCompose` (chosenbox models) |
| POST | `/api/search` | User | `search2_btn` / `getFilterByKeyword*` |
| POST | `/api/search/summary` | User | `ShowSummaryController` |
| POST | `/api/search/map` | User | `btn_gmap` / `createPanel` |
| GET | `/api/search/studies/{id}` | User | Study detail tab (`StudySrcRenderer`) |
| POST | `/api/search/export` | User | Export dialogs + POI workbook |
| GET | `/api/lookups` | User | `AppContext` CV combobox helpers, lookup services |
| GET | `/api/studies?status=&q=` | Curator | `ViewStudyController` list and search |
| GET | `/api/studies/{id}` | Curator | `EditStudyController` init |
| POST | `/api/studies` | Curator | `AddStudyController.recordSave` |
| PUT | `/api/studies/{id}` | Curator (status change: Manager) | `EditStudyController.saveRecord` |
| POST | `/api/studies/status` | Manager | `btn_approve` / `btn_pending` / `btn_disapprove` |
| POST | `/api/studies/delete` | Manager | `btn_delete` |
| GET | `/api/dictionary/terms?cv=` | Curator | CV tabs (`CvTermRowRenderer`) |
| POST, PUT, DELETE | `/api/dictionary/terms[/{id}]` | Manager | `Currency`/`Beneficial`/`BeneficiaryType`/`Method`/`AvailableStudyInfo`/`WHO` controllers |
| GET, POST, PUT, DELETE | `/api/dictionary/countries[/{id}]` | read Curator, write Manager | `CountryController`, `CountryItemRenderer` |
| POST | `/api/dictionary/countries/{id}/infos` | Manager | `CountryInfoController` |
| PUT, DELETE | `/api/dictionary/country-infos/{id}` | Manager | `EditCountryInfoController`, `CountryInfoRenderer` |
| GET, POST | `/api/dictionary/protected-areas` | Curator | `PAController` (also used from the Add Site dialog) |
| PUT, DELETE | `/api/dictionary/protected-areas/{id}` | Manager | `PAController`, `PaItemRenderer` |
| GET, POST | `/api/dictionary/climate-types` | read Curator, write Manager | `ClimateController` |
| GET | `/api/dictionary/realms` | Curator | Ecosystem tree |
| POST | `/api/dictionary/realms`, `/biomes`, `/efgs`, `/ecosystems` | Manager | `Realm`/`Biome`/`Efg`/`Ecosystem` controllers |
| GET | `/api/dictionary/eservices` | Curator | Service tree |
| POST | `/api/dictionary/eservices`, `/esubservices`, `/services` | Manager | `Eservice`/`EsubService`/`Service` controllers |
| GET | `/api/users?role=` | Admin | `UserMgtController` grid |
| POST | `/api/users/role` | Admin | `setUserLevel` |
| GET, PUT | `/api/users/email-config` | Admin | Email Config tab |
| GET | `/actuator/health` | public | Docker health check |
