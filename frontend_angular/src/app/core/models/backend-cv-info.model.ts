/**
 * Interface representing the CV candidate info structure returned by the backend
 */
export interface BackendCVInfo {
  name: string;
  email?: string;
  phone?: string;
  downloadId?: string;
  score: number;
  matchExplanation?: string;
  skills?: string[];
  // These fields could be strings, arrays, or undefined based on the backend response
  experience?: string | string[];
  education?: string | string[];
  languages?: string[];
  interviewQuestions?: string[];
} 