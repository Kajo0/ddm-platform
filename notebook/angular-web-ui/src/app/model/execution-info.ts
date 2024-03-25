import {InstanceNode} from './instance-info';

export interface ExecutionInfo {
  [key: string]: Execution;
}

export interface Execution {
  id: string;
  instanceId: string;
  algorithmId: string;
  trainDataId: string;
  testDataId: string;
  distanceFunctionId: string;
  distanceFunctionName: string;
  masterAddr: InstanceNode;
  status: ExecutionStatus;
  appId: string;
  executionParams: string;
  started: Date;
  updated: Date;
  stopped: Date;
  message: string;
}

export enum ExecutionStatus {
  INITIALIZING = 'initializing',
  STARTED = 'started',
  STOPPED = 'stopped',
  FAILED = 'failed',
  FINISHED = 'finished'
}
