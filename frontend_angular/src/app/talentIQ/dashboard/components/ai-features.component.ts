import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';

@Component({
  selector: 'app-ai-features',
  standalone: true,
  imports: [
    CommonModule,
    CardModule
  ],
  template: `
    <p-card styleClass="mb-4 bg-gray-50 border-0">
      <ng-template pTemplate="title">
        <h3 class="text-lg font-medium text-center mb-2">Fonctionnalités d'IA avancées</h3>
      </ng-template>
      <ng-template pTemplate="content">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="flex items-start space-x-3">
            <div class="rounded-full bg-blue-100 p-2 text-blue-500">
              <i class="pi pi-cog text-xl"></i>
            </div>
            <div>
              <h6 class="font-medium mb-1">Analyse contextuelle profonde</h6>
              <p class="text-sm text-gray-500">Compréhension sémantique des compétences et expériences</p>
            </div>
          </div>
          <div class="flex items-start space-x-3">
            <div class="rounded-full bg-blue-100 p-2 text-blue-500">
              <i class="pi pi-percentage text-xl"></i>
            </div>
            <div>
              <h6 class="font-medium mb-1">Score de correspondance</h6>
              <p class="text-sm text-gray-500">Évaluation précise de l'adéquation au poste</p>
            </div>
          </div>
          <div class="flex items-start space-x-3">
            <div class="rounded-full bg-blue-100 p-2 text-blue-500">
              <i class="pi pi-comments text-xl"></i>
            </div>
            <div>
              <h6 class="font-medium mb-1">Explications détaillées</h6>
              <p class="text-sm text-gray-500">Analyse des forces et faiblesses</p>
            </div>
          </div>
          <div class="flex items-start space-x-3">
            <div class="rounded-full bg-blue-100 p-2 text-blue-500">
              <i class="pi pi-question-circle text-xl"></i>
            </div>
            <div>
              <h6 class="font-medium mb-1">Questions d'entretien</h6>
              <p class="text-sm text-gray-500">Suggestions personnalisées pour chaque candidat</p>
            </div>
          </div>
        </div>
      </ng-template>
    </p-card>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6 mb-10">
      <div class="text-center">
        <div class="inline-flex items-center justify-content-center rounded-full bg-blue-100 p-3 mb-3 text-blue-500">
          <i class="pi pi-sync text-2xl"></i>
        </div>
        <h5 class="font-medium mb-1">IA Avancée</h5>
        <p class="text-sm text-gray-500">Analyse intelligente des compétences et expériences.</p>
      </div>
      <div class="text-center">
        <div class="inline-flex items-center justify-content-center rounded-full bg-blue-100 p-3 mb-3 text-blue-500">
          <i class="pi pi-bolt text-2xl"></i>
        </div>
        <h5 class="font-medium mb-1">Rapide et Précis</h5>
        <p class="text-sm text-gray-500">Résultats en quelques secondes.</p>
      </div>
      <div class="text-center">
        <div class="inline-flex items-center justify-content-center rounded-full bg-blue-100 p-3 mb-3 text-blue-500">
          <i class="pi pi-check-circle text-2xl"></i>
        </div>
        <h5 class="font-medium mb-1">Top 5 Candidats</h5>
        <p class="text-sm text-gray-500">Sélection des profils les plus pertinents.</p>
      </div>
    </div>
  `
})
export class AiFeaturesComponent {} 