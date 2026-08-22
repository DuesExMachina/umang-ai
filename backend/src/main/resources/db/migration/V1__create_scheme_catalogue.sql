create table scheme (
  id uuid primary key,
  code varchar(80) not null unique,
  title varchar(200) not null,
  summary text not null,
  category varchar(40) not null,
  synthetic_demo boolean not null,
  disclaimer text not null,
  status varchar(20) not null,
  catalogue_priority integer not null default 0,
  version varchar(30) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null,
  constraint ck_scheme_category check (category in ('EDUCATION','HEALTHCARE','EMPLOYMENT','AGRICULTURE','SENIOR_CITIZENS','FINANCIAL_ASSISTANCE')),
  constraint ck_scheme_status check (status in ('DRAFT','PUBLISHED','ARCHIVED')),
  constraint ck_synthetic_demo check (synthetic_demo = true)
);
create index idx_scheme_category_status on scheme(category, status);

create table eligibility_rule (
  id uuid primary key,
  scheme_id uuid not null references scheme(id) on delete cascade,
  rule_json jsonb not null,
  effective_from date not null,
  effective_to date,
  constraint ck_rule_dates check (effective_to is null or effective_to >= effective_from)
);
create index idx_eligibility_rule_scheme on eligibility_rule(scheme_id);

create table required_document (
  id uuid primary key,
  scheme_id uuid not null references scheme(id) on delete cascade,
  name varchar(200) not null,
  description text not null,
  requiredness varchar(20) not null,
  condition_json jsonb,
  display_order integer not null,
  constraint ck_document_requiredness check (requiredness in ('COMMON','CONDITIONAL')),
  constraint uq_document_order unique(scheme_id, display_order)
);

create table application_information (
  id uuid primary key,
  scheme_id uuid not null unique references scheme(id) on delete cascade,
  application_mode varchar(30) not null,
  instructions text not null,
  application_url text,
  contact_information text,
  constraint ck_application_mode check (application_mode in ('ONLINE','OFFLINE','ASSISTED','INFORMATION_ONLY')),
  constraint ck_synthetic_application_url check (application_url is null)
);

create table scheme_source_information (
  id uuid primary key,
  scheme_id uuid not null references scheme(id) on delete cascade,
  source_type varchar(30) not null,
  source_name varchar(200) not null,
  source_reference text not null,
  source_url text,
  verified_on date,
  constraint ck_source_type check (source_type in ('SYNTHETIC_DEMO','VERIFIED_OFFICIAL','CURATED_REFERENCE')),
  constraint ck_synthetic_source check (source_type = 'SYNTHETIC_DEMO' and source_url is null and verified_on is null)
);
create index idx_scheme_source_scheme on scheme_source_information(scheme_id);
