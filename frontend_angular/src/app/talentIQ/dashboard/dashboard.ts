import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CVService } from '../../core/services/cv.service';
import { CVResult } from '../../core/models/cv-result.model';
import { BackendCVInfo } from '../../core/models/backend-cv-info.model';

// Import components individually to avoid path issues
import { CvUploadFormComponent } from './components/cv-upload-form.component';
import { CvResultsListComponent } from './components/cv-results-list.component';
import { AiFeaturesComponent } from './components/ai-features.component';
import { CvDetailDialogComponent } from './components/cv-detail-dialog.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    CvUploadFormComponent,
    CvResultsListComponent,
    AiFeaturesComponent,
    CvDetailDialogComponent
  ],
  template: `
    <div class="container mx-auto p-4">
      <div class="grid grid-cols-12 gap-8">
        <div class="col-span-12 xl:col-span-10">
          <app-cv-upload-form
            (analyze)="onAnalyze($event)"
            #uploadForm
          ></app-cv-upload-form>

          <app-cv-results-list
            [results]="results"
            (viewDetails)="showDetails($event)"
            (downloadCV)="downloadCV($event)"
          ></app-cv-results-list>

          <app-ai-features></app-ai-features>
        </div>
      </div>
    </div>

    <app-cv-detail-dialog
      [visible]="displayDetailDialog"
      [candidate]="selectedCandidate"
      (closeDialog)="displayDetailDialog = false"
      (downloadCV)="downloadCV($event)"
    ></app-cv-detail-dialog>
  `
})
export class Dashboard {
  @ViewChild('uploadForm') uploadFormComponent!: CvUploadFormComponent;

  results: CVResult[] = [];

  // Dialog properties
  displayDetailDialog = false;
  selectedCandidate: CVResult | null = null;

  constructor(private cvService: CVService) {}

  showDetails(candidate: CVResult) {
    this.selectedCandidate = candidate;
    this.displayDetailDialog = true;
  }

  downloadCV(downloadId: string | undefined) {
    if (downloadId) {
      window.open(`${this.cvService.getApiUrl()}/apiV2/download/${downloadId}`, '_blank');
    }
  }

  onAnalyze(data: {files: File[], jobDescription: string}) {
    console.log(data.files[0]);
    console.log(data.jobDescription);

    this.results = []; // Clear previous results

    this.cvService.uploadAndAnalyze(data.files, data.jobDescription)
      .subscribe({
        next: (response) => {
          console.log("API Response:", response);

          // Add more detailed logging for debugging
          if (response && response.candidates && response.candidates.length > 0) {
            console.log("First candidate structure:", response.candidates[0]);
            console.log("Experience type:", typeof response.candidates[0].experience);
            console.log("Experience value:", response.candidates[0].experience);
          }

          // The API returns candidates in response.candidates, not response.results
          if (response && response.candidates) {
            try {
              // Map the candidates array to match our CVResult interface
              this.results = response.candidates.map((candidate: BackendCVInfo) => {
                // Helper function to handle data that might be string, array, or undefined
                const formatField = (field: string | string[] | undefined): string | undefined => {
                  if (!field) return undefined;
                  if (Array.isArray(field)) return field.join('\n');
                  return field.toString();
                };

                // Log each candidate's downloadId for debugging
                console.log(`Candidate ${candidate.name} - downloadId: ${candidate.downloadId || 'undefined'}`);

                return {
                  candidateName: candidate.name || 'Candidat',
                  matchScore: candidate.score || 0,
                  summary: candidate.matchExplanation || 'Aucune explication disponible',
                  rating: Math.ceil(candidate.score / 20) || 3, // Convert score to 1-5 rating
                  strengths: candidate.skills || [],
                  weaknesses: [],
                  suggestedQuestions: candidate.interviewQuestions || [],
                  // Add additional fields from the backend
                  email: candidate.email,
                  phone: candidate.phone,
                  downloadId: candidate.downloadId,
                  experience: formatField(candidate.experience),
                  education: formatField(candidate.education),
                  languages: candidate.languages
                };
              });

              console.log("Mapped results:", this.results);
            } catch (error) {
              console.error("Error mapping results:", error);
              this.mockResults();
            }
          } else {
            console.warn("No candidates in response, using mock data");
            this.mockResults();
          }
        },
        error: (error) => {
          console.error('Error uploading files:', error);
          alert('Une erreur est survenue lors du traitement des fichiers. Veuillez réessayer.');

          // Reset loading state when there's an error
          if (this.uploadFormComponent) {
            this.uploadFormComponent.resetForm();
          }

          // Mock data for demonstration
          this.mockResults();
        },
        complete: () => {
          // Reset the loading state in the form using the ViewChild reference
          if (this.uploadFormComponent) {
            this.uploadFormComponent.resetForm();
          }
        }
      });
  }

