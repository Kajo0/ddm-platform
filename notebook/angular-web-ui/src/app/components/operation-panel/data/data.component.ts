import {NgForOf, NgIf} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatDivider} from '@angular/material/divider';
import {MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle} from '@angular/material/expansion';
import {MatIcon} from '@angular/material/icon';
import {MatInput, MatSuffix} from '@angular/material/input';
import {MatList, MatListItem} from '@angular/material/list';
import {MatFormField, MatLabel, MatOption, MatSelect} from '@angular/material/select';
import {FileInput, FileValidator, MaterialFileInputModule} from 'ngx-material-file-input';
import {throwError} from 'rxjs';
import {
  DataConfig,
  DataInfo,
  DistanceFunction,
  DistanceFunctionConfig,
  PartitionStrategy,
  PartitionStrategyConfig
} from '../../../model/data-info';
import {Instance, InstanceConfig, InstanceNode} from '../../../model/instance-info';
import {ConfigEventService} from '../../../services/config-event.service';
import {RestService} from '../../../services/rest.service';

@Component({
  selector: 'app-data',
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
    MatSuffix,
    MatCheckbox
  ],
  templateUrl: './data.component.html',
  styleUrl: './data.component.scss'
})
export class DataComponent implements OnInit {

  uploadDataForm = new FormGroup({
    file: new FormControl<FileInput>(null!, [Validators.required, FileValidator.maxContentSize(200 * 1024 * 1024)]),
    idIndex: new FormControl<number>(null!, Validators.min(0)),
    labelIndex: new FormControl<number>(null!, [Validators.required, Validators.min(0)]),
    separator: new FormControl<string>(null!, Validators.required),
    deductType: new FormControl<boolean>(true),
    vectorizeStrings: new FormControl<boolean>(false),
    extractTrainPercentage: new FormControl<number>(null!, [Validators.min(1), Validators.max(99)]),
    expandAmount: new FormControl<number>(null!, Validators.min(1)),
    seed: new FormControl<number>(null!, Validators.min(1))
  });

  uploadForm = new FormGroup({
    file: new FormControl<FileInput>(null!, [Validators.required, FileValidator.maxContentSize(10 * 1024 * 1024)]),
    type: new FormControl<string>('data', Validators.required),
  });

  broadcastDistanceFunctionForm = new FormGroup({
    instanceId: new FormControl<string>(null!, Validators.required),
    distanceFunctionId: new FormControl<string>(null!, Validators.required)
  });

  scatterDataForm = new FormGroup({
    instanceId: new FormControl<string>(null!, Validators.required),
    dataId: new FormControl<string>(null!, Validators.required),
    strategy: new FormControl<string>(null!, Validators.required),
    strategyParams: new FormControl<string>(null!),
    distanceFunction: new FormControl<string>(null!),
    type: new FormControl<string>('train', Validators.required),
    seed: new FormControl<number>(null!, Validators.min(1))
  });

  private dataConfig?: DataConfig;
  private distanceFunctionConfig?: DistanceFunctionConfig;
  private partitioningStrategyConfig?: PartitionStrategyConfig;

  private instanceConfig?: InstanceConfig;
  private currentInstance?: Instance;

  constructor(private restService: RestService,
              private configEventService: ConfigEventService) {
  }

  ngOnInit(): void {
    this.loadData();
    this.loadDistanceFunctions();
    this.loadPartitioningStrategies();

    this.configEventService.dataConfig()
      .subscribe(config => this.dataConfig = config);
    this.configEventService.distanceFunctionConfig()
      .subscribe(config => this.distanceFunctionConfig = config);
    this.configEventService.partitionStrategyConfig()
      .subscribe(config => this.partitioningStrategyConfig = config);

    this.configEventService.instanceConfig()
      .subscribe(config => this.instanceConfig = config);
    this.configEventService.selectedInstance()
      .subscribe(instance => {
        this.currentInstance = instance;
        this.selectCurrentInstance();
      });
  }

  loadData(): void {
    this.restService.dataLoadInfo()
      .subscribe(resp => {
        this.dataConfig = resp;
        this.configEventService.dataConfigChange(this.dataConfig);
      });
  }

  loadDistanceFunctions(): void {
    this.restService.dataDistanceFunctionsLoadInfo()
      .subscribe(resp => {
        this.distanceFunctionConfig = resp;
        this.configEventService.distanceFunctionConfigChange(this.distanceFunctionConfig);
      });
  }

