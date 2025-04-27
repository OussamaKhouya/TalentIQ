import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface LLMStatus {
  provider: string;
  enabled: boolean;
  accessible: boolean;
  fallbackMode: boolean;
  geminiApiKey: string;
  ollamaApiUrl: string;
  ollamaModel: string;
}

@Injectable({
  providedIn: 'root'
})
export class LLMService {
  // Hardcode the API URL for now instead of importing from environment
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  /**
   * Get LLM status information
   */
  getLLMStatus(): Observable<LLMStatus> {
    return this.http.get<LLMStatus>(`${this.apiUrl}/apiV2/llm/status`);
  }

  /**
   * Change LLM provider
   * @param provider LLM provider name ('gemini' or 'ollama')
   */
  changeLLMProvider(provider: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/apiV2/llm/config?provider=${provider}`);
  }

  /**
   * Test LLM with a sample prompt
   * @param provider LLM provider to test
   * @param prompt Test prompt
   */
  testLLM(provider: string, prompt: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/apiV2/llm/test?provider=${provider}`, { prompt });
  }
} 