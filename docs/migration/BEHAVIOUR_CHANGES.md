# Behaviour changes vs. the ZK app

The React + Spring Boot version keeps the ZK app's behaviour, except for the legacy bugs and gaps listed below.
Each entry names the legacy code it replaces. Events marked **Fixed** or **New** in [EVENTS.md](EVENTS.md) point here.

## What did not change

- **Database:** same PostgreSQL schema. No column was added, removed or altered. Hibernate runs with `ddl-auto=validate`, which stops startup if an entity no longer matches the schema.
- **Data rules:** soft deletes (`isdeleted`), status codes (0 Pending, 1 Approved, 2 Disapproved), reviewer and date stamping, and the four roles (User / Curator / Manager / Admin) all work as before.
- **Passwords:** stored in the legacy AES format by default, so the ZK app and the new app can share the database during the switch-over. Set `APP_AUTH_UPGRADE_HASH=true` once ZK is retired to re-hash passwords to bcrypt on login.
- **E-mail:** the subjects and texts are the same. Old links still work: `validate.zul?token=…`, `resetpassword.zul?token=…` and `validateEmail.zul?token=…` redirect to the new routes, keeping the token.
- **Valuation rows:** every valuation still creates its own `ecosystem`, `service`, `valuationmethod` and `value` rows, as `AddStudyController.recordSave` did.
- **Search access:** searching still requires a login (`SearchInit`).

## Security and accounts

| # | Legacy | Now |
|---|---|---|
| S1 | reCAPTCHA only enabled the submit button in the browser (`RegisterController`, `forgotPasswordController`). Contact Us never enforced it. | The server verifies the token on register, forgot password and contact (`CaptchaService`). |
| S2 | SMTP password, reCAPTCHA secret, Maps API key and the AES key were hard-coded in 5+ classes. | All come from environment variables (`.env.example`). |
| S3 | Forgot password sent the reset link to a hard-coded personal address, "from" the user's typed address (`forgotPasswordController:101`). | The link goes to the account's own e-mail, from the system address. |
| S4 | The reset token was stored in `user.validation`, which blocked login until the reset finished. | New reset tokens are prefixed `reset:` and do not block login. Legacy 70-letter tokens are still accepted for reset. |
| S5 | Login compared the e-mail case-sensitively, while registration lowercased it. | Login matches the e-mail case-insensitively. |
| S6 | Roles were only enforced by hiding menus. A Curator could open `dictionary.zul` by URL. The study-list header checkbox showed the approve and delete buttons to any role. | Every API endpoint checks the role (`@PreAuthorize`): dictionary writes and approval need Manager+, user management needs Admin. The UI guards match (`RequireRole`). |
| S7 | No CSRF protection. Session id kept on login. | CSRF cookie and header on every write. A new session is created at login. |
| S8 | `deleteDialog.zul`: the random code was shown but never checked. | Delete stays disabled until the code is retyped (`ConfirmDeleteDialog`). |
| S9 | Admins could demote themselves. | You cannot remove your own Admin role. |

## Search (`searchContent.zul`, `SearchController`)

| # | Legacy | Now |
|---|---|---|
| Q1 | "Match any" read only the first selected ecosystem. A missing brace made the realm branch `return` before checking for a match (`SearchController:599-611`). | Every selected ecosystem, service, method and country is checked. |
| Q2 | "Match all" `switch` fell through `(realm)` → `(biome)` → `(efg)`, so a realm code could match a biome code. | Each filter level is matched only against its own level. |
| Q3 | Studies were walked through `studysrc.getSites()`, which misses the 36 sites whose `studysrcid` is null. | Sites are taken from the study **and** from its datasets (`StudyMapper.liveSites`). |
| Q4 | Enter in the keyword box did nothing. "Clear Filters" had no handler. | Enter searches. Clear Filters resets every filter. |
| Q5 | Each search reloaded entire tables through eager reverse collections (for example, every vocabulary term loaded all its usages). | Reverse "usage" collections are lazy. A full search on the local data dropped from about 17 s to under 1 s. |

## Excel export (`btn_export` → `inputDownloadDialog.zul`)

| # | Legacy | Now |
|---|---|---|
| X1 | `V_fullSummary` used `studyid` as `@Id`, but the view has one row per value. Hibernate therefore returned the first row of each study repeated N times. | The view is read with JDBC. Every value row is exported (`ExportService`). |
| X2 | Column 46 (Value_Year) was never written, so from column 47 on every value sat under the wrong header. Coded/Reviewed columns were empty. The header read "Value_Type\tTemporary". | The columns line up with the headers. Value year, coded-by/date and reviewed-by/date are filled. The header reads "Value_Type". |
| X3 | The file was written to the server's working directory under the typed name (path risk, never cleaned up). | The file is streamed, and the name is sanitised. |

## Study data entry (`_study.zul`, `editstudy.zul`, `study_view.zul` and dialogs)