  loadPartitioningStrategies(): void {
    this.restService.dataPartitioningStrategiesLoadInfo()
      .subscribe(resp => {
        this.partitioningStrategyConfig = resp;
        this.configEventService.partitionStrategyConfigChange(this.partitioningStrategyConfig);
      });
  }

  uploadData() {
    const input: FileInput = this.uploadDataForm.controls.file.getRawValue()!;
    const file = input.files[0];

    if (file) {
      this.restService.dataUpload(
        file,
        this.uploadDataForm.controls.separator.value!,
        this.uploadDataForm.controls.labelIndex.value!,
        this.uploadDataForm.controls.idIndex.value!,
        this.uploadDataForm.controls.vectorizeStrings.value || undefined,
        this.uploadDataForm.controls.deductType.value || undefined,
        this.uploadDataForm.controls.extractTrainPercentage.value || undefined,
        this.uploadDataForm.controls.expandAmount.value || undefined,
        this.uploadDataForm.controls.seed.value || undefined
      )
        .subscribe({
          next: () => {
            this.loadData();
            this.uploadDataForm.reset();
          }
        });
    }
  }

  uploadDistanceFunctionOrPartitioningStrategy() {
    const input: FileInput = this.uploadForm.controls.file.getRawValue()!;
    const file = input.files[0];
    const type = this.uploadForm.controls.type.getRawValue()!;

    if (file) {
      if (type === 'distance-function') {
        this.restService.dataDistanceFunctionUpload(file)
          .subscribe({
            next: () => {
              this.loadDistanceFunctions();
              this.uploadForm.reset();
            }
          });
      } else if (type === 'partitioning-strategy') {
        this.restService.dataPartitionStrategyUpload(file)
          .subscribe({
            next: () => {
              this.loadPartitioningStrategies();
              this.uploadForm.reset();
            }
          });
      } else {
        throw new Error('Unknown type: ' + type);
      }
    }
  }

  broadcastDistanceFunction(): void {
    const instanceId = this.broadcastDistanceFunctionForm.getRawValue().instanceId!;
    const algorithmId = this.broadcastDistanceFunctionForm.getRawValue().distanceFunctionId!;

    this.broadcastDistanceFunctionForm.reset();
    this.restService.dataDistanceFunctionBroadcast(instanceId, algorithmId)
      .subscribe(resp => this.configEventService.distanceFunctionBroadcastToInstance(instanceId));
  }

  scatterData(): void {
    const instanceId = this.scatterDataForm.getRawValue().instanceId!;
    const dataId = this.scatterDataForm.getRawValue().dataId!;

    this.broadcastDistanceFunctionForm.reset();
    this.restService.dataScatter(
      instanceId,
      dataId,
      this.scatterDataForm.controls.strategy.value!,
      this.scatterDataForm.controls.type.value!,
      this.scatterDataForm.controls.distanceFunction.value || undefined,
      this.scatterDataForm.controls.strategyParams.value || undefined,
      this.scatterDataForm.controls.seed.value || undefined
    )
      .subscribe({
        next: () => {
          this.scatterDataForm.reset();
          this.configEventService.distanceFunctionBroadcastToInstance(instanceId)
        }
      });
  }

  get emptyDataConfig(): boolean {
    return !this.dataConfig || Object.keys(this.dataConfig).length === 0;
  }

  get emptyDistanceFunctionConfig(): boolean {
    return !this.distanceFunctionConfig || Object.keys(this.distanceFunctionConfig).length === 0;
  }

  get emptyPartitioningStrategyConfig(): boolean {
    return !this.partitioningStrategyConfig || Object.keys(this.partitioningStrategyConfig).length === 0;
  }

  get data(): DataInfo[] {
    if (!this.dataConfig) {
      return [];
    } else {
      return Object.values(this.dataConfig);
    }
  }

  get distanceFunctions(): DistanceFunction[] {
    if (!this.distanceFunctionConfig) {
      return [];
    } else {
      return Object.values(this.distanceFunctionConfig);
    }
  }

  get partitioningStrategies(): PartitionStrategy[] {
    if (!this.partitioningStrategyConfig) {
      return [];
    } else {
      return Object.values(this.partitioningStrategyConfig);
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
      this.broadcastDistanceFunctionForm.controls.instanceId.reset();
      this.scatterDataForm.controls.instanceId.reset();
    } else {
      this.broadcastDistanceFunctionForm.controls.instanceId.setValue(this.currentInstance.id);
      this.scatterDataForm.controls.instanceId.setValue(this.currentInstance.id);
    }
  }
}
