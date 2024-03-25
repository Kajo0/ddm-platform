import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AlgorithmConfig} from '../model/algorithm-info';
import {DataConfig, DistanceFunctionConfig, PartitionStrategyConfig} from '../model/data-info';
import {Execution, ExecutionInfo} from '../model/execution-info';
import {InstanceConfig} from '../model/instance-info';

@Injectable({providedIn: 'root'})
export class RestService {

  private baseUrl = 'http://localhost:7000/coordinator/command';

  constructor(private http: HttpClient) {
  }

  instanceLoadInfo(): Observable<InstanceConfig> {
    return this.http.get<InstanceConfig>(`${this.baseUrl}/instance/info`);
  }

  instanceCreateSetup(workers: number, cpu: number, disk: number, masterMemory: number, workerMemory: number): Observable<string> {
    return this.http.post(`${this.baseUrl}/instance/create/${workers}`, null, {
      responseType: 'text',
      params: {
        cpu: cpu,
        disk: disk,
        masterMemory: masterMemory,
        workerMemory: workerMemory
      }
    });
  }

  instanceStatus(id: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/instance/status/${id}`, {responseType: 'text'});
  }

  instanceConfigUpdate(id: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/instance/config/${id}/update`, {responseType: 'text'});
  }

  instanceDestroyAll(): Observable<string> {
    return this.http.get(`${this.baseUrl}/instance/destroy/all`, {responseType: 'text'});
  }

  instanceDestroy(id: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/instance/destroy/${id}`, {responseType: 'text'});
  }

  algorithmLoadInfo(): Observable<AlgorithmConfig> {
    return this.http.get<AlgorithmConfig>(`${this.baseUrl}/algorithm/info`);
  }

  algorithmUpload(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post(`${this.baseUrl}/algorithm/load`, formData);
  }

  algorithmBroadcast(instanceId: string, algorithmId: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/algorithm/broadcast/instance/${instanceId}/${algorithmId}`, {responseType: 'text'});
  }

  dataLoadInfo(): Observable<DataConfig> {
    return this.http.get<DataConfig>(`${this.baseUrl}/data/info`);
  }

  dataUpload(file: File, separator: string, labelIndex: number, idIndex?: number, vectorizeStrings?: boolean, deductType?: boolean, extractTrainPercentage?: number, expandAmount?: number, seed?: number): Observable<any> {
    const formData = new FormData();
    formData.append('dataFile', file, file.name);
    formData.append('separator', separator);
    formData.append('labelIndex', String(labelIndex));

    if (idIndex !== undefined) {
      formData.append('idIndex', String(idIndex));
    }
    if (vectorizeStrings) {
      formData.append('vectorizeStrings', String(vectorizeStrings));
    }
    if (deductType) {
      formData.append('deductType', String(deductType));
    }
    if (extractTrainPercentage) {
      formData.append('extractTrainPercentage', String(extractTrainPercentage));
    }
    if (expandAmount) {
      formData.append('expandAmount', String(expandAmount));
    }
    if (seed) {
      formData.append('expandAmount', String(seed));
    }

    return this.http.post(`${this.baseUrl}/data/load/file`, formData);
  }

  dataDistanceFunctionsLoadInfo(): Observable<DistanceFunctionConfig> {
    return this.http.get<DistanceFunctionConfig>(`${this.baseUrl}/data/info/distance-functions`);
  }

  dataDistanceFunctionBroadcast(instanceId: string, distanceFunctionId: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/data/distance-function/broadcast/${instanceId}/${distanceFunctionId}`, {responseType: 'text'});
  }

  dataDistanceFunctionUpload(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('distanceFunctionFile', file, file.name);
    return this.http.post(`${this.baseUrl}/data/distance-function/load/file`, formData);
  }

  dataPartitioningStrategiesLoadInfo(): Observable<PartitionStrategyConfig> {
    return this.http.get<PartitionStrategyConfig>(`${this.baseUrl}/data/info/partitioning-strategies`);
  }

  dataScatter(instanceId: string, dataId: string, strategy: string, typeCode: string, distanceFunction?: string, strategyParams?: string, seed?: number): Observable<string> {
    const formData = new FormData();
    formData.append('strategy', strategy);
    formData.append('typeCode', typeCode);

    if (distanceFunction) {
      formData.append('distanceFunction', distanceFunction);
    }
    if (strategyParams) {
      formData.append('strategyParams', strategyParams);
    }
    if (seed) {
      formData.append('seed', String(seed));
    }

    return this.http.post(`${this.baseUrl}/data/scatter/${instanceId}/${dataId}`, formData, {responseType: 'text'});
  }

  dataPartitionStrategyUpload(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('partitioningStrategyFile', file, file.name);
    return this.http.post(`${this.baseUrl}/data/partitioning-strategy/load/file`, formData);
  }

  executionLoadInfo(): Observable<ExecutionInfo> {
    return this.http.get<ExecutionInfo>(`${this.baseUrl}/execution/info`);
  }

  executionCollectLogs(executionId: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/execution/logs/collect/${executionId}`, {responseType: 'text'});
  }

  executionFetchLogs(executionId: string, nodeId: string, count: number): Observable<string> {
    return this.http.get(`${this.baseUrl}/execution/logs/fetch/${executionId}/${nodeId}/${count}`, {responseType: 'text'});
  }

  executionCollectResults(executionId: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/execution/results/collect/${executionId}`, {responseType: 'text'});
  }

  executionStatus(executionId: string): Observable<Execution> {
    return this.http.get<Execution>(`${this.baseUrl}/execution/status/${executionId}`);
  }

  executionStart(instanceId: string, algorithmId: string, trainDataId: string, params: string, testDataId?: string): Observable<string> {
    const formData = new FormData();
    formData.append('executionParams', params);
    if (testDataId) {
      formData.append('testDataId', testDataId);
    }

    return this.http.post(`${this.baseUrl}/execution/start/${instanceId}/${algorithmId}/${trainDataId}`, formData, {responseType: 'text'});
  }

  executionStop(executionId: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/execution/stop/${executionId}`, {responseType: 'text'});
  }

  resultsValidate(executionId: string, metrics: string[]): Observable<Map<string, number>> {
    const formData = new FormData();
    formData.append('metrics', metrics.join(','));

    return this.http.post<Map<string, number>>(`${this.baseUrl}/results/validate/${executionId}`, formData);
  }

  resultsStats(executionId: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/results/stats/${executionId}`, {responseType: 'text'}); // FIXME dto return
  }

}
