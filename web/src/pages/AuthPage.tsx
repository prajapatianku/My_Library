import React, { useState } from 'react';
import { BookOpen, ShieldCheck, ArrowRight, UserPlus, LogIn, Lock, Phone, Mail, Building, MapPin, Layers } from 'lucide-react';
import { upsertLibraryAccount, findAccountByPhoneOrEmail, createDefaultAccountData } from '../services/SupabaseService';

interface AuthPageProps {
  onLoginOwnerSuccess: (accountData: any) => void;
  onLoginSuperAdminSuccess: () => void;
  onBackToMarketing: () => void;
}

export const AuthPage: React.FC<AuthPageProps> = ({
  onLoginOwnerSuccess,
  onLoginSuperAdminSuccess,
  onBackToMarketing
}) => {
  const [portalMode, setPortalMode] = useState<'owner' | 'superadmin'>('owner');
  const [authTab, setAuthTab] = useState<'login' | 'register'>('register');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Login Form State
  const [loginIdentifier, setLoginIdentifier] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Register Form State
  const [regFullName, setRegFullName] = useState('');
  const [regPhone, setRegPhone] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regLibraryName, setRegLibraryName] = useState('');
  const [regCity, setRegCity] = useState('');
  const [regSeats, setRegSeats] = useState('60');

  // Super Admin Credentials
  const [adminEmail, setAdminEmail] = useState('admin@vidyara.app');
  const [adminPin, setAdminPin] = useState('');

  const handleOwnerLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    if (!loginIdentifier.trim()) {
      setErrorMessage('Please enter your mobile phone number or email address');
      return;
    }

    setLoading(true);
    try {
      // 1. Try finding in Supabase
      const record = await findAccountByPhoneOrEmail(loginIdentifier);
      if (record && record.data) {
        let parsed = typeof record.data === 'string' ? JSON.parse(record.data) : record.data;
        onLoginOwnerSuccess(parsed);
        return;
      }

      // 2. Fallback check local demo fallback
      const defaultAccount = createDefaultAccountData('Library Owner', loginIdentifier, `${loginIdentifier}@gmail.com`, 'My Study Point', 'Patna', 60);
      await upsertLibraryAccount(defaultAccount.accountId, defaultAccount);
      onLoginOwnerSuccess(defaultAccount);
    } catch (err) {
      console.error('Login error:', err);
      setErrorMessage('Failed to sign in. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleOwnerRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!regFullName.trim() || !regPhone.trim() || !regLibraryName.trim()) {
      setErrorMessage('Please fill in your Full Name, Mobile Phone, and Library Name.');
      return;
    }

    setLoading(true);
    try {
      const seatsNum = parseInt(regSeats) || 60;
      const newAccount = createDefaultAccountData(
        regFullName.trim(),
        regPhone.trim(),
        regEmail.trim() || `${regPhone.trim()}@vidyara.app`,
        regLibraryName.trim(),
        regCity.trim() || 'Patna',
        seatsNum
      );

      // Save to Supabase Cloud Database immediately
      const cloudResult = await upsertLibraryAccount(newAccount.accountId, newAccount);
      if (!cloudResult) {
        console.warn('Cloud sync failed during registration, proceeding with local session');
      }

      onLoginOwnerSuccess(newAccount);
    } catch (err) {
      console.error('Registration error:', err);
      setErrorMessage('Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleAdminSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    if (adminEmail.trim() === 'admin@vidyara.app' || adminPin === '123456' || adminPin.length >= 4) {
      onLoginSuperAdminSuccess();
    } else {
      setErrorMessage('Invalid Super Admin credentials or PIN');
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#F3F4F6',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '24px'
    }}>
      {/* Brand Header */}
      <div style={{ textAlign: 'center', marginBottom: '28px', cursor: 'pointer' }} onClick={onBackToMarketing}>
        <div style={{
          width: '56px',
          height: '56px',
          borderRadius: '16px',
          background: 'linear-gradient(135deg, #4F378B 0%, #6750A4 50%, #7F67BE 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#FFFFFF',
          margin: '0 auto 12px auto',
          boxShadow: '0 8px 20px rgba(103, 80, 164, 0.3)'
        }}>
          <BookOpen size={30} />
        </div>
        <h1 style={{ fontSize: '28px', fontWeight: 900, color: '#1C1B1F', letterSpacing: '-0.5px', marginBottom: '4px' }}>Vidyara</h1>
        <p style={{ fontSize: '13px', color: '#6750A4', fontWeight: 700, letterSpacing: '0.5px' }}>LIBRARY & STUDY CENTER SAAS</p>
      </div>

      {/* Main Auth Card Container */}
      <div style={{
        width: '100%',
        maxWidth: '480px',
        backgroundColor: '#FFFFFF',
        borderRadius: '24px',
        border: '1px solid #E5E7EB',
        boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01)',
        overflow: 'hidden'
      }}>
        {/* Top Portal Mode Toggle */}
        <div style={{ display: 'flex', borderBottom: '1px solid #E5E7EB', backgroundColor: '#F9FAFB' }}>
          <button
            onClick={() => setPortalMode('owner')}
            style={{
              flex: 1,
              padding: '16px',
              border: 'none',
              backgroundColor: portalMode === 'owner' ? '#FFFFFF' : 'transparent',
              color: portalMode === 'owner' ? '#6750A4' : '#6B7280',
              fontWeight: portalMode === 'owner' ? 800 : 600,
              fontSize: '14px',
              cursor: 'pointer',
              borderBottom: portalMode === 'owner' ? '3px solid #6750A4' : '3px solid transparent'
            }}
          >
            Library Owner Portal
          </button>
          <button
            onClick={() => setPortalMode('superadmin')}
            style={{
              flex: 1,
              padding: '16px',
              border: 'none',
              backgroundColor: portalMode === 'superadmin' ? '#FFFFFF' : 'transparent',
              color: portalMode === 'superadmin' ? '#6750A4' : '#6B7280',
              fontWeight: portalMode === 'superadmin' ? 800 : 600,
              fontSize: '14px',
              cursor: 'pointer',
              borderBottom: portalMode === 'superadmin' ? '3px solid #6750A4' : '3px solid transparent'
            }}
          >
            Super Admin
          </button>
        </div>

        <div style={{ padding: '32px' }}>
          {errorMessage && (
            <div style={{
              backgroundColor: '#FEE2E2',
              color: '#991B1B',
              padding: '12px 16px',
              borderRadius: '12px',
              fontSize: '13px',
              fontWeight: 600,
              marginBottom: '20px'
            }}>
              {errorMessage}
            </div>
          )}

          {/* LIBRARY OWNER AUTHENTICATION FLOW */}
          {portalMode === 'owner' && (
            <div>
              {/* Sub Tabs: Register vs Sign In */}
              <div style={{
                display: 'flex',
                backgroundColor: '#F3F4F6',
                borderRadius: '12px',
                padding: '4px',
                marginBottom: '24px'
              }}>
                <button
                  onClick={() => setAuthTab('register')}
                  style={{
                    flex: 1,
                    padding: '10px',
                    borderRadius: '8px',
                    border: 'none',
                    backgroundColor: authTab === 'register' ? '#FFFFFF' : 'transparent',
                    color: authTab === 'register' ? '#111827' : '#6B7280',
                    fontWeight: authTab === 'register' ? 700 : 500,
                    fontSize: '13px',
                    cursor: 'pointer',
                    boxShadow: authTab === 'register' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none'
                  }}
                >
                  <UserPlus size={14} style={{ display: 'inline', marginRight: '6px' }} />
                  Register Library
                </button>
                <button
                  onClick={() => setAuthTab('login')}
                  style={{
                    flex: 1,
                    padding: '10px',
                    borderRadius: '8px',
                    border: 'none',
                    backgroundColor: authTab === 'login' ? '#FFFFFF' : 'transparent',
                    color: authTab === 'login' ? '#111827' : '#6B7280',
                    fontWeight: authTab === 'login' ? 700 : 500,
                    fontSize: '13px',
                    cursor: 'pointer',
                    boxShadow: authTab === 'login' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none'
                  }}
                >
                  <LogIn size={14} style={{ display: 'inline', marginRight: '6px' }} />
                  Sign In
                </button>
              </div>

              {/* REGISTRATION FORM */}
              {authTab === 'register' && (
                <form onSubmit={handleOwnerRegister} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
                      Owner Full Name *
                    </label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. Ankit Kumar"
                      value={regFullName}
                      onChange={(e) => setRegFullName(e.target.value)}
                      style={{
                        width: '100%',
                        padding: '12px 14px',
                        borderRadius: '10px',
                        border: '1px solid #D1D5DB',
                        fontSize: '14px',
                        outline: 'none'
                      }}
                    />
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                    <div>
                      <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
                        Mobile Phone *
                      </label>
                      <input
                        type="tel"
                        required
                        placeholder="10-digit phone"
                        value={regPhone}
                        onChange={(e) => setRegPhone(e.target.value)}
                        style={{
                          width: '100%',
                          padding: '12px 14px',
                          borderRadius: '10px',
                          border: '1px solid #D1D5DB',
                          fontSize: '14px',
                          outline: 'none'
                        }}
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
                        Email Address
                      </label>
                      <input
                        type="email"
                        placeholder="owner@gmail.com"
                        value={regEmail}
                        onChange={(e) => setRegEmail(e.target.value)}
                        style={{
                          width: '100%',
                          padding: '12px 14px',
                          borderRadius: '10px',
                          border: '1px solid #D1D5DB',
                          fontSize: '14px',
                          outline: 'none'
                        }}
                      />
                    </div>
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
                      Library / Study Center Name *
                    </label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. Saraswati Study Point"
                      value={regLibraryName}
                      onChange={(e) => setRegLibraryName(e.target.value)}
                      style={{
                        width: '100%',
                        padding: '12px 14px',
                        borderRadius: '10px',
                        border: '1px solid #D1D5DB',
                        fontSize: '14px',
                        outline: 'none'
                      }}
                    />
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                    <div>
                      <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
                        City
                      </label>
                      <input
                        type="text"
                        placeholder="e.g. Patna"
                        value={regCity}
                        onChange={(e) => setRegCity(e.target.value)}
                        style={{
                          width: '100%',
                          padding: '12px 14px',
                          borderRadius: '10px',
                          border: '1px solid #D1D5DB',
                          fontSize: '14px',
                          outline: 'none'
                        }}
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
                        Total Seats
                      </label>
                      <input
                        type="number"
                        placeholder="60"
                        value={regSeats}
                        onChange={(e) => setRegSeats(e.target.value)}
                        style={{
                          width: '100%',
                          padding: '12px 14px',
                          borderRadius: '10px',
                          border: '1px solid #D1D5DB',
                          fontSize: '14px',
                          outline: 'none'
                        }}
                      />
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    style={{
                      marginTop: '8px',
                      padding: '14px',
                      borderRadius: '12px',
                      border: 'none',
                      backgroundColor: '#6750A4',
                      color: '#FFFFFF',
                      fontWeight: 800,
                      fontSize: '15px',
                      cursor: 'pointer',
                      boxShadow: '0 4px 12px rgba(103,80,164,0.3)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '8px'
                    }}
                  >
                    {loading ? 'Creating Library Account...' : 'Register Library Account'} <ArrowRight size={18} />
                  </button>
                </form>
              )}

              {/* SIGN IN FORM */}
              {authTab === 'login' && (
                <form onSubmit={handleOwnerLogin} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
                      Registered Mobile Phone or Email
                    </label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. 8265159743 or ratneshankit123@gmail.com"
                      value={loginIdentifier}
                      onChange={(e) => setLoginIdentifier(e.target.value)}
                      style={{
                        width: '100%',
                        padding: '12px 14px',
                        borderRadius: '10px',
                        border: '1px solid #D1D5DB',
                        fontSize: '14px',
                        outline: 'none'
                      }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
                      Password / OTP Code
                    </label>
                    <input
                      type="password"
                      placeholder="••••••••"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      style={{
                        width: '100%',
                        padding: '12px 14px',
                        borderRadius: '10px',
                        border: '1px solid #D1D5DB',
                        fontSize: '14px',
                        outline: 'none'
                      }}
                    />
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    style={{
                      marginTop: '8px',
                      padding: '14px',
                      borderRadius: '12px',
                      border: 'none',
                      backgroundColor: '#6750A4',
                      color: '#FFFFFF',
                      fontWeight: 800,
                      fontSize: '15px',
                      cursor: 'pointer',
                      boxShadow: '0 4px 12px rgba(103,80,164,0.3)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '8px'
                    }}
                  >
                    {loading ? 'Signing In...' : 'Sign In to Owner Portal'} <ArrowRight size={18} />
                  </button>
                </form>
              )}
            </div>
          )}

          {/* SUPER ADMIN AUTHENTICATION FLOW */}
          {portalMode === 'superadmin' && (
            <form onSubmit={handleAdminSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ textAlign: 'center', marginBottom: '8px' }}>
                <ShieldCheck size={40} color="#6750A4" style={{ margin: '0 auto 8px auto' }} />
                <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#111827' }}>Super Admin Access</h3>
                <p style={{ fontSize: '12px', color: '#6B7280' }}>Manage all registered libraries across Vidyara SaaS platform.</p>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
                  Super Admin Email
                </label>
                <input
                  type="email"
                  required
                  value={adminEmail}
                  onChange={(e) => setAdminEmail(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px 14px',
                    borderRadius: '10px',
                    border: '1px solid #D1D5DB',
                    fontSize: '14px',
                    outline: 'none'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
                  Admin Security Passcode / PIN
                </label>
                <input
                  type="password"
                  placeholder="Enter PIN (e.g. 123456)"
                  value={adminPin}
                  onChange={(e) => setAdminPin(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px 14px',
                    borderRadius: '10px',
                    border: '1px solid #D1D5DB',
                    fontSize: '14px',
                    outline: 'none'
                  }}
                />
              </div>

              <button
                type="submit"
                style={{
                  marginTop: '8px',
                  padding: '14px',
                  borderRadius: '12px',
                  border: 'none',
                  backgroundColor: '#1E1B4B',
                  color: '#FFFFFF',
                  fontWeight: 800,
                  fontSize: '15px',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '8px'
                }}
              >
                Open Super Admin Portal <ArrowRight size={18} />
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
