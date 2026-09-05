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

export async function fetchLibraryAccountById(accountId: string): Promise<LibraryAccountRecord | null> {
  try {
    const { data, error } = await supabase
      .from('library_accounts')
      .select('*')
      .eq('id', accountId)
      .single();

    if (error || !data) return null;
    return data;
  } catch (err) {
    console.error('Error fetching account by ID:', err);
    return null;
  }
}

export async function findAccountByPhoneOrEmail(input: string): Promise<LibraryAccountRecord | null> {
  try {
    const trimmed = input.trim();
    const { data, error } = await supabase
      .from('library_accounts')
      .select('*')
      .or(`phone.eq.${trimmed},email.ilike.${trimmed},id.eq.${trimmed}`);

    if (error || !data || data.length === 0) return null;
    return data[0];
  } catch (err) {
    console.error('Error searching account by phone/email:', err);
    return null;
  }
}

export function createDefaultAccountData(ownerName: string, phone: string, email: string, libraryName: string, city: string, totalSeats: number = 60) {
  const accountId = 'acc_' + (phone.replace(/\D/g, '') || Date.now().toString());
  return {
    accountId,
    ownerProfile: {
      fullName: ownerName,
      phone,
      email
    },
    library: {
      name: libraryName,
      address: `${city} Center`,
      city: city || 'Patna',
      totalSeats: totalSeats || 60
    },
    saasSubscription: {
      planType: 'FREE' as const,
      status: 'ACTIVE',
      validUntil: 'Lifetime'
    },
    branches: [
      { id: 'branch_01', name: `${libraryName} Main Branch` }
    ],
    activeBranchId: 'branch_01',
    studentsCount: 3,
    occupiedSeatsCount: 3,
    pendingDuesCount: 1,
    students: [
      { id: 'std_01', name: 'Aryan Patel', phone: '9876543210', seatNo: 1, shift: 'Full Day', feeAmount: 1200, dueStatus: 'PAID', joinDate: '2026-08-01' },
      { id: 'std_02', name: 'Priya Sharma', phone: '9876543211', seatNo: 2, shift: 'Morning', feeAmount: 800, dueStatus: 'PAID', joinDate: '2026-08-05' },
      { id: 'std_03', name: 'Rohan Kumar', phone: '9876543212', seatNo: 3, shift: 'Evening', feeAmount: 800, dueStatus: 'OVERDUE', joinDate: '2026-08-10' }
    ],
    paymentsHistory: [
      { id: 'pay_01', studentName: 'Aryan Patel', amount: 1200, date: '2026-08-01', mode: 'UPI', receiptNo: 'REC-1001' },
      { id: 'pay_02', studentName: 'Priya Sharma', amount: 800, date: '2026-08-05', mode: 'CASH', receiptNo: 'REC-1002' }
    ]
  };
}
