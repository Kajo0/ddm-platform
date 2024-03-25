import {JsonPipe, KeyValuePipe, NgForOf, NgIf} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButton, MatFabButton, MatIconButton} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {MatDivider} from '@angular/material/divider';
import {
  MatExpansionPanel,
  MatExpansionPanelDescription,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatGridList, MatGridTile} from '@angular/material/grid-list';
import {MatIcon} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';
import {MatList, MatListItem} from '@angular/material/list';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatTooltip} from '@angular/material/tooltip';
import {catchError, concat} from 'rxjs';
import {AlgorithmConfig, AlgorithmInfo} from '../../../model/algorithm-info';
import {DataConfig, DataInfo} from '../../../model/data-info';
import {HealthStatus, Instance, InstanceConfig, InstanceNode} from '../../../model/instance-info';
import {ConfigEventService} from '../../../services/config-event.service';
import {RestService} from '../../../services/rest.service';
import {ConfirmationDialog} from '../../confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-instance',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatLabel,
    MatButton,
    MatGridList,
    MatGridTile,
    KeyValuePipe,
    NgIf,
    NgForOf,
    MatExpansionPanelTitle,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelDescription,
    JsonPipe,
    MatList,
    MatListItem,
    MatDivider,
    MatIcon,
    MatFabButton,
    MatIconButton,
    MatTooltip
  ],
  templateUrl: './instance.component.html',
  styleUrl: './instance.component.scss',
})
export class InstanceComponent implements OnInit {

  setupForm = new FormGroup({
    workers: new FormControl<number>(2, [Validators.required, Validators.min(1)]),
    cpu: new FormControl<number>(2, [Validators.required, Validators.min(1)]),
    masterMemory: new FormControl<number>(4, [Validators.required, Validators.min(1)]),
    workerMemory: new FormControl<number>(2, [Validators.required, Validators.min(1)]),
    disk: new FormControl<number>(5, [Validators.required, Validators.min(1)]),
  });

  private instanceConfig?: InstanceConfig;
  private currentInstanceId?: string;
  private algorithmConfig?: AlgorithmConfig;
  private dataConfig?: DataConfig;

  private instanceHealth: Map<string, HealthStatus> = new Map();

  constructor(private restService: RestService,
              private configEventService: ConfigEventService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.loadInstances();

    this.configEventService.algorithmConfig()
      .subscribe(config => this.algorithmConfig = config);
    this.configEventService.dataConfig()
      .subscribe(config => this.dataConfig = config);
    this.configEventService.algorithmBroadcastToInstanceChange()
      .subscribe(config => this.loadInstances());
    this.configEventService.distanceFunctionBroadcastToInstanceChange()
      .subscribe(config => this.loadInstances());
    this.configEventService.dataScatterToInstanceChange()
      .subscribe(config => this.loadInstances());
  }

  loadInstances(): void {
    this.restService.instanceLoadInfo()
      .subscribe(resp => {
        this.instanceConfig = resp;
        this.instanceHealth.clear();
        this.configEventService.instanceConfigChange(this.instanceConfig);
      });
  }

  setupInstance(): void {
    this.dialog.open(ConfirmationDialog)
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) {
          const values = this.setupForm.getRawValue();
          this.restService.instanceCreateSetup(values.workers!, values.cpu!, values.disk!, values.masterMemory!, values.workerMemory!)
            .subscribe(resp => {
              this.currentInstanceId = resp;
              this.loadInstances();
            });
        }
      });
  }

  destroyAll(): void {
    this.dialog.open(ConfirmationDialog)
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) {
          this.restService.instanceDestroyAll()
            .subscribe(() => {
              this.selectInstance(undefined!);
              this.instanceConfig = undefined;
              this.instanceHealth.clear();
              this.configEventService.instanceConfigChange(this.instanceConfig!);
            });
        }
      });
  }

  destroyInstance(instance: Instance): void {
    this.dialog.open(ConfirmationDialog)
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) {
          this.restService.instanceDestroy(instance.id)
            .subscribe(() => {
              if (instance.id === this.currentInstanceId) {
                this.selectInstance(undefined!);
              }
              this.instanceHealth.delete(instance.id);
              this.loadInstances();
            });
        }
      });
  }

  selectInstance(instance: Instance): void {
    this.currentInstanceId = instance?.id;
    this.configEventService.selectedInstanceChange(instance);
  }

  checkHealthStatus(instance: Instance) {
    concat(
      this.restService.instanceStatus(instance.id),
      this.restService.instanceConfigUpdate(instance.id)
    )
      .pipe(catchError((e) => {
        this.instanceHealth.set(instance.id, HealthStatus.NOT_OK);
        throw e;
      }))
      .subscribe(status => {
        this.instanceHealth.set(instance.id, status as HealthStatus);
      });
  }

  getHealthStatus(instance: Instance): HealthStatus | undefined {
    return this.instanceHealth.get(instance.id);
  }

  isCurrent(instance: Instance): boolean {
    return this.currentInstanceId === instance.id;
  }

  get emptyConfig(): boolean {
    return !this.instanceConfig || Object.keys(this.instanceConfig).length === 0;
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

  algorithm(algorithmId: string): AlgorithmInfo {
    if (!this.algorithmConfig) {
      return null!;
    } else {
      return this.algorithmConfig[algorithmId];
    }
  }

  data(dataId: string): DataInfo {
    if (!this.dataConfig) {
      return null!;
    } else {
      return this.dataConfig[dataId];
    }
  }
}
