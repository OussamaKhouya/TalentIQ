import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CVResult } from '../../../core/models/cv-result.model';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { BadgeModule } from 'primeng/badge';
import { RatingModule } from 'primeng/rating';

@Component({
  selector: 'app-cv-results-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    ButtonModule,
    BadgeModule,
    RatingModule
  ],
  template: `
    <p-card *ngIf="results.length > 0" styleClass="mb-4 shadow-sm">
      <ng-template pTemplate="title">
        <h3 class="text-lg font-medium text-blue-800">Résultats d'analyse</h3>
      </ng-template>
      <ng-template pTemplate="content">
        <div class="space-y-4">
          <div *ngFor="let result of results; let i = index"
              class="p-4 rounded-lg border transition-all"
              [ngClass]="{'border-blue-500 bg-blue-50': i === 0, 'border-gray-300': i !== 0}">
            <div class="flex justify-between items-center mb-2">
              <h4 class="text-lg font-medium">{{ result.candidateName || 'Candidat ' + (i + 1) }}</h4>
              <p-badge [value]="result.matchScore + '%'" severity="info" styleClass="text-sm"></p-badge>
            </div>
            <p class="text-gray-600 mb-3">{{ result.summary }}</p>
            <div class="flex justify-between items-center">
              <div class="flex gap-2">
                <p-button label="Voir détails" icon="pi pi-eye" styleClass="p-button-outlined p-button-sm" (click)="viewDetails.emit(result)"></p-button>
                <p-button *ngIf="result.downloadId" label="Télécharger CV" icon="pi pi-download" styleClass="p-button-outlined p-button-sm p-button-success" (click)="downloadCV.emit(result.downloadId)"></p-button>
              </div>
              <p-rating [ngModel]="result.rating" [readonly]="true" styleClass="text-yellow-500"></p-rating>
            </div>
          </div>
        </div>
      </ng-template>
    </p-card>
  `
})
export class CvResultsListComponent {
  @Input() results: CVResult[] = [];
  @Output() viewDetails = new EventEmitter<CVResult>();
  @Output() downloadCV = new EventEmitter<string>();
}
