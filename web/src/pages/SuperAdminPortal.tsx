import React, { useState, useEffect } from 'react';
import { ShieldCheck, BookOpen, Users, CreditCard, RefreshCw, Trash2, ArrowUpRight, Search, LogOut, ArrowLeft } from 'lucide-react';
import { fetchAllLibraryAccounts, upsertLibraryAccount, deleteLibraryAccountCloud, LibraryAccountRecord } from '../services/SupabaseService';

interface SuperAdminPortalProps {
  onLogout: () => void;
  onBackToMarketing: () => void;
}

export const SuperAdminPortal: React.FC<SuperAdminPortalProps> = ({ onLogout, onBackToMarketing }) => {
  const [accounts, setAccounts] = useState<LibraryAccountRecord[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>('');

  const loadData = async () => {
    setLoading(true);
    const data = await fetchAllLibraryAccounts();
    setAccounts(data);
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleUpgradePlan = async (acc: LibraryAccountRecord, targetPlan: string) => {
    let parsed: any = {};
    try {
      parsed = typeof acc.data === 'string' ? JSON.parse(acc.data) : acc.data;
    } catch (e) {
      parsed = {};
    }

    parsed.saasSubscription = {
      ...(parsed.saasSubscription || {}),
      planType: targetPlan,
      status: 'ACTIVE'
    };

    const success = await upsertLibraryAccount(acc.id, parsed);
    if (success) {
      loadData();
    } else {
      alert('Failed to update SaaS plan on Supabase cloud');
    }
  };

  const handleDeleteAccount = async (accId: string) => {
    if (!confirm('Are you sure you want to permanently delete this library account from Supabase database?')) return;
    const success = await deleteLibraryAccountCloud(accId);
    if (success) {
      loadData();
    } else {
      alert('Failed to delete library account from Supabase cloud');
    }
  };

  const filteredAccounts = accounts.filter(acc => {
    const q = searchQuery.toLowerCase();
    return (
      (acc.library_name || '').toLowerCase().includes(q) ||
      (acc.owner_name || '').toLowerCase().includes(q) ||
      (acc.phone || '').toLowerCase().includes(q) ||
      (acc.email || '').toLowerCase().includes(q) ||
      (acc.city || '').toLowerCase().includes(q)
    );
  });

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#F8FAFC' }}>
      {/* Super Admin Top Header */}
      <header style={{
        backgroundColor: '#1E1B4B',
        color: '#FFFFFF',
        padding: '16px 32px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 100
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{
            width: '42px',
            height: '42px',
            borderRadius: '12px',
            background: 'linear-gradient(135deg, #4F378B 0%, #6750A4 50%, #7F67BE 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#FFFFFF'
          }}>
            <ShieldCheck size={26} />
          </div>
          <div>
            <h1 style={{ fontSize: '20px', fontWeight: 900, letterSpacing: '-0.5px' }}>Vidyara Super Admin Control Center</h1>
            <p style={{ fontSize: '11px', color: '#A5B4FC', fontWeight: 700 }}>DATABASE OVERVIEW & SAAS TIERS</p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <button onClick={loadData} style={{ padding: '8px 14px', borderRadius: '8px', border: '1px solid #4338CA', backgroundColor: '#312E81', color: '#E0E7FF', cursor: 'pointer', fontSize: '13px', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '6px' }}>
            <RefreshCw size={14} className={loading ? 'spin' : ''} /> Refresh DB
          </button>
          <button onClick={onLogout} style={{ padding: '8px 16px', borderRadius: '8px', border: 'none', backgroundColor: '#EF4444', color: '#FFFFFF', cursor: 'pointer', fontSize: '13px', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '6px' }}>
            <LogOut size={16} /> Logout
          </button>
        </div>
      </header>

      {/* Main Workspace Layout */}
      <main style={{ flex: 1, padding: '32px', maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
        {/* Metrics Bar */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '32px' }}>
          <div style={{ backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0' }}>
            <p style={{ fontSize: '13px', color: '#64748B', fontWeight: 700 }}>Total Registered Libraries</p>
            <h3 style={{ fontSize: '36px', fontWeight: 900, color: '#6750A4', marginTop: '8px' }}>{accounts.length}</h3>
          </div>
          <div style={{ backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0' }}>
            <p style={{ fontSize: '13px', color: '#64748B', fontWeight: 700 }}>Business Plan Libraries</p>
            <h3 style={{ fontSize: '36px', fontWeight: 900, color: '#059669', marginTop: '8px' }}>
              {accounts.filter(a => (a.plan_type || '').toUpperCase() === 'BUSINESS').length}
            </h3>
          </div>
          <div style={{ backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0' }}>
            <p style={{ fontSize: '13px', color: '#64748B', fontWeight: 700 }}>Total Seats Managed</p>
            <h3 style={{ fontSize: '36px', fontWeight: 900, color: '#2563EB', marginTop: '8px' }}>
              {accounts.reduce((sum, a) => sum + (a.total_seats || 60), 0)}
            </h3>
          </div>
        </div>

        {/* Database Search Filter */}
        <div style={{ backgroundColor: '#FFFFFF', padding: '20px', borderRadius: '16px', border: '1px solid #E2E8F0', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Search size={20} color="#64748B" />
          <input
            type="text"
            placeholder="Search database by Library Name, Owner Name, Phone, Email, or City..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{ width: '100%', border: 'none', outline: 'none', fontSize: '15px', fontWeight: 500 }}
          />
        </div>

        {/* Live Accounts Database Table */}
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '20px', border: '1px solid #E2E8F0', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
            <thead>
              <tr style={{ backgroundColor: '#F8FAFC', borderBottom: '1px solid #E2E8F0', color: '#64748B', fontWeight: 700 }}>
                <th style={{ padding: '16px 20px' }}>Library Name & Location</th>
                <th style={{ padding: '16px 20px' }}>Owner Details</th>
                <th style={{ padding: '16px 20px' }}>Total Seats</th>
                <th style={{ padding: '16px 20px' }}>SaaS Plan</th>
                <th style={{ padding: '16px 20px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredAccounts.length === 0 ? (
                <tr>
                  <td colSpan={5} style={{ padding: '32px', textAlign: 'center', color: '#94A3B8', fontWeight: 600 }}>
                    {loading ? 'Fetching database accounts...' : 'No accounts found in database'}
                  </td>
                </tr>
              ) : (
                filteredAccounts.map((acc) => (
                  <tr key={acc.id} style={{ borderBottom: '1px solid #F1F5F9' }}>
                    <td style={{ padding: '16px 20px' }}>
                      <div style={{ fontWeight: 800, color: '#0F172A' }}>{acc.library_name || acc.id}</div>
                      <div style={{ fontSize: '12px', color: '#64748B' }}>{acc.city || 'Patna'}</div>
                    </td>
                    <td style={{ padding: '16px 20px' }}>
                      <div style={{ fontWeight: 700, color: '#334155' }}>{acc.owner_name || 'N/A'}</div>
                      <div style={{ fontSize: '12px', color: '#64748B' }}>📞 {acc.phone || 'N/A'} | ✉️ {acc.email || 'N/A'}</div>
                    </td>
                    <td style={{ padding: '16px 20px', fontWeight: 800, color: '#6750A4' }}>
                      {acc.total_seats || 60} Seats
                    </td>
                    <td style={{ padding: '16px 20px' }}>
                      <span style={{
                        padding: '4px 10px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 800,
                        backgroundColor: (acc.plan_type || '').toUpperCase() === 'BUSINESS' ? '#F3EDF7' : (acc.plan_type || '').toUpperCase() === 'PREMIUM' ? '#EFF6FF' : '#F1F5F9',
                        color: (acc.plan_type || '').toUpperCase() === 'BUSINESS' ? '#6750A4' : (acc.plan_type || '').toUpperCase() === 'PREMIUM' ? '#1D4ED8' : '#475569'
                      }}>
                        {acc.plan_type || 'FREE'}
                      </span>
                    </td>
                    <td style={{ padding: '16px 20px', display: 'flex', gap: '8px' }}>
                      <button
                        onClick={() => handleUpgradePlan(acc, 'BUSINESS')}
                        style={{ padding: '6px 12px', borderRadius: '8px', border: 'none', backgroundColor: '#6750A4', color: '#FFFFFF', fontWeight: 700, fontSize: '12px', cursor: 'pointer' }}
                      >
                        Set Business
                      </button>
                      <button
                        onClick={() => handleDeleteAccount(acc.id)}
                        style={{ padding: '6px 12px', borderRadius: '8px', border: 'none', backgroundColor: '#FEE2E2', color: '#991B1B', fontWeight: 700, fontSize: '12px', cursor: 'pointer' }}
                      >
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
};
