-- Supabase Database Trigger for Auto Role Assignment
-- Run this SQL in your Supabase SQL Editor

-- Step 1: Create a roles table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.roles (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Step 2: Create user_roles junction table
CREATE TABLE IF NOT EXISTS public.user_roles (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    role_id UUID REFERENCES public.roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    assigned_by UUID REFERENCES auth.users(id),
    UNIQUE(user_id, role_id)
);

-- Step 3: Insert default roles
INSERT INTO public.roles (name, description) VALUES
    ('UNREGISTERED', 'Users who have not completed registration'),
    ('REGISTERED', 'Basic registered users with standard access'),
    ('MENTOR', 'Users eligible to mentor others'),
    ('MODERATOR', 'Users with moderation privileges'),
    ('ADMIN', 'Users with full administrative access')
ON CONFLICT (name) DO NOTHING;

-- Step 4: Create function to handle new user registration
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    -- Insert REGISTERED role for new user
    INSERT INTO public.user_roles (user_id, role_id)
    SELECT 
        NEW.id,
        r.id
    FROM public.roles r
    WHERE r.name = 'REGISTERED';
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Step 5: Create trigger that fires when new user is created
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Step 6: Enable Row Level Security (RLS)
ALTER TABLE public.roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_roles ENABLE ROW LEVEL SECURITY;

-- Step 7: Create RLS policies for roles table
CREATE POLICY "Roles are viewable by everyone" ON public.roles
    FOR SELECT USING (true);

CREATE POLICY "Only admins can manage roles" ON public.roles
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM public.user_roles ur
            JOIN public.roles r ON ur.role_id = r.id
            WHERE ur.user_id = auth.uid() AND r.name = 'ADMIN'
        )
    );

-- Step 8: Create RLS policies for user_roles table
CREATE POLICY "Users can view their own roles" ON public.user_roles
    FOR SELECT USING (user_id = auth.uid());

CREATE POLICY "Users can view all user roles if they are admin/moderator" ON public.user_roles
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.user_roles ur
            JOIN public.roles r ON ur.role_id = r.id
            WHERE ur.user_id = auth.uid() AND r.name IN ('ADMIN', 'MODERATOR')
        )
    );

CREATE POLICY "Only admins can assign/remove roles" ON public.user_roles
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM public.user_roles ur
            JOIN public.roles r ON ur.role_id = r.id
            WHERE ur.user_id = auth.uid() AND r.name = 'ADMIN'
        )
    );

-- Step 9: Create helper functions for role management
CREATE OR REPLACE FUNCTION public.assign_user_role(target_user_id UUID, role_name TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    role_id UUID;
    current_user_is_admin BOOLEAN;
BEGIN
    -- Check if current user is admin
    SELECT EXISTS (
        SELECT 1 FROM public.user_roles ur
        JOIN public.roles r ON ur.role_id = r.id
        WHERE ur.user_id = auth.uid() AND r.name = 'ADMIN'
    ) INTO current_user_is_admin;
    
    IF NOT current_user_is_admin THEN
        RETURN FALSE;
    END IF;
    
    -- Get role ID
    SELECT id INTO role_id FROM public.roles WHERE name = role_name;
    
    IF role_id IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- Insert role assignment
    INSERT INTO public.user_roles (user_id, role_id, assigned_by)
    VALUES (target_user_id, role_id, auth.uid())
    ON CONFLICT (user_id, role_id) DO NOTHING;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.remove_user_role(target_user_id UUID, role_name TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    role_id UUID;
    current_user_is_admin BOOLEAN;
BEGIN
    -- Check if current user is admin
    SELECT EXISTS (
        SELECT 1 FROM public.user_roles ur
        JOIN public.roles r ON ur.role_id = r.id
        WHERE ur.user_id = auth.uid() AND r.name = 'ADMIN'
    ) INTO current_user_is_admin;
    
    IF NOT current_user_is_admin THEN
        RETURN FALSE;
    END IF;
    
    -- Get role ID
    SELECT id INTO role_id FROM public.roles WHERE name = role_name;
    
    IF role_id IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- Remove role assignment
    DELETE FROM public.user_roles 
    WHERE user_id = target_user_id AND role_id = role_id;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Step 10: Create view for easy role querying
CREATE OR REPLACE VIEW public.user_roles_view AS
SELECT 
    u.id as user_id,
    u.email,
    u.raw_user_meta_data->>'first_name' as first_name,
    u.raw_user_meta_data->>'last_name' as last_name,
    array_agg(r.name ORDER BY 
        CASE r.name 
            WHEN 'ADMIN' THEN 1
            WHEN 'MODERATOR' THEN 2
            WHEN 'MENTOR' THEN 3
            WHEN 'REGISTERED' THEN 4
            WHEN 'UNREGISTERED' THEN 5
            ELSE 6
        END
    ) as roles,
    (array_agg(r.name ORDER BY 
        CASE r.name 
            WHEN 'ADMIN' THEN 1
            WHEN 'MODERATOR' THEN 2
            WHEN 'MENTOR' THEN 3
            WHEN 'REGISTERED' THEN 4
            WHEN 'UNREGISTERED' THEN 5
            ELSE 6
        END
    ))[1] as primary_role
FROM auth.users u
LEFT JOIN public.user_roles ur ON u.id = ur.user_id
LEFT JOIN public.roles r ON ur.role_id = r.id
GROUP BY u.id, u.email, u.raw_user_meta_data;

-- Grant necessary permissions
GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT SELECT ON public.roles TO anon, authenticated;
GRANT SELECT ON public.user_roles TO authenticated;
GRANT SELECT ON public.user_roles_view TO authenticated;
