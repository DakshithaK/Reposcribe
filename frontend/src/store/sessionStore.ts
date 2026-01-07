import { create } from 'zustand';

interface SessionState {
  sessionId: string | null;
  uploadType: 'file' | 'git' | null;
  setSession: (sessionId: string, uploadType: 'file' | 'git') => void;
  clearSession: () => void;
}

export const useSessionStore = create<SessionState>((set) => ({
  sessionId: null,
  uploadType: null,
  setSession: (sessionId: string, uploadType: 'file' | 'git') => {
    set({ sessionId, uploadType });
  },
  clearSession: () => {
    set({ sessionId: null, uploadType: null });
  },
}));

