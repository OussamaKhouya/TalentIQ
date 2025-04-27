import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DropdownModule } from 'primeng/dropdown';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { LLMService } from '../../../core/services/llm.service';

interface LLMProvider {
  name: string;
  value: string;
}

@Component({
  selector: 'app-cv-upload-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    CardModule,
    DropdownModule,
    ToastModule
  ],
  providers: [
    MessageService
  ],
  template: `
    <p-toast></p-toast>
    <p-card styleClass="mb-4 shadow-sm">
      <ng-template pTemplate="title">
        <h2 class="text-xl font-semibold text-center mb-4">Analyser des CVs</h2>
      </ng-template>
      <ng-template pTemplate="content">
        <form (ngSubmit)="onSubmit()" class="space-y-4">
          <div>
            <label for="cvFiles" class="block text-sm font-medium text-gray-700 mb-1">Importer les CVs (PDF ou Texte)</label>
            <div 
              class="border-2 border-dashed rounded-lg p-6 text-center cursor-pointer transition-all"
              [ngClass]="{'border-blue-500 bg-blue-50': isDragging, 'border-gray-300 hover:border-blue-400 hover:bg-gray-50': !isDragging}"
              (dragover)="onDragOver($event)"
              (dragleave)="onDragLeave()"
              (drop)="onDrop($event)"
              (click)="fileInput.click()"
            >
              <i class="pi pi-cloud-upload text-3xl text-blue-500 mb-2"></i>
              <div class="mt-2 text-sm text-gray-600">{{ fileLabel }}</div>
              <input 
                #fileInput
                type="file" 
                id="cvFiles" 
                class="hidden"
                multiple 
                accept=".pdf,.txt" 
                (change)="onFileSelected($event)"
              >
            </div>
            <small class="text-gray-500 mt-1 block">Formats acceptés: PDF, TXT</small>
          </div>
          
          <div>
            <label for="jobOffer" class="block text-sm font-medium text-gray-700 mb-1">Description de l'offre d'emploi</label>
            <textarea 
              id="jobOffer" 
              rows="5"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Entrez les détails de l'offre d'emploi, compétences requises, responsabilités..."
              [(ngModel)]="jobDescription" 
              name="jobDescription">
            </textarea>
          </div>
          
          <div class="flex justify-between items-center">
            <div>
              <span class="mr-2 text-sm font-medium">IA:</span>
              <p-dropdown 
                [options]="providers" 
                [(ngModel)]="selectedProvider" 
                optionLabel="name"
                [style]="{'min-width':'8rem'}"
                (onChange)="changeProvider()"
                name="provider">
              </p-dropdown>
            </div>
            <p-button 
              type="submit" 
              label="Analyser et Sélectionner" 
              icon="pi pi-search" 
              [loading]="isLoading"
              styleClass="w-auto">
            </p-button>
          </div>
        </form>
      </ng-template>
    </p-card>
  `
})
export class CvUploadFormComponent implements OnInit {
  @Output() analyze = new EventEmitter<{files: File[], jobDescription: string}>();
  
  providers: LLMProvider[] = [
    { name: 'Gemini', value: 'gemini' },
    { name: 'Ollama', value: 'ollama' }
  ];
  
  selectedProvider: LLMProvider = this.providers[0];
  jobDescription = '';
  selectedFiles: File[] = [];
  fileLabel = 'Glisser-déposer des fichiers ou cliquer pour parcourir';
  isDragging = false;
  isLoading = false;
  changingProvider = false;

  constructor(
    private llmService: LLMService,
    private messageService: MessageService
  ) {}

  ngOnInit() {
    this.fetchLLMStatus();
  }

  fetchLLMStatus() {
    this.llmService.getLLMStatus().subscribe({
      next: (status) => {
        // Set the selected provider based on current config
        const providerIndex = this.providers.findIndex(p => p.value === status.provider);
        if (providerIndex >= 0) {
          this.selectedProvider = this.providers[providerIndex];
        }
      },
      error: (err) => {
        console.error('Error fetching LLM status:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de récupérer le statut de l\'IA'
        });
      }
    });
  }

  changeProvider() {
    if (this.changingProvider) return;
    
    this.changingProvider = true;
    this.llmService.changeLLMProvider(this.selectedProvider.value).subscribe({
      next: (response) => {
        this.changingProvider = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: `Utilisation de ${this.selectedProvider.name} activée`
        });
      },
      error: (err) => {
        this.changingProvider = false;
        console.error('Error changing LLM provider:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: err.error?.error || 'Erreur lors du changement de fournisseur IA'
        });
      }
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFiles = Array.from(input.files);
      this.updateFileLabel();
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave() {
    this.isDragging = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
    
    if (event.dataTransfer && event.dataTransfer.files.length > 0) {
      this.selectedFiles = Array.from(event.dataTransfer.files);
      this.updateFileLabel();
    }
  }

  updateFileLabel() {
    if (this.selectedFiles.length === 1) {
      this.fileLabel = this.selectedFiles[0].name;
    } else if (this.selectedFiles.length > 1) {
      this.fileLabel = `${this.selectedFiles.length} fichiers sélectionnés`;
    } else {
      this.fileLabel = 'Glisser-déposer des fichiers ou cliquer pour parcourir';
    }
  }

  onSubmit() {
    if (this.selectedFiles.length === 0 || !this.jobDescription.trim()) {
      alert('Veuillez sélectionner au moins un fichier CV et saisir une description de poste.');
      return;
    }

    this.isLoading = true;
    this.analyze.emit({
      files: this.selectedFiles,
      jobDescription: this.jobDescription
    });
  }

  resetForm() {
    this.isLoading = false;
  }
} 