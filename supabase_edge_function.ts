// Supabase Edge Function for handling new user registration
// This would go in supabase/functions/handle-new-user/index.ts

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  // Handle CORS preflight requests
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    // Create Supabase client with service role key
    const supabaseAdmin = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '',
      {
        auth: {
          autoRefreshToken: false,
          persistSession: false
        }
      }
    )

    const { record } = await req.json()
    
    // Get the REGISTERED role ID
    const { data: registeredRole, error: roleError } = await supabaseAdmin
      .from('roles')
      .select('id')
      .eq('name', 'REGISTERED')
      .single()

    if (roleError) {
      console.error('Error finding REGISTERED role:', roleError)
      throw roleError
    }

    // Assign REGISTERED role to new user
    const { error: assignError } = await supabaseAdmin
      .from('user_roles')
      .insert({
        user_id: record.id,
        role_id: registeredRole.id
      })

    if (assignError) {
      console.error('Error assigning role:', assignError)
      throw assignError
    }

    console.log(`Successfully assigned REGISTERED role to user ${record.id}`)

    return new Response(
      JSON.stringify({ success: true }),
      {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200,
      },
    )
  } catch (error) {
    console.error('Error in handle-new-user function:', error)
    return new Response(
      JSON.stringify({ error: error.message }),
      {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 400,
      },
    )
  }
})
