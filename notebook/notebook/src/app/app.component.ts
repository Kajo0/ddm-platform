import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterOutlet} from '@angular/router';
import {ResultsComponent} from "./results/results.component";
import {OperationPanelComponent} from "./operation-panel/operation-panel.component";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {Metrics, ResultsView} from "./results/model/results-view";
import {ValidationResults} from "./results/model/execution-results";
import {InstanceConfig} from "./results/model/instance-info";
import {AlgorithmConfig} from "./results/model/algorithm-info";
import {DataConfig, DistanceFunctionConfig, PartitionStrategyConfig} from "./results/model/data-info";
import {catchError, throwError} from "rxjs";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, ResultsComponent, OperationPanelComponent, HttpClientModule, ReactiveFormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'notebook';

  protected readonly Object = Object;
  readonly resultsView = ResultsView;

  currentView = ResultsView.INSTANCE;

  currentInstanceId?: string;
  lastExecutionId?: string;
  lastResults?: ValidationResults;

  instanceConfig?: InstanceConfig;
  algorithmConfig?: AlgorithmConfig;
  dataConfig?: DataConfig;
  distanceFunctionConfig?: DistanceFunctionConfig;
  partitionStrategyConfig?: PartitionStrategyConfig;

  setupForm = new FormGroup({
    workers: new FormControl(2, Validators.required),
    cpu: new FormControl(2, Validators.required),
    masterMemory: new FormControl(4, Validators.required),
    workerMemory: new FormControl(2, Validators.required),
    disk: new FormControl(5, Validators.required),
  });

  choosingForm = new FormGroup({
    instanceId: new FormControl(null, Validators.required),
    algorithmId: new FormControl(null, Validators.required),
  });

  tmpAlgStatus: "initial" | "uploading" | "success" | "fail" = "initial";
  private tmpAlgFile?: File;

  constructor(private http: HttpClient) {
  }

  ngOnInit(): void {
    this.loadData();
    this.loadAlgorithms();
    this.loadInstances();
  }

  loadInstances(): void {
    this.currentView = ResultsView.INSTANCE;
    this.http.get<InstanceConfig>('http://localhost:7000/coordinator/command/instance/info')
      .subscribe(resp => this.instanceConfig = resp);
  }

  loadAlgorithms(): void {
    this.currentView = ResultsView.ALGORITHMS;
    this.http.get<AlgorithmConfig>('http://localhost:7000/coordinator/command/algorithm/info')
      .subscribe(resp => this.algorithmConfig = resp);
  }

  loadData(): void {
    this.currentView = ResultsView.DATA;
    this.http.get<DataConfig>('http://localhost:7000/coordinator/command/data/info')
      .subscribe(resp => this.dataConfig = resp);
    this.http.get<DistanceFunctionConfig>('http://localhost:7000/coordinator/command/data/info/distance-functions')
      .subscribe(resp => this.distanceFunctionConfig = resp);
    this.http.get<PartitionStrategyConfig>('http://localhost:7000/coordinator/command/data/info/partitioning-strategies')
      .subscribe(resp => this.partitionStrategyConfig = resp);
  }

  loadLastResults(): void {
    this.loadResults(this.lastExecutionId!, Object.values(Metrics));
  }

  loadResults(executionId: string, metrics: string[]): void {
    this.currentView = ResultsView.RESULTS;
    const combinedMetrics = metrics.join(',');
    this.http.get(`http://localhost:7000/coordinator/command/execution/results/collect/${executionId}`, {responseType: 'text'})
      .pipe(catchError(err => {
        console.log('not yet');
        throw err;
      }))
      .subscribe(() =>
        this.http.post<ValidationResults>(`http://localhost:7000/coordinator/command/results/validate/${executionId}`, null, {params: {metrics: combinedMetrics}})
          .subscribe(resp => this.lastResults = resp)
      );
  }

  setupInstance(): void {
    const values = this.setupForm.getRawValue();
    this.http.post(`http://localhost:7000/coordinator/command/instance/create/${values.workers}`, null, {
      responseType: 'text',
      params: {
        cpu: values.cpu!,
        disk: values.disk!,
        masterMemory: values.masterMemory!,
        workerMemory: values.workerMemory!
      }
    })
      .subscribe(resp => this.currentInstanceId = resp);
  }

  destroyAll(): void {
    this.http.get(`http://localhost:7000/coordinator/command/instance/destroy/all`, {responseType: 'text'})
      .subscribe(() => {
        this.instanceConfig = undefined;
        this.currentInstanceId = undefined;
        this.choosingForm.reset();
      });
  }

  onChangeAlgorithmFile(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.tmpAlgStatus = "initial";
      this.tmpAlgFile = file;
    }
  }

  uploadAlgorithm() {
    if (this.tmpAlgFile) {
      const formData = new FormData();

      formData.append('file', this.tmpAlgFile, this.tmpAlgFile.name);
      const upload$ = this.http.post('http://localhost:7000/coordinator/command/algorithm/load', formData);
      this.tmpAlgStatus = 'uploading';

      upload$.subscribe({
        next: () => {
          this.tmpAlgStatus = 'success';
          this.loadAlgorithms();
        },
        error: (error: any) => {
          this.tmpAlgStatus = 'fail';
          return throwError(() => error);
        },
      });
    }
  }

  broadcastAlgorithm() {
    const instanceId = this.choosingForm.getRawValue().instanceId;
    const algorithmId = this.choosingForm.getRawValue().algorithmId;
    this.http.get(`http://localhost:7000/coordinator/command/algorithm/broadcast/instance/${instanceId}/${algorithmId}`, {responseType: 'text'})
      .subscribe(resp => this.loadInstances());
  }

  execute(): void {
    const instanceId = '';
    const algorithmId = '';
    const trainDataId = '';
    this.http.post<string>(`http://localhost:7000/coordinator/command/execution/start/${instanceId}/${algorithmId}/${trainDataId}`, null, {
      params: {
        executionParams: '',
        testDataId: ''
      }
    })
      .subscribe(resp => this.lastExecutionId = resp);
  }

}
