import { BackendCVInfo } from './backend-cv-info.model';

/**
 * Interface representing the response structure returned by the backend API
 */
export interface BackendResponse {
  candidates: BackendCVInfo[];
  llmInfo: {
    enabled: boolean;
    accessible: boolean;
    useFallback: boolean;
    provider: string;
  };
} 