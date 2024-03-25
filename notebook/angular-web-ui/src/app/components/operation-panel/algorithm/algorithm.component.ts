import {NgForOf, NgIf} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatDivider} from '@angular/material/divider';
import {MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle} from '@angular/material/expansion';
import {MatIcon} from '@angular/material/icon';
import {MatInput, MatSuffix} from '@angular/material/input';
import {MatList, MatListItem} from '@angular/material/list';
import {MatFormField, MatLabel, MatOption, MatSelect} from '@angular/material/select';
import {FileInput, FileValidator, MaterialFileInputModule} from 'ngx-material-file-input';
import {throwError} from 'rxjs';
import {AlgorithmConfig, AlgorithmInfo} from '../../../model/algorithm-info';
import {Instance, InstanceConfig, InstanceNode} from '../../../model/instance-info';
import {ConfigEventService} from '../../../services/config-event.service';
import {RestService} from '../../../services/rest.service';

@Component({
  selector: 'app-algorithm',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatSelect,
    MatLabel,
    NgForOf,
    NgIf,
    MatFormField,
    MatOption,
    MatInput,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatButton,
    MatDivider,
    MatList,
    MatListItem,
    MatIcon,
    MaterialFileInputModule,
    MatSuffix
  ],
  templateUrl: './algorithm.component.html',
  styleUrl: './algorithm.component.scss'
})
export class AlgorithmComponent implements OnInit {

  choosingForm = new FormGroup({
    instanceId: new FormControl<string>(null!, Validators.required),
    algorithmId: new FormControl<string>(null!, Validators.required)
  });

  uploadForm = new FormGroup({
    file: new FormControl<FileInput>(null!, [Validators.required, FileValidator.maxContentSize(100 * 1024 * 1024)]),
  });

  private algorithmConfig?: AlgorithmConfig;
  private instanceConfig?: InstanceConfig;
  private currentInstance?: Instance;

  constructor(private restService: RestService,
              private configEventService: ConfigEventService) {
  }

  ngOnInit(): void {
    this.choosingForm.reset();

    this.loadAlgorithms();

    this.configEventService.instanceConfig()
      .subscribe(config => this.instanceConfig = config);
    this.configEventService.selectedInstance()
      .subscribe(instance => {
        this.currentInstance = instance;
        this.selectCurrentInstance();
      });
  }

  loadAlgorithms(): void {
    this.restService.algorithmLoadInfo()
      .subscribe(resp => {
        this.algorithmConfig = resp;
        this.configEventService.algorithmConfigChange(this.algorithmConfig);
      });
  }

  uploadAlgorithm() {
    const input: FileInput = this.uploadForm.controls.file.getRawValue()!;
    const file = input.files[0];

    if (file) {
      this.restService.algorithmUpload(file)
        .subscribe({
          next: () => {
            this.loadAlgorithms();
            this.uploadForm.reset();
          }
        });
    }
  }

  broadcastAlgorithm(): void {
    const instanceId = this.choosingForm.getRawValue().instanceId!;
    const algorithmId = this.choosingForm.getRawValue().algorithmId!;

    this.choosingForm.reset();
    this.restService.algorithmBroadcast(instanceId, algorithmId)
      .subscribe(resp => this.configEventService.algorithmBroadcastToInstance(instanceId));
  }

  get emptyConfig(): boolean {
    return !this.algorithmConfig || Object.keys(this.algorithmConfig).length === 0;
  }

  get algorithms(): AlgorithmInfo[] {
    if (!this.algorithmConfig) {
      return [];
    } else {
      return Object.values(this.algorithmConfig);
    }
  }

  get instances(): Instance[] {
    if (!this.instanceConfig) {
      return [];
    } else {
      return Object.values(this.instanceConfig);
    }
  }

  instanceNodes(instance: Instance): InstanceNode[] {
    return Object.values(instance.nodes);
  }

  private selectCurrentInstance() {
    if (!this.currentInstance) {
      this.choosingForm.controls.instanceId.reset();
    } else {
      this.choosingForm.controls.instanceId.setValue(this.currentInstance.id);
    }
  }
}
