package com.aritra.d.riad.CoWork.model;

import jakarta.persistence.Entity;

/*
 * User.java
 * This class represents a user in the CoWork application.
 * It contains user-related information and behaviors.
 * -- Step 1: Create the custom ENUM types
-- This is necessary because ENUMs are distinct types in PostgreSQL.

CREATE TYPE user_role AS ENUM (
  'unregistered',
  'registered',
  'mentor',
  'moderator',
  'admin'
);

CREATE TYPE user_status AS ENUM (
  'active',
  'banned',
  'deleted'
);


-- Step 2: Create the 'users' table
-- This table references the ENUM types we just created.

CREATE TABLE public.users (
  -- 'id' is a UUID, which is great for unique, non-sequential identifiers.
  -- gen_random_uuid() is the modern Postgres function for generating UUIDs.
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  -- OAuth and Personal Information
  google_id VARCHAR(255),
  email VARCHAR(255) UNIQUE NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  profile_picture TEXT, -- TEXT is suitable for storing long URLs.

  -- Role and Status using our custom ENUM types
  -- We set a default role and status for new users.
  "role" user_role NOT NULL DEFAULT 'unregistered',
  status user_status NOT NULL DEFAULT 'active',

  -- Profile and Mentorship details
  location VARCHAR(255),
  bio TEXT,
  mentorship_eligible BOOLEAN DEFAULT FALSE,
  mentorship_rating DECIMAL(3, 2), -- e.g., 4.75

  -- Timestamps and Ban Information
  -- TIMESTAMPTZ (timestamp with time zone) is recommended for web applications.
  -- Supabase defaults to UTC.
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  ban_until TIMESTAMPTZ
);

-- Optional: Add comments to your table and columns for better documentation
COMMENT ON TABLE public.users IS 'Stores user profile information, authentication details, and roles.';
COMMENT ON COLUMN public.users.google_id IS 'Google OAuth ID for authentication.';
COMMENT ON COLUMN public.users.mentorship_rating IS 'Average rating from mentees, out of 5.00.';


-- Step 3: Create a trigger to automatically update the 'updated_at' column on change.
-- The DEFAULT NOW() only works on creation, not on updates.

-- 1. Create the function that will be called by the trigger.
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Create the trigger that executes the function before any update on the 'users' table.
CREATE TRIGGER on_users_update
  BEFORE UPDATE ON public.users
  FOR EACH ROW
  EXECUTE PROCEDURE public.handle_updated_at();
 */

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

enum UserRole {
    UNREGISTERED,
    REGISTERED,
    MENTOR,
    MODERATOR,
    ADMIN
}

enum UserStatus {
    ACTIVE,
    BANNED,
    DELETED
}

@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String googleId;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private UserRole role;
    private UserStatus status;
    private String location;
    private String bio;
    private boolean mentorshipEligible;
    private double mentorshipRating;
    private String createdAt;
    private String updatedAt;
    private String banUntil;
}