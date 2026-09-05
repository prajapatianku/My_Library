import React, { useState } from 'react';
import { MarketingPages } from './pages/MarketingPages';
import { OwnerPortal } from './pages/OwnerPortal';
import { SuperAdminPortal } from './pages/SuperAdminPortal';

export const App: React.FC = () => {
  const [view, setView] = useState<'marketing' | 'owner' | 'superadmin'>('marketing');
  const [marketingRoute, setMarketingRoute] = useState<string>('/');

  return (
    <div>
      {view === 'marketing' && (
        <MarketingPages
          currentRoute={marketingRoute}
          onNavigate={(route) => setMarketingRoute(route)}
          onLaunchOwnerPortal={() => setView('owner')}
          onLaunchSuperAdmin={() => setView('superadmin')}
        />
      )}

      {view === 'owner' && (
        <OwnerPortal onBackToMarketing={() => setView('marketing')} />
      )}

      {view === 'superadmin' && (
        <SuperAdminPortal onBackToMarketing={() => setView('marketing')} />
      )}
    </div>
  );
};

export default App;
