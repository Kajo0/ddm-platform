import {Component} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {InstanceConfig} from "./model/instance-info";
import {JsonPipe, KeyValuePipe, NgForOf, NgIf} from "@angular/common";
import {ResultsView} from "./model/results-view";
import {AlgorithmConfig} from "./model/algorithm-info";
import {DataConfig, DistanceFunctionConfig, PartitionStrategyConfig} from "./model/data-info";
import {ValidationResults} from "./model/execution-results";
import {catchError} from "rxjs";

@Component({
  selector: 'app-results',
  standalone: true,
  imports: [
    JsonPipe,
    NgIf,
    NgForOf,
    KeyValuePipe
  ],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css'
})
export class ResultsComponent {

  protected readonly Object = Object;
  readonly resultsView = ResultsView;

  currentView = ResultsView.INSTANCE;

  lastExecutionId?: string;
  lastResults?: ValidationResults;

  instanceConfig?: InstanceConfig;
  algorithmConfig?: AlgorithmConfig;
  dataConfig?: DataConfig;
  distanceFunctionConfig?: DistanceFunctionConfig;
  partitionStrategyConfig?: PartitionStrategyConfig;

  constructor(private http: HttpClient) {
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

  loadResults(executionId: string = '49f58a8b-f387-40e1-b4cc-21eda3587db2', metrics: string[] = ['ARI', 'F-measure']): void {
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

}
