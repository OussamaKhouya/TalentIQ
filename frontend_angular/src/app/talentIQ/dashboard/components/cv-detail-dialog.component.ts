import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { CVResult } from '../../../core/models/cv-result.model';

@Component({
  selector: 'app-cv-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    DialogModule,
    ButtonModule,
    TagModule,
    TooltipModule
  ],
  template: `
    <p-dialog 
      [(visible)]="visible" 
      [style]="{width: '90vw', maxWidth: '800px'}" 
      [modal]="true"
      [draggable]="false"
      [resizable]="false"
      styleClass="p-fluid"
      [closeOnEscape]="true"
      header="Détails du Candidat"
      (onHide)="closeDialog.emit()">
      <ng-template pTemplate="header">
        <div class="flex justify-between items-center w-full bg-blue-600 text-white p-4 rounded-t-lg">
          <h3 class="text-xl font-semibold m-0">{{ candidate?.candidateName || 'Détails du Candidat' }}</h3>
          <div class="w-12 h-12 rounded-full bg-white text-blue-600 flex items-center justify-center font-bold text-lg">
            {{ candidate?.matchScore }}%
          </div>
        </div>
      </ng-template>

      <div *ngIf="candidate" class="p-4">
        <!-- Contact Information -->
        <div class="border-b pb-4 mb-4">
          <div class="flex flex-col md:flex-row md:justify-between">
            <div>
              <h5 class="text-lg font-medium text-blue-600 mb-3 flex items-center">
                <i class="pi pi-envelope mr-2"></i>Coordonnées
              </h5>
              <p class="mb-2"><strong>Email:</strong> {{ candidate.email || 'Non disponible' }}</p>
              <p><strong>Téléphone:</strong> {{ candidate.phone || 'Non disponible' }}</p>
            </div>
            <div class="mt-3 md:mt-0">
              <p-button 
                *ngIf="candidate.downloadId" 
                icon="pi pi-download" 
                label="Télécharger le CV" 
                styleClass="p-button-outlined"
                (click)="downloadCV.emit(candidate.downloadId)">
              </p-button>
            </div>
          </div>
        </div>

        <!-- Evaluation -->
        <div class="border-b pb-4 mb-4" *ngIf="candidate.summary">
          <h5 class="text-lg font-medium text-blue-600 mb-3 flex items-center">
            <i class="pi pi-chart-pie mr-2"></i>Évaluation
          </h5>
          <p class="text-gray-700">{{ candidate.summary }}</p>
        </div>
        
        <!-- Skills -->
        <div class="border-b pb-4 mb-4" *ngIf="candidate.strengths && candidate.strengths.length > 0">
          <h5 class="text-lg font-medium text-blue-600 mb-3 flex items-center">
            <i class="pi pi-cog mr-2"></i>Compétences
          </h5>
          <div class="flex flex-wrap gap-2">
            <p-tag *ngFor="let skill of candidate.strengths" 
                  severity="info" 
                  [value]="skill"
                  [rounded]="true"></p-tag>
          </div>
        </div>
        
        <!-- Experience -->
        <div class="border-b pb-4 mb-4" *ngIf="candidate.experience">
          <h5 class="text-lg font-medium text-blue-600 mb-3 flex items-center">
            <i class="pi pi-briefcase mr-2"></i>Expérience
          </h5>
          <p class="text-gray-700 whitespace-pre-line">{{ candidate.experience }}</p>
        </div>
        
        <!-- Education -->
        <div class="border-b pb-4 mb-4" *ngIf="candidate.education">
          <h5 class="text-lg font-medium text-blue-600 mb-3 flex items-center">
            <i class="pi pi-book mr-2"></i>Formation
          </h5>
          <p class="text-gray-700 whitespace-pre-line">{{ candidate.education }}</p>
        </div>
        
        <!-- Languages -->
        <div class="border-b pb-4 mb-4" *ngIf="candidate.languages && candidate.languages.length > 0">
          <h5 class="text-lg font-medium text-blue-600 mb-3 flex items-center">
            <i class="pi pi-globe mr-2"></i>Langues
          </h5>
          <div class="flex flex-wrap gap-2">
            <p-tag *ngFor="let language of candidate.languages" 
                  severity="success" 
                  [value]="language"
                  [rounded]="true"></p-tag>
          </div>
        </div>
        
        <!-- Interview Questions -->
        <div *ngIf="candidate.suggestedQuestions && candidate.suggestedQuestions.length > 0">
          <h5 class="text-lg font-medium text-blue-600 mb-3 flex items-center">
            <i class="pi pi-comments mr-2"></i>Questions d'entretien suggérées
          </h5>
          <div class="space-y-3">
            <div *ngFor="let question of candidate.suggestedQuestions" 
                  class="bg-gray-50 border-l-4 border-blue-400 p-3 rounded-r-lg">
              {{ question }}
            </div>
          </div>
        </div>
      </div>
      
      <ng-template pTemplate="footer">
        <div class="flex justify-end">
          <p-button icon="pi pi-times" label="Fermer" (click)="closeDialog.emit()" styleClass="p-button-outlined"></p-button>
        </div>
      </ng-template>
    </p-dialog>
  `
})
export class CvDetailDialogComponent {
  @Input() visible = false;
  @Input() candidate: CVResult | null = null;
  @Output() closeDialog = new EventEmitter<void>();
  @Output() downloadCV = new EventEmitter<string>();
} 