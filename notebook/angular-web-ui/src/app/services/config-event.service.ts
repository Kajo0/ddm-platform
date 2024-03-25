import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {AlgorithmConfig} from '../model/algorithm-info';
import {DataConfig, DistanceFunctionConfig, PartitionStrategyConfig} from '../model/data-info';
import {Instance, InstanceConfig} from '../model/instance-info';

@Injectable({
  providedIn: 'root'
})
export class ConfigEventService {

  private instanceSubject = new BehaviorSubject<InstanceConfig>(null!);
  private selectedInstanceSubject = new BehaviorSubject<Instance>(null!);

  private algorithmsSubject = new BehaviorSubject<AlgorithmConfig>(null!);
  private algorithmBroadcastSubject = new BehaviorSubject<string>(null!);

  private dataSubject = new BehaviorSubject<DataConfig>(null!);
  private distanceFunctionSubject = new BehaviorSubject<DistanceFunctionConfig>(null!);
  private partitionStrategySubject = new BehaviorSubject<PartitionStrategyConfig>(null!);
  private distanceFunctionBroadcastSubject = new BehaviorSubject<string>(null!);
  private dataScatterToInstanceSubject = new BehaviorSubject<string>(null!);

  instanceConfig(): Observable<InstanceConfig> {
    return this.instanceSubject.asObservable();
  }

  instanceConfigChange(config: InstanceConfig) {
    this.instanceSubject.next(config);
  }

  selectedInstance(): Observable<Instance> {
    return this.selectedInstanceSubject.asObservable();
  }

  selectedInstanceChange(instance: Instance) {
    this.selectedInstanceSubject.next(instance);
  }

  algorithmConfig(): Observable<AlgorithmConfig> {
    return this.algorithmsSubject.asObservable();
  }

  algorithmConfigChange(config: AlgorithmConfig) {
    this.algorithmsSubject.next(config);
  }

  algorithmBroadcastToInstance(instanceId: string) {
    this.algorithmBroadcastSubject.next(instanceId);
  }

  algorithmBroadcastToInstanceChange(): Observable<string> {
    return this.algorithmBroadcastSubject.asObservable();
  }

  dataConfig(): Observable<DataConfig> {
    return this.dataSubject.asObservable();
  }

  dataConfigChange(config: DataConfig) {
    this.dataSubject.next(config);
  }

  distanceFunctionConfig(): Observable<DistanceFunctionConfig> {
    return this.distanceFunctionSubject.asObservable();
  }

  distanceFunctionConfigChange(config: DistanceFunctionConfig) {
    this.distanceFunctionSubject.next(config);
  }

  distanceFunctionBroadcastToInstanceChange(): Observable<string> {
    return this.distanceFunctionBroadcastSubject.asObservable();
  }

  distanceFunctionBroadcastToInstance(instanceId: string) {
    this.distanceFunctionBroadcastSubject.next(instanceId);
  }

  dataScatterToInstanceChange(): Observable<string> {
    return this.dataScatterToInstanceSubject.asObservable();
  }

  dataScatterToInstanceChangeToInstance(instanceId: string) {
    this.dataScatterToInstanceSubject.next(instanceId);
  }

  partitionStrategyConfig(): Observable<PartitionStrategyConfig> {
    return this.partitionStrategySubject.asObservable();
  }

  partitionStrategyConfigChange(config: PartitionStrategyConfig) {
    this.partitionStrategySubject.next(config);
  }

}
