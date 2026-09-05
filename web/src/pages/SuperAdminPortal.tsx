import React, { useState, useEffect } from 'react';
import { ShieldAlert, Users, Radio, Tag, Download, ArrowLeft, Trash2, CheckCircle2, RefreshCw } from 'lucide-react';
import { fetchAllLibraryAccounts, deleteLibraryAccountCloud, LibraryAccountRecord } from '../services/SupabaseService';

interface SuperAdminPortalProps {
  onBackToMarketing: () => void;
}

export const SuperAdminPortal: React.FC<SuperAdminPortalProps> = ({ onBackToMarketing }) => {
  const [accounts, setAccounts] = useState<LibraryAccountRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedPlanFilter, setSelectedPlanFilter] = useState('ALL');

  const loadCloudAccounts = async () => {
    setLoading(true);
    const data = await fetchAllLibraryAccounts();
    setAccounts(data);
    setLoading(false);
  };

  useEffect(() => {
    loadCloudAccounts();
  }, []);

  const handleDeleteAccount = async (id: string, name: string) => {
    if (confirm(`Are you sure you want to permanently delete library "${name}" (${id}) from Supabase cloud database?`)) {
      const ok = await deleteLibraryAccountCloud(id);
      if (ok) {
        setAccounts(prev => prev.filter(a => a.id !== id));
      } else {
        alert('Failed to delete account from cloud.');
      }
    }
  };

  const filtered = accounts.filter(acc => {
    const term = searchTerm.toLowerCase();
    const matchesSearch = !term ||
      acc.id.toLowerCase().includes(term) ||
      (acc.owner_name && acc.owner_name.toLowerCase().includes(term)) ||
      (acc.library_name && acc.library_name.toLowerCase().includes(term)) ||
      (acc.phone && acc.phone.includes(term)) ||
      (acc.email && acc.email.toLowerCase().includes(term));

    const matchesPlan = selectedPlanFilter === 'ALL' || acc.plan_type === selectedPlanFilter;

    return matchesSearch && matchesPlan;
  });

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#F8FAFC' }}>
      {/* Top Admin Header */}
      <header style={{
        backgroundColor: '#0F172A',
        color: '#FFFFFF',
        padding: '16px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 100
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <button onClick={onBackToMarketing} style={{ padding: '6px 12px', border: '1px solid #334155', backgroundColor: '#1E293B', color: '#FFFFFF', borderRadius: '8px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '13px', fontWeight: 600 }}>
            <ArrowLeft size={16} /> Marketing
          </button>
          <div>
            <h2 style={{ fontSize: '18px', fontWeight: 900, color: '#F8FAFC' }}>👑 Vidyara Super Admin Platform</h2>
            <p style={{ fontSize: '11px', color: '#94A3B8' }}>Master Control Center & Live Cloud Operations</p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <button onClick={loadCloudAccounts} style={{ padding: '8px 14px', borderRadius: '8px', border: 'none', backgroundColor: '#2563EB', color: '#FFFFFF', fontWeight: 700, fontSize: '13px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <RefreshCw size={14} className={loading ? 'spin' : ''} /> Refresh Directory
          </button>
        </div>
      </header>

      {/* Admin Content Area */}
      <main style={{ flex: 1, padding: '32px', maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <h3 style={{ fontSize: '24px', fontWeight: 900, color: '#0F172A' }}>Registered Library Owners Directory</h3>
            <p style={{ fontSize: '14px', color: '#64748B' }}>{filtered.length} Unique Libraries Registered Across All Devices</p>
          </div>

          <div style={{ display: 'flex', gap: '12px' }}>
            <input
              type="text"
              placeholder="Search by owner, phone, email, library..."
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              style={{ padding: '10px 16px', borderRadius: '10px', border: '1px solid #CBD5E1', minWidth: '280px', fontSize: '14px' }}
            />
          </div>
        </div>

        {/* Directory Grid */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '60px', fontSize: '16px', color: '#64748B' }}>Loading cloud records from Supabase...</div>
        ) : filtered.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '60px', backgroundColor: '#FFFFFF', borderRadius: '16px', border: '1px solid #E2E8F0' }}>
            <p style={{ fontSize: '16px', color: '#64748B' }}>No registered library accounts found.</p>
          </div>
        ) : (
          <div style={{ display: 'grid', gap: '16px' }}>
            {filtered.map(acc => (
              <div key={acc.id} style={{ backgroundColor: '#FFFFFF', padding: '20px', borderRadius: '14px', border: '1px solid #E2E8F0', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
                <div>
                  <h4 style={{ fontSize: '18px', fontWeight: 800, color: '#0F172A' }}>{acc.library_name || 'Unnamed Library'}</h4>
                  <p style={{ fontSize: '13px', color: '#334155', fontWeight: 600, marginTop: '4px' }}>
                    👤 {acc.owner_name || 'Owner'} • 📞 {acc.phone || 'N/A'}
                  </p>
                  <p style={{ fontSize: '12px', color: '#0747A6', fontWeight: 600, marginTop: '2px' }}>
                    ✉️ {acc.email || 'No email registered'}
                  </p>
                  <p style={{ fontSize: '11px', color: '#94A3B8', marginTop: '6px' }}>
                    ID: {acc.id} | City: {acc.city || 'N/A'} | Updated: {acc.updated_at ? new Date(acc.updated_at).toLocaleString() : 'N/A'}
                  </p>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <span style={{
                    padding: '6px 14px',
                    borderRadius: '20px',
                    fontSize: '12px',
                    fontWeight: 800,
                    backgroundColor: acc.plan_type === 'BUSINESS' ? '#EDE9FE' : acc.plan_type === 'PREMIUM' ? '#FEF3C7' : '#F1F5F9',
                    color: acc.plan_type === 'BUSINESS' ? '#6D28D9' : acc.plan_type === 'PREMIUM' ? '#D97706' : '#475569'
                  }}>
                    {acc.plan_type || 'FREE'}
                  </span>

                  <button onClick={() => handleDeleteAccount(acc.id, acc.library_name || acc.id)} style={{ padding: '8px 12px', backgroundColor: '#FEF2F2', color: '#DC2626', borderRadius: '8px', border: '1px solid #FCA5A5', fontWeight: 700, fontSize: '12px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Trash2 size={14} /> Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};
