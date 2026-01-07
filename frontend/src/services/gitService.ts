import api from './api';

export interface GitCloneRequest {
  repositoryUrl: string;
  username?: string;
  password?: string;
}

export interface GitCloneResponse {
  success: boolean;
  message: string;
  sessionId?: string;
  repositoryUrl?: string;
}

export const gitService = {
  cloneRepository: async (
    repositoryUrl: string,
    username?: string,
    password?: string
  ): Promise<GitCloneResponse> => {
    const request: GitCloneRequest = {
      repositoryUrl,
      ...(username && password && { username, password }),
    };

    const response = await api.post<GitCloneResponse>('/git/clone', request);
    return response.data;
  },
};

