import React, { useState, useEffect } from 'react';
import { BookOpen, Users, LayoutGrid, QrCode, CreditCard, BarChart2, Settings, AlertTriangle, CheckCircle, RefreshCw, Plus, ArrowLeft } from 'lucide-react';
import { fetchAllLibraryAccounts, upsertLibraryAccount } from '../services/SupabaseService';
import { hasFeature, FeatureKeys } from '../services/EntitlementService';

interface OwnerPortalProps {
  onBackToMarketing: () => void;
}

export const OwnerPortal: React.FC<OwnerPortalProps> = ({ onBackToMarketing }) => {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'seats' | 'students' | 'attendance' | 'payments' | 'settings'>('dashboard');
  const [syncState, setSyncState] = useState<'synced' | 'pending' | 'error'>('synced');
  const [lastSyncedTime, setLastSyncedTime] = useState<string>('Just now');

  // Account State
  const [account, setAccount] = useState({
    accountId: 'owner_demo_01',
    ownerProfile: {
      fullName: 'Ratnesh Ankit',
      phone: '8265159743',
      email: 'ratneshankit123@gmail.com'
    },
    library: {
      name: 'Vidyara Study Point & Library',
      address: 'Main Road, Boring Road',
      city: 'Patna',
      totalSeats: 60
    },
    saasSubscription: {
      planType: 'BUSINESS' as const
    },
    branches: [
      { id: 'branch_01', name: 'Primary Campus (Patna)' },
      { id: 'branch_02', name: 'West Branch (Kankerbagh)' }
    ],
    activeBranchId: 'branch_01',
    studentsCount: 42,
    occupiedSeatsCount: 38,
    pendingDuesCount: 4
  });

  const handleManualSync = async () => {
    setSyncState('pending');
    const success = await upsertLibraryAccount(account.accountId, account);
    if (success) {
      setSyncState('synced');
      setLastSyncedTime(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
    } else {
      setSyncState('error');
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#F8FAFC' }}>
      {/* Top Header & Navigation Bar */}
      <header style={{
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid #E2E8F0',
        padding: '12px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 100
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <button onClick={onBackToMarketing} style={{ padding: '6px 12px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', borderRadius: '8px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '13px', fontWeight: 600 }}>
            <ArrowLeft size={16} /> Home
          </button>
          <div>
            <h2 style={{ fontSize: '18px', fontWeight: 800, color: '#0F172A' }}>{account.library.name}</h2>
            <p style={{ fontSize: '12px', color: '#64748B' }}>Owner: {account.ownerProfile.fullName} ({account.ownerProfile.phone})</p>
          </div>
        </div>

        {/* Sync Status Badge */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            padding: '6px 12px',
            borderRadius: '20px',
            fontSize: '12px',
            fontWeight: 700,
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            backgroundColor: syncState === 'synced' ? '#ECFDF5' : syncState === 'pending' ? '#FEF3C7' : '#FEF2F2',
            color: syncState === 'synced' ? '#059669' : syncState === 'pending' ? '#D97706' : '#DC2626'
          }}>
            {syncState === 'synced' ? <CheckCircle size={14} /> : <RefreshCw size={14} className="spin" />}
            {syncState === 'synced' ? `Synced (${lastSyncedTime})` : syncState === 'pending' ? 'Syncing...' : 'Sync Error (Offline Cache Active)'}
          </div>
          <button onClick={handleManualSync} style={{ padding: '6px 12px', borderRadius: '8px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer', fontSize: '12px', fontWeight: 600 }}>
            Sync Cloud
          </button>
        </div>
      </header>

      {/* Main Container Layout */}
      <div style={{ flex: 1, display: 'flex' }}>
        {/* Sidebar (Desktop UX) */}
        <aside style={{
          width: '240px',
          backgroundColor: '#FFFFFF',
          borderRight: '1px solid #E2E8F0',
          padding: '24px 16px',
          display: 'flex',
          flexDirection: 'column',
          gap: '8px'
        }}>
          <button onClick={() => setActiveTab('dashboard')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'dashboard' ? '#EFF6FF' : 'transparent', color: activeTab === 'dashboard' ? '#0747A6' : '#475569', fontWeight: activeTab === 'dashboard' ? 700 : 500, cursor: 'pointer', textAlign: 'left' }}>
            <BookOpen size={20} /> Dashboard
          </button>
          <button onClick={() => setActiveTab('seats')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'seats' ? '#EFF6FF' : 'transparent', color: activeTab === 'seats' ? '#0747A6' : '#475569', fontWeight: activeTab === 'seats' ? 700 : 500, cursor: 'pointer', textAlign: 'left' }}>
            <LayoutGrid size={20} /> Visual Seat Map
          </button>
          <button onClick={() => setActiveTab('students')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'students' ? '#EFF6FF' : 'transparent', color: activeTab === 'students' ? '#0747A6' : '#475569', fontWeight: activeTab === 'students' ? 700 : 500, cursor: 'pointer', textAlign: 'left' }}>
            <Users size={20} /> Students Directory
          </button>
          <button onClick={() => setActiveTab('attendance')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'attendance' ? '#EFF6FF' : 'transparent', color: activeTab === 'attendance' ? '#0747A6' : '#475569', fontWeight: activeTab === 'attendance' ? 700 : 500, cursor: 'pointer', textAlign: 'left' }}>
            <QrCode size={20} /> Live Attendance QR
          </button>
          <button onClick={() => setActiveTab('payments')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'payments' ? '#EFF6FF' : 'transparent', color: activeTab === 'payments' ? '#0747A6' : '#475569', fontWeight: activeTab === 'payments' ? 700 : 500, cursor: 'pointer', textAlign: 'left' }}>
            <CreditCard size={20} /> Dues & Payments
          </button>
          <button onClick={() => setActiveTab('settings')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'settings' ? '#EFF6FF' : 'transparent', color: activeTab === 'settings' ? '#0747A6' : '#475569', fontWeight: activeTab === 'settings' ? 700 : 500, cursor: 'pointer', textAlign: 'left' }}>
            <Settings size={20} /> Settings & Branches
          </button>
        </aside>

        {/* Dynamic Workspace Content */}
        <main style={{ flex: 1, padding: '32px', maxWidth: '1200px' }}>
          {activeTab === 'dashboard' && (
            <div>
              <h3 style={{ fontSize: '24px', fontWeight: 800, marginBottom: '24px' }}>Dashboard Overview</h3>
              {/* Metrics Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '32px' }}>
                <div style={{ backgroundColor: '#FFFFFF', padding: '20px', borderRadius: '14px', border: '1px solid #E2E8F0' }}>
                  <p style={{ fontSize: '13px', color: '#64748B', fontWeight: 600 }}>Active Students</p>
                  <h4 style={{ fontSize: '28px', fontWeight: 900, color: '#0747A6', marginTop: '8px' }}>{account.studentsCount}</h4>
                </div>
                <div style={{ backgroundColor: '#FFFFFF', padding: '20px', borderRadius: '14px', border: '1px solid #E2E8F0' }}>
                  <p style={{ fontSize: '13px', color: '#64748B', fontWeight: 600 }}>Occupied Seats</p>
                  <h4 style={{ fontSize: '28px', fontWeight: 900, color: '#059669', marginTop: '8px' }}>{account.occupiedSeatsCount} / {account.library.totalSeats}</h4>
                </div>
                <div style={{ backgroundColor: '#FFFFFF', padding: '20px', borderRadius: '14px', border: '1px solid #E2E8F0' }}>
                  <p style={{ fontSize: '13px', color: '#64748B', fontWeight: 600 }}>Pending Dues Alert</p>
                  <h4 style={{ fontSize: '28px', fontWeight: 900, color: '#EA580C', marginTop: '8px' }}>{account.pendingDuesCount} Students</h4>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'seats' && (
            <div>
              <h3 style={{ fontSize: '24px', fontWeight: 800, marginBottom: '16px' }}>Visual Seat Map ({account.library.totalSeats} Desks)</h3>
              <p style={{ fontSize: '14px', color: '#64748B', marginBottom: '24px' }}>Numeric seat grid (1, 2, 3...). Green = Available, Blue = Occupied.</p>
              {/* Seat Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(64px, 1fr))', gap: '12px', backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0' }}>
                {Array.from({ length: account.library.totalSeats }).map((_, idx) => {
                  const isOccupied = idx < account.occupiedSeatsCount;
                  return (
                    <div key={idx} style={{
                      height: '64px',
                      borderRadius: '10px',
                      backgroundColor: isOccupied ? '#EFF6FF' : '#ECFDF5',
                      border: `1.5px solid ${isOccupied ? '#3B82F6' : '#10B981'}`,
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 800,
                      fontSize: '14px',
                      color: isOccupied ? '#1D4ED8' : '#047857',
                      cursor: 'pointer'
                    }}>
                      <span>{idx + 1}</span>
                      <span style={{ fontSize: '9px', fontWeight: 600 }}>{isOccupied ? 'OCCUPIED' : 'FREE'}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {activeTab === 'students' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h3 style={{ fontSize: '24px', fontWeight: 800 }}>Student Directory ({account.studentsCount})</h3>
                <button style={{ padding: '10px 18px', backgroundColor: '#0747A6', color: '#FFFFFF', borderRadius: '10px', border: 'none', fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Plus size={16} /> Register Student
                </button>
              </div>
              <div style={{ backgroundColor: '#FFFFFF', borderRadius: '14px', border: '1px solid #E2E8F0', padding: '16px' }}>
                <p style={{ fontSize: '14px', color: '#64748B' }}>Search students by name, phone, or assigned seat number...</p>
              </div>
            </div>
          )}

          {activeTab === 'attendance' && (
            <div>
              <h3 style={{ fontSize: '24px', fontWeight: 800, marginBottom: '16px' }}>Live Attendance & QR Identity Scanner</h3>
              <div style={{ backgroundColor: '#FFFFFF', padding: '32px', borderRadius: '16px', border: '1px solid #E2E8F0', textAlign: 'center' }}>
                <QrCode size={64} color="#0747A6" style={{ margin: '0 auto 16px auto' }} />
                <h4 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '8px' }}>Scan Student QR Identity</h4>
                <p style={{ fontSize: '14px', color: '#64748B', maxWidth: '400px', margin: '0 auto 20px auto' }}>Point the camera at the student identity card to instantly mark check-in / check-out time.</p>
                <button style={{ padding: '12px 24px', backgroundColor: '#0747A6', color: '#FFFFFF', borderRadius: '10px', border: 'none', fontWeight: 700, cursor: 'pointer' }}>Start Scanner</button>
              </div>
            </div>
          )}

          {activeTab === 'payments' && (
            <div>
              <h3 style={{ fontSize: '24px', fontWeight: 800, marginBottom: '16px' }}>Dues & Student Payment History</h3>
              <p style={{ fontSize: '14px', color: '#64748B', marginBottom: '24px' }}>Domain A: Student fee collection & pre-filled WhatsApp payment reminders.</p>
            </div>
          )}

          {activeTab === 'settings' && (
            <div>
              <h3 style={{ fontSize: '24px', fontWeight: 800, marginBottom: '16px' }}>Library Settings & Multi-Branch Switcher</h3>
              <div style={{ backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0' }}>
                <h4 style={{ fontSize: '16px', fontWeight: 800, marginBottom: '12px' }}>Active SaaS Subscription</h4>
                <p style={{ fontSize: '14px', color: '#0747A6', fontWeight: 700 }}>{account.saasSubscription.planType} PLAN ACTIVE</p>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};
