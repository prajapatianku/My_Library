import { createClient } from '@supabase/supabase-js';

export const SUPABASE_URL = 'https://ynqtzrkeburayuplqwek.supabase.co';
export const SUPABASE_PUBLISHABLE_KEY = 'sb_publishable_b9PQVAKGt1qLn-t5v9Bi1A_u9dVoTQD';
export const PRIMARY_DOMAIN = 'vidyara.app';

export const supabase = createClient(SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY);

export interface LibraryAccountRecord {
  id: string;
  owner_name?: string;
  phone?: string;
  email?: string;
  library_name?: string;
  city?: string;
  plan_type?: string;
  total_seats?: number;
  updated_at?: string;
  data: string;
}

export async function fetchAllLibraryAccounts(): Promise<LibraryAccountRecord[]> {
  try {
    const { data, error } = await supabase
      .from('library_accounts')
      .select('*')
      .order('updated_at', { ascending: false });

    if (error) {
      console.error('Supabase fetch error:', error);
      return [];
    }
    return data || [];
  } catch (err) {
    console.error('Network error fetching accounts:', err);
    return [];
  }
}

export async function upsertLibraryAccount(accountId: string, accountJson: any): Promise<boolean> {
  try {
    const dataStr = typeof accountJson === 'string' ? accountJson : JSON.stringify(accountJson);
    const parsed = typeof accountJson === 'object' ? accountJson : JSON.parse(accountJson);

    const payload: Partial<LibraryAccountRecord> = {
      id: accountId,
      data: dataStr,
      owner_name: parsed.ownerProfile?.fullName || '',
      phone: parsed.ownerProfile?.phone || '',
      email: parsed.ownerProfile?.email || '',
      library_name: parsed.library?.name || '',
      city: parsed.library?.city || '',
      plan_type: parsed.saasSubscription?.planType || 'FREE',
      total_seats: parsed.library?.totalSeats || 60,
      updated_at: new Date().toISOString()
    };

    const { error } = await supabase
      .from('library_accounts')
      .upsert(payload, { onConflict: 'id' });

    if (error) {
      console.error('Upsert account error:', error);
      return false;
    }
    return true;
  } catch (err) {
    console.error('Network error upserting account:', err);
    return false;
  }
}

export async function deleteLibraryAccountCloud(accountId: string): Promise<boolean> {
  try {
    const { error } = await supabase
      .from('library_accounts')
      .delete()
      .eq('id', accountId);

    if (error) {
      console.error('Delete account error:', error);
      return false;
    }
    return true;
  } catch (err) {
    console.error('Network error deleting account:', err);
    return false;
  }
}