| # | Legacy | Now |
|---|---|---|
| D1 | Saving a study made ~10 separate service calls with no shared transaction, so a failure left a half-saved study. | One `@Transactional` `POST`/`PUT` saves the whole graph (`StudyService`). |
| D2 | A stray `;` at `AddStudyController:269` made "There is an empty valuation for a site." fire only for `null` lists. | A site with no live valuation is rejected (client and server). |
| D3 | An empty publication year crashed on `Integer.parseInt("")`. | An empty year is saved as 0, as the column requires. |
| D4 | Add wizard: deleting a site showed the code dialog but never removed the site. Deleting a valuation called a hard DB delete on an unsaved row. Editing a valuation opened an empty form whose result was ignored. | The site is removed from the list, the unsaved row is removed, and edit opens the valuation and keeps the changes. |
| D5 | Edit study: Cancel in "Add site" added a `null` row, which broke the grid. New sites added while editing were saved without `studysrcid`. | Cancel adds nothing. New sites are linked to the study. |
| D6 | Deleting a site in edit left its valuations live. | Deleting a site also soft-deletes its valuations. |
| D7 | Approve / Pending / Disapprove / Delete mapped checked rows by **index**. After sorting a column, the wrong studies were changed. | Selection is by study id. |
| D8 | Opening a study with a null status threw a NullPointerException. | A null status is treated as Pending. |
| D9 | Add Site Valuation: rows removed from the staged list were still returned. "Save To List" with nothing added threw a NullPointerException. Description lengths were not checked. | The staged list is what gets saved, Save needs at least one row, and every constraint is checked. |
| D10 | Edit Site Valuation threw a NullPointerException when the dataset had no valuation method. | Missing parts are created empty. |
| D11 | Degrees/minutes/seconds ignored the sign (−14° 30′ gave −13.5). | Negative degrees give a negative decimal (−14.5). |
| D12 | The "Valuation method" list included deleted terms in Add but not in Edit. Protected status, scale and site attribute lists also included deleted terms. | All lookups show only non-deleted terms. |
| D13 | `btnprev_confirm` ("Go Back" after saving) had no handler. | "Add another study" resets the wizard. A link goes to View/Edit. |

## Dictionary (`_dictionary.zul` and dialogs)

| # | Legacy | Now |
|---|---|---|
| T1 | `RealmController`: Save was bound to `onSelect`, so realms were never saved. | Saved. |
| T2 | Realm, Biome, EFG, E-service and E-sub-service duplicate checks compared against the textbox component, not its value, so duplicates were never caught. | Codes and names are checked against existing rows and the staged list. |
| T3 | PA Category edit asked for CV "PA Category", which does not exist, and crashed with IndexOutOfBounds. Delete never refreshed, and the list showed deleted terms. | "PA Category" is treated as PAC1/PAC2/PAC3. Edit, delete and refresh work, deleted terms are hidden, and the category is chosen on add. |
| T4 | `MethodController` never set the CV on a new valuation method. | The new term gets the "Valuation Method" CV. |
| T5 | Protected area `location` was never saved on add or edit. Edit had no validation. | Saved and validated. |
| T6 | Lists showed stale or wrong data after a dialog closed: <ul><li>country edit did not refresh;</li><li>PA edit and country "add year" reloaded deleted rows;</li><li>Beneficiary add did not reload;</li><li>Available-study search reloaded Currency;</li><li>service dialogs refreshed even on cancel.</li></ul> | Lists refresh after a save, show only non-deleted rows, and nothing reloads on cancel. |
| T7 | `btn_PA` had two `@Listen` handlers that both fired. | One search. |
| T8 | Searches for PA Category, Beneficiary Unit and Beneficiary Type, and "New" for PA Category, had no handler. | Implemented. |
| T9 | `AddServiceDialog`: the sub-service code was a readonly combobox with no options, so it was always empty. | Free text. |

## User management (`userMgtContent.zul`)

| # | Legacy | Now |
|---|---|---|
| U1 | The Manager button had no handler, the Admin handler was empty, and user search was not implemented. | All implemented. |
| U2 | Saving Email Config did a full page redirect inside the mail loop. | Saves, sends the invitations, and refreshes the tab. |
| U3 | The info icons next to each Email Config category did nothing. | They show a tooltip. |

## Not migrated (dead or demo code)

These are listed with a reason in [ZUL_MAPPING.md](ZUL_MAPPING.md) and [EVENTS.md](EVENTS.md):

- **ZK demos:** the order and survey wizards (`order.zul`, `survey.zul`, `WEB-INF/zul/**`) and `temp_result.zul`.
- **Unreferenced pages:** `home.zul`, `pageLayout.zul`, `sidebar.zul`, `timeout.zul`, `searchGoogleMap.zul`, `_googlehome.zul` and `emailSetting*` (replaced by the Email Config tab).
- **Orphan dialogs:** `AddWHO_RegionDialog.zul`, `AddWBGroupDialog.zul` and `AddCvDialog.zul` (their tabs are commented out).
- **Dead handlers:** listeners whose component does not exist (`#search_btn`, `#btn_Filter`, `#openPreview_btn`, `#detailPanel`), renderers or methods that are never called (`COPYStudySrcRenderer`, `CvTermItemRenderer`, `WHORowRenderer`, `StudySrcRenderer.addComponent`, `CountryItemRenderer.addBtnListCell`, `SearchController.getGridLayout`), and the static navbar links inside `_dictionary.zul`.
