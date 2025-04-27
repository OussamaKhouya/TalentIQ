import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { DropdownModule } from 'primeng/dropdown';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';
import { LLMService } from '../../../core/services/llm.service';

interface LLMProvider {
  name: string;
  value: string;
}

@Component({
  selector: 'app-llm-settings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    DropdownModule,
    ToastModule,
    ConfirmDialogModule
  ],
  providers: [
    MessageService,
    ConfirmationService
  ],
  template: `
    <p-toast></p-toast>
    <p-confirmDialog header="Confirmation" 
                    icon="pi pi-exclamation-triangle" 
                    message="Changer le fournisseur d'IA nécessite un redémarrage de l'application. Voulez-vous continuer?" 
                    [style]="{width: '450px'}"></p-confirmDialog>
    
    <p-card styleClass="mb-4 bg-gray-50 border-0">
      <ng-template pTemplate="title">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-medium">Réglages de l'IA</h3>
          <div class="text-sm text-green-600 font-medium" *ngIf="currentProvider">
            <i class="pi pi-check-circle mr-1"></i>
            {{ currentProvider === 'gemini' ? 'Gemini (Google)' : 'Ollama (Local)' }}
          </div>
        </div>
      </ng-template>
      <ng-template pTemplate="content">
        <div class="flex flex-column md:flex-row justify-content-between gap-3">
          <div class="flex flex-column">
            <h6 class="font-medium mb-2">Fournisseur IA</h6>
            <p-dropdown 
              [options]="providers" 
              [(ngModel)]="selectedProvider" 
              optionLabel="name"
              [style]="{'min-width':'14rem'}"
              (onChange)="confirmProviderChange()">
            </p-dropdown>
          </div>
          <div class="flex flex-column" *ngIf="llmStatus">
            <h6 class="font-medium mb-2">État</h6>
            <div class="flex flex-column gap-1">
              <div class="flex items-center gap-2">
                <i class="pi" [ngClass]="llmStatus.enabled ? 'pi-check-circle text-green-500' : 'pi-times-circle text-red-500'"></i>
                <span>API configurée: {{ llmStatus.enabled ? 'Oui' : 'Non' }}</span>
              </div>
              <div class="flex items-center gap-2">
                <i class="pi" [ngClass]="llmStatus.accessible ? 'pi-check-circle text-green-500' : 'pi-times-circle text-red-500'"></i>
                <span>API accessible: {{ llmStatus.accessible ? 'Oui' : 'Non' }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="mt-4 p-2 bg-blue-50 rounded border border-blue-200" *ngIf="isChangingProvider">
          <div class="flex items-center">
            <i class="pi pi-spin pi-spinner mr-2 text-blue-500"></i>
            <span>Mise à jour de la configuration...</span>
          </div>
        </div>
      </ng-template>
    </p-card>
  `
})
export class LLMSettingsComponent implements OnInit {
  providers: LLMProvider[] = [
    { name: 'Gemini (Google)', value: 'gemini' },
    { name: 'Ollama (Local)', value: 'ollama' }
  ];
  
  selectedProvider: LLMProvider = this.providers[0];
  currentProvider: string | null = null;
  llmStatus: any = null;
  isChangingProvider: boolean = false;

  constructor(
    private llmService: LLMService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit() {
    this.fetchLLMStatus();
  }

  fetchLLMStatus() {
    this.llmService.getLLMStatus().subscribe({
      next: (status) => {
        this.llmStatus = status;
        this.currentProvider = status.provider;
        
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
  
  confirmProviderChange() {
    if (this.selectedProvider.value === this.currentProvider) {
      return; // No change needed
    }
    
    this.confirmationService.confirm({
      message: 'Changer le fournisseur d\'IA nécessite un redémarrage de l\'application. Voulez-vous continuer?',
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.changeProvider();
      },
      reject: () => {
        // Reset dropdown to current value
        const providerIndex = this.providers.findIndex(p => p.value === this.currentProvider);
        if (providerIndex >= 0) {
          this.selectedProvider = this.providers[providerIndex];
        }
        
        this.messageService.add({
          severity: 'info',
          summary: 'Annulé',
          detail: 'Changement de fournisseur annulé'
        });
      }
    });
  }

  changeProvider() {
    this.isChangingProvider = true;
    
    this.llmService.changeLLMProvider(this.selectedProvider.value).subscribe({
      next: (response) => {
        this.isChangingProvider = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: response.success || 'Fournisseur IA modifié avec succès. Veuillez redémarrer l\'application.'
        });
        this.currentProvider = this.selectedProvider.value;
      },
      error: (err) => {
        this.isChangingProvider = false;
        console.error('Error changing LLM provider:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: err.error?.error || 'Erreur lors du changement de fournisseur IA'
        });
        
        // Reset dropdown to current value
        const providerIndex = this.providers.findIndex(p => p.value === this.currentProvider);
        if (providerIndex >= 0) {
          this.selectedProvider = this.providers[providerIndex];
        }
      }
    });
  }
} 