-- Local PostgreSQL substitute for the referenced authentication user IDs.
-- This table does not implement an authentication service.
CREATE SCHEMA IF NOT EXISTS auth;
CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid()
);

CREATE TABLE IF NOT EXISTS public.roles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nazwa TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.app_users (
    id UUID PRIMARY KEY REFERENCES auth.users(id),
    imie TEXT,
    nazwisko TEXT,
    id_rola BIGINT REFERENCES public.roles(id),
    email TEXT NOT NULL,
    haslo TEXT
);
COMMENT ON COLUMN public.app_users.haslo IS 'Wyłącznie skrót hasła, nigdy hasło jawne.';

CREATE TABLE IF NOT EXISTS public.statuses (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nazwa TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_owner UUID NOT NULL REFERENCES public.app_users(id),
    id_users UUID REFERENCES public.app_users(id),
    data_utw TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    data_odd TIMESTAMP WITH TIME ZONE,
    tytul TEXT NOT NULL,
    opis TEXT,
    obrazek TEXT
);

CREATE TABLE IF NOT EXISTS public.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_status BIGINT REFERENCES public.statuses(id),
    id_projekt UUID NOT NULL REFERENCES public.projects(id),
    id_users UUID REFERENCES public.app_users(id),
    nazwa TEXT NOT NULL,
    opis TEXT,
    data_utw TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    data_odd TIMESTAMP WITH TIME ZONE,
    priorytet TEXT
);

CREATE TABLE IF NOT EXISTS public.kanban (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_task UUID NOT NULL REFERENCES public.tasks(id),
    id_status BIGINT REFERENCES public.statuses(id)
);

INSERT INTO public.roles (nazwa) VALUES ('Administrator'), ('Użytkownik')
ON CONFLICT (nazwa) DO NOTHING;
INSERT INTO public.statuses (nazwa) VALUES ('Do zrobienia'), ('W trakcie'), ('Zrobione')
ON CONFLICT (nazwa) DO NOTHING;

-- Owner for the current project form, which does not yet have working login.
INSERT INTO auth.users (id) VALUES ('00000000-0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;
INSERT INTO public.app_users (id, imie, nazwisko, id_rola, email)
SELECT '00000000-0000-0000-0000-000000000001', 'Użytkownik', 'Lokalny', id, 'local@example.invalid'
FROM public.roles WHERE nazwa = 'Użytkownik'
ON CONFLICT (id) DO NOTHING;
