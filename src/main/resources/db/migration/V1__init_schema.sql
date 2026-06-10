CREATE TABLE IF NOT EXISTS users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE forms (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    slug        VARCHAR(100) UNIQUE NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by  VARCHAR(255),
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE form_fields (
     id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     form_id      UUID NOT NULL REFERENCES forms(id) ON DELETE CASCADE,
     label        VARCHAR(255) NOT NULL,
     field_key    VARCHAR(100) NOT NULL,
     field_type   VARCHAR(30) NOT NULL,
     help_text    TEXT,
     placeholder  VARCHAR(255),
     default_value TEXT,
     field_order  INT NOT NULL DEFAULT 0,
     required     BOOLEAN NOT NULL DEFAULT FALSE,
     options      JSONB,
     validation   JSONB,
     UNIQUE (form_id, field_key)
);

CREATE TABLE form_submissions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    form_id      UUID NOT NULL REFERENCES forms(id),
    submitted_by VARCHAR(255),
    submitted_at TIMESTAMPTZ DEFAULT NOW(),
    data         JSONB NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_form_submissions_form_id ON form_submissions(form_id);
CREATE INDEX idx_form_submissions_data ON form_submissions USING GIN (data);