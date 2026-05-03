# Entity Relationship Diagram

## Entities

### USER
| Type | Column | Key |
|---|---|---|
| bigint | id | PK |
| string | name | |
| string | email | |
| string | password | |
| string | role | |
| datetime | created_at | |

---

### VEHICLE_CATEGORY
| Type | Column | Key |
|---|---|---|
| bigint | id | PK |
| string | name | |
| string | description | |

---

### VEHICLE
| Type | Column | Key |
|---|---|---|
| bigint | id | PK |
| bigint | category_id | FK |
| string | name | |
| string | type | |
| string | registration_number | |
| boolean | is_active | |
| string | description | |
| double | price_per_day | |
| datetime | created_at | |

---

### BOOKING
| Type | Column | Key |
|---|---|---|
| bigint | id | PK |
| bigint | user_id | FK |
| bigint | vehicle_id | FK |
| datetime | start_time | |
| datetime | end_time | |
| double | total_cost | |
| string | status | |
| datetime | created_at | |

---

## Relationships

| From | Relationship | To |
|---|---|---|
| VEHICLE_CATEGORY | classifies (one to many) | VEHICLE |
| USER | places (one to many) | BOOKING |
| VEHICLE | reserved in (one to many) | BOOKING |