  // Mock method for demonstration purposes only
  private mockResults() {
    this.results = [
      {
        candidateName: 'Jean Dupont',
        email: 'jean.dupont@example.com',
        phone: '+33 6 12 34 56 78',
        matchScore: 95,
        summary: 'Profil idéal avec 5 ans d\'expérience en développement web et une expertise solide en Angular et Spring Boot.',
        rating: 5,
        strengths: ['Angular', 'Spring Boot', 'Java', 'TypeScript', 'RESTful APIs'],
        weaknesses: [],
        suggestedQuestions: [
          'Décrivez un projet complexe où vous avez utilisé Angular et Spring Boot ensemble.',
          'Comment gériez-vous les problèmes de performance dans vos applications?',
          'Quelle est votre approche pour assurer la qualité du code?'
        ],
        experience: 'Développeur Full Stack chez TechSolutions (2019-2023)\nDéveloppeur Front-end chez WebAgency (2017-2019)',
        education: 'Master en Informatique - Université de Paris (2017)',
        languages: ['Français (natif)', 'Anglais (courant)']
      },
      {
        candidateName: 'Marie Martin',
        email: 'marie.martin@example.com',
        phone: '+33 7 98 76 54 32',
        matchScore: 87,
        summary: 'Bonnes compétences techniques mais expérience limitée dans le secteur spécifique demandé.',
        rating: 4,
        strengths: ['React', 'Node.js', 'JavaScript', 'MongoDB'],
        weaknesses: [],
        suggestedQuestions: [
          'Comment pouvez-vous appliquer votre expérience React à des projets Angular?',
          'Parlez-moi de votre expérience avec les bases de données SQL.'
        ]
      },
      {
        candidateName: 'Ahmed Bennani',
        email: 'ahmed.bennani@example.com',
        phone: '+33 6 45 67 89 01',
        matchScore: 82,
        summary: 'Développeur full-stack avec une solide formation et des projets pertinents, mais manque d\'expérience professionnelle.',
        rating: 4,
        strengths: ['JavaScript', 'Vue.js', 'Spring', 'Docker'],
        weaknesses: [],
        suggestedQuestions: [
          'Comment compensez-vous votre manque d\'expérience professionnelle?',
          'Décrivez un projet personnel qui démontre vos capacités techniques.'
        ]
      },
      {
        candidateName: 'Sophie Lefebvre',
        email: 'sophie.lefebvre@example.com',
        phone: '+33 6 23 45 67 89',
        matchScore: 76,
        summary: 'Compétences techniques adéquates mais profil plus orienté vers le design que le développement.',
        rating: 3,
        strengths: ['UI/UX Design', 'CSS', 'HTML', 'Sketch', 'Figma'],
        weaknesses: [],
        suggestedQuestions: [
          'Comment collaborez-vous avec les développeurs dans vos projets?',
          'Quelle est votre expérience avec le développement front-end?'
        ]
      },
      {
        candidateName: 'Pierre Moreau',
        email: 'pierre.moreau@example.com',
        phone: '+33 7 12 34 56 78',
        matchScore: 72,
        summary: 'Expérience solide mais compétences techniques qui ne correspondent pas parfaitement au poste.',
        rating: 3,
        strengths: ['C#', '.NET', 'SQL Server', 'Azure'],
        weaknesses: [],
        suggestedQuestions: [
          'Quelle est votre expérience avec les frameworks JavaScript?',
          'Comment envisagez-vous votre transition vers les technologies demandées?'
        ]
      }
    ];
  }
}
