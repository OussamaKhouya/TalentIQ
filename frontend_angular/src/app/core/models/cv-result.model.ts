export interface CVResult {
  candidateName: string;
  matchScore: number;
  summary: string;
  rating: number;
  strengths?: string[];
  weaknesses?: string[];
  suggestedQuestions?: string[];
  skills?: {
    name: string;
    score: number;
  }[];
  experiences?: {
    title: string;
    company?: string;
    duration?: string;
    description?: string;
    relevance?: number;
  }[];
  // Additional fields from backend
  email?: string;
  phone?: string;
  downloadId?: string;
  experience?: string;
  education?: string;
  languages?: string[];
} 