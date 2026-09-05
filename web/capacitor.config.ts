import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.aistudio.mylibrary.rxzqpk',
  appName: 'Vidyara',
  webDir: 'dist',
  server: {
    androidScheme: 'https',
    hostname: 'vidyara.app'
  },
  plugins: {
    Camera: {
      permissions: ['camera']
    },
    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert']
    },
    StatusBar: {
      style: 'DARK',
      backgroundColor: '#0747A6'
    }
  }
};

export default config;
