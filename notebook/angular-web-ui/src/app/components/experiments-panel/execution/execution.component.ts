import {DatePipe, NgForOf, NgIf} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatOption} from '@angular/material/autocomplete';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {
  MatExpansionPanel,
  MatExpansionPanelDescription,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';
import {MatList, MatListItem} from '@angular/material/list';
import {MatSelect} from '@angular/material/select';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatTooltip} from '@angular/material/tooltip';
import {zip} from 'rxjs';
import {AlgorithmConfig, AlgorithmInfo} from '../../../model/algorithm-info';
import {DataConfig, DataInfo} from '../../../model/data-info';
import {Execution, ExecutionInfo, ExecutionStatus} from '../../../model/execution-info';
import {Instance, InstanceConfig, InstanceNode} from '../../../model/instance-info';
import {ConfigEventService} from '../../../services/config-event.service';
import {RestService} from '../../../services/rest.service';
import {ConfirmationDialog} from '../../confirmation-dialog/confirmation-dialog.component';
import {ContentDialog} from '../../content-dialog/content-dialog.component';

@Component({
  selector: 'app-execution',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatLabel,
    MatButton,
    NgIf,
    NgForOf,
    MatExpansionPanelTitle,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelDescription,
    MatList,
    MatListItem,
    MatIcon,
    MatIconButton,
    DatePipe,
    MatOption,
    MatSelect,
    MatTooltip
  ],
  templateUrl: './execution.component.html',
  styleUrl: './execution.component.scss',
})
export class ExecutionComponent implements OnInit {

  executionForm = new FormGroup({
    instanceId: new FormControl<string>(null!, Validators.required),
    algorithmId: new FormControl<string>(null!, Validators.required),
    trainDataId: new FormControl<string>(null!, Validators.required),
    testDataId: new FormControl<string>(null!),
    params: new FormControl<string>(null!, Validators.required)
  });

  private executionInfo?: ExecutionInfo;
  private instanceConfig?: InstanceConfig;
  private algorithmConfig?: AlgorithmConfig;
  private dataConfig?: DataConfig;

  private currentInstance?: Instance;

  constructor(private restService: RestService,
              private configEventService: ConfigEventService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.loadExecutions();

    this.configEventService.instanceConfig()
      .subscribe(config => this.instanceConfig = config);
    this.configEventService.algorithmConfig()
      .subscribe(config => this.algorithmConfig = config);
    this.configEventService.dataConfig()
      .subscribe(config => this.dataConfig = config);
    this.configEventService.selectedInstance()
      .subscribe(instance => this.currentInstance = instance)
    this.configEventService.selectedInstance()
      .subscribe(instance => {
        this.currentInstance = instance;
        this.selectCurrentInstance();
      });
  }

  loadExecutions(): void {
    this.restService.executionLoadInfo()
      .subscribe(resp => this.executionInfo = resp);
  }

  execute(): void {
    const instanceId = this.executionForm.getRawValue().instanceId!;
    const algorithmId = this.executionForm.getRawValue().algorithmId!;
    const trainDataId = this.executionForm.getRawValue().trainDataId!;
    const testDataId = this.executionForm.getRawValue().testDataId;
    const params = this.executionForm.getRawValue().params!;

    this.restService.executionStart(instanceId, algorithmId, trainDataId, params, testDataId || undefined)
      .subscribe(executionId => {
        this.snackBar.open('started! with id: ' + executionId, 'OK');
        this.loadExecutions();
      });
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

  formInstanceHasAlgorithm(algorithmId: string): boolean {
    if (!this.executionForm.controls.instanceId.getRawValue()) {
      return false;
    } else {
      return this.instanceConfig![this.executionForm.controls.instanceId.getRawValue()!]
        .info.algorithmScatter.includes(algorithmId);
    }
  }

  // TODO add data scatter info to form
  formInstanceHasData(dataId: string): boolean {
    if (!this.executionForm.controls.instanceId.getRawValue()) {
      return false;
    } else {
      const ds = this.instanceConfig![this.executionForm.controls.instanceId.getRawValue()!].info.dataScatter;
      return Object.keys(ds).includes(dataId);
    }
  }

  get algorithms(): AlgorithmInfo[] {
    if (!this.algorithmConfig) {
      return [];
    } else {
      return Object.values(this.algorithmConfig);
    }
  }

  get data(): DataInfo[] {
    if (!this.dataConfig) {
      return [];
    } else {
      return Object.values(this.dataConfig);
    }
  }

  algorithm(algorithmId: string): AlgorithmInfo {
    if (!this.algorithmConfig) {
      return null!;
    } else {
      return this.algorithmConfig[algorithmId];
    }
  }

  dataInfo(dataId: string): DataInfo {
    if (!this.dataConfig) {
      return null!;
    } else {
      return this.dataConfig[dataId];
    }
  }

  checkExecutionStatus(execution: Execution): void {
    this.restService.executionStatus(execution.id)
      .subscribe(status => this.executionInfo![execution.id] = status);
  }

  stopExecution(execution: Execution): void {
    this.dialog.open(ConfirmationDialog)
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) {
          this.restService.executionStop(execution.id)
            .subscribe(resp => {
              this.snackBar.open(resp, 'OK');
              this.loadExecutions();
            });
        }
      });
  }

  showDetails(details: string): void {
    const dialogRef = this.dialog.open(ContentDialog);
    dialogRef.componentInstance.contentText = details;
  }

  fetchLastLogs(instanceId: string, executionId: string): void {
    this.restService.executionCollectLogs(executionId)
      .subscribe(() => {
        zip(this.findInstanceNodes(instanceId)
          .map(node => this.restService.executionFetchLogs(executionId, node.id, -1000))
        )
          .subscribe(logs => {
            const dialogRef = this.dialog.open(ContentDialog);
            dialogRef.componentInstance.contentCodes = logs;
          });
      });
  }

  showStats(executionId: string): void {
    this.restService.executionCollectResults(executionId)
      .subscribe(() => {
        this.restService.resultsStats(executionId)
          .subscribe(stats => {
            const dialogRef = this.dialog.open(ContentDialog);
            dialogRef.componentInstance.contentJson = JSON.parse(stats);
          });
      });
  }

  validate(executionId: string): void {
    this.restService.executionCollectResults(executionId)
      .subscribe(() => {
        this.restService.resultsValidate(executionId, ['accuracy', 'recall', 'precision', 'f-measure', 'ARI']) // FIXME choose metrics
          .subscribe(results => {
            const dialogRef = this.dialog.open(ContentDialog);
            dialogRef.componentInstance.contentJson = results;
          });
      });
  }

  isExecutionFinished(status: ExecutionStatus): boolean {
    return status.toString() === 'FINISHED'; // FIXME enum key
  }

  get executions(): Execution[] {
    if (!this.executionInfo) {
      return [];
    } else {
      return Object.values(this.executionInfo)
        .sort((a: Execution, b: Execution) => new Date(b.started).getTime() - new Date(a.started).getTime());
    }
  }

  get emptyInfo(): boolean {
    return !this.executionInfo || Object.keys(this.executionInfo).length === 0;
  }

  private selectCurrentInstance(): void {
    this.executionForm.reset();
    if (this.currentInstance) {
      this.executionForm.controls.instanceId.setValue(this.currentInstance.id);
    }
  }

  private findInstanceNodes(instanceId: string): InstanceNode[] {
    const nodes = this.instances
      .filter(i => i.id === instanceId)
      .map(nodes => this.instanceNodes(nodes));
    return nodes ? nodes[0] : [];
  }

}
