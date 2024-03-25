export interface InstanceConfig {
  [key: string]: Instance;
}

export interface Instance {
  id: string;
  type: string;
  networkName: string;
  nodes: Map<string, InstanceNode>;
  info: InstanceInfo;
}

export interface InstanceNode {
  id: string;
  containerId: string;
  name: string;
  type: string;
  address: string;
  localhostName: string;
  localhostIp: string;
  alive: boolean;
  port: string;
  uiPort: string;
  agentPort: string;
  cpu: number;
  memory: number;
  disk: number;
}

export interface InstanceInfo {
  dataScatter: Map<string, InstanceDataScatter>;
  algorithmScatter: string[];
}

export interface InstanceDataScatter {
  strategyName: string;
  strategyParams: string;
  distanceFunction: string;
  seed: string;
}

export enum HealthStatus {
  OK = 'ok',
  NOT_OK = 'not-ok'
}
