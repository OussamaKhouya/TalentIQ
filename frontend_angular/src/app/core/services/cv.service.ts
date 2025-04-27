import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BackendResponse } from '../models/backend-response.model';

@Injectable({
  providedIn: 'root'
})
export class CVService {
  // Hardcode the API URL for now instead of importing from environment
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  /**
   * Get the base API URL
   * @returns The base API URL
   */
  getApiUrl(): string {
    return this.apiUrl;
  }

  /**
   * Upload CV files and analyze them based on a job description
   * @param files CV files to upload (PDF or TXT)
   * @param jobDescription Job description to match against CVs
   * @returns Observable with analysis results
   */
  uploadAndAnalyze(files: File[], jobDescription: string): Observable<BackendResponse> {
    const formData = new FormData();

    // Add each file to the form data
    files.forEach((file, index) => {
      formData.append('cvFiles', file, file.name);
    });

    // Add the job description
    formData.append('jobOffer', jobDescription);

    // Use the apiV2/analyze endpoint which is expected to be available in the backend
    return this.http.post<BackendResponse>(`${this.apiUrl}/apiV2/analyze`, formData);
  }

  /**
   * Get analysis results for a specific batch ID
   * @param batchId The batch ID of the analysis
   * @returns Observable with analysis results
   */
  getAnalysisResults(batchId: string): Observable<BackendResponse> {
    return this.http.get<BackendResponse>(`${this.apiUrl}/apiV2/results/${batchId}`);
  }
}
