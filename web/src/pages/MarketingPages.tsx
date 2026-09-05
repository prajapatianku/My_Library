import React from 'react';
import { BookOpen, ShieldCheck, CheckCircle2, QrCode, Smartphone, Users, ChevronRight, Zap } from 'lucide-react';

interface MarketingPagesProps {
  currentRoute: string;
  onNavigate: (route: string) => void;
  onLaunchOwnerPortal: () => void;
  onLaunchSuperAdmin: () => void;
}

export const MarketingPages: React.FC<MarketingPagesProps> = ({
  currentRoute,
  onNavigate,
  onLaunchOwnerPortal,
  onLaunchSuperAdmin
}) => {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#F8FAFC' }}>
      {/* Header / Navbar */}
      <header style={{
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid #E2E8F0',
        padding: '16px 24px',
        position: 'sticky',
        top: 0,
        zIndex: 100,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }} onClick={() => onNavigate('/')}>
          <div style={{
            width: '42px',
            height: '42px',
            borderRadius: '12px',
            background: 'linear-gradient(135deg, #4F378B 0%, #6750A4 50%, #7F67BE 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#FFFFFF',
            fontWeight: 'bold',
            fontSize: '20px',
            boxShadow: '0 4px 12px rgba(103, 80, 164, 0.25)'
          }}>
            <BookOpen size={24} />
          </div>
          <div>
            <h1 style={{ fontSize: '20px', fontWeight: 900, color: '#1C1B1F', letterSpacing: '-0.5px' }}>Vidyara</h1>
            <p style={{ fontSize: '10px', color: '#6750A4', fontWeight: 700 }}>LIBRARY & STUDY CENTER SAAS</p>
          </div>
        </div>

        <nav style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <button onClick={() => onNavigate('/')} style={{ background: 'none', border: 'none', fontWeight: currentRoute === '/' ? 700 : 500, color: currentRoute === '/' ? '#6750A4' : '#475569', cursor: 'pointer' }}>Home</button>
          <button onClick={() => onNavigate('/features')} style={{ background: 'none', border: 'none', fontWeight: currentRoute === '/features' ? 700 : 500, color: currentRoute === '/features' ? '#6750A4' : '#475569', cursor: 'pointer' }}>Features</button>
          <button onClick={() => onNavigate('/pricing')} style={{ background: 'none', border: 'none', fontWeight: currentRoute === '/pricing' ? 700 : 500, color: currentRoute === '/pricing' ? '#6750A4' : '#475569', cursor: 'pointer' }}>Pricing</button>
          <button onClick={() => onNavigate('/how-it-works')} style={{ background: 'none', border: 'none', fontWeight: currentRoute === '/how-it-works' ? 700 : 500, color: currentRoute === '/how-it-works' ? '#6750A4' : '#475569', cursor: 'pointer' }}>How it Works</button>
        </nav>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <button onClick={onLaunchSuperAdmin} style={{ padding: '8px 14px', borderRadius: '8px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', color: '#334155', fontWeight: 600, fontSize: '13px', cursor: 'pointer' }}>Super Admin</button>
          <button onClick={onLaunchOwnerPortal} style={{ padding: '8px 16px', borderRadius: '8px', border: 'none', backgroundColor: '#6750A4', color: '#FFFFFF', fontWeight: 700, fontSize: '13px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px', boxShadow: '0 4px 10px rgba(103,80,164,0.25)' }}>
            Login / Register <ChevronRight size={16} />
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <main style={{ flex: 1 }}>
        {currentRoute === '/' && (
          <section style={{ padding: '60px 24px', maxWidth: '1100px', margin: '0 auto', textAlign: 'center' }}>
            <span style={{ backgroundColor: '#E0E7FF', color: '#3730A3', padding: '6px 14px', borderRadius: '20px', fontSize: '12px', fontWeight: 700, display: 'inline-block', marginBottom: '16px' }}>
              🚀 VIDYARA v2.5 NOW LIVE ON VIDYARA.APP
            </span>
            <h1 style={{ fontSize: '44px', fontWeight: 900, color: '#0F172A', lineHeight: 1.2, marginBottom: '16px' }}>
              The Complete SaaS Platform for <span style={{ color: '#0747A6' }}>Library & Study Center Owners</span>
            </h1>
            <p style={{ fontSize: '18px', color: '#475569', maxWidth: '750px', margin: '0 auto 32px auto', lineHeight: 1.6 }}>
              Effortlessly manage seats, shift timings, student fees, live attendance QR scanning, WhatsApp payment reminders, and multi-branch operations — online or offline.
            </p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '16px', flexWrap: 'wrap' }}>
              <button onClick={onLaunchOwnerPortal} style={{ padding: '14px 28px', backgroundColor: '#0747A6', color: '#FFFFFF', fontWeight: 800, fontSize: '16px', borderRadius: '12px', border: 'none', cursor: 'pointer', boxShadow: '0 4px 14px rgba(7,71,166,0.3)' }}>
                Get Started Free
              </button>
              <button onClick={() => onNavigate('/pricing')} style={{ padding: '14px 28px', backgroundColor: '#FFFFFF', color: '#0F172A', fontWeight: 700, fontSize: '16px', borderRadius: '12px', border: '1px solid #CBD5E1', cursor: 'pointer' }}>
                View SaaS Pricing
              </button>
            </div>

            {/* Feature Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '24px', marginTop: '60px', textAlign: 'left' }}>
              <div style={{ backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 2px 8px rgba(0,0,0,0.04)' }}>
                <div style={{ width: '44px', height: '44px', borderRadius: '10px', backgroundColor: '#EFF6FF', color: '#0747A6', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
                  <BookOpen size={24} />
                </div>
                <h3 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '8px' }}>Visual Seat Layout</h3>
                <p style={{ fontSize: '14px', color: '#64748B', lineHeight: 1.5 }}>Graphical grid of available, occupied, and reserved seats across customizable Morning, Evening, and 24x7 shifts.</p>
              </div>

              <div style={{ backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 2px 8px rgba(0,0,0,0.04)' }}>
                <div style={{ width: '44px', height: '44px', borderRadius: '10px', backgroundColor: '#ECFDF5', color: '#059669', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
                  <QrCode size={24} />
                </div>
                <h3 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '8px' }}>Live Attendance & QR</h3>
                <p style={{ fontSize: '14px', color: '#64748B', lineHeight: 1.5 }}>Instant QR code identity scanner for student check-in, check-out, daily attendance registers, and shift tracking.</p>
              </div>

              <div style={{ backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 2px 8px rgba(0,0,0,0.04)' }}>
                <div style={{ width: '44px', height: '44px', borderRadius: '10px', backgroundColor: '#FFF7ED', color: '#EA580C', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
                  <Smartphone size={24} />
                </div>
                <h3 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '8px' }}>WhatsApp Reminders</h3>
                <p style={{ fontSize: '14px', color: '#64748B', lineHeight: 1.5 }}>One-click payment reminder dispatch via WhatsApp with pre-filled fee due messages and receipts.</p>
              </div>
            </div>
          </section>
        )}

        {currentRoute === '/pricing' && (
          <section style={{ padding: '60px 24px', maxWidth: '1000px', margin: '0 auto' }}>
            <div style={{ textAlign: 'center', marginBottom: '48px' }}>
              <h2 style={{ fontSize: '36px', fontWeight: 900, color: '#0F172A' }}>Vidyara SaaS Plans & Pricing</h2>
              <p style={{ fontSize: '16px', color: '#64748B', marginTop: '8px' }}>Transparent pricing designed for single libraries and growing multi-branch networks.</p>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '24px' }}>
              {/* FREE */}
              <div style={{ backgroundColor: '#FFFFFF', padding: '32px', borderRadius: '20px', border: '1px solid #E2E8F0', display: 'flex', flexDirection: 'column' }}>
                <h3 style={{ fontSize: '20px', fontWeight: 800, color: '#0F172A' }}>Vidyara Free</h3>
                <div style={{ fontSize: '36px', fontWeight: 900, color: '#0F172A', margin: '16px 0 8px 0' }}>₹0</div>
                <p style={{ fontSize: '13px', color: '#64748B', marginBottom: '24px' }}>No registration fee. Core library management.</p>
                <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '12px', flex: 1, marginBottom: '24px' }}>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> Up to 30 Students</li>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> Visual Seat Map</li>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> Attendance & QR</li>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> WhatsApp Reminders</li>
                </ul>
                <button onClick={onLaunchOwnerPortal} style={{ padding: '12px', borderRadius: '10px', backgroundColor: '#F1F5F9', color: '#0F172A', fontWeight: 700, border: 'none', cursor: 'pointer' }}>Get Started</button>
              </div>

              {/* PREMIUM */}
              <div style={{ backgroundColor: '#FFFFFF', padding: '32px', borderRadius: '20px', border: '2px solid #0747A6', display: 'flex', flexDirection: 'column', position: 'relative' }}>
                <span style={{ position: 'absolute', top: '-12px', right: '24px', backgroundColor: '#0747A6', color: '#FFFFFF', padding: '4px 12px', borderRadius: '12px', fontSize: '11px', fontWeight: 800 }}>POPULAR</span>
                <h3 style={{ fontSize: '20px', fontWeight: 800, color: '#0747A6' }}>Vidyara Pro</h3>
                <div style={{ fontSize: '36px', fontWeight: 900, color: '#0F172A', margin: '16px 0 4px 0' }}>₹99 <span style={{ fontSize: '14px', color: '#64748B', fontWeight: 500 }}>/month</span></div>
                <p style={{ fontSize: '12px', color: '#059669', fontWeight: 700, marginBottom: '24px' }}>OR ₹399 / 6 Months (Save 33%)</p>
                <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '12px', flex: 1, marginBottom: '24px' }}>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> Up to 500 Students</li>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> Detailed Revenue & CSV Exports</li>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> Custom WhatsApp Templates</li>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> Priority Support</li>
                </ul>
                <button onClick={onLaunchOwnerPortal} style={{ padding: '12px', borderRadius: '10px', backgroundColor: '#0747A6', color: '#FFFFFF', fontWeight: 700, border: 'none', cursor: 'pointer' }}>Upgrade to Pro</button>
              </div>

              {/* BUSINESS */}
              <div style={{ backgroundColor: '#FFFFFF', padding: '32px', borderRadius: '20px', border: '1px solid #E2E8F0', display: 'flex', flexDirection: 'column' }}>
                <h3 style={{ fontSize: '20px', fontWeight: 800, color: '#6D28D9' }}>Vidyara Business</h3>
                <div style={{ fontSize: '36px', fontWeight: 900, color: '#0F172A', margin: '16px 0 4px 0' }}>₹199 <span style={{ fontSize: '14px', color: '#64748B', fontWeight: 500 }}>/month</span></div>
                <p style={{ fontSize: '12px', color: '#059669', fontWeight: 700, marginBottom: '24px' }}>OR ₹999 / 6 Months (Save 16%)</p>
                <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '12px', flex: 1, marginBottom: '24px' }}>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> Up to 2,000 Students</li>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> Multi-Branch Support (Up to 3 Branches)</li>
                  <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#334155' }}><CheckCircle2 size={18} color="#059669" /> All Pro Features Included</li>
                </ul>
                <button onClick={onLaunchOwnerPortal} style={{ padding: '12px', borderRadius: '10px', backgroundColor: '#6D28D9', color: '#FFFFFF', fontWeight: 700, border: 'none', cursor: 'pointer' }}>Upgrade to Business</button>
              </div>
            </div>
          </section>
        )}

        {(currentRoute === '/features' || currentRoute === '/how-it-works') && (
          <section style={{ padding: '60px 24px', maxWidth: '900px', margin: '0 auto', textAlign: 'center' }}>
            <h2 style={{ fontSize: '32px', fontWeight: 900, marginBottom: '16px' }}>How Vidyara Works</h2>
            <p style={{ fontSize: '16px', color: '#64748B', marginBottom: '40px' }}>Designed to give library owners total control over seats, student renewals, and revenue.</p>
            <div style={{ display: 'grid', gap: '20px', textAlign: 'left' }}>
              <div style={{ padding: '20px', backgroundColor: '#FFFFFF', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
                <h4 style={{ fontSize: '16px', fontWeight: 800, color: '#0747A6' }}>1. Register your Library & Define Shifts</h4>
                <p style={{ fontSize: '14px', color: '#475569', marginTop: '6px' }}>Enter your library details, total seat capacity, and create custom shifts (Morning, Evening, 24x7).</p>
              </div>
              <div style={{ padding: '20px', backgroundColor: '#FFFFFF', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
                <h4 style={{ fontSize: '16px', fontWeight: 800, color: '#0747A6' }}>2. Assign Seats & Students</h4>
                <p style={{ fontSize: '14px', color: '#475569', marginTop: '6px' }}>Assign numeric seat numbers (1, 2, 3...) to students, set monthly subscription dates, and collect fees.</p>
              </div>
              <div style={{ padding: '20px', backgroundColor: '#FFFFFF', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
                <h4 style={{ fontSize: '16px', fontWeight: 800, color: '#0747A6' }}>3. Auto WhatsApp Reminders & Attendance</h4>
                <p style={{ fontSize: '14px', color: '#475569', marginTop: '6px' }}>Dispatch automatic payment reminders on WhatsApp and track daily student attendance with QR codes.</p>
              </div>
            </div>
          </section>
        )}
      </main>

      {/* Footer */}
      <footer style={{ backgroundColor: '#0F172A', color: '#94A3B8', padding: '32px 24px', textAlign: 'center', fontSize: '13px' }}>
        <p>© 2026 Vidyara Platform (vidyara.app). All rights reserved.</p>
      </footer>
    </div>
  );
};
