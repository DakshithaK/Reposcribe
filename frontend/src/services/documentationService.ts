import api from './api';

export interface DocumentationResponse {
  success: boolean;
  documentation?: string;
  format?: string;
  message?: string;
}

export interface DocumentationProgress {
  status: string;
  progress: number;
  documentation?: string;
  error?: string;
}

export const documentationService = {
  generate: async (sessionId: string): Promise<DocumentationResponse> => {
    const response = await api.post<DocumentationResponse>('/documentation/generate', null, {
      params: { sessionId },
    });
    return response.data;
  },

  generateWithProgress: async (sessionId: string): Promise<DocumentationProgress> => {
    const response = await api.post<DocumentationProgress>(
      '/documentation/generate-with-progress',
      null,
      {
        params: { sessionId },
      }
    );
    return response.data;
  },

  download: async (sessionId: string, format: string = 'markdown'): Promise<Blob> => {
    const response = await api.get('/documentation/download', {
      params: { sessionId, format },
      responseType: 'blob',
    });
    return response.data;
  },
};